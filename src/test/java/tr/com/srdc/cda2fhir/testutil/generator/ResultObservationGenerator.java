package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ResultObservationGenerator extends ObservationGenerator {
	@Override
	public ResultObservation createForGenerate(CDAFactories factories) {
		return factories.consol.createResultObservation();
	}

	public static ResultObservationGenerator getDefaultInstance() {
		ResultObservationGenerator rog = new ResultObservationGenerator();
		ObservationGenerator.fillDefaultInstance(rog);
		return rog;
	}
}
