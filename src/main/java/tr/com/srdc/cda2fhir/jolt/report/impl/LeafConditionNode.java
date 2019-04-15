package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class LeafConditionNode extends LeafNode implements IConditionNode {
	private int rank;

	public LeafConditionNode(IParentNode parent, int rank, String target) {
		super(parent, "!" + rank, target);
		this.rank = rank;
	}

	@Override
	public LeafConditionNode clone(IParentNode parent) {
		String target = getTarget();
		LeafConditionNode result = new LeafConditionNode(parent, rank, target);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();

		if (rank <= grandParent.originalNodeCount()) {
			LeafNode result = new LeafNode(grandParent, parentPath, target);
			result.copyConditions(parent);
			result.copyConditions(this);
			grandParent.addChild(result);
			parent.removeChild(this);
			return;
		}

		LeafConditionNode result = new LeafConditionNode(grandParent, rank - 1, target);
		result.copyConditions(parent);
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		grandParent.addChild(result);
		parent.removeChild(this);
		result.mergeToParent();
	}
}
