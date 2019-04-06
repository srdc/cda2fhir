package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AllergyConcernActGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public AllergyProblemAct generate(CDAFactories factories) {
		AllergyProblemAct apa = factories.consol.createAllergyProblemAct();

		idGenerators.forEach(g -> {
			II ii = g.generate(factories);
			apa.getIds().add(ii);
		});

		return apa;
	}

	public static AllergyConcernActGenerator getDefaultInstance() {
		AllergyConcernActGenerator prg = new AllergyConcernActGenerator();

		prg.idGenerators.add(IDGenerator.getNextInstance());

		return prg;
	}

	public void verify(AllergyIntolerance allergy) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No patient identifier", !allergy.hasIdentifier());
		} else {
			IDGenerator.verifyList(allergy.getIdentifier(), idGenerators);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		// BundleUtil util = new BundleUtil(bundle);
		AllergyIntolerance allergy = BundleUtil.findOneResource(bundle, AllergyIntolerance.class);

		verify(allergy);
	}
}
