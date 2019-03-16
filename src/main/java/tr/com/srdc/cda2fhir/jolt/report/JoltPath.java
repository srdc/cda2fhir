package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltPath implements INode {
	private String path;
	private String target;
	private String link;
	public LinkedList<JoltPath> children = new LinkedList<JoltPath>();
	public List<JoltCondition> conditions = new ArrayList<JoltCondition>();

	public JoltPath(String path) {
		this.path = path;
	}

	public JoltPath(String path, String target) {
		this.path = path;
		this.target = target;
	}

	public JoltPath(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
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
	public JoltPath clone() {
		JoltPath result = new JoltPath(path, target, link);
		children.forEach(child -> {
			JoltPath childClone = child.clone();
			result.addChild(childClone);
		});
		conditions.forEach(condition -> {
			JoltCondition conditionClone = condition.clone();
			result.conditions.add(conditionClone);
		});
		return result;
	}

	@Override
	public List<JoltPath> getChildren() {
		return children;
	}
	
	@Override
	public List<JoltCondition> getConditions() {
		return conditions;
	}
	
	public String getTarget() {
		return target;
	}

	public String getLink() {
		return link;
	}

	@Override
	public void addChild(JoltPath child) {
		children.add(child);
	}

	public void removeChild(JoltPath child) {
		children.remove(child);
	}
	
	@Override
	public void addCondition(JoltCondition condition) {
		conditions.add(condition);		
	}
	
	public void addChildren(List<JoltPath> children) {
		this.children.addAll(children);
	}

	private void fillLinks(List<JoltPath> result) {
		if (link != null) {
			result.add(this);
		} else {
			children.forEach(child -> child.fillLinks(result));
		}
	}

	@Override
	public List<JoltPath> getLinks() {
		List<JoltPath> result = new ArrayList<JoltPath>();
		fillLinks(result);
		return result;
	}

	public void setTarget(String target) {
		this.target = target;
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
		children.forEach(child -> child.promoteTargets(parentTarget));
	}

	private boolean isLeaf() {
		return children.isEmpty();
	}
	
	@Override
	public void expandLinks(Map<String, RootNode> linkMap) {
		if (!isLeaf()) {	
			children.stream().filter(c -> !c.isLeaf()).forEach(c -> c.expandLinks(linkMap));
		
			List<JoltPath> linkedChildren = children.stream().filter(c -> c.isLeaf() && c.link != null).collect(Collectors.toList());
		
			linkedChildren.forEach(linkedChild -> {
				RootNode linkedNode = linkMap.get(linkedChild.link);
				if (linkedNode != null) {
					List<JoltPath> newChildren = linkedNode.getAsLinkReplacement(linkedChild.path, linkedChild.target);
					newChildren.forEach(newChild -> {
						newChild.conditions.addAll(linkedChild.conditions);
						newChild.expandLinks(linkMap);
					});
					children.remove(linkedChild);
					children.addAll(newChildren);
				}
			});
		}
	}

	public boolean isCondition() {
		return path.length() > 0 && path.charAt(0) == '!';
	}

	public JoltPath mergeToParent(JoltPath parent) {
		throw new NotImplementedException("Not available for arbirtrary nodes");
	}

	public void conditionalize() {
		children.forEach(child -> child.conditionalize());

		ListIterator<JoltPath> childIterator = children.listIterator();
		while (childIterator.hasNext()) {
			JoltPath child = childIterator.next();

			if (child.isLeaf()) continue;
			
			List<JoltPath> conditionNodes = child.children.stream().filter(n -> n.isCondition()).collect(Collectors.toList());
			if (conditionNodes.size() == 0) {
				continue;
			}
			if (conditionNodes.size() == child.children.size()) {
				childIterator.remove();
			}
			conditionNodes.forEach(node -> {
				JoltPath merged = node.mergeToParent(child);
				childIterator.add(merged);								
			});
		}
	}

	public List<TableRow> toTableRows() {
		if (children.size() < 1) {
			TableRow row = new TableRow(path, target, link);
			conditions.forEach(condition -> {
				String conditionAsString = condition.toString(path);
				row.addCondition(conditionAsString);
			});
			List<TableRow> result = new ArrayList<TableRow>();
			result.add(row);
			return result;
		}

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
