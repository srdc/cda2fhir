package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.impl.Performer2Impl;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PerformerGenerator {
	private AssignedEntityGenerator assignedEntityGenerator;

	public PerformerGenerator() {
		assignedEntityGenerator = new AssignedEntityGenerator();
	}

	public PerformerGenerator(AssignedEntityGenerator assignedEntityGenerator) {
		this.assignedEntityGenerator = assignedEntityGenerator;
	}

	public Performer2 generate(CDAFactories factories) {
		Performer2Impl performer = (Performer2Impl) factories.base.createPerformer2();
		AssignedEntity entity = assignedEntityGenerator.generate(factories);
		performer.setAssignedEntity(entity);
		return performer;
	}

	public AssignedEntityGenerator getAssignedEntityGenerator() {
		return assignedEntityGenerator;
	}

	public static PerformerGenerator getDefaultInstance() {
		AssignedEntityGenerator aeg = AssignedEntityGenerator.getDefaultInstance();
		return new PerformerGenerator(aeg);
	}

	public static PerformerGenerator getFullInstance() {
		AssignedEntityGenerator aeg = AssignedEntityGenerator.getFullInstance();
		return new PerformerGenerator(aeg);
	}

	public String getCodeCode() {
		return assignedEntityGenerator.getCodeCode();
	}

	public void verify(Practitioner practitioner) {
		assignedEntityGenerator.verify(practitioner);
	}

	public void verify(PractitionerRole role) {
		assignedEntityGenerator.verify(role);
	}

	public void verify(org.hl7.fhir.dstu3.model.Organization org) {
		assignedEntityGenerator.verify(org);
	}

	public void verifyFromPractionerId(Bundle bundle, String practitionerId) {
		assignedEntityGenerator.verifyFromPractionerId(bundle, practitionerId);
	}

	public void setAssignedEntityGenerator(AssignedEntityGenerator assignedEntityGenerator) {
		this.assignedEntityGenerator = assignedEntityGenerator;
	}
}
