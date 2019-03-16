package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	public void setPath(String path) {
		this.path = path;
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

	@Override
	public void addCondition(JoltCondition condition) {
		conditions.add(condition);		
	}
	
	public void addChildren(List<JoltPath> children) {
		this.children.addAll(children);
	}

	public void addChildrenOf(JoltPath joltPath) {
		this.children.addAll(joltPath.children);
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

	private boolean isSpecial(char type) {
		return path.length() > 0 && path.charAt(0) == type;
	}

	private long specialChildCount(char type) {
		if (children.size() == 0) {
			return 0;
		}
		return children.stream().filter(child -> child.isSpecial(type)).count();
	}

	private long specialGrandChildrenCount(char type) {
		if (children.size() == 0) {
			return 0;
		}
		return children.stream().filter(child -> {
			return child.specialChildCount(type) > 0;
		}).count();
	}

	private void mergeSpecialChild() {
		if (children.size() != 1) {
			throw new ReportException("Only a unique child can be merged.");

		}
		JoltPath child = children.get(0);
		if (!child.isSpecial('!')) {
			throw new ReportException("Only special children can be merged.");
		}

		children.remove(child);
		children.addAll(child.children);
		link = child.link;
		target = child.target;

		int childRank = Integer.valueOf(child.path.substring(1));
		if (childRank == 0) {
			conditions.addAll(child.conditions);
			return;
		}
		child.conditions.forEach(condition -> {
			condition.prependPath(path);
			conditions.add(condition);
		});
		path = "!" + (childRank - 1);
	}

	public void mergeSpecialGrandChildren() {
		children.forEach(child -> child.mergeSpecialGrandChildren());

		long specialCount = specialGrandChildrenCount('!');
		if (specialCount == 0) {
			return;
		}
		List<JoltPath> childClones = new ArrayList<JoltPath>();
		List<JoltPath> childrenToBeRemoved = new ArrayList<JoltPath>();
		children.forEach(child -> {
			if (child.specialChildCount('!') < 1) {
				return;
			}
			if (child.children.size() == 1) {
				child.mergeSpecialChild();
				return;
			}
			List<JoltPath> specialGrandChildren = child.children.stream().filter(c -> c.isSpecial('!'))
					.collect(Collectors.toList());
			for (JoltPath grandChild : specialGrandChildren) {
				JoltPath childClone = child.clone();
				childClone.children.clear();
				childClone.children.add(grandChild);
				child.children.remove(grandChild);
				childClones.add(childClone);
			}
			if (child.children.size() == 0) {
				childrenToBeRemoved.add(child);
			}
		});
		childClones.forEach(childClone -> {
			children.add(childClone);
			childClone.mergeSpecialChild();
		});
		children.removeAll(childrenToBeRemoved);
	}

	@Override
	public void conditionalize() {
		mergeSpecialGrandChildren();
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
