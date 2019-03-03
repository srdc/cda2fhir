package tr.com.srdc.cda2fhir.transform.entry;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;

public interface IEntityInfo {
	Practitioner getPractitioner();
	
	PractitionerRole getPractitionerRole();
	
	Organization getOrganization();
}
