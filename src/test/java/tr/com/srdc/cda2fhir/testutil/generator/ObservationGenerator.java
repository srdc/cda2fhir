package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ObservationGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public Observation generate(CDAFactories factories) {
		Observation obs = factories.base.createObservation();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			obs.getIds().add(ii);
		});

		return obs;
	}

	public static ObservationGenerator getDefaultInstance() {
		ObservationGenerator obs = new ObservationGenerator();

		obs.idGenerators.add(IDGenerator.getNextInstance());

		return obs;
	}

	public void verify(org.hl7.fhir.dstu3.model.Observation observation) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No observation identifier", !observation.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(observation.getIdentifier().get(index));
			}
		}

	}

	public void verify(Bundle bundle) throws Exception {
		org.hl7.fhir.dstu3.model.Observation obs = BundleUtil.findOneResource(bundle,
				org.hl7.fhir.dstu3.model.Observation.class);
		verify(obs);
	}
}
