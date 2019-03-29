package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.Utility;

public class Main {
	private static Map<String, JoltTemplate> createHandlers() {
		Map<String, List<Object>> rawTemplates = Utility.readTemplates();
		if (rawTemplates == null) {
			return null;
		}
		return rawTemplates.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> JoltTemplate.getInstance(e.getKey(), e.getValue())));
	}

	public static Table transformationTable(String name) {
		Map<String, JoltTemplate> templateMap = createHandlers();
		if (templateMap != null) {
			JoltTemplate template = templateMap.get(name);
			Table table = template.createTable(templateMap);
			table.sort();
			return table;
		}
		return null;

	}

	public static String transformationCSV(String name) {
		Table table = transformationTable(name);
		if (table != null) {
			String output = table.toCsv();
			return output;
		}
		return null;
	}

	public static String transformationText(String name) {
		Table table = transformationTable(name);
		if (table != null) {
			String output = table.toString();
			return output;
		}
		return null;
	}

	public static void main(String[] args) {
		String output = transformationCSV("ProcedureActivityProcedure");
		System.out.print(output);
	}
}
