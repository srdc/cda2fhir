package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergyObservationGenerator {
	private static final Map<String, Object> ALLERGY_INTOLERANCE_TYPE = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceType.json");
	private static final Map<String, Object> ALLERGY_INTOLERANCE_CATEGORY = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceCategory.json");
	private static final Map<String, Object> ALLERGY_INTOLERANCE_CRITICALITY = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceCriticality.json");
	private static final Map<String, Object> ALLERGY_INTOLERANCE_CLINICAL_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceClinicalStatus.json");

	private List<AuthorGenerator> authorGenerators = new ArrayList<>();
	private List<PlayingEntityGenerator> codeGenerators = new ArrayList<>();

	private CECodeGenerator typeGenerator;
	private CECodeGenerator categoryGenerator;

	private List<AllergyReactionObservationGenerator> reactionGenerators = new ArrayList<>();

	private EffectiveTimeGenerator effectiveTimeGenerator;

	private CECodeGenerator criticalityGenerator;

	private CECodeGenerator clinicalStatusGenerator;

	public List<AllergyReactionObservationGenerator> getReactionGenerators() {
		return Collections.unmodifiableList(reactionGenerators);
	}

	public void setClinicalStatusCode(String code) {
		if (clinicalStatusGenerator == null) {
			clinicalStatusGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_CLINICAL_STATUS);
		}
		clinicalStatusGenerator.set(code);
	}

	public boolean hasCategoryGenerator() {
		return categoryGenerator != null;
	}

	public void setAuthorGenerator(AuthorGenerator authorGenerator) {
		authorGenerators.clear();
		authorGenerators.add(authorGenerator);
	}

	public AllergyObservation generate(CDAFactories factories) {
		AllergyObservation ao = factories.consol.createAllergyObservation();

		authorGenerators.forEach(g -> ao.getAuthors().add(g.generate(factories)));

		codeGenerators.forEach(g -> {
			Participant2 p2 = factories.base.createParticipant2();
			ParticipationType pt = ParticipationType.CSM;
			p2.setTypeCode(pt);
			ParticipantRole pr = factories.base.createParticipantRole();
			p2.setParticipantRole(pr);
			RoleClassRoot rcr = RoleClassRoot.MANU;
			pr.setClassCode(rcr);
			PlayingEntity pe = g.generate(factories);
			pr.setPlayingEntity(pe);
			ao.getParticipants().add(p2);
		});

		if (typeGenerator != null && categoryGenerator != null) {
			if (typeGenerator.get() != categoryGenerator.get()) {
				throw new TestSetupException("Category and type generators must generate the same.");
			}
			CE ce = typeGenerator.generate(factories);
			ao.getValues().add(ce);
		} else if (typeGenerator != categoryGenerator) {
			throw new TestSetupException("Category and type generators must generate the same.");
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			ao.setEffectiveTime(ivlTs);
		}

		if (clinicalStatusGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			ao.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			er.setInversionInd(true);
			Observation o = factories.consol.createAllergyStatusObservation();
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.28");
			o.getTemplateIds().add(templateId);
			CE ce = clinicalStatusGenerator.generate(factories);
			o.getValues().add(ce);
			er.setObservation(o);
		}

		reactionGenerators.forEach(g -> {
			EntryRelationship er = factories.base.createEntryRelationship();
			ao.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.MFST);
			er.setInversionInd(true);
			ReactionObservation ro = g.generate(factories);
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.9");
			ro.getTemplateIds().add(templateId);
			er.setObservation(ro);
		});

		if (criticalityGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			ao.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			er.setInversionInd(true);
			Observation o = factories.base.createObservation();
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.145");
			o.getTemplateIds().add(templateId);
			CE ce = criticalityGenerator.generate(factories);
			o.getValues().add(ce);
			er.setObservation(o);
		}

		return ao;
	}

	public static AllergyObservationGenerator getDefaultInstance() {
		AllergyObservationGenerator aog = new AllergyObservationGenerator();

		aog.authorGenerators.add(AuthorGenerator.getDefaultInstance());
		aog.codeGenerators.add(PlayingEntityGenerator.getDefaultInstance());

		aog.typeGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_TYPE);
		aog.typeGenerator.set("419511003");
		aog.categoryGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_CATEGORY);
		aog.categoryGenerator.set("419511003");
		aog.effectiveTimeGenerator = new EffectiveTimeGenerator("20161008", "20181128");
		aog.reactionGenerators.add(AllergyReactionObservationGenerator.getDefaultInstance());
		aog.criticalityGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_CRITICALITY);
		aog.criticalityGenerator.set("crith");
		aog.clinicalStatusGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_CLINICAL_STATUS);
		aog.clinicalStatusGenerator.set("55561003");

		return aog;
	}

	public void verify(AllergyIntolerance allergyIntolerance) {
		if (codeGenerators.isEmpty()) {
			Assert.assertTrue("No allergy code", !allergyIntolerance.hasCode());
		} else {
			PlayingEntityGenerator peg = codeGenerators.get(codeGenerators.size() - 1);
			peg.verify(allergyIntolerance.getCode());
		}

		if (typeGenerator == null) {
			Assert.assertTrue("No type", !allergyIntolerance.hasType());
		} else {
			typeGenerator.verify(allergyIntolerance.getType().toCode());
		}

		Assert.assertNotNull("Clinical status exists", allergyIntolerance.hasClinicalStatus());
		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No allergy onset", !allergyIntolerance.hasOnset());
		} else {
			String value = effectiveTimeGenerator.getLowOrValue();
			if (value == null) {
				Assert.assertTrue("No allergy onset", !allergyIntolerance.hasOnset());
			} else {
				String actual = FHIRUtil.toCDADatetime(allergyIntolerance.getOnsetDateTimeType().asStringValue());
				Assert.assertEquals("Allergy offset", value, actual);
			}
		}

		if (clinicalStatusGenerator == null) {
			Assert.assertTrue("Clinical status exists", allergyIntolerance.hasClinicalStatus());
			Assert.assertEquals("Default clinical status", "active", allergyIntolerance.getClinicalStatus().toCode());
		} else {
			Assert.assertTrue("Clinical status exists", allergyIntolerance.hasClinicalStatus());
			clinicalStatusGenerator.verify(allergyIntolerance.getClinicalStatus().toCode());
		}

		if (reactionGenerators.isEmpty()) {
			Assert.assertTrue("No reaction", !allergyIntolerance.hasReaction());
		}

		if (criticalityGenerator == null) {
			Assert.assertTrue("No criticality", !allergyIntolerance.hasCriticality());
		} else {
			Assert.assertTrue("Has criticality", allergyIntolerance.hasCriticality());
			criticalityGenerator.verify(allergyIntolerance.getCriticality().toCode());
		}
	}

	public void verifyCategory(String category) {
		if (categoryGenerator == null) {
			Assert.assertNull("No category", category);
		} else {
			categoryGenerator.verify(category);
		}
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

	public static Set<String> getPossibleClinicalStatusCodes() {
		return Collections.unmodifiableSet(ALLERGY_INTOLERANCE_CLINICAL_STATUS.keySet());
	}
}
