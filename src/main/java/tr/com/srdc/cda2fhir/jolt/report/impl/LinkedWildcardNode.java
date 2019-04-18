package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;

public class LinkedWildcardNode extends LinkedNode implements IWildcardNode {
	public LinkedWildcardNode(IParentNode parent, String path, String target, String link) {
		super(parent, path, target, link);
	}

	@Override
	public LinkedWildcardNode clone(IParentNode parent) {
		String path = getPath();
		String target = getTarget();
		String link = getLink();
		LinkedWildcardNode result = new LinkedWildcardNode(parent, path, target, link);
		result.addConditions(getConditions());
		return result;
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
		String newPath = addSquareBrackets() ? parentPath + "[]" : parentPath;
		LinkedNode result = new LinkedNode(grandparent, newPath, this.getTarget(), this.getLink());
		grandparent.addChild(result);
		parent.removeChild(this);
		result.copyConditions(parent);
		result.copyConditions(this);
	}
}
