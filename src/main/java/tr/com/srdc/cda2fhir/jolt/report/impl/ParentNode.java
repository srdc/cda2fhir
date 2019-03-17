package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class ParentNode implements IParentNode {
	private String path;
	public LinkedList<INode> children = new LinkedList<INode>();
	public List<JoltCondition> conditions = new ArrayList<JoltCondition>();

	public ParentNode(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void addConditions(List<JoltCondition> conditions) {
		this.conditions.addAll(conditions);
	}
	
	@Override
	public ParentNode clone() {
		ParentNode result = new ParentNode(path);
		children.forEach(child -> {
			INode childClone = child.clone();
			result.addChild(childClone);
		});
		conditions.forEach(condition -> {
			JoltCondition conditionClone = condition.clone();
			result.conditions.add(conditionClone);
		});
		return result;
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}
	
	@Override
	public List<JoltCondition> getConditions() {
		return conditions;
	}
	
	@Override
	public void addChild(INode child) {
		children.add(child);
	}

	public void removeChild(INode child) {
		children.remove(child);
	}
	
	@Override
	public void addCondition(JoltCondition condition) {
		conditions.add(condition);		
	}
	
	public void addChildren(List<INode> children) {
		this.children.addAll(children);
	}

	public void fillLinks(List<ILeafNode> result) {
		children.forEach(child -> child.fillLinks(result));
	}

	@Override
	public List<ILeafNode> getLinks() {
		List<ILeafNode> result = new ArrayList<ILeafNode>();
		fillLinks(result);
		return result;
	}

	public void promoteTargets(String parentTarget) {
		children.forEach(child -> child.promoteTargets(parentTarget));
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	@Override
	public void expandLinks(Map<String, RootNode> linkMap) {
		children.stream().filter(c -> !c.isLeaf()).forEach(c -> c.expandLinks(linkMap));
		
		List<ILeafNode> linkedChildren = getLinks();
		
		linkedChildren.forEach(linkedChild -> {
			RootNode linkedNode = linkMap.get(linkedChild.getLink());
			if (linkedNode != null) {
				List<INode> newChildren = linkedNode.getAsLinkReplacement(linkedChild.getPath(), linkedChild.getTarget());
				newChildren.forEach(newChild -> {
					newChild.getConditions().addAll(linkedChild.getConditions());
					newChild.expandLinks(linkMap);
				});
				children.remove(linkedChild);
				children.addAll(newChildren);
			}
		});
	}

	public boolean isCondition() {
		return path.length() > 0 && path.charAt(0) == '!';
	}

	public INode mergeToParent(INode parent) {
		throw new NotImplementedException("Not available for arbirtrary nodes");
	}

	public void conditionalize() {
		children.forEach(child -> child.conditionalize());

		ListIterator<INode> childIterator = children.listIterator();
		while (childIterator.hasNext()) {
			INode child = childIterator.next();

			if (child.isLeaf()) continue;
			
			List<INode> conditionNodes = child.getChildren().stream().filter(n -> n.isCondition()).collect(Collectors.toList());
			if (conditionNodes.size() == 0) {
				continue;
			}
			if (conditionNodes.size() == child.getChildren().size()) {
				childIterator.remove();
			}
			conditionNodes.forEach(node -> {
				INode merged = node.mergeToParent(child);
				childIterator.add(merged);								
			});
		}
	}

	public List<TableRow> toTableRows() {
		List<TableRow> rows = new ArrayList<TableRow>();
		children.forEach(child -> {
			List<TableRow> childRows = child.toTableRows();
			rows.addAll(childRows);
		});
		rows.forEach(row -> {
			row.promotePath(path);

			conditions.forEach(condition -> {
				String conditionAsString = condition.toString(path);
				row.addCondition(conditionAsString);
			});
		});
		return rows;
	}

	public Table toTable() {
		Table result = new Table();
		children.forEach(jp -> {
			List<TableRow> rows = jp.toTableRows();
			result.addRows(rows);
		});
		return result;
	}
}
