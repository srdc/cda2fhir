package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.PathPredicate;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;
import tr.com.srdc.cda2fhir.jolt.report.Templates;

public class ParentNode extends Node implements IParentNode {
	public LinkedList<INode> children = new LinkedList<INode>();

	public ParentNode(IParentNode parent, String path) {
		super(parent, path);
	}

	@Override
	public ParentNode clone(IParentNode parent) {
		String path = getPath();
		ParentNode result = new ParentNode(parent, path);
		children.forEach(child -> {
			INode childClone = child.clone(result);
			result.addChild(childClone);
		});
		getConditions().forEach(condition -> {
			JoltCondition conditionClone = condition.clone();
			result.addCondition(conditionClone);
		});
		return result;
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}

	@Override
	public void addChild(INode child) {
		children.add(child);
	}

	@Override
	public void removeChild(INode child) {
		IParentNode parent = getParent();
		children.remove(child);
		if (children.size() == 0) {
			parent.removeChild(this);
		}
	}

	public void addChildren(List<INode> children) {
		this.children.addAll(children);
	}

	@Override
	public void fillLinkedNodes(List<ILinkedNode> result) {
		children.forEach(child -> child.fillLinkedNodes(result));
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
		children.forEach(child -> child.fillConditionNodes(result));
	}

	@Override
	public void fillWildcardNodes(List<IWildcardNode> result) {
		children.forEach(child -> child.fillWildcardNodes(result));
	}

	@Override
	public void promoteTargets(String parentTarget) {
		children.forEach(child -> child.promoteTargets(parentTarget));
	}

	@Override
	public List<TableRow> toTableRows(Templates templates) {
		String path = getPath();
		List<TableRow> rows = new ArrayList<TableRow>();
		children.forEach(child -> {
			List<TableRow> childRows = child.toTableRows(templates);
			rows.addAll(childRows);
		});
		rows.forEach(row -> {
			row.promotePath(path);

			getConditions().forEach(condition -> {
				String conditionAsString = condition.toString(path);
				row.addCondition(conditionAsString);
			});
		});
		return rows;
	}

	@Override
	public void fillNodes(List<INode> result, PathPredicate pathPredicate) {
		children.forEach(child -> child.fillNodes(result, pathPredicate));
		super.fillNodes(result, pathPredicate);

	}

	public Table toTable(Templates templates) {
		Table result = new Table();
		children.forEach(jp -> {
			List<TableRow> rows = jp.toTableRows(templates);
			result.addRows(rows);
		});
		return result;
	}

	public void copyChildren(IParentNode source) {
		List<INode> sourceChildren = source.getChildren();
		sourceChildren.forEach(child -> child.setParent(this));
		children.addAll(source.getChildren());
	}
}
