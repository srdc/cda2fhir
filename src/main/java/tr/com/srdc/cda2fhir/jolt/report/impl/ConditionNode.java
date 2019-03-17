package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.INode;

public class ConditionNode extends ParentNode {
	public ConditionNode(String path) {
		super(path);
	}

	public boolean isCondition() {
		return true;
	}
	
	public INode mergeToParent(INode parent) {
		parent.removeChild(this);
		
		String path = this.getPath();
		String parentPath = parent.getPath();
		
		int rank = Integer.valueOf(path.substring(1));		
		if (rank == 0) {
			ParentNode result = new ParentNode(parentPath);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			result.addChildren(this.getChildren());
			return result;
		}		
		
		ConditionNode result = new ConditionNode("!" + (rank - 1));
		result.addChildren(this.getChildren());
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
