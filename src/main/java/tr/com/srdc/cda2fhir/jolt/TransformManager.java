package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class TransformManager {
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> chooseResources(List<Object> resources, String resourceType) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object resource : resources) {
			Map<String, Object> map = (Map<String, Object>) resource;
			String actualResourceType = (String) map.get("resourceType");
			if (resourceType.equals(actualResourceType)) {
				result.add(map);
			}
		}
		return result;
	}

	public static Map<String, Object> chooseResource(List<Object> resources, String resourceType) {
		List<Map<String, Object>> results = chooseResources(resources, resourceType);
		return results.size() == 0 ? null : results.get(0);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> chooseResourceById(List<Object> resources, String resourceType, String id) {
		for (Object resource : resources) {
			Map<String, Object> map = (Map<String, Object>) resource;
			String actualResourceType = (String) map.get("resourceType");
			if (resourceType.equals(actualResourceType)) {
				Object actualId = map.get("id");
				if (actualId != null && id.equals(actualId.toString())) {
					return map;
				}
			}
		}
		return null;
	}

	public static Map<String, Object> chooseResourceByReference(List<Object> resources, String reference) {
		String[] pieces = reference.split("/");
		String resourceType = pieces[0];
		String id = pieces[1];
		return chooseResourceById(resources, resourceType, id);
	}

	private static Map<String, Object> getInitialContext() {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("Resources", new ArrayList<Object>());
		return context;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> transformEntryInFile(String cdaName, String filepath) {
		String specpath = String.format("src/test/resources/jolt/entry/%s.json", cdaName);
		List<Object> chainrSpec = JsonUtils.filepathToList(specpath);

		Chainr chainr = Chainr.fromSpec(chainrSpec);
		Object input = JsonUtils.filepathToObject(filepath);

		Map<String, Object> context = getInitialContext();
		chainr.transform(input, context);
		List<Object> result = (List<Object>) context.get("Resources");
		return result;
	}
}
