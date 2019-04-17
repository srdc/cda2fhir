package tr.com.srdc.cda2fhir.transform.entry;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

public interface IEntityResult extends IEntityInfo {
	boolean isFromExisting();

	List<II> getNewIds();

	IEntityInfo getInfo();

	void copyTo(Bundle bundle);

	boolean isEmpty();

	Bundle getBundle();

	boolean hasPractitioner();

	boolean hasOrganization();

	boolean hasPractitionerRole();

	boolean hasPractitionerRoleCode();

	Reference getPractitionerReference();

	Reference getOrganizationReference();

	Reference getPractitionerRoleReference();

	CodeableConcept getPractitionerRoleCode();

	String getPractitionerId();

	boolean hasIIResourceMaps();

	CDAIIResourceMaps<IBaseResource> getResourceMaps();

	void put(List<II> iis, Class<? extends IBaseResource> clazz, IBaseResource resource);
}
