package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.PathPredicate;

public abstract class Node implements INode {
	private IParentNode parent;
	private String path;

	private List<JoltCondition> conditions = new ArrayList<JoltCondition>();

	public Node(IParentNode parent, String path) {
		this.parent = parent;
		this.path = path;
	}

	@Override
	public IParentNode getParent() {
		return parent;
	}

	@Override
	public void setParent(IParentNode parent) {
		this.parent = parent;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public List<ILinkedNode> getLinkedNodes() {
		List<ILinkedNode> result = new ArrayList<ILinkedNode>();
		fillLinkedNodes(result);
		return result;
	}

	@Override
	public void fillLinkedNodes(List<ILinkedNode> result) {
	}

	@Override
	public List<IConditionNode> getConditionNodes() {
		List<IConditionNode> result = new ArrayList<IConditionNode>();
		fillConditionNodes(result);
		return result;
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
	}

	@Override
	public List<IWildcardNode> getWildcardNodes() {
		List<IWildcardNode> result = new ArrayList<IWildcardNode>();
		fillWildcardNodes(result);
		return result;
	}

	@Override
	public void fillWildcardNodes(List<IWildcardNode> result) {
	}

	@Override
	public void addCondition(JoltCondition condition) {
		conditions.add(condition);
	}

	@Override
	public void addConditions(List<JoltCondition> conditions) {
		this.conditions.addAll(conditions);
	}

	@Override
	public List<JoltCondition> getConditions() {
		return conditions;
	}

	@Override
	public void fillNodes(List<INode> result, PathPredicate pathPredicate) {
		if (pathPredicate.compare(path)) {
			result.add(this);
		}
	}

	@Override
	public List<INode> findNodes(PathPredicate pathPredicate) {
		List<INode> result = new ArrayList<INode>();
		fillNodes(result, pathPredicate);
		return result;
	}

	@Override
	public int originalNodeCount() {
		return 0;
	}

	@Override
	public boolean hasSibling() {
		return parent.getChildren().size() > 1;
	}

	public void copyConditions(INode source) {
		conditions.addAll(source.getConditions());
	}
}
