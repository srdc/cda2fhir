package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class RawConditionNode extends ParentNode implements IConditionNode {
	private int rank;

	public RawConditionNode(IParentNode parent, int rank) {
		super(parent, "@" + rank);
		this.rank = rank;
	}

	@Override
	public RawConditionNode clone(IParentNode parent) {
		RawConditionNode result = new RawConditionNode(parent, rank);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		super.fillConditionNodes(result);
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		parent.addChildren(this.getChildren());
		this.getChildren().forEach(child -> child.setParent(parent));
		parent.removeChild(this);	
	}
}
