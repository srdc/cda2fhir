package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class LeafConditionNode extends LeafNode implements IConditionNode {
	private int rank;
	
	public LeafConditionNode(IParentNode parent, int rank, String target, String link) {
		super(parent, "!" + rank, target, link);
		this.rank = rank;
	}

	@Override
	public LeafConditionNode clone(IParentNode parent) {
		String target = getTarget();
		String link = getLink();
		LeafConditionNode result = new LeafConditionNode(parent, rank, target, link);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		result.add(this);
	}
	
	@Override
	public INode mergeToParent() {
		IParentNode parent = getParent();
		String link = this.getLink();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();
		
		if (rank == 0) {
			LeafNode result = new LeafNode(parent, parentPath, target, link);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			grandParent.addChild(result);
			parent.removeChild(this);				
			return result;
		}		
		
		LeafConditionNode result = new LeafConditionNode(grandParent, rank - 1, target, link);
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		grandParent.addChild(result);
		parent.removeChild(this);
		result.mergeToParent();
		return result;
	}
}
