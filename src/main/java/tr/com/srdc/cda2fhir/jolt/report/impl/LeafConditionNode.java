package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.INode;

public class LeafConditionNode extends LeafNode {
	public LeafConditionNode(String path, String target, String link) {
		super(path, target, link);
	}

	public boolean isCondition() {
		return true;
	}
	
	public INode mergeToParent(INode parent) {
		parent.removeChild(this);
		
		String path = this.getPath();
		String link = this.getLink();
		String target = this.getTarget();
		String parentPath = parent.getPath();
		
		int rank = Integer.valueOf(path.substring(1));		
		if (rank == 0) {
			LeafNode result = new LeafNode(parentPath, target, link);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			return result;
		}		
		
		LeafConditionNode result = new LeafConditionNode("!" + (rank - 1), target, link);
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
