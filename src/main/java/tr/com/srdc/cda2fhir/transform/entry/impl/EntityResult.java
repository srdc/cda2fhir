package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.entry.CDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;

public class EntityResult implements IEntityResult {
	private IEntityInfo info;
	private List<II> ids;
	private CDAIIResourceMaps<IBaseResource> resourceMaps;
	private boolean fromExisting;

	public EntityResult() {
		info = new EntityInfo();
		fromExisting = true;
	}

	public EntityResult(IEntityInfo info) {
		this.info = info;
		fromExisting = true;
	}

	public EntityResult(IEntityInfo info, List<II> ids) {
		this.info = info;
		this.ids = ids;
		fromExisting = false;
	}

	@Override
	public boolean isFromExisting() {
		return fromExisting;
	}

	@Override
	public List<II> getNewIds() {
		return ids;
	}

	@Override
	public IEntityInfo getInfo() {
		return info;
	}

	@Override
	public void copyTo(Bundle bundle) {
		Practitioner practitioner = info.getPractitioner();
		if (practitioner != null) {
			bundle.addEntry().setResource(practitioner);
		}
		PractitionerRole role = info.getPractitionerRole();
		if (role != null) {
			bundle.addEntry().setResource(role);
		}

		Organization organization = info.getOrganization();
		if (organization != null && info.isOrgNew()) {
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
		return info.getPractitioner() == null && info.getPractitionerRole() == null && info.getOrganization() == null;
	}

	@Override
	public boolean hasPractitioner() {
		return info.getPractitioner() != null;
	}

	@Override
	public boolean hasOrganization() {
		return info.getOrganization() != null;
	}

	@Override
	public boolean hasPractitionerRole() {
		return info.getPractitionerRole() != null;
	}

	@Override
	public boolean hasPractitionerRoleCode() {
		PractitionerRole role = info.getPractitionerRole();
		return role != null && role.hasCode();
	}

	@Override
	public Practitioner getPractitioner() {
		return info.getPractitioner();
	}

	@Override
	public PractitionerRole getPractitionerRole() {
		return info.getPractitionerRole();
	}

	@Override
	public Organization getOrganization() {
		return info.getOrganization();
	}

	@Override
	public Reference getPractitionerReference() {
		Practitioner practitioner = info.getPractitioner();
		if (practitioner != null) {
			return new Reference(practitioner.getId());
		}
		return null;
	}

	@Override
	public Reference getOrganizationReference() {
		Organization organization = info.getOrganization();
		if (organization != null) {
			return new Reference(organization.getId());
		}
		return null;
	}

	@Override
	public Reference getPractitionerRoleReference() {
		PractitionerRole role = info.getPractitionerRole();
		if (role != null) {
			return new Reference(role.getId());
		}
		return null;
	}

	@Override
	public CodeableConcept getPractitionerRoleCode() {
		PractitionerRole role = info.getPractitionerRole();
		if (role != null && role.hasCode()) {
			return role.getCode().get(0);
		}
		return null;
	}

	@Override
	public String getPractitionerId() {
		Practitioner practitioner = info.getPractitioner();
		if (practitioner != null) {
			return practitioner.getId();
		}
		return null;
	}

	@Override
	public boolean hasIIResourceMaps() {
		return resourceMaps != null;
	}

	@Override
	public CDAIIResourceMaps<IBaseResource> getResourceMaps() {
		return resourceMaps;
	}

	@Override
	public void put(List<II> iis, Class<? extends IBaseResource> clazz, IBaseResource resource) {
		if (iis != null && clazz != null && resource != null) {
			this.resourceMaps.put(iis, clazz, resource);
		}
	}

	public void updateFrom(IEntryResult result) {
		if (resourceMaps == null) {
			resourceMaps = new CDAIIResourceMaps<IBaseResource>();
		}
		if (result.hasIIResourceMaps()) {
			resourceMaps.put(result.getResourceMaps());
		}
	}

	@Override
	public boolean isOrgNew() {
		return info.isOrgNew();
	}

	@Override
	public void setOrgIsNew(boolean orgIsNew) {
		info.setOrgIsNew(orgIsNew);
	}

}
