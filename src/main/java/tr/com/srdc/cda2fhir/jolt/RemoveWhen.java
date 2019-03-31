package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class RemoveWhen implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public RemoveWhen(Object spec) {
		this.spec = (Map<String, Object>) spec;
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
