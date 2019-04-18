package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tr.com.srdc.cda2fhir.jolt.report.impl.EqualCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.MultiAndCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.MultiOrCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NotNullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class KeepWhen implements ContextualTransform, SpecDriven, IRootNodeUpdater {
	private List<Map<String, Object>> orConditions;
	private String target;
	private Map<String, Object> values;
	private Collection<ICondition> conditions;
	private List<Set<String>> orConditionVariables = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Inject
	public KeepWhen(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		target = (String) specAsMap.get("target");
		values = (Map<String, Object>) specAsMap.get("values");
		List<Object> rawOrConditions = (List<Object>) specAsMap.get("conditions");
		orConditions = rawOrConditions.stream().map(r -> (Map<String, Object>) r).collect(Collectors.toList());
		conditions = getCondition();
	}

	private Collection<ICondition> getCondition() {
		List<ICondition> orResults = orConditions.stream().map(orCondition -> {
			RootNode rootNode = NodeFactory.getInstance(orCondition);
			Table table = rootNode.toTable(new Templates());
			Set<String> conditionVariables = new HashSet<>();
			List<ICondition> conditions = table.getRows().stream().map(row -> {
				String path = row.getPath();
				String target = row.getTarget();
				if ("n".equals(target)) {
					return new NullCondition(path);
				}
				if (target.charAt(0) == 'y') {
					conditionVariables.add(target);
					return new NotNullCondition(path);
				}
				if (target.charAt(0) == '=') {
					conditionVariables.add(target);
					String key = target.substring(1);
					if (values == null) {
						throw new ReportException("Value for variable " + key + " not found.");
					}
					String value = (String) values.get(key);
					if (value == null) {
						throw new ReportException("Value for variable " + key + " not found.");
					}
					return new EqualCondition(path, value);
				}
				throw new ReportException("KeepWhen can only have 'n', 'y*', or '=*' as targets.");
			}).collect(Collectors.toList());
			orConditionVariables.add(conditionVariables);
			if (conditions.size() == 1) {
				return conditions.get(0);
			}
			return new MultiAndCondition(conditions);
		}).collect(Collectors.toList());
		if (orResults.size() == 1) {
			ICondition singleCondition = orResults.get(0);
			if (!(singleCondition instanceof MultiAndCondition)) {
				return Collections.singletonList(singleCondition);
			}
			MultiAndCondition multiAndCondition = (MultiAndCondition) singleCondition;
			return multiAndCondition.getChildConditions();
		}
		return Collections.singletonList(new MultiOrCondition(orResults));
	}

	@Override
	public void update(RootNode rootNode) {
		rootNode.updateBase(base -> {
			if ("*".equals(target)) {
				conditions.forEach(condition -> base.addCondition(condition));
				return;
			}
			List<IParentNode> newBases = base.separateChildLines(target);
			newBases.forEach(newBase -> {
				conditions.forEach(condition -> {
					newBase.addCondition(condition);
				});
				if (base != newBase) {
					rootNode.addRootChild(newBase);
				}
			});
		});
	}

	private boolean checkOutputForVariables(Set<String> variables, Map<String, Object> output) {
		for (String variable : variables) {
			Object value = output.get(variable);
			if (value == null) {
				return false;
			}
			if (variable.charAt(0) == 'y') {
				continue;
			}
			String key = variable.substring(1);
			String expectedValue = (String) values.get(key);
			if (!expectedValue.equals(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		if (!("*".equals(target) || inputAsMap.containsKey(target))) {
			return input;
		}
		for (int index = 0; index < orConditions.size(); ++index) {
			Map<String, Object> orCondition = orConditions.get(index);
			Set<String> variables = orConditionVariables.get(index);
			Shiftr shiftr = new Shiftr(orCondition);
			Map<String, Object> output = (Map<String, Object>) shiftr.transform(input);
			if (output == null) {
				if (variables == null) {
					return input;
				}
				continue;
			}
			if (output.get("n") != null) {
				continue;
			}
			if (checkOutputForVariables(variables, output)) {
				return input;
			}
		}
		if ("*".equals(target)) {
			return null;
		}
		inputAsMap.remove(target);
		if (inputAsMap.isEmpty()) {
			return null;
		}
		return input;
	}
}
