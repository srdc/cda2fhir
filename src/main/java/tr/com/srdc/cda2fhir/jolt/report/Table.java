package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Table {
	private List<TableRow> rows = new ArrayList<TableRow>();

	public void addRows(Collection<TableRow> rows) {
		this.rows.addAll(rows);
	}

	public void addRow(TableRow row) {
		this.rows.add(row);
	}

	public void sort() {
		rows.forEach(row -> row.sortConditions());
		Collections.sort(rows);
	}

	public void updateFormats(JoltFormat formats) {
		rows.forEach(row -> row.updateFormat(formats));
	}

	@Override
	public String toString() {
		return rows.stream().map(r -> r.toString()).collect(Collectors.joining("\n"));
	}

	public String toCsv() {
		int conditionCount = rows.stream().map(r -> r.conditionCount()).mapToInt(Integer::intValue).max().getAsInt();
		String header = String.format("%s,%s,%s,%s", "CCDA Source", "Target", "Link", "Format");
		for (int index = 0; index < conditionCount; ++index) {
			header += "," + "Condition " + (index + 1);
		}
		header += "\n";
		return header + rows.stream().map(row -> row.toCsvRow()).collect(Collectors.joining("\n"));
	}
}
