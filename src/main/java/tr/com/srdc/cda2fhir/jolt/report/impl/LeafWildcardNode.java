package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;

public class LeafWildcardNode extends LeafNode implements IWildcardNode {
	public LeafWildcardNode(IParentNode parent, String path, String target) {
		super(parent, path, target);
	}

	@Override
	public void fillWildcardNodes(List<IWildcardNode> result) {
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		IParentNode grandparent = parent.getParent();
		String parentPath = parent.getPath();
		String newPath = hasSibling() ? parentPath : parentPath + "[]";
		MergedLeafNode result = new MergedLeafNode(grandparent, newPath, this.getTarget());
		grandparent.addChild(result);
		parent.removeChild(this);		
		result.copyConditions(parent);
		result.copyConditions(this);		
	}
}
