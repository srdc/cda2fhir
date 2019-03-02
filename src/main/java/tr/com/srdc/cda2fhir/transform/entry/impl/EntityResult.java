package tr.com.srdc.cda2fhir.transform.entry.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;

import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;

public class EntityResult implements IEntityResult {
	private Practitioner practitioner;
	private PractitionerRole role;
	private Organization organization;

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
	public void copyTo(Bundle bundle) {
		if (practitioner != null) {
			bundle.addEntry().setResource(practitioner);
		}
		if (role != null) {
			bundle.addEntry().setResource(role);
		}
		if (organization != null) {
			bundle.addEntry().setResource(organization);
		}
	}

	@Override
	public Bundle getBundle() {
		if (!isEmpty()) {
			Bundle result = new Bundle();
			copyTo(result);
			return result;
		}
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return practitioner == null && role == null && organization == null;
	}

	@Override
	public boolean hasPractitioner() {
		return practitioner != null;
	}

	@Override
	public boolean hasOrganization() {
		return organization != null;
	}

	@Override
	public boolean hasPractitionerRole() {
		return role != null;
	}

	@Override
	public boolean hasPractitionerRoleCode() {
		return role != null && role.hasCode();
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

	@Override
	public Reference getPractitionerReference() {
		if (practitioner != null) {
			return new Reference(practitioner.getId());
		}
		return null;
	}

	@Override
	public Reference getOrganizationReference() {
		if (organization != null) {
			return new Reference(organization.getId());
		}
		return null;
	}

	@Override
	public Reference getPractitionerRoleReference() {
		if (role != null) {
			return new Reference(role.getId());
		}
		return null;
	}

	@Override
	public CodeableConcept getPractitionerRoleCode() {
		if (role != null && role.hasCode()) {
			return role.getCode().get(0);
		}
		return null;
	}

	@Override
	public String getPractitionerId() {
		if (practitioner != null) {
			return practitioner.getId();
		}
		return null;
	}
}
