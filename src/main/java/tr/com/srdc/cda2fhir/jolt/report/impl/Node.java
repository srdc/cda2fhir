package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;

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
	public List<IConditionNode> getConditionNodes() {
		List<IConditionNode> result = new ArrayList<IConditionNode>();
		fillConditionNodes(result);
		return result;		
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
}
