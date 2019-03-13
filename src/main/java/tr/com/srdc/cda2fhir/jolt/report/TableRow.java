package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableRow {
	public String path = "";
	public String format = "";
	public String target = "";
	public String link;

	public List<String> conditions = new ArrayList<String>();
		
	TableRow(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}
	
	public String getCSVRow() {
		return String.format("%s,%s,%s,%s\n", path, format, target);
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
