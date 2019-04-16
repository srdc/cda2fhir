package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class ConditionNode extends ParentNode implements IConditionNode {
	private int rank;

	public ConditionNode(IParentNode parent, int rank) {
		super(parent, "!" + rank);
		this.rank = rank;
	}

	@Override
	public ConditionNode clone(IParentNode parent) {
		ConditionNode result = new ConditionNode(parent, rank);
		copyToClone(result);
		return result;
	}

	@Override
	public ConditionNode cloneEmpty() {
		IParentNode parent = getParent();
		return new ConditionNode(parent, rank);
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		super.fillConditionNodes(result);
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();

		if (rank <= grandParent.originalNodeCount()) {
			ParentNode result = new ParentNode(grandParent, parentPath);
			result.copyConditions(parent);
			result.copyConditions(this);
			result.copyChildren(this);
			grandParent.addChild(result);
			parent.removeChild(this);
			return;
		}

		ConditionNode result = new ConditionNode(grandParent, rank - 1);
		result.copyChildren(this);
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
