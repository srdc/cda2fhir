package tr.com.srdc.cda2fhir.jolt;

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

public class Utility {
	final private static String PATH = "src/test/resources/jolt/";

	@SuppressWarnings("unchecked")
	public static Map<String, List<Object>> readTemplates() {
		try (Stream<Path> walk = Files.walk(Paths.get(PATH))) {
			Map<String, List<Object>> result = new HashMap<>();
			List<Path> jsonPaths = walk.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList());
			jsonPaths.forEach(jsonPath -> {
				String parentPath = jsonPath.getParent().toString();
				if (parentPath.endsWith("value-maps")) {
					return;
				}
				String filename = jsonPath.getFileName().toString();
				String name = filename.substring(0, filename.length() - 5);
				Object content = JsonUtils.filepathToObject(jsonPath.toString());
				if (content instanceof List) {
					List<Object> template = (List<Object>) content;
					result.put(name, template);
				}
			});
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
