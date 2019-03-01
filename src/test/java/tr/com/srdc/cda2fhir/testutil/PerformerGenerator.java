package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.impl.Performer2Impl;

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

	public void verify(Practitioner practitioner) {
		assignedEntityGenerator.verify(practitioner);
    }
	
	public void verify(PractitionerRole role) {
		assignedEntityGenerator.verify(role);
	}
	
	public void verify(org.hl7.fhir.dstu3.model.Organization org) {
		assignedEntityGenerator.verify(org);
	}
}
