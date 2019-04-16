package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class LinkedConditionNode extends LinkedNode implements IConditionNode {
	private int rank;

	public LinkedConditionNode(IParentNode parent, int rank, String target, String link) {
		super(parent, "!" + rank, target, link);
		this.rank = rank;
	}

	@Override
	public LinkedConditionNode clone(IParentNode parent) {
		String target = getTarget();
		String link = getLink();
		LinkedConditionNode result = new LinkedConditionNode(parent, rank, target, link);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		String link = this.getLink();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();

		if (rank <= grandParent.originalNodeCount()) {
			LinkedNode result = new LinkedNode(grandParent, parentPath, target, link);
			if (rank == 1) {
				result.removeExtra = true;
			}
			result.copyConditions(parent);
			result.copyConditions(this);
			grandParent.addChild(result);
			parent.removeChild(this);
			return;
		}

		LinkedConditionNode result = new LinkedConditionNode(grandParent, rank - 1, target, link);
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
