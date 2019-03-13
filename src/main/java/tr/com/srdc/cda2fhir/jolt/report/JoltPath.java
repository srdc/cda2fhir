package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoltPath {
	private LinkedList<JoltPath> children = new LinkedList<JoltPath>();
	private List<JoltCondition> conditions = new ArrayList<JoltCondition>();
	private String path;
	private String target;
	private String link;
	
	private JoltPath(String path) {
		this.path = path;
	}
	
	private JoltPath(String path, String target) {
		this.path = path;
		this.target = target;
	}
	
	private JoltPath(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}
	
	@Override
	public JoltPath clone() {
		JoltPath result = new JoltPath(path, target, link);
		result.children.addAll(children);
		result.conditions.addAll(conditions);
		return result;
	}
	
	public String getLink() {
		return link;
	}
	
	
	public void addChild(JoltPath child) {
		children.add(child);
	}
	
	public void addChildren(List<JoltPath> children) {
		this.children.addAll(children);
	}
	
	public void setTarget(String target) {
		this.target = target; 
	}

	public void expandLinks(Map<String, List<JoltPath>> linkMap) {
		if (link == null) {
			children.forEach(c -> c.expandLinks(linkMap));
			return;
		}
		
		List<JoltPath> linkPaths = linkMap.get(link);
		if (linkPaths != null) {
			linkPaths.forEach(lp -> lp.expandLinks(linkMap));
			children.addAll(linkPaths);
			link = null;
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
	
	private List<JoltCondition> childToCondition(String value, List<JoltCondition> allConditions) {
		if ("*".equals(value)) {
			List<JoltCondition> reverseConditions = allConditions.stream().map(c -> c.not()).collect(Collectors.toList());
			return reverseConditions;
		}
		if (value.length() == 0) {
			JoltCondition condition = new JoltCondition("", "isnull");
			List<JoltCondition> conditions = new ArrayList<JoltCondition>();
			conditions.add(condition);
			return conditions;
		}
		JoltCondition condition = new JoltCondition("", "equal", value);
		List<JoltCondition> conditions = new ArrayList<JoltCondition>();
		conditions.add(condition);
		return conditions;
	}
	
	private static String decrementSpecialPath(String path) {
		int value = Integer.valueOf(path.substring(1));
		value -= 1;
		return "!" + value;
	}
	
	private void mergeSpecialGrandChild(JoltPath child) {
		if (child.children.size() != 1) {
			throw new ReportException("Only unique Special children can be merged");
			
		}		
		JoltPath grandChild = child.children.get(0);
		child.conditions.addAll(grandChild.conditions);
		child.children.remove(grandChild);
		child.children.addAll(grandChild.children);		
		int grandChildRank = Integer.valueOf(grandChild.path.substring(1));
		if (grandChildRank == 0) {
			return;
		}
		JoltPath replacementChild = new JoltPath("!" + (grandChildRank - 1));
		child.conditions.forEach(condition -> {
			condition.prependPath(child.path);
			replacementChild.conditions.add(condition);
		});
		replacementChild.children.addAll(child.children);
		children.remove(child);
		children.add(replacementChild);		
	}
	
	public void mergeSpecialGrandChildren() {
		for (JoltPath child: children) {
			child.mergeSpecialGrandChildren();
		}
		
		long specialCount = specialGrandChildrenCount('!');
		if (specialCount == 0) {
			return;
		}
		List<JoltPath> childClones = new ArrayList<JoltPath>();
		List<JoltPath> childrenToBeRemoved = new ArrayList<JoltPath>();
		for (JoltPath child: children) {
			if (child.specialChildCount('!') < 1) {
				continue;
			}			
			if (child.children.size() == 1) {				
				mergeSpecialGrandChild(child);
				continue;
			}
			List<JoltPath> specialGrandChildren = child.children.stream().filter(c -> c.isSpecial('!')).collect(Collectors.toList());			
			for (JoltPath grandChild: specialGrandChildren) {
				JoltPath childClone = child.clone();
				childClone.children.clear();
				childClone.children.add(grandChild);
				child.children.remove(grandChild);
				childClones.add(childClone);
			}
			if (child.children.size() == 0) {
				childrenToBeRemoved.add(child);
			}
		};
		childClones.forEach(childClone -> {
			children.add(childClone);
			mergeSpecialGrandChild(childClone);
		});
		children.removeAll(childrenToBeRemoved);
	}
	
	public void createConditions(JoltPath parent) {
		if (children.size() == 0) {
			return;
		}
		children.forEach(child -> child.createConditions(this));
		List<JoltPath> nullChildren = children.parallelStream().filter(child -> {
			return child.children.size() == 0 && child.target == null;
		}).collect(Collectors.toList());
		nullChildren.forEach(child -> {
			String conditionPath = child.path;
			JoltCondition condition = new JoltCondition(conditionPath, "isnull");
			conditions.add(condition);
			children.remove(child);
		});
		
		long valueCount = specialGrandChildrenCount('@');
		if (valueCount > 0) {
			if (children.size() != valueCount) {
				throw new ReportException("Unsupported Jolt template.");
			}
			List<JoltCondition> allConditions = new ArrayList<JoltCondition>();
			
			List<JoltPath> newChildren = children.stream().map(child -> {
				JoltPath grandChild = child.children.get(0);
				String newPath = decrementSpecialPath(grandChild.path);
				JoltPath newChild = new JoltPath(newPath, grandChild.target, grandChild.link);
				newChild.children.addAll(grandChild.children);
				List<JoltCondition> conditions = childToCondition(child.path, allConditions);
				allConditions.addAll(conditions);
				newChild.conditions.addAll(conditions);
				return newChild;
			}).collect(Collectors.toList());
			
			children.clear();
			children.addAll(newChildren);			
		}
	}
	
	public List<TableRow> toTableRows() {
		if (children.size() < 1) {
			TableRow row = new TableRow(path, target, link);
			conditions.forEach(condition -> {
				String conditionAsString = condition.toString(path);
				row.conditions.add(conditionAsString);
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
			row.path = path + "." + row.path;
			if (target != null) {
				row.target = target + "." + row.target;
			}
			
			for (int index=0; index < row.conditions.size(); ++index) {
				String value = row.conditions.get(index);
				row.conditions.set(index, path + "." + value);
			}
			
			conditions.forEach(condition -> {
				String conditionAsString = condition.toString(path);
				row.conditions.add(conditionAsString);
			});
		});
		return rows;
	}
	
	public static JoltPath getInstance(String path, String target) {
		if (target == null) {
			return new JoltPath(path, null);			
		}		
		String[] pieces = target.split("\\.");		
		int length = pieces.length;
		String lastPiece = pieces[length-1];
		if (!lastPiece.startsWith("->")) {
			return new JoltPath(path, target);						
		}
		String link = lastPiece.substring(2);
		if (length == 1) {
			return new JoltPath(path, "", link);
		}
		String reducedTarget = pieces[0];
		for (int index = 1; index < length - 1; ++index) {
			reducedTarget += "." + pieces[index];
		}
		return new JoltPath(path, reducedTarget, link);
	}
}
