package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.INode;

public class ConditionNode extends ParentNode {
	public ConditionNode(String path) {
		super(path);
	}

	public ConditionNode(String path, String target, String link) {
		super(path, target, link);
	}

	public INode mergeToParent(INode parent) {
		parent.removeChild(this);
		
		String path = this.getPath();
		String link = this.getLink();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		
		int rank = Integer.valueOf(path.substring(1));		
		if (rank == 0) {
			ParentNode result = new ParentNode(parentPath, target, link);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			result.addChildren(this.getChildren());
			return result;
		}		
		
		ConditionNode result = new ConditionNode("!" + (rank - 1), target, link);
		result.addChildren(this.getChildren());
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
