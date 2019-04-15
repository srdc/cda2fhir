package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ReactionObservationGenerator extends ObservationGenerator {
	@Override
	public ReactionObservation createForGenerate(CDAFactories factories) {
		return factories.consol.createReactionObservation();
	}

	public static ReactionObservationGenerator getDefaultInstance() {
		ReactionObservationGenerator rog = new ReactionObservationGenerator();
		ObservationGenerator.fillDefaultInstance(rog);
		return rog;
	}
}
