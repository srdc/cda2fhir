package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergyConcernActGenerator {
	private static final Map<String, Object> VERIFICATION_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceVerificationStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();
	private CSCodeGenerator statusCodeGenerator;
	private EffectiveTimeGenerator effectiveTimeGenerator;
	private List<AllergyObservationGenerator> observationGenerators = new ArrayList<>();

	private String nullFlavor;

	public void setNullFlavor() {
		this.nullFlavor = "UNK";
	}

	public void setAuthorGenerator(AuthorGenerator authorGenerator) {
		if (observationGenerators.isEmpty()) {
			observationGenerators.add(new AllergyObservationGenerator());
		}
		observationGenerators.get(observationGenerators.size() - 1).setAuthorGenerator(authorGenerator);
	}

	public AllergyProblemAct generate(CDAFactories factories) {
		AllergyProblemAct apa = factories.consol.createAllergyProblemAct();

		idGenerators.forEach(g -> {
			II ii = g.generate(factories);
			apa.getIds().add(ii);
		});

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			apa.setStatusCode(cs);
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			apa.setEffectiveTime(ivlTs);
		}

		observationGenerators.forEach(g -> apa.addObservation(g.generate(factories)));

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			apa.setNullFlavor(nf);
		}

		return apa;
	}

	public void setObservationGenerator(AllergyObservationGenerator observationGenerator) {
		observationGenerators.clear();
		observationGenerators.add(observationGenerator);
	}

	public static AllergyConcernActGenerator getDefaultInstance() {
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();

		acag.idGenerators.add(IDGenerator.getNextInstance());
		acag.statusCodeGenerator = new CSCodeGenerator(VERIFICATION_STATUS);
		acag.statusCodeGenerator.set("active");
		acag.effectiveTimeGenerator = new EffectiveTimeGenerator("20171108", "20181223");
		acag.observationGenerators.add(AllergyObservationGenerator.getDefaultInstance());

		return acag;
	}

	public void verify(AllergyIntolerance allergy) {
		if (nullFlavor != null) {
			Assert.assertNull("Null flavor", allergy);
			return;
		}

		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No patient identifier", !allergy.hasIdentifier());
		} else {
			IDGenerator.verifyList(allergy.getIdentifier(), idGenerators);
		}

		if (statusCodeGenerator == null) {
			Assert.assertEquals("Default verification status", "unconfirmed", allergy.getVerificationStatus().toCode());
		} else {
			statusCodeGenerator.verify(allergy.getVerificationStatus().toCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No asserted date", !allergy.hasAssertedDate());
		} else {
			String value = effectiveTimeGenerator.getLowOrValue();
			if (value == null) {
				Assert.assertTrue("No asserted date", !allergy.hasAssertedDate());
			} else {
				String actual = FHIRUtil.toCDADatetime(allergy.getAssertedDateElement().asStringValue());
				Assert.assertEquals("Allergy asserted date", value, actual);
			}
		}

		if (observationGenerators.isEmpty()) {
			Assert.assertTrue("No allergy code", !allergy.hasCode());
			Assert.assertTrue("No type", !allergy.hasType());
			Assert.assertTrue("No onset", !allergy.hasOnset());
			Assert.assertEquals("Default clinical status", "active", allergy.getClinicalStatus().toCode());
			Assert.assertTrue("No reaction", !allergy.hasReaction());
			Assert.assertTrue("No category", !allergy.hasCategory());
			return;
		}

		AllergyObservationGenerator aog = observationGenerators.get(observationGenerators.size() - 1);
		aog.verify(allergy);

		int actualIndex = 0;
		for (int index = 0; index < observationGenerators.size(); ++index) {
			AllergyObservationGenerator g = observationGenerators.get(index);
			if (g.hasCategoryGenerator()) {
				g.verifyCategory(allergy.getCategory().get(actualIndex).asStringValue());
				++actualIndex;
			}
		}
		if (actualIndex == 0) {
			Assert.assertTrue("No categories exist", !allergy.hasCategory());
		} else {
			Assert.assertEquals("Category count", actualIndex, allergy.getCategory().size());
		}

		{
			List<AllergyReactionObservationGenerator> gs = new ArrayList<>();
			observationGenerators.forEach(og -> {
				gs.addAll(og.getReactionGenerators());
			});

			AllergyReactionObservationGenerator.verifyList(allergy.getReaction(), gs);
		}
	}

	public void verify(Bundle bundle, AllergyIntolerance allergy) throws Exception {
		if (nullFlavor != null) {
			Assert.assertNull("Null flavor", allergy);
			return;
		}

		verify(allergy);

		if (observationGenerators.isEmpty()) {
			Assert.assertTrue("No recorder", !allergy.hasRecorder());
		} else {
			AllergyObservationGenerator aog = observationGenerators.get(observationGenerators.size() - 1);
			aog.verify(bundle);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		if (nullFlavor != null) {
			AllergyIntolerance allergy = FHIRUtil.findFirstResource(bundle, AllergyIntolerance.class);
			verify(bundle, allergy);
			return;
		}

		AllergyIntolerance allergy = BundleUtil.findOneResource(bundle, AllergyIntolerance.class);
		verify(bundle, allergy);
	}

	public static Set<String> getPossibleClinicalStatusCodes() {
		return AllergyObservationGenerator.getPossibleClinicalStatusCodes();
	}
}
