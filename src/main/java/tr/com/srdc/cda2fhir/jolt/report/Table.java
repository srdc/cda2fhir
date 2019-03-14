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
	
	@Override
	public String toString() {
		return rows.stream().map(r -> r.toString()).collect(Collectors.joining("\n"));					
	}
	
	public String toCsv() {
		return rows.stream().map(row -> row.toCsvRow()).collect(Collectors.joining("\n"));
	}
}
