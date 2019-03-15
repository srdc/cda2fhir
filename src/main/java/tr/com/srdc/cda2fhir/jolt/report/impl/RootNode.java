package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.JoltFormat;
import tr.com.srdc.cda2fhir.jolt.report.JoltPath;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;

public class RootNode implements INode {
	private JoltPath root = new JoltPath("root");

	public RootNode() {
		root = new JoltPath("root");
		JoltPath base = new JoltPath("base");
		root.addChild(base);
	}
	
	public void addChild(JoltPath node) {
		root.children.get(0).addChild(node);
	}
	
	public List<JoltPath> getLinks() {
		return root.getLinks();
	}

	public void expandLinks(Map<String, RootNode> linkMap) {
		root.expandLinks(linkMap);
	}

	public void conditionalize() {
		root.createConditions();
		root.mergeSpecialDescendants();
	}

	public Table toTable(JoltFormat resolvedFormat) {
		Table result = new Table();
		root.children.forEach(child -> {
			child.children.forEach(grandChild -> {
				List<TableRow> grandChildRows = grandChild.toTableRows();
				grandChildRows.forEach(row -> {
					child.conditions.forEach(condition -> {
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

	public List<JoltPath> getAsLinkReplacement(String path, String target) {
		List<JoltPath> result = new ArrayList<JoltPath>();
		root.children.forEach(base -> {
			JoltPath node = base.clone();
			if (target.length() > 0) {
				node.promoteTargets(target);
			}
			node.setPath(path);
			result.add(node);
			
		});
		return result;
	}
}
