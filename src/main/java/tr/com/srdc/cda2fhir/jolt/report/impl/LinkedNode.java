package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltTemplate;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;
import tr.com.srdc.cda2fhir.jolt.report.Templates;

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
		result.addConditions(getConditions());
		return result;
	}

	@Override
	public void expandLinks(Map<String, JoltTemplate> templateMap) {
		JoltTemplate template = templateMap.get(link);
		if (template != null) {
			RootNode rootNode = template.getRootNode();
			IParentNode parent = getParent();
			List<INode> newChildren = rootNode.getAsLinkReplacement(this);
			newChildren.forEach(newChild -> {
				newChild.getConditions().addAll(getConditions());
				parent.addChild(newChild);
				List<ILinkedNode> linkedNodesOfLink = newChild.getLinkedNodes();
				linkedNodesOfLink.forEach(lnon -> lnon.expandLinks(templateMap));
			});
			parent.removeChild(this);
		}
	}

	public List<TableRow> toTableRows(Templates templates) {
		String path = getPath();
		String target = getTarget();

		String rootResourceType = templates.getRootResource();
		boolean isResourceLink = templates.doesGenerateResource(link);

		String format = templates.getFormat(target);

		String actualTarget = rootResourceType == null || isResourceLink ? target : rootResourceType + "." + target;

		TableRow row = isResourceLink ? new TableRow(path, actualTarget) : new TableRow(path, actualTarget, link);
		row.setFormat(format);
		getConditions().forEach(condition -> {
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
