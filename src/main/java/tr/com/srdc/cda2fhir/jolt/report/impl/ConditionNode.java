package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class ConditionNode extends ParentNode implements IConditionNode {
	private int rank;
	
	public ConditionNode(IParentNode parent, int rank) {
		super(parent, "!" + rank);
		this.rank = rank;
	}

	public boolean isCondition() {
		return true;
	}
	
	@Override
	public INode mergeToParent() {
		IParentNode parent = getParent();
		parent.removeChild(this);				
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();
		
		if (rank == 0) {
			ParentNode result = new ParentNode(grandParent, parentPath);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			result.addChildren(this.getChildren());
			return result;
		}		
		
		ConditionNode result = new ConditionNode(grandParent, rank - 1);
		result.addChildren(this.getChildren());
		result.addConditions(parent.getConditions());
		this.getConditions().forEach(condition -> {
			condition.prependPath(parentPath);
			result.addCondition(condition);
		});
		return result;
	}
}
