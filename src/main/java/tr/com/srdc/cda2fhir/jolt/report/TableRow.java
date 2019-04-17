package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TableRow implements Comparable<TableRow> {
	private String path = "";
	private String target = "";
	private String link;
	private String format = "";

	private List<ICondition> conditions = new ArrayList<ICondition>();

	private static final String[] pluralFormatWords = { "max", "min", "first", "last" };

	public TableRow(String path, String target) {
		this.path = path;
		this.target = target;
	}

	public TableRow(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}

	@Override
	public TableRow clone() {
		TableRow row = new TableRow(path, target, link);
		row.format = format;
		row.conditions.addAll(conditions);
		return row;
	}

	public String getPath() {
		return path;
	}

	public String getTarget() {
		return target;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void promotePath(String parentPath) {
		if (path.charAt(0) != '\'' && path.charAt(0) != '#') {
			path = parentPath + "." + path;
		}

		conditions.forEach(c -> c.prependPath(parentPath));
	}

	public void addCondition(ICondition condition) {
		conditions.add(condition);
	}

	public int conditionCount() {
		return conditions.size();
	}

	public String toCsvRow() {
		String csvLink = link == null ? "" : link;
		String result = String.format("%s,%s,%s,", path, target, csvLink, format);
		if (!format.isEmpty()) {
			result += String.format("\"%s\"", format);
		}
		if (conditions.size() > 0) {
			String conditionInfo = conditions.stream().map(r -> r.toString()).collect(Collectors.joining(","));
			result += "," + conditionInfo;
		}
		return result;
	}

	public void sortConditions() {
		Collections.sort(conditions);
	}

	public void promoteTarget(String path) {
		if (target.isEmpty()) {
			target = path;
		} else {
			target = path + "." + target;
		}
	}

	public void correctArrayOnFormat() {
		if (!format.isEmpty() && target.indexOf("[") >= 0) {
			for (int index = 0; index < pluralFormatWords.length; ++index) {
				if (format.startsWith(pluralFormatWords[index])) {
					target = target.split("\\[")[0];
				}
			}
		}
	}

	public void updateResourceType(String resourceType, Set<String> exceptions) {
		String targetPiece = target.split("\\.")[0];
		if (!exceptions.contains(targetPiece)) {
			target = resourceType + "." + target;
		}
		String pathPiece = path.split("\\.")[0];
		if (!exceptions.contains(pathPiece) && path.charAt(0) != '#') {
			path = resourceType + "." + path;
		}
	}

	@Override
	public int compareTo(TableRow rhs) {
		int targetResult = target.compareTo(rhs.target);
		if (targetResult != 0) {
			return targetResult;
		}
		return path.compareTo(rhs.path);
	}

	@Override
	public String toString() {
		String targetDisplay = link != null ? String.format("%s (%s)", target, link) : target;
		if (conditions.size() == 0) {
			return String.format("%s -> %s", path, targetDisplay);
		}
		String space = "    ";
		String result = path + " ->";
		result += "\n" + space + "*condition "
				+ conditions.stream().map(r -> r.toString()).collect(Collectors.joining("\n" + space + "*condition "));
		if (!format.isEmpty()) {
			result += "\n" + space + "*format " + format;
		}
		result += "\n" + space + targetDisplay;
		return result;
	}

	public Set<String> getPathKeys() {
		Set<String> result = new HashSet<>();
		result.add(path);
		if (path.indexOf("[]") > 0) {
			String pathZeroIndex = path.replace("[]", "[0]");
			result.add(pathZeroIndex);
		}
		return result;
	}

	public TableRow getUpdatedFromPathMap(Map<String, TableRow> map) {
		TableRow mapped = map.get(target);
		if (mapped == null) {
			return null;
		}
		TableRow result = clone();
		result.target = mapped.target;
		return result;
	}
}
