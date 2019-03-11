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

import org.json.JSONArray;
import org.json.JSONObject;

import com.bazaarvoice.jolt.JsonUtils;

public class Main {
	final private static String PATH = "src/test/resources/jolt/";
	final private static String OUTPUT_PATH = "src/test/resources/output/jolt-report";

	private static JoltTemplate toJoltTemplate(JSONArray content) {
		JoltTemplate result = new JoltTemplate();

		boolean beforeShift = true;
		int length = content.length();
		for (int index = 0; index < length; ++index) {
			JSONObject transform = content.getJSONObject(index);
			String operation = transform.getString("operation");
			if (operation.equals("cardinality")) {
				if (beforeShift) {
					result.cardinality = transform.getJSONObject("spec");
				}
				continue;
			}
			if (operation.equals("shift")) {
				JSONObject shift = transform.getJSONObject("spec");
				result.shifts.add(shift);
				continue;
			}
			if (operation.endsWith("ResourceAccumulator")) {
				result.topTemplate = true;
				continue;
			}
			if (operation.endsWith("AdditionalModifier")) {
				result.format = transform.getJSONObject("spec");
				continue;
			}
		}
		
		return result;
	}
	
	private static Map<String, JoltTemplate> createHandlers() {
		try (Stream<Path> walk = Files.walk(Paths.get(PATH))) {
			List<Path> jsonPaths = walk.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList());
			Map<String, JoltTemplate> templateMap = new HashMap<String, JoltTemplate>();
			for (Path jsonPath : jsonPaths) {
				String filename = jsonPath.getFileName().toString();
				String name = filename.substring(0, filename.length() - 4);
				Object content = JsonUtils.filepathToObject(jsonPath.toString());
				if (content instanceof JSONArray) {
					JSONArray template = (JSONArray) content;
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

	public static void main(String[] args) {
		Map<String, JoltTemplate> templateMap = createHandlers();
		if (templateMap != null) {
			JoltTemplate template = templateMap.get("AllergyConcernAct");
			Table table = template.createTable(templateMap);
			String output = table.writeCsv("");
			System.out.print(output);
		}
	}
}
