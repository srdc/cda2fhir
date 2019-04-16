package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;

public class MergedLeafNode extends LeafNode {
	public MergedLeafNode(IParentNode parent, String path, String target) {
		super(parent, path, target);
	}

	@Override
	public MergedLeafNode clone(IParentNode parent) {
		String path = getPath();
		String target = getTarget();
		MergedLeafNode result = new MergedLeafNode(parent, path, target);
		result.addConditions(getConditions());
		return result;
	}

	@Override
	public int originalNodeCount() {
		return 1;
	}
}
