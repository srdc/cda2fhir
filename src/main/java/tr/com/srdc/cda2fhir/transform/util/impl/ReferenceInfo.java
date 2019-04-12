package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.Iterator;
import java.util.List;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;

public class ReferenceInfo {

	/**
	 * Walks a codeable concept and returns a string representation.
	 *
	 * @param entry codeable concept to analyze.
	 * @return string of the codeable concept.
	 */
	private static String getStringFromConcept(CodeableConcept entry) {
		if (entry != null) {
			if (entry.hasText()) {
				return entry.getText();
			} else {
				// if no text loop the displays and take the first one.
				if (entry.getCoding() != null) {
					if (entry.getCoding().size() > 0) {
						for (Coding codeEntry : entry.getCoding()) {
							if (codeEntry.getDisplay() != null) {
								return codeEntry.getDisplay();
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns a string representation used for display assignment for references.
	 *
	 * @param resource to be referenced which will be searched for a display value.
	 * @return a string representation of the resource.
	 */
	public static String getDisplay(Resource resource) {

		// take coded object to get display value if possible.
		if (resource.getNamedProperty("code") != null) {
			if (!resource.getNamedProperty("code").getValues().isEmpty()) {
				CodeableConcept code = (CodeableConcept) resource.getNamedProperty("code").getValues().get(0);
				String outputString = getStringFromConcept(code);
				if (outputString != null) {
					return outputString;
				}
			}
		}

		// for vaccines, take vaccine code.
		if (resource.getNamedProperty("vaccineCode") != null) {
			if (!resource.getNamedProperty("vaccineCode").getValues().isEmpty()) {
				CodeableConcept vaccineCode = (CodeableConcept) resource.getNamedProperty("vaccineCode").getValues()
						.get(0);
				String outputString = getStringFromConcept(vaccineCode);
				if (outputString != null) {
					return outputString;
				}

			}
		}

		// for med statement/requests, get ref med.
		if (resource.getNamedProperty("medicationReference") != null) {
			if (!resource.getNamedProperty("medicationReference").getValues().isEmpty()) {
				Reference medRef = (Reference) resource.getNamedProperty("medicationReference").getValues().get(0);
				if (medRef != null) {
					Medication med = (Medication) medRef.getResource();
					String outputString = getStringFromConcept(med.getCode());
					if (outputString != null) {
						return outputString;
					}
				}
			}
		}

		// for encounter/device use type if present.
		if (resource.getNamedProperty("type") != null) {
			if (!resource.getNamedProperty("type").getValues().isEmpty()) {
				Base val = resource.getNamedProperty("type").getValues().get(0);
				if (!(val instanceof Enumeration)) {
					CodeableConcept typeCode = (CodeableConcept) resource.getNamedProperty("type").getValues().get(0);
					String outputString = getStringFromConcept(typeCode);
					if (outputString != null) {
						return outputString;
					}
				}
			}
		}

		// for all others attempt to take name.
		if (resource.getNamedProperty("name") != null) {
			if (!resource.getNamedProperty("name").getValues().isEmpty()) {

				Object nameObj = resource.getNamedProperty("name").getValues().get(0);
				if (nameObj instanceof StringType) {

					StringType str = (StringType) resource.getNamedProperty("name").getValues().get(0);
					if (str != null) {
						return str.asStringValue();
					}

				} else if (nameObj instanceof HumanName) {

					List<Base> nameList = resource.getNamedProperty("name").getValues();
					if (!nameList.isEmpty()) {
						String allNames = "";
						Iterator<Base> iter = nameList.listIterator();
						while (iter.hasNext()) {
							Base humanNameBase = iter.next();
							HumanName humanName = (HumanName) humanNameBase;
							if (!humanName.getNameAsSingleString().trim().contentEquals("")) {
								allNames += humanName.getNameAsSingleString();
								if (iter.hasNext())
									allNames += ", ";
							}
						}
						return allNames;
					}
				}

			}

		}
		return null;
	}

}
