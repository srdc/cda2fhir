package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;
import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.PathPredicate;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;
import tr.com.srdc.cda2fhir.jolt.report.Templates;

public class ParentNode extends Node implements IParentNode {
	public LinkedList<INode> children = new LinkedList<INode>();

	public ParentNode(IParentNode parent, String path) {
		super(parent, path);
	}

	protected void copyToClone(ParentNode theClone) {
		children.forEach(child -> {
			INode childClone = child.clone(theClone);
			theClone.addChild(childClone);
		});
		getConditions().forEach(condition -> {
			ICondition conditionClone = condition.clone();
			theClone.addCondition(conditionClone);
		});
	}

	@Override
	public ParentNode clone(IParentNode parent) {
		String path = getPath();
		ParentNode result = new ParentNode(parent, path);
		copyToClone(result);
		return result;
	}

	@Override
	public ParentNode cloneEmpty() {
		IParentNode parent = getParent();
		String path = getPath();
		return new ParentNode(parent, path);
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
		children.remove(child);
		if (children.size() == 0) {
			IParentNode parent = getParent();
			parent.removeChild(this);
		}
	}

	@Override
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

			getConditions().forEach(condition -> row.addCondition(condition.clone(path)));
		});
		return rows;
	}

	@Override
	public void fillNodes(List<INode> result, PathPredicate pathPredicate) {
		children.forEach(child -> child.fillNodes(result, pathPredicate));
		super.fillNodes(result, pathPredicate);

	}

	@Override
	public List<INode> findChildren(String path) {
		return children.stream().filter(r -> path.equals(r.getPath())).collect(Collectors.toList());
	}

	@Override
	public List<IParentNode> separateChildLines(String path) {
		List<INode> pathChildren = findChildren(path);
		if (pathChildren.size() < 1) {
			return Collections.<IParentNode>emptyList();
		}
		if (children.size() == pathChildren.size()) {
			return Collections.singletonList(this);
		}
		return pathChildren.stream().map(child -> {
			removeChild(child);
			IParentNode newMe = cloneEmpty();
			newMe.copyConditions(this);
			newMe.addChild(child);
			child.setParent(newMe);
			return newMe;

		}).collect(Collectors.toList());
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
