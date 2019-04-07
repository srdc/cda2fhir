package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AllergyObservationGenerator {
	private List<AuthorGenerator> authorGenerators = new ArrayList<>();

	public AllergyObservation generate(CDAFactories factories) {
		AllergyObservation ao = factories.consol.createAllergyObservation();

		authorGenerators.forEach(g -> ao.getAuthors().add(g.generate(factories)));

		return ao;
	}

	public static AllergyObservationGenerator getDefaultInstance() {
		AllergyObservationGenerator aog = new AllergyObservationGenerator();

		aog.authorGenerators.add(AuthorGenerator.getDefaultInstance());

		return aog;
	}

	public void verify(AllergyIntolerance allergyIntolerance) {

	}

	public void verify(Bundle bundle) throws Exception {
		AllergyIntolerance allergyIntolerance = BundleUtil.findOneResource(bundle, AllergyIntolerance.class);
		verify(allergyIntolerance);

		if (authorGenerators.isEmpty()) {
			Assert.assertTrue("No recorder", !allergyIntolerance.hasRecorder());
		} else {
			AuthorGenerator ag = authorGenerators.get(authorGenerators.size() - 1);
			String practitionerId = allergyIntolerance.getRecorder().getReference();
			ag.verifyFromPractionerId(bundle, practitionerId);
		}
	}
}
