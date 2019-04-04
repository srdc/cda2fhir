package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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
	private static ArrayList<AbstractMap.SimpleEntry<String, Map<String, String>>> createMap() {

		ArrayList<AbstractMap.SimpleEntry<String, Map<String, String>>> urlStringMap = new ArrayList<AbstractMap.SimpleEntry<String, Map<String, String>>>();

		Map<String, String> patientMap = new HashMap<>();
		patientMap.put("type", "identifier");
		patientMap.put("url", "urn:oid:2.16.840.1.113883.3.552.1.3.11.13.1.8.2");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Patient", patientMap));

		Map<String, String> conditionMap = new HashMap<>();
		conditionMap.put("type", "identifier");
		conditionMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Condition", conditionMap));

		Map<String, String> reportMap = new HashMap<>();
		reportMap.put("type", "identifier");
		reportMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("DiagnosticReport", reportMap));

		Map<String, String> allergyMap = new HashMap<>();
		allergyMap.put("type", "identifier");
		allergyMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("AllergyIntolerance", allergyMap));

		Map<String, String> medStatementMap = new HashMap<>();
		medStatementMap.put("type", "identifier");
		medStatementMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("MedicationStatement", medStatementMap));

		Map<String, String> medRequestMap = new HashMap<>();
		medRequestMap.put("type", "identifier");
		medRequestMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("MedicationRequest", medRequestMap));

		Map<String, String> procMap = new HashMap<>();
		procMap.put("type", "identifier");
		procMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.1.1988.1");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Procedure", procMap));

		Map<String, String> immunizationMap = new HashMap<>();
		immunizationMap.put("type", "identifier");
		immunizationMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Immunization", immunizationMap));

		Map<String, String> resultsMap = new HashMap<>();
		resultsMap.put("type", "identifier");
		resultsMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.6.798268.2000");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Observation", resultsMap));

		Map<String, String> vitalSignsMap = new HashMap<>();
		vitalSignsMap.put("type", "identifier");
		vitalSignsMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.1.2109.1");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Observation", vitalSignsMap));

		Map<String, String> encounterMap = new HashMap<>();
		encounterMap.put("type", "identifier");
		encounterMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.3.698084.8");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Encounter", encounterMap));

		Map<String, String> pracMap = new HashMap<>();
		pracMap.put("type", "identifier");
		pracMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.697780");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Practitioner", pracMap));

		Map<String, String> orgMap = new HashMap<>();
		orgMap.put("type", "identifier");
		orgMap.put("url", "urn:oid:1.2.840.114350.1.13.88.3.7.2.696570");
		urlStringMap.add(new AbstractMap.SimpleEntry<>("Organization", orgMap));

		return urlStringMap;

	}

	/**
	 * Takes a bundle entry and generates the ifNotExists String.
	 *
	 * @param bundleEntry
	 * @return String for application on request.
	 */
	public static String generateIfNoneExist(BundleEntryComponent bundleEntry) {

		ArrayList<SimpleEntry<String, Map<String, String>>> urlStringMap = createMap();

		String ifNotExistString = null;

		// Loop the map to find your values
		for (SimpleEntry<String, Map<String, String>> resource : urlStringMap) {

			System.out.println(bundleEntry.getResource().getResourceType().name());

			// If you match, pull the map type.
			if (bundleEntry.getResource().getResourceType().name() == resource.getKey()) {

				Map<String, String> entryMap = resource.getValue();
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
									break;
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
