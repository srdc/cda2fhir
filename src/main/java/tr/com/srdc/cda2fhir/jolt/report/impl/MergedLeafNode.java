package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedLeafNode extends LeafNode {
	public MergedLeafNode(IParentNode parent, String path, String target) {
		super(parent, path, target);
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
