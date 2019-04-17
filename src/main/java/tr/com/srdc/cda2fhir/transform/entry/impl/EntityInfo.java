package tr.com.srdc.cda2fhir.transform.entry.impl;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;

import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;

public class EntityInfo implements IEntityInfo {
	private Practitioner practitioner;
	private PractitionerRole role;
	private Organization organization;
	private boolean orgIsNew = false;

	public void setPractitioner(Practitioner practitioner) {
		this.practitioner = practitioner;
	}

	public void setPractitionerRole(PractitionerRole practitionerRole) {
		this.role = practitionerRole;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Override
	public Practitioner getPractitioner() {
		return practitioner;
	}

	@Override
	public PractitionerRole getPractitionerRole() {
		return role;
	}

	@Override
	public Organization getOrganization() {
		return organization;
	}

	public boolean isOrgNew() {
		return orgIsNew;
	}

	public void setOrgIsNew(boolean orgIsNew) {
		this.orgIsNew = orgIsNew;
	}
}
