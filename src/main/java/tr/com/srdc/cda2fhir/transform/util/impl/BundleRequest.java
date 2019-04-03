package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Identifier;

public class BundleRequest {

	/**
	 * Adds an entry to the ifNotExists map.
	 *
	 * @param resource     - String name of the resource for the condition.
	 * @param location     - FHIR location of the resource to be used in the map.
	 * @param system       - system used in the identifier lookup.
	 * @param conditionMap - Map to which the entry needs to be appended.
	 */
	private static void addMapEntry(String resource, String location, String system,
			HashMap<String, Map<String, String>> conditionMap) {

		Map<String, String> map = new HashMap<>();
		map.put("location", location);
		map.put("system", system);
		conditionMap.put(resource, map);

	}

	/**
	 * Takes a bundle entry and generates the ifNotExists String.
	 *
	 * @param bundleEntry
	 * @return String for application on request.
	 */
	public static String generateIfNoneExist(BundleEntryComponent bundleEntry) {

		HashMap<String, Map<String, String>> urlStringMap = new HashMap<String, Map<String, String>>();

		Map<String, String> map = new HashMap<>();
		// named property, and string with value replacement.
		map.put("type", "identifier");
		map.put("url", "urn:oid:2.16.840.1.113883.3.552.1.3.11.11.1.8.2");
		urlStringMap.put("Patient", map);

		String ifNotExistString = null;

		for (String resourceType : urlStringMap.keySet()) {
			if (bundleEntry.getResource().getResourceType().name() == resourceType) {

				Map<String, String> entryMap = urlStringMap.get(resourceType);
				String type = entryMap.get("type");

				// Handle identifiers.
				if (type == "identifier") {

					List<Base> identifiers = bundleEntry.getResource().getNamedProperty(type).getValues();
					for (Base identifier : identifiers) {

						Identifier currentId = (Identifier) identifier;

						if (currentId.getSystem().equals(entryMap.get("url"))) {
							ifNotExistString = entryMap.get("type") + "=" + currentId.getSystem() + "|"
									+ currentId.getValue();
							break;
						}
					}
				}
			}
		}

		return ifNotExistString;

	}

	/**
	 * Adds request field to the entry, method is POST, url is resource type.
	 *
	 * @param entry Entry which request field to be added.
	 */
	public static void addRequestToEntry(BundleEntryComponent entry) {

		BundleEntryRequestComponent request = new BundleEntryRequestComponent();

		String ifNoneExistString = generateIfNoneExist(entry);
		if (ifNoneExistString != null) {
			request.setIfNoneExist(ifNoneExistString);
		}

		request.setMethod(HTTPVerb.POST);
		// request.setUrl(entry.getResource().getResourceName());
		request.setUrl(entry.getResource().getResourceType().name());
		entry.setRequest(request);
	}

}
