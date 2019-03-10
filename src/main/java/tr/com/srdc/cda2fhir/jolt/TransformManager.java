package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class TransformManager {
	@SuppressWarnings("unchecked")
	public static Map<String, Object> chooseResource(List<Object> resources, String resourceType) {
		for (Object resource: resources) {
			Map<String, Object> map = (Map<String, Object>) resource;
			String actualResourceType = (String) map.get("resourceType");
			if (resourceType.equals(actualResourceType)) {
				return map;
			}
		}
		return null;
	}
	
	private static Map<String, Object> getInitialContext() {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("Resources", new ArrayList<Object>());
		return context;
	}

	public static Object transformEntryInFile(String cdaName, String filepath) {
		String specpath = String.format("src/test/resources/jolt/entry/%s.json", cdaName);		
		List<Object> chainrSpec = JsonUtils.filepathToList(specpath);
		
        Chainr chainr = Chainr.fromSpec(chainrSpec);
        Object input = JsonUtils.filepathToObject(filepath);
 
        Map<String, Object> context = getInitialContext();
        chainr.transform(input, context);		
        Object result = context.get("Resources");
        return result;
	}
}
