package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.INode;

public class LeafConditionNode extends LeafNode {
	private int rank;
	
	public LeafConditionNode(int rank, String target, String link) {
		super("!" + rank, target, link);
		this.rank = rank;
	}

	public boolean isCondition() {
		return true;
	}
	
	@Override
	public INode mergeToParent(INode parent) {
		String link = this.getLink();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		
		if (rank == 0) {
			LeafNode result = new LeafNode(parentPath, target, link);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			return result;
		}		
		
		LeafConditionNode result = new LeafConditionNode(rank - 1, target, link);
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
