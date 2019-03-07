package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JsonUtils;

public class EntryToResource implements ContextualTransform {
    private final Logger logger = LoggerFactory.getLogger(EntryToResource.class);

    private static Chainr generateChainr(String filepath) {
        List<Object> spec = JsonUtils.filepathToList("src/test/resources/jolt/" + filepath);
        Chainr chainr = Chainr.fromSpec(spec);
        return chainr;
    }
    
    static Map<String, Chainr> templates = new HashMap<String, Chainr>();
    static {
    	templates.put("--AllergyConcernAct", generateChainr("entry/AllergyConcernAct.json"));
    	templates.put("--AssignedAuthorPractitioner", generateChainr("entry/AssignedAuthorPractitioner.json"));
    	templates.put("--AssignedAuthorPractitionerRole", generateChainr("entry/AssignedAuthorPractitionerRole.json"));
    }
    
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		Map<String, Object> map = (Map<String, Object>) input;
		Optional<Map.Entry<String, Object>> optional = map.entrySet().parallelStream().findFirst();
		if (!optional.isPresent()) {
			logger.error("No template key found in input");
			return input;
		}
		Map.Entry<String, Object> element = optional.get();
		String key = element.getKey();
		Chainr chainr = templates.get(key);
		if (chainr == null) {
			logger.error("No template found for " + key);
			return input;
		}
		Map<String, Object> resource = (Map<String, Object>) chainr.transform(element.getValue(), context);
		if (resource == null) {
			return null;
		}
		List<Object> resources = (List<Object>) context.get("Resources");
		int id = resources.size() + 1;
		resource.put("id", id);
		resources.add(resource);
		String resourceType = (String) resource.get("resourceType");
		String reference = String.format("%s/%s", resourceType, id);
		return reference;
	}
}
