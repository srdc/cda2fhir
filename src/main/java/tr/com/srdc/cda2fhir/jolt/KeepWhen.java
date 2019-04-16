package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.NodeFactory;
import tr.com.srdc.cda2fhir.jolt.report.ReportException;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.Templates;
import tr.com.srdc.cda2fhir.jolt.report.impl.MultiAndCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.MultiOrCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NotNullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class KeepWhen implements ContextualTransform, SpecDriven, IRootNodeUpdater {
	private List<Map<String, Object>> orConditions;
	private String target;

	@SuppressWarnings("unchecked")
	@Inject
	public KeepWhen(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		target = (String) specAsMap.get("target");
		List<Object> rawOrConditions = (List<Object>) specAsMap.get("conditions");
		orConditions = rawOrConditions.stream().map(r -> (Map<String, Object>) r).collect(Collectors.toList());
	}

	private ICondition getCondition() {
		List<ICondition> orResults = orConditions.stream().map(orCondition -> {
			RootNode rootNode = NodeFactory.getInstance(orCondition);
			Table table = rootNode.toTable(new Templates());
			List<ICondition> conditions = table.getRows().stream().map(row -> {
				String path = row.getPath();
				String target = row.getTarget();
				if ("n".equals(target)) {
					return new NullCondition(path);
				}
				if ("y".equals(target)) {
					return new NotNullCondition(path);
				}
				throw new ReportException("KeepWhen can only have 'n' or 'y' as targets.");
			}).collect(Collectors.toList());
			if (conditions.size() == 1) {
				return conditions.get(0);
			}
			return new MultiAndCondition(conditions);
		}).collect(Collectors.toList());
		if (orResults.size() == 1) {
			return orResults.get(0);
		}
		return new MultiOrCondition(orResults);
	}

	@Override
	public void update(RootNode rootNode) {
		ICondition condition = getCondition();
		rootNode.updateBase(base -> {
			List<IParentNode> newBases = base.separateChildLines(target);
			newBases.forEach(newBase -> {
				newBase.addCondition(condition);
				if (base != newBase) {
					rootNode.addRootChild(newBase);
				}
			});
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		if (!inputAsMap.containsKey(target)) {
			return input;
		}
		for (Map<String, Object> orCondition : orConditions) {
			Shiftr shiftr = new Shiftr(orCondition);
			Map<String, Object> output = (Map<String, Object>) shiftr.transform(input);
			if (output == null) {
				continue;
			}
			if (output.get("n") != null) {
				continue;
			}
			if (output.get("y") != null) {
				return input;
			}
		}
		inputAsMap.remove(target);
		if (inputAsMap.isEmpty()) {
			return null;
		}
		return input;
	}
}
