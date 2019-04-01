package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class LeafRawConditionNode extends LeafNode implements IConditionNode {
	private int rank;

	public LeafRawConditionNode(IParentNode parent, int rank, String target) {
		super(parent, "@" + rank, target);
		this.rank = rank;
	}

	@Override
	public LeafRawConditionNode clone(IParentNode parent) {
		String target = getTarget();
		LeafRawConditionNode result = new LeafRawConditionNode(parent, rank, target);
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
		LeafNode result = new LeafNode(grandParent, parent.getPath(), getTarget());
		result.copyConditions(parent);
		grandParent.addChild(result);
		parent.removeChild(this);
	}
}
