package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class VitalSignObservationGenerator extends ObservationGenerator {
	@Override
	public VitalSignObservation createForGenerate(CDAFactories factories) {
		return factories.consol.createVitalSignObservation();
	}

	public static VitalSignObservationGenerator getDefaultInstance() {
		VitalSignObservationGenerator rog = new VitalSignObservationGenerator();
		ObservationGenerator.fillDefaultInstance(rog);
		return rog;
	}
}
