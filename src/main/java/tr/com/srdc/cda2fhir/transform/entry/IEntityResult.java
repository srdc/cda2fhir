package tr.com.srdc.cda2fhir.transform.entry;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;

public interface IEntityResult {
	void copyTo(Bundle bundle);

	boolean isEmpty();
	
	Bundle getBundle();

	boolean hasPractitioner();

	boolean hasOrganization();

	boolean hasPractitionerRole();

	boolean hasPractitionerRoleCode();

	Practitioner getPractitioner();
	
	PractitionerRole getPractitionerRole();
	
	Organization getOrganization();
	
	Reference getPractitionerReference();

	Reference getOrganizationReference();

	Reference getPractitionerRoleReference();

	CodeableConcept getPractitionerRoleCode();

	String getPractitionerId();
}
