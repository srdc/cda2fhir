package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Table {
	private List<TableRow> rows;

	public Table() {
		rows = new ArrayList<>();
	}

	private Table(List<TableRow> rows) {
		this.rows = rows;
	}

	public void addRows(Collection<TableRow> rows) {
		this.rows.addAll(rows);
	}

	public void addRow(TableRow row) {
		this.rows.add(row);
	}

	public void addTable(Table table) {
		addRows(table.rows);
	}

	public List<TableRow> getRows() {
		return rows;
	}

	public void correctArrayOnFormat() {
		rows.forEach(row -> row.correctArrayOnFormat());
	}

	public void sort() {
		rows.forEach(row -> row.sortConditions());
		Collections.sort(rows);
	}

	public void promoteTargets(String path) {
		if (!path.isEmpty()) {
			rows.forEach(row -> row.promoteTarget(path));
		}
	}

	public void renameSources(Map<String, String> alias) {
		if (alias != null) {
			rows.forEach(row -> row.renameSources(alias));
		}
	}

	public void updateResourceType(String resourceType, Set<String> exceptions) {
		rows.forEach(row -> row.updateResourceType(resourceType, exceptions));
	}

	@Override
	public Table clone() {
		Table result = new Table();
		rows.forEach(row -> result.addRow(row.clone()));
		return result;
	}

	@Override
	public String toString() {
		return rows.stream().map(r -> r.toString()).collect(Collectors.joining("\n"));
	}

	public String toCsv() {
		int conditionCount = rows.stream().map(r -> r.conditionCount()).mapToInt(Integer::intValue).max().getAsInt();
		String header = String.format("%s,%s,%s,%s,%s", "CCDA Source", "Target", "Link", "Format", "Default");
		for (int index = 0; index < conditionCount; ++index) {
			header += "," + "Condition " + (index + 1);
		}
		header += "\n";
		return header + rows.stream().map(row -> row.toCsvRow()).collect(Collectors.joining("\n"));
	}

	public Map<String, TableRow> getPathMap() {
		Map<String, TableRow> result = new HashMap<>();
		rows.forEach(row -> {
			Set<String> keys = row.getPathKeys();
			keys.forEach(key -> result.put(key, row));
		});
		return result;
	}

	public Table getUpdatedFromPathMap(Map<String, TableRow> map) {
		List<TableRow> newRows = rows.stream().map(r -> r.getUpdatedFromPathMap(map)).filter(r -> r != null)
				.collect(Collectors.toList());
		return new Table(newRows);
	}

	public void moveTargets(Map<String, String> moveMap) {
		rows.forEach(row -> {
			String target = row.getTarget();
			if (target != null) {
				String moveValue = moveMap.get(target);
				if (moveValue != null) {
					for (TableRow row2 : rows) {
						String target2 = row2.getTarget();
						if (target2 != null && target2.split("\\[")[0].equals(moveValue)) {
							moveValue = target2;
							break;
						}
					}
					row.promoteTarget(moveValue);
				}
			}
		});
	}

	public void flattenTarget(String flattened) {
		rows.forEach(row -> {
			String target = row.getTarget();
			if (target != null && target.startsWith(flattened)) {
				int location = target.indexOf('.') + 1;
				if (location > 0) {
					String newTarget = target.substring(location);
					row.setTarget(newTarget);
				}
			}

		});
	}

	public void addDefaultValues(Map<String, String> values) {
		if (values != null) {
			rows.forEach(row -> row.setDefaultValue(values));
		}
	}

	public int rowCount() {
		return rows.size();
	}
}
