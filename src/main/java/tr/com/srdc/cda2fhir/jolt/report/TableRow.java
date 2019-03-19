package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TableRow implements Comparable<TableRow> {
	private String path = "";
	private String target = "";
	private String link;
	private String format = "";

	private List<String> conditions = new ArrayList<String>();

	public TableRow(String path, String target) {
		this.path = path;
		this.target = target;
	}

	public TableRow(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}

	public void promotePath(String parentPath) {
		if (path.charAt(0) != '\'') {
			path = parentPath + "." + path;
		}

		for (int index = 0; index < conditions.size(); ++index) {
			String value = conditions.get(index);
			conditions.set(index, parentPath + "." + value);
		}
	}

	public void addCondition(String condition) {
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
			String conditionInfo = conditions.stream().collect(Collectors.joining(","));
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
				+ conditions.stream().collect(Collectors.joining("\n" + space + "*condition "));
		if (!format.isEmpty()) {
			result += "\n" + space + "*format " + format;
		}
		result += "\n" + space + targetDisplay;
		return result;
	}
}
