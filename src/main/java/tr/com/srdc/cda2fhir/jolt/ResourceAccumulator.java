package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class ResourceAccumulator implements ContextualTransform {
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}		
		Map<String, Object> resource = (Map<String, Object>) input;
		List<Object> resources = (List<Object>) context.get("Resources");
		if (resources == null) {
			resources = new ArrayList<Object>();
			context.put("Resources", resources);
		}
		int id = resources.size() + 1;
		resource.put("id", id);
		resources.add(resource);
		String resourceType = (String) resource.get("resourceType");
		String reference = String.format("%s/%s", resourceType, id);
		List<Object> identifiers = (List<Object>) resource.get("identifier");
		if (identifiers == null) {
			return reference;
		}		
		IdentifierMap<String> refsByIdentifier = (IdentifierMap<String>) context.get("RefsByIdentifier");
		if (refsByIdentifier == null) {
			refsByIdentifier = new IdentifierMap<String>();
			context.put("RefsByIdentifier", refsByIdentifier);
		}
		for (Object identifier: identifiers) {
			Map<String, Object> map = (Map<String, Object>) identifier;
			String system = (String) map.get("system");
			Object valueObject = map.get("value");
			String value = valueObject == null ? null : valueObject.toString(); 
			refsByIdentifier.put(resourceType, system, value, reference);
			
		}
		return reference;
	}
}
