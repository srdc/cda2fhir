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
					JoltTemplate joltTemplate = JoltTemplate.getInstance(template);
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
		String output = transformationCSV("AllergyIntoleranceObservation");
		//String output = transformationCSV("EffectiveTimeLowOrValue");
		//String output = transformationText("AllergyConcernAct");
		System.out.print(output);
	}
}
