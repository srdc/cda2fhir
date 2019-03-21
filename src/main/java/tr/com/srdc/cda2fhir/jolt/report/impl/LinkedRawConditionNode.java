package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class LinkedRawConditionNode extends LinkedNode implements IConditionNode {
	private int rank;

	public LinkedRawConditionNode(IParentNode parent, int rank, String target, String link) {
		super(parent, "@" + rank, target, link);
		this.rank = rank;
	}

	@Override
	public LinkedRawConditionNode clone(IParentNode parent) {
		String target = getTarget();
		String link = getLink();
		LinkedRawConditionNode result = new LinkedRawConditionNode(parent, rank, target, link);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		IParentNode grandParent = parent.getParent();
		LinkedNode result = new LinkedNode(grandParent, parent.getPath(), getTarget(), getLink());
		result.addConditions(parent.getConditions());
		grandParent.addChild(result);
		parent.removeChild(this);
		grandParent.removeChild(parent);
	}
}
