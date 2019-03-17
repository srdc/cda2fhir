package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.JoltCondition;
import tr.com.srdc.cda2fhir.jolt.report.JoltFormat;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class RootNode {
	private ParentNode root;
	private ParentNode base;
	
	public RootNode() {
		root = new ParentNode("root");
		base = new ParentNode("base");
		root.addChild(base);
	}
	
	public void addChild(ParentNode node) {
		root.children.get(0).addChild(node);
	}
	
	public List<INode> getChildren() {
		return null;
	}
	
	public ParentNode getBase() {
		return base;
	}
	
	public void addCondition(JoltCondition condition) {
		root.children.get(0).addCondition(condition);		
	}
	
	public List<JoltCondition> getConditions() {
		return null;
	}
	
	public List<ILeafNode> getLinks() {
		return root.getLinks();
	}

	public void expandLinks(Map<String, RootNode> linkMap) {
		root.expandLinks(linkMap);
	}

	public void conditionalize() {
		root.conditionalize();
	}

	public Table toTable(JoltFormat resolvedFormat) {
		Table result = new Table();
		root.children.forEach(child -> {
			child.getChildren().forEach(grandChild -> {
				List<TableRow> grandChildRows = grandChild.toTableRows();
				grandChildRows.forEach(row -> {
					child.getConditions().forEach(condition -> {
						String conditionAsString = condition.toString();
						row.addCondition(conditionAsString);
					});					
				});
				result.addRows(grandChildRows);				
			});
		});
		if (resolvedFormat != null) {
			result.updateFormats(resolvedFormat);
		}
		return result;
	}

	public List<INode> getAsLinkReplacement(String path, String target) {
		List<INode> result = new ArrayList<INode>();
		root.children.forEach(base -> {
			INode node = base.clone();
			if (target.length() > 0) {
				node.promoteTargets(target);
			}
			node.setPath(path);
			result.add(node);
			
		});
		return result;
	}
}
