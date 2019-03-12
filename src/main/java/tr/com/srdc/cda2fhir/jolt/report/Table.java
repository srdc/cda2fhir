package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Table {
	public List<TableRow> rows = new ArrayList<TableRow>();
		
	public String writeCsv() {
		String result = rows.stream().map(row -> row.getCSVRow()).collect(Collectors.joining());
		return result;
	}
}
