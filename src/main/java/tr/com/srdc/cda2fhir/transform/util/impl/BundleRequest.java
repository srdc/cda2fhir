package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.List;
import java.util.ResourceBundle;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleRequest {

	private final static Logger logger = LoggerFactory.getLogger(BundleRequest.class);

	/**
	 * Reads the configuration file, and parses out any overriding OIDs.
	 *
	 * @param resourceType - the resource name of the OID
	 * @return Array of OID strings for use.
	 */
	private static String[] getDefinedOIDs(String resourceType) {

		String[] splitOIDs = null;
		try {
			final ResourceBundle rb = ResourceBundle.getBundle("config");
			String newOIDs = rb.getString(resourceType);
			splitOIDs = newOIDs.split(",");
		} catch (Exception e) {
			// do not log exception.
		}
		return splitOIDs;
	}

	/**
	 * Takes a bundle entry and generates the ifNotExists String.
	 *
	 * @param bundleEntry
	 * @return String for application on request.
	 */
	public static String generateIfNoneExist(BundleEntryComponent bundleEntry) {

		String ifNotExistString = "";

		// all ifNoneExist strings attempt to start with an identifier.
		Property identifierObject = bundleEntry.getResource().getNamedProperty("identifier");

		// find any overriding OIDs.
		String[] subsetOIDArray = getDefinedOIDs(bundleEntry.getResource().getResourceType().name());

		if (identifierObject != null) {
			List<Base> identifiers = identifierObject.getValues();
			if (identifiers != null) {
				for (Base identifier : identifiers) {

					Identifier currentId = (Identifier) identifier;
					if (currentId.getSystem() != null & currentId.getValue() != null) {

						if (subsetOIDArray != null) {

							// override OID selection(s).
							for (String OID : subsetOIDArray) {
								System.out.println(currentId.getSystem());
								if (currentId.getSystem().equals(OID)) {
									if (ifNotExistString != "") {
										ifNotExistString = ifNotExistString + ",";
									} else {
										ifNotExistString = "identifier=";
									}
									ifNotExistString = ifNotExistString + currentId.getSystem() + "|"
											+ currentId.getValue();
								}
							}
						} else {
							// add or for multiple parameters
							if (ifNotExistString != "") {
								ifNotExistString = ifNotExistString + ",";
							} else {
								ifNotExistString = "identifier=";
							}
							ifNotExistString = ifNotExistString + currentId.getSystem() + "|" + currentId.getValue();
						}
					}
				}
			}
		}

		// if we can't pull an identifier, try other logic.
		if (ifNotExistString == "") {
			// if it's a medication, check by RxNorm.
			if (bundleEntry.getResource().getResourceType().name() == "Medication") {
				Property medicationCode = bundleEntry.getResource().getChildByName("code");
				if (medicationCode != null) {
					List<Base> conceptList = medicationCode.getValues();
					for (Base concept : conceptList) {
						CodeableConcept currentConcept = (CodeableConcept) concept;
						List<Coding> currentCoding = currentConcept.getCoding();
						for (Coding coding : currentCoding) {
							if (coding.getSystem() == "http://www.nlm.nih.gov/research/umls/rxnorm"
									& coding.getCode() != null) {
								// add or for multiple parameters
								if (ifNotExistString != "") {
									ifNotExistString = ifNotExistString + ",";
								} else {
									ifNotExistString = "code=" + coding.getSystem() + "|";
								}
								ifNotExistString = ifNotExistString + coding.getCode();
							}
						}
					}
				}
			}

			// if it's a practitioner role, de-duplicate by reference ids.
			if (bundleEntry.getResource().getResourceType().name() == "PractitionerRole") {
				PractitionerRole practitionerRole = (PractitionerRole) bundleEntry.getResource();
				Identifier practitionerIdentifier = practitionerRole.getPractitionerTarget().getIdentifierFirstRep();
				Identifier organizationIdentifier = practitionerRole.getOrganizationTarget().getIdentifierFirstRep();
				if (organizationIdentifier != null & practitionerIdentifier != null) {
					ifNotExistString = "practitioner.identifier=" + practitionerIdentifier.getSystem() + "|"
							+ practitionerIdentifier.getValue();
					ifNotExistString = ifNotExistString + "&" + "organization.identifier="
							+ organizationIdentifier.getSystem() + "|" + organizationIdentifier.getValue();
				}

			}

			// next after med here.

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

		String ifNoneExistString = generateIfNoneExist(entry).replace(" ", "+");
		if (ifNoneExistString != null) {
			request.setIfNoneExist(ifNoneExistString);
		}

		request.setMethod(HTTPVerb.POST);
		// request.setUrl(entry.getResource().getResourceName());
		request.setUrl(entry.getResource().getResourceType().name());
		entry.setRequest(request);
	}

}
