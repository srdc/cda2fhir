package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class LeafNode implements ILeafNode {
	private String path;
	private String target;
	private String link;

	public List<JoltCondition> conditions = new ArrayList<JoltCondition>();

	public LeafNode(String path) {
		this.path = path;
	}

	public LeafNode(String path, String target) {
		this.path = path;
		this.target = target;
	}

	public LeafNode(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}

	public String getPath() {
		return path;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getTarget() {
		return target;
	}

	@Override
	public LeafNode clone() {
		LeafNode result = new LeafNode(path, target, link);
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
		this.conditions.addAll(conditions)
;	}

	public void addChild(INode node) {}
	
	public void removeChild(INode node) {}
	
	public List<INode> getChildren() {
		return null;
	}
	
	public List<ILeafNode> getLinks() {
		return null;
	}

	public void expandLinks(Map<String, RootNode> linkMap) {
	}
	
	public void conditionalize() {
	}

	public List<TableRow> toTableRows() {
		TableRow row = new TableRow(path, target, link);
		conditions.forEach(condition -> {
			String conditionAsString = condition.toString(path);
			row.addCondition(conditionAsString);
		});
		List<TableRow> result = new ArrayList<TableRow>();
		result.add(row);
		return result;
	}
	
	public boolean isLeaf() {
		return true;
	}
	
	public void fillLinks(List<ILeafNode> result) {
		if (link != null) {
			result.add(this);
		}		
	}
	
	public boolean isCondition() {
		return false;
	}
	
	public INode mergeToParent(INode node) {
		return null;
	}

	public void promoteTargets(String parentTarget){
		if (target != null) {
			if (target.length() > 0) {
				target = parentTarget + "." + target;
			} else {
				target = parentTarget;
			}
			return;
		}		
	}
	
	public void setPath(String path){
		this.path = path;
	}
}
