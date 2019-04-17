package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedLinkedNode extends LinkedNode {
	public MergedLinkedNode(IParentNode parent, String path, String target, String link) {
		super(parent, path, target, link);
	}

	@Override
	public MergedLinkedNode clone(IParentNode parent) {
		String path = this.getPath();
		String target = this.getTarget();
		String link = this.getLink();
		MergedLinkedNode result = new MergedLinkedNode(parent, path, target, link);
		result.addConditions(getConditions());
		return result;
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
