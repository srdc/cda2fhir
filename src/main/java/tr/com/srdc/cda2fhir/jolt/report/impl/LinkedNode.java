package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class LinkedNode extends LeafNode implements ILinkedNode {
	private String link;

	public LinkedNode(IParentNode parent, String path, String target, String link) {
		super(parent, path, target);
		this.link = link;
	}

	@Override
	public String getLink() {
		return link;
	}

	@Override
	public LinkedNode clone(IParentNode parent) {
		String path = this.getPath();
		String target = this.getTarget();
		LinkedNode result = new LinkedNode(parent, path, target, link);
		result.conditions.addAll(conditions);
		return result;
	}

	@Override
	public void expandLinks(Map<String, RootNode> linkMap) {
		RootNode rootNode = linkMap.get(link);
		if (rootNode != null) {
			IParentNode parent = getParent();
			String path = getPath();
			String target = getTarget();
			List<INode> newChildren = rootNode.getAsLinkReplacement(parent, path, target);
			newChildren.forEach(newChild -> {
				newChild.getConditions().addAll(getConditions());
				parent.addChild(newChild);
				List<ILinkedNode> linkedNodesOfLink = newChild.getLinkedNodes();
				linkedNodesOfLink.forEach(lnon -> lnon.expandLinks(linkMap));
			});
			parent.removeChild(this);
		}
	}

	public List<TableRow> toTableRows() {
		String path = getPath();
		String target = getTarget();
		TableRow row = new TableRow(path, target, link);
		conditions.forEach(condition -> {
			String conditionAsString = condition.toString(path);
			row.addCondition(conditionAsString);
		});
		List<TableRow> result = new ArrayList<TableRow>();
		result.add(row);
		return result;
	}

	@Override
	public void fillLinkedNodes(List<ILinkedNode> result) {
		result.add(this);
	}
}
