package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.Map;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class DeferredUpdate {
	private Map<String, Object> resource;
	private Map<String, Object> identifier;

	public DeferredUpdate(Map<String, Object> resource, Map<String, Object> identifier) {
		this.resource = resource;
		this.identifier = identifier;
	}

	public void update(IdentifierMap<String> referenceMap, IdentifierMap<String> displayMap) {
		String resourceType = (String) resource.get("resourceType");
		if (!"Procedure".equals(resourceType)) { // only procedures
			return;
		}
		String reference = referenceMap.getFromJSONArray("Encounter", identifier);
		// String display = displayMap.getFromJSONArray("Encounter", identifier);
		// Keeps the bug in cda2fhir to be able to compare
		String display = AdditionalModifier.getDisplay(resource);
		if (reference == null && display == null) {
			return;
		}
		Map<String, Object> procedureContext = new LinkedHashMap<>();
		if (reference != null) {
			procedureContext.put("reference", reference);
		}
		if (display != null) {
			procedureContext.put("display", display);
		}
		resource.put("context", procedureContext);
	}
}
