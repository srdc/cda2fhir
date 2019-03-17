package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;

public abstract class Node implements INode {
	@Override
	public List<ILinkedNode> getLinkedNodes() {
		List<ILinkedNode> result = new ArrayList<ILinkedNode>();
		fillLinkedNodes(result);
		return result;
	}

	@Override
	public List<IConditionNode> getConditionNodes() {
		List<IConditionNode> result = new ArrayList<IConditionNode>();
		fillConditionNodes(result);
		return result;		
	}
}
