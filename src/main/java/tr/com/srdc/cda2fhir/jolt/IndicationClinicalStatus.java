package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class IndicationClinicalStatus implements ContextualTransform, SpecDriven, IRootNodeUpdater {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public IndicationClinicalStatus(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	public void update(RootNode rootNode) {
		final Map<String, INode> nodes = new HashMap<>();
		String[] names = { "#low", "#high", "#value" };
		rootNode.updateBase(base -> {
			List<INode> numberNodes = base.findChildren("#low");
			for (int index = 0; index < names.length; ++index) {
				if (!numberNodes.isEmpty()) {
					nodes.put(names[index], numberNodes.get(0));
				}
			}
		});
		INode lowNode = nodes.get("#low");
		INode highNode = nodes.get("#high");
		INode valueNode = nodes.get("#value");

		highNode.setPath("#resolved");
		IParentNode highParent = highNode.getParent();
		IParentNode newBase = highParent.separateChildLines("#high").get(0);
		newBase.copyConditions(lowNode);
		if (highParent != newBase) {
			rootNode.addRootChild(newBase);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null || !(input instanceof Map)) {
			return input;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Object clinicalStatus = inputAsMap.get("clinicalStatus");
		if (clinicalStatus == null) {
			return input;
		}
		List<Object> list = (List<Object>) clinicalStatus;
		if (list.indexOf("high") >= 0 && list.indexOf("low") >= 0) {
			inputAsMap.put("clinicalStatus", "resolved");
			return input;
		}
		if (list.indexOf("value") >= 0 || list.indexOf("low") >= 0) {
			inputAsMap.put("clinicalStatus", "active");
			return input;
		}
		inputAsMap.remove("clinicalStatus");
		return input;
	}
}
