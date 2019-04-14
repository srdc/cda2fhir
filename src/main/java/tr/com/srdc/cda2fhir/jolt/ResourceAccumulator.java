package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class ResourceAccumulator implements SpecDriven, ContextualTransform {
	private String resourceType;
	private boolean keepNull = false;

	@Inject
	@SuppressWarnings("unchecked")
	public ResourceAccumulator(Object spec) {
		Map<String, Object> map = (Map<String, Object>) spec;
		resourceType = (String) map.get("resourceType");
		Boolean keepNullObject = (Boolean) map.get("keepNull");
		if (keepNullObject != null) {
			keepNull = keepNullObject.booleanValue();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null && !keepNull) {
			return null;
		}
		Map<String, Object> resource = (Map<String, Object>) input;
		if (resource == null) {
			resource = new LinkedHashMap<String, Object>();
		}
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
		String display = AdditionalModifier.getDisplay(resource);
		List<Object> identifiers = (List<Object>) resource.get("identifier");
		if (identifiers == null) {
			return resource;
		}
		IdentifierMap<String> refsByIdentifier = (IdentifierMap<String>) context.get("RefsByIdentifier");
		if (refsByIdentifier == null) {
			refsByIdentifier = new IdentifierMap<String>();
			context.put("RefsByIdentifier", refsByIdentifier);
		}
		IdentifierMap<String> refDisplaysByIdentifier = (IdentifierMap<String>) context.get("RefDisplaysByIdentifier");
		if (refDisplaysByIdentifier == null) {
			refDisplaysByIdentifier = new IdentifierMap<String>();
			context.put("RefDisplaysByIdentifier", refDisplaysByIdentifier);
		}
		Iterator<Object> itr = identifiers.iterator();
		while (itr.hasNext()) {
			Object identifier = itr.next();
			Map<String, Object> map = (Map<String, Object>) identifier;
			String system = (String) map.get("system");
			Object valueObject = map.get("value");
			String value = valueObject == null ? null : valueObject.toString();
			refsByIdentifier.put(resourceType, system, value, reference);
			if (display != null) {
				refDisplaysByIdentifier.put(resourceType, system, value, display);
			}
			if (system != null && system.endsWith("0.0.0.0.0.0")) {
				itr.remove();
			}
		}
		if (identifiers.size() == 0) {
			resource.remove("identifier");
		}
		return resource;
	}
}
