package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedLinkedNode  extends LinkedNode {
	public MergedLinkedNode(IParentNode parent, String path, String target, String link) {
		super(parent, path, target, link);
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
