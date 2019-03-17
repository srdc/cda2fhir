package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
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
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		super.fillConditionNodes(result);
		result.add(this);
	}

	@Override
	public INode mergeToParent() {
		IParentNode parent = getParent();
		String parentPath = parent.getPath();
		IParentNode grandParent = parent.getParent();
		
		if (rank == 0) {
			ParentNode result = new ParentNode(grandParent, parentPath);
			result.addConditions(parent.getConditions());
			result.addConditions(this.getConditions());
			result.addChildren(this.getChildren());
			grandParent.addChild(result);
			parent.removeChild(this);				
			return result;
		}		
		
		ConditionNode result = new ConditionNode(grandParent, rank - 1);
		result.addChildren(this.getChildren());
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
