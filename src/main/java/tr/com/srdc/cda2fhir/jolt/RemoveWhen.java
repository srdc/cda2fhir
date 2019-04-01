package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.Condition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.OrCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class RemoveWhen implements ContextualTransform, SpecDriven, IRootNodeUpdater {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public RemoveWhen(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	private static final class Resolution {
		public String target;
		public String path;

		Resolution(String target, String path) {
			this.target = target;
			this.path = path;
		}
	}

	@SuppressWarnings("unchecked")
	private List<Resolution> resolve(Object updateInfo, String parentPath) {
		Map<String, Object> updateInfoAsMap = (Map<String, Object>) updateInfo;
		return resolve(updateInfoAsMap, parentPath);
	}

	@SuppressWarnings("unchecked")
	private List<Resolution> resolve(Map<String, Object> updateInfo, String parentPath) {
		List<Resolution> result = new ArrayList<>();
		for (Map.Entry<String, Object> entry : updateInfo.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			String path = parentPath.isEmpty() ? key : parentPath + "." + key;
			if (value instanceof String) {
				result.add(new Resolution((String) value, path));
				continue;
			}
			if (value instanceof List) {
				List<Object> valueAsList = (List<Object>) value;
				for (Object valueElement : valueAsList) {
					if (valueElement instanceof String) {
						result.add(new Resolution((String) valueElement, path));
						continue;
					}
					List<Resolution> elementResult = resolve(valueElement, path);
					result.addAll(elementResult);
				}
				continue;
			}
			List<Resolution> elementResult = resolve(value, path);
			result.addAll(elementResult);
		}
		return result;
	}

	@Override
	public void update(RootNode rootNode) {
		List<Resolution> rwrs = resolve(this.spec, "");
		final Map<String, ICondition> alreadySeen = new HashMap<>();
		rwrs.forEach(rwr -> {
			final String target = rwr.target;
			final String path = rwr.path;
			if ("*".equals(target)) {
				rootNode.updateBase(base -> {
					Condition condition = new NullCondition(path);
					base.addCondition(condition);
				});
				return;
			}
			rootNode.updateBase(base -> {
				List<IParentNode> newBases = base.separateChildLines(target);
				newBases.forEach(newBase -> {
					ICondition condition = new NullCondition(path);
					String rootPath = path.split("\\.")[0];
					ICondition prevCondition = alreadySeen.get(rootPath);
					if (prevCondition != null) {
						condition = new OrCondition(prevCondition.not(), condition);
					}
					newBase.addCondition(condition);
					alreadySeen.put(target, condition);
					if (base != newBase) {
						rootNode.addRootChild(newBase);
					}
				});
			});
		});
	}

	private static final class ToBeRemoved {
		public String target;
		public String source;

		ToBeRemoved(String target, String source) {
			this.target = target;
			this.source = source;
		}
	}

	@SuppressWarnings("unchecked")
	private static List<ToBeRemoved> apply(Object source, Object input, String path) {
		Map<String, Object> sourceAsMap = (Map<String, Object>) source;
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		return get(sourceAsMap, inputAsMap, path);
	}

	@SuppressWarnings("unchecked")
	private static List<ToBeRemoved> get(Map<String, Object> source, Map<String, Object> input, String path) {
		List<ToBeRemoved> result = new ArrayList<>();
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String key = entry.getKey();
			if (!input.containsKey(key)) {
				continue;
			}
			String topPath = path.isEmpty() ? key : path;
			Object value = entry.getValue();
			if (value instanceof String) {
				result.add(new ToBeRemoved((String) value, topPath));
				continue;
			}
			if (value instanceof List) {
				List<Object> valueAsList = (List<Object>) value;
				for (Object valueElement : valueAsList) {
					if (valueElement instanceof String) {
						result.add(new ToBeRemoved((String) valueElement, topPath));
						continue;
					}
					List<ToBeRemoved> elementResult = apply(valueElement, input.get(key), topPath);
					result.addAll(elementResult);
				}
				continue;
			}
			Object inputBranch = input.get(key);
			if (!(inputBranch instanceof Map)) {
				continue;
			}
			List<ToBeRemoved> branchResult = apply(value, inputBranch, topPath);
			result.addAll(branchResult);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		List<ToBeRemoved> tbrs = get(this.spec, inputAsMap, "");
		Set<String> alreadyRemoved = new HashSet<>();
		for (ToBeRemoved tbr : tbrs) {
			if ("*".equals(tbr.target)) {
				return null;
			}
			if (alreadyRemoved.contains(tbr.source)) {
				continue;
			}
			inputAsMap.remove(tbr.target);
			alreadyRemoved.add(tbr.target);
		}
		return input;
	}
}
