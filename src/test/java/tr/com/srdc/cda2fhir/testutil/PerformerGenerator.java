package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.impl.Performer2Impl;

public class PerformerGenerator {
	private AssignedEntityGenerator assignedEntityGenerator = new AssignedEntityGenerator();

	@SuppressWarnings("unused")
	private PerformerGenerator() {}
	
	public PerformerGenerator(AssignedEntityGenerator assignedEntityGenerator) {
		this.assignedEntityGenerator = assignedEntityGenerator;
	}
	
	public Performer2 generate(CDAFactories factories) {
		Performer2Impl performer = (Performer2Impl) factories.base.createPerformer2();
		AssignedEntity entity = assignedEntityGenerator.generate(factories);
		performer.setAssignedEntity(entity);
		return performer;
	}
}
