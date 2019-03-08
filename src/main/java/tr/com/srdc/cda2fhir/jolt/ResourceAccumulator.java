package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

public class ResourceAccumulator implements ContextualTransform {
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}		
		Map<String, Object> resource = (Map<String, Object>) input;
		List<Object> resources = (List<Object>) context.get("Resources");
		int id = resources.size() + 1;
		resource.put("id", id);
		resources.add(resource);
		String resourceType = (String) resource.get("resourceType");
		String reference = String.format("%s/%s", resourceType, id);
		return reference;
	}
}
