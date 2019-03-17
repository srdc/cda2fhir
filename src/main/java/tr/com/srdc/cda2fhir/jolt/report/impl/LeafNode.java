package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class LeafNode extends Node implements ILeafNode {
	private String target;

	public LeafNode(IParentNode parent, String path) {
		super(parent, path);
	}

	public LeafNode(IParentNode parent, String path, String target) {
		super(parent, path);
		this.target = target;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public LeafNode clone(IParentNode parent) {
		String path = getPath();
		LeafNode result = new LeafNode(parent, path, target);
		result.addConditions(getConditions());
		return result;
	}

	public List<TableRow> toTableRows() {
		String path = getPath();
		TableRow row = new TableRow(path, target, null);
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
}
