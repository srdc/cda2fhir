package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.jolt.report.impl.Condition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NotNullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class RemoveWhenNull implements ContextualTransform, SpecDriven, IRootNodeUpdater {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public RemoveWhenNull(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	public void update(RootNode rootNode) {
		for (String key : this.spec.keySet()) { // assume top level for now
			rootNode.updateBase(base -> {
				Condition condition = new NotNullCondition(key);
				base.addCondition(condition);
			});

		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		for (String key : this.spec.keySet()) { // assume top level for now
			if (!inputAsMap.containsKey(key)) {
				return null;
			}
		}
		return input;
	}
}
