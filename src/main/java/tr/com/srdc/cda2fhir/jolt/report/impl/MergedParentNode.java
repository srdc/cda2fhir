package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedParentNode extends ParentNode {
	public MergedParentNode(IParentNode parent, String path) {
		super(parent, path);
	}

	@Override
	public MergedParentNode clone(IParentNode parent) {
		MergedParentNode result = new MergedParentNode(parent, getPath());
		copyToClone(result);
		return result;
	}

	@Override
	public MergedParentNode cloneEmpty() {
		IParentNode parent = getParent();
		String path = getPath();
		return new MergedParentNode(parent, path);
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
