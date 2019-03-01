package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.MedicationDispense;

public class MedicationDispenseGenerator {
	private PerformerGenerator performerGenerator;

	MedicationDispenseGenerator() {
		performerGenerator = new PerformerGenerator();		
	}

	MedicationDispenseGenerator(PerformerGenerator performerGenerator) {
		this.performerGenerator = performerGenerator;
	}

	public MedicationDispense generate(CDAFactories factories) {
		MedicationDispense md = factories.consol.createMedicationDispense();
		Performer2 performer = performerGenerator.generate(factories);
		md.getPerformers().add(performer);
		return md;
	}
	
	public static MedicationDispenseGenerator getDefaultInstance() {
		PerformerGenerator pg = PerformerGenerator.getDefaultInstance();
		return new MedicationDispenseGenerator(pg);
	}

	public void verify(Practitioner practitioner) {
		performerGenerator.verify(practitioner);
    }
	
	public void verify(PractitionerRole role) {
		performerGenerator.verify(role);
	}
	
	public void verify(org.hl7.fhir.dstu3.model.Organization org) {
		performerGenerator.verify(org);
	}
}
