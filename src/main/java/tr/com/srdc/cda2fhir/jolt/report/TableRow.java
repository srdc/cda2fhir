package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TableRow implements Comparable<TableRow> {
	public String path = "";
	public String target = "";
	public String link;

	public List<String> conditions = new ArrayList<String>();
		
	TableRow(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}
	
	public String toCsvRow() {
		String result = String.format("%s,%s", target, path);
		if (conditions.size() > 0) {
			String conditionInfo = conditions.stream().collect(Collectors.joining(","));
			result += "," + conditionInfo;
		}
		return result;
	}

	public void sortConditions() {
		Collections.sort(conditions);
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
		result += "\n" + space + "* " + conditions.stream().collect(Collectors.joining("\n" + space + "* "));
		result += "\n" + space + targetDisplay;
		return result;
	}
}
