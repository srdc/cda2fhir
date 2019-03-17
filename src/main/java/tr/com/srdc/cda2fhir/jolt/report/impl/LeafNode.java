package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class LeafNode extends Node implements ILeafNode {
	private IParentNode parent;
	private String path;
	private String target;

	public List<JoltCondition> conditions = new ArrayList<JoltCondition>();

	public LeafNode(IParentNode parent, String path) {
		this.parent = parent;
		this.path = path;
	}

	public LeafNode(IParentNode parent, String path, String target) {
		this.parent = parent;
		this.path = path;
		this.target = target;
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
	public String getTarget() {
		return target;
	}

	@Override
	public LeafNode clone(IParentNode parent) {
		LeafNode result = new LeafNode(parent, path, target);
		result.conditions.addAll(conditions);
		return result;
	}

	public void addCondition(JoltCondition condition) {
		conditions.add(condition);
	}

	public List<JoltCondition> getConditions() {
		return conditions;
	}

	public void addConditions(List<JoltCondition> conditions) {
		this.conditions.addAll(conditions);
	}

	public List<TableRow> toTableRows() {
		TableRow row = new TableRow(path, target, null);
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
	}

	@Override
	public void fillConditionNodes(List<IConditionNode> result) {
	}

	public void promoteTargets(String parentTarget) {
		if (target != null) {
			if (target.length() > 0) {
				target = parentTarget + "." + target;
			} else {
				target = parentTarget;
			}
			return;
		}
	}

	public void setPath(String path) {
		this.path = path;
	}
}
