package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;

public class WildcardNode extends ParentNode implements IWildcardNode {
	public WildcardNode(IParentNode parent, String path) {
		super(parent, path);
	}

	@Override
	public void fillWildcardNodes(List<IWildcardNode> result) {
		super.fillWildcardNodes(result);
		result.add(this);
	}

	@Override
	public void mergeToParent() {
		IParentNode parent = getParent();
		IParentNode grandparent = parent.getParent();
		String parentPath = parent.getPath();
		String newPath = hasSibling() ? parentPath : parentPath + "[*]";
		MergedParentNode result = new MergedParentNode(grandparent, newPath);
		grandparent.addChild(result);
		parent.removeChild(this);		
		result.copyConditions(parent);
		result.copyChildren(this);
		result.copyConditions(this);		
	}
}
