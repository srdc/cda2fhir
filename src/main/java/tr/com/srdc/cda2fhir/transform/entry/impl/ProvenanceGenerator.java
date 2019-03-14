package tr.com.srdc.cda2fhir.transform.entry.impl;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Organization.OrganizationContactComponent;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.dstu3.model.codesystems.ContactentityType;
import org.hl7.fhir.dstu3.model.codesystems.OrganizationType;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentType;

public class ProvenanceGenerator {
	/**
	 * Creates a static Organization constant built off of Amida's info.
	 */
	public static Organization generateAmidaOrganization() {
		Organization org = new Organization();
		org.setName("Amida Technology Solutions");
		org.setActive(true);

		Coding typeCoding = new Coding(OrganizationType.BUS.getSystem(), OrganizationType.BUS.toCode(),
				OrganizationType.BUS.getDisplay());
		org.addType(new CodeableConcept().addCoding(typeCoding));

		OrganizationContactComponent occ = new OrganizationContactComponent();
		Coding purposeCoding = new Coding(ContactentityType.ADMIN.getSystem(), ContactentityType.ADMIN.toCode(),
				ContactentityType.ADMIN.getDisplay());
		occ.setPurpose(new CodeableConcept().addCoding(purposeCoding));

		Address address = new Address();
		address.addLine("1640 Rhode Island Avenue Suite 650");
		address.setCity("Washington");
		address.setState("District of Columbia");
		address.setPostalCode("20036");
		occ.setAddress(address);

		ContactPoint phoneContact = new ContactPoint();
		phoneContact.setSystem(ContactPointSystem.PHONE);
		phoneContact.setValue("(202) 735-1790");
		occ.addTelecom(phoneContact);

		ContactPoint emailContact = new ContactPoint();
		emailContact.setSystem(ContactPointSystem.EMAIL);
		emailContact.setValue("hello@amida.com");
		occ.addTelecom(emailContact);

		org.addContact(occ);
		return org;
	}

	/**
	 * Creates a static Provenance constant built off of Amida's info.
	 * 
	 */
	public Provenance generateAmidaProvenance() {
		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		Organization amidaOrganization = generateAmidaOrganization();
		pac.setId(amidaOrganization.getId());

		Coding agentTypeCoding = new Coding(ProvenanceAgentType.ORGANIZATION.getSystem(),
				ProvenanceAgentType.ORGANIZATION.toCode(), ProvenanceAgentType.ORGANIZATION.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentTypeCoding));

		Coding agentRoleCoding = new Coding(ProvenanceAgentRole.ASSEMBLER.getSystem(),
				ProvenanceAgentRole.ASSEMBLER.toCode(), ProvenanceAgentRole.ASSEMBLER.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentRoleCoding));

		Provenance provenance = new Provenance().addAgent(pac);
		provenance.addContained(amidaOrganization);
		return provenance;
	}
}