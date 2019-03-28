package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class EncounterActivityGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public EncounterActivities generate(CDAFactories factories) {
		EncounterActivities ec = factories.consol.createEncounterActivities();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ec.getIds().add(ii);
		});

		return ec;
	}

	public static EncounterActivityGenerator getDefaultInstance() {
		EncounterActivityGenerator papg = new EncounterActivityGenerator();

		papg.idGenerators.add(IDGenerator.getNextInstance());

		return papg;
	}

	public void verify(Encounter encounter) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(encounter.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !encounter.hasIdentifier());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Encounter encounter = BundleUtil.findOneResource(bundle, Encounter.class);

		verify(encounter);
	}
}
