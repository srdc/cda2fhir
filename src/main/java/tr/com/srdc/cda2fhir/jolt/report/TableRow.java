package tr.com.srdc.cda2fhir.jolt.report;

public class TableRow {
	public String path = "";
	public String condition = "";
	public String format = "";
	public String target = "";

	public String getCSVRow() {
		return String.format("%s,%s,%s,%s\n", path, condition, format, target);
	}
}
