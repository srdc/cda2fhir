package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.INode;

public class ConditionNode extends ParentNode {
	private int rank;
	
	public ConditionNode(int rank) {
		super("!" + rank);
		this.rank = rank;
	}

	public boolean isCondition() {
		return true;
	}
	
	@Override
	public INode mergeToParent(INode parent) {
		String parentPath = parent.getPath();
		
		if (rank == 0) {
			ParentNode result = new ParentNode(parentPath);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			result.addChildren(this.getChildren());
			return result;
		}		
		
		ConditionNode result = new ConditionNode(rank - 1);
		result.addChildren(this.getChildren());
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
