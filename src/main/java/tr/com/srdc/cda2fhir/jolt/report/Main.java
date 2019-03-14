package tr.com.srdc.cda2fhir.jolt.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bazaarvoice.jolt.JsonUtils;

public class Main {
	final private static String PATH = "src/test/resources/jolt/";
	// final private static String OUTPUT_PATH = "src/test/resources/output/jolt-report";

	@SuppressWarnings("unchecked")
	private static JoltTemplate toJoltTemplate(List<Object> content) {
		JoltTemplate result = new JoltTemplate();

		boolean beforeShift = true;
		int length = content.size();
		for (int index = 0; index < length; ++index) {
			Map<String, Object> transform = (Map<String, Object>) content.get(index);
			String operation = (String) transform.get("operation");
			if (operation.equals("cardinality")) {
				if (beforeShift) {
					result.cardinality = (Map<String, Object>) transform.get("spec");
				}
				continue;
			}
			if (operation.equals("shift")) {
				Map<String, Object> shift = (Map<String, Object>) transform.get("spec");
				result.shifts.add(shift);
				continue;
			}
			if (operation.endsWith("ResourceAccumulator")) {
				result.topTemplate = true;
				continue;
			}
			if (operation.endsWith("AdditionalModifier")) {
				result.format = (Map<String, Object>) transform.get("spec");
				continue;
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, JoltTemplate> createHandlers() {
		try (Stream<Path> walk = Files.walk(Paths.get(PATH))) {
			List<Path> jsonPaths = walk.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList());
			Map<String, JoltTemplate> templateMap = new HashMap<String, JoltTemplate>();
			for (Path jsonPath : jsonPaths) {
				String filename = jsonPath.getFileName().toString();
				String name = filename.substring(0, filename.length() - 5);
				Object content = JsonUtils.filepathToObject(jsonPath.toString());
				if (content instanceof List) {
					List<Object> template = (List<Object>) content;
					JoltTemplate joltTemplate = toJoltTemplate(template);
					if (name.equals("ID") || name.contentEquals("CD")) {
						joltTemplate.leafTemplate = true;
					}
					templateMap.put(name, joltTemplate);
				}
			}
			return templateMap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
		// String output = transformationCSV("AllergyConcernAct");
		String output = transformationText("AllergyConcernAct");
		System.out.print(output);
	}
}
