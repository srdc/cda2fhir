package tr.com.srdc.cda2fhir.jolt.report;

public class TableRow {
	public String path = "";
	public String condition = "";
	public String format = "";
	public String target = "";
	public String link;

	public String getCSVRow() {
		return String.format("%s,%s,%s,%s\n", path, condition, format, target);
	}
	
	@Override
	public String toString() {
		String display = String.format("%s -> %s", path, target);
		return link != null ? String.format("%s (%s)", display, link) : display;
	}
}
