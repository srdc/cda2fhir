package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedParentNode extends ParentNode {
	public MergedParentNode(IParentNode parent, String path) {
		super(parent, path);
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
