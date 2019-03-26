package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class ResourceAccumulator implements SpecDriven, ContextualTransform {
	private String resourceType;

	@Inject
	@SuppressWarnings("unchecked")
	public ResourceAccumulator(Object spec) {
		Map<String, Object> map = (Map<String, Object>) spec;
		resourceType = (String) map.get("resourceType");
	}

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
		resource.put("resourceType", resourceType);
		resource.put("id", id);
		resources.add(resource);
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
		Iterator<Object> itr = identifiers.iterator();
		while (itr.hasNext()) {
			Object identifier = itr.next();
			Map<String, Object> map = (Map<String, Object>) identifier;
			String system = (String) map.get("system");
			Object valueObject = map.get("value");
			String value = valueObject == null ? null : valueObject.toString();
			refsByIdentifier.put(resourceType, system, value, reference);
			if (system != null && system.endsWith("0.0.0.0.0.0")) {
				itr.remove();
			}
		}
		if (identifiers.size() == 0) {
			resource.remove("identifier");
		}
		return reference;
	}
}
