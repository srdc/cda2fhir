package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.Iterator;
import java.util.List;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;

public class ReferenceInfo {

	/**
	 * Returns a string representation used for display assignment for references.
	 *
	 * @param resource to be referenced which will be searched for a display value.
	 * @return a string representation of the resource.
	 */
	public static String getDisplay(Resource resource) {

		System.out.println(resource.toString());

		if (resource.getNamedProperty("code") != null) {
			if (!resource.getNamedProperty("code").getValues().isEmpty()) {

				// TODO: Should really be checking all the encodings for one.
				CodeableConcept code = (CodeableConcept) resource.getNamedProperty("code").getValues().get(0);
				if (code != null) {
					if (code.hasText()) {
						return code.getText();
					} else if (resource instanceof PractitionerRole || resource instanceof Procedure) {
						if (code.getCodingFirstRep() != null && code.getCodingFirstRep().getDisplay() != null) {
							return code.getCodingFirstRep().getDisplay();
						}
					}
				}
			}

		} else if (resource.getNamedProperty("vaccineCode") != null
				&& !resource.getNamedProperty("vaccineCode").getValues().isEmpty()) {
			CodeableConcept vaccineCode = (CodeableConcept) resource.getNamedProperty("vaccineCode").getValues().get(0);

			if (vaccineCode != null && vaccineCode.hasText()) {
				return vaccineCode.getText();
			}

		} else if (resource.getNamedProperty("name") != null
				&& !resource.getNamedProperty("name").getValues().isEmpty()) {
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
		return null;
	}

}
