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
	 * Generates the hashMap used to create the ifNotExist Conditions.
	 */
	private static HashMap<String, Map<String, String>> createMap() {

		HashMap<String, Map<String, String>> urlStringMap = new HashMap<String, Map<String, String>>();

		Map<String, String> patientMap = new HashMap<>();
		patientMap.put("type", "identifier");
		patientMap.put("url", "urn:oid:2.16.840.1.113883.3.552.1.3.11.13.1.8.2");
		urlStringMap.put("Patient", patientMap);

		Map<String, String> conditionMap = new HashMap<>();
		conditionMap.put("type", "identifier");
		conditionMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.put("Condition", conditionMap);

		Map<String, String> reportMap = new HashMap<>();
		reportMap.put("type", "identifier");
		reportMap.put("url", "urn:oid:urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.put("Diagnostic Report", reportMap);

		Map<String, String> allergyMap = new HashMap<>();
		allergyMap.put("type", "identifier");
		allergyMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.put("Allergy Intolerance", allergyMap);

		Map<String, String> medStatementMap = new HashMap<>();
		medStatementMap.put("type", "identifier");
		medStatementMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.put("Medication Statement", medStatementMap);

		Map<String, String> medRequestMap = new HashMap<>();
		medRequestMap.put("type", "identifier");
		medRequestMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.put("Medication Request", medRequestMap);

		Map<String, String> procMap = new HashMap<>();
		procMap.put("type", "identifier");
		procMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.1.1988.1");
		urlStringMap.put("Procedure", procMap);

		Map<String, String> immunizationMap = new HashMap<>();
		immunizationMap.put("type", "identifier");
		immunizationMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.put("Immunization", immunizationMap);

		Map<String, String> observationMap = new HashMap<>();
		observationMap.put("type", "identifier");
		observationMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.6.798268.2000");
		urlStringMap.put("Observation", observationMap);

		Map<String, String> encounterMap = new HashMap<>();
		encounterMap.put("type", "identifier");
		encounterMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.3.698084.8");
		urlStringMap.put("Encounter", encounterMap);

		Map<String, String> pracMap = new HashMap<>();
		pracMap.put("type", "identifier");
		pracMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.697780");
		urlStringMap.put("Practitioner", pracMap);

		Map<String, String> orgMap = new HashMap<>();
		orgMap.put("type", "identifier");
		orgMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.696570");
		urlStringMap.put("Organization", orgMap);

		return urlStringMap;

	}

	/**
	 * Takes a bundle entry and generates the ifNotExists String.
	 *
	 * @param bundleEntry
	 * @return String for application on request.
	 */
	public static String generateIfNoneExist(BundleEntryComponent bundleEntry) {

		HashMap<String, Map<String, String>> urlStringMap = createMap();

		String ifNotExistString = null;

		for (String resourceType : urlStringMap.keySet()) {
			if (bundleEntry.getResource().getResourceType().name() == resourceType) {

				Map<String, String> entryMap = urlStringMap.get(resourceType);
				String type = entryMap.get("type");

				// Handle identifiers.
				if (type == "identifier") {

					List<Base> identifiers = bundleEntry.getResource().getNamedProperty(type).getValues();

					if (identifiers != null) {
						for (Base identifier : identifiers) {

							Identifier currentId = (Identifier) identifier;

							if (currentId.getSystem() != null) {
								System.out.println(currentId.getSystem());
								if (currentId.getSystem().equals(entryMap.get("url"))) {
									ifNotExistString = entryMap.get("type") + "=" + currentId.getSystem() + "|"
											+ currentId.getValue();
									return ifNotExistString;
								}
							}
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
