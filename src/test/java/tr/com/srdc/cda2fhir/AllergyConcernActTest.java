package tr.com.srdc.cda2fhir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.impl.AllergyObservationImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.AllergyProblemActImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.AllergyStatusObservationImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ReactionObservationImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.EntryRelationshipImpl;
import org.openhealthtools.mdht.uml.cda.impl.Participant2Impl;
import org.openhealthtools.mdht.uml.cda.impl.PlayingEntityImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class AllergyConcernActTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

	private static Map<String, String> cdaProblemStatusCodeToName = new HashMap<String, String>();
	private static Map<String, String> cdaAllergyIntoleranceTypeCodeToName = new HashMap<String, String>();

	private static Map<String, Object> verificationStatusMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceVerificationStatus.json");
	private static Map<String, Object> categoryMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceCategory.json");
	private static Map<String, Object> typeMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceType.json");

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();

		cdaProblemStatusCodeToName.put("55561003", "Active");
		cdaProblemStatusCodeToName.put("73425007", "Inactive");
		cdaProblemStatusCodeToName.put("413322009", "Resolved");

		cdaAllergyIntoleranceTypeCodeToName.put("419199007", "Allergy to substance (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("416098002", "Drug allergy (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("59037007", "Drug intolerance (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("414285001", "Food allergy (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("235719002", "Food intolerance (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("420134006", "Propensity to adverse reactions (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("419511003", "Propensity to adverse reactions to drug (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("418471000", "Propensity to adverse reactions to food (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("418038007", "Propensity to adverse reactions to substance (disorder)");
		cdaAllergyIntoleranceTypeCodeToName.put("232347008", "Dander (animal) allergy");
	}

	static private AllergyIntolerance findOneResource(Bundle bundle) throws Exception {
		List<AllergyIntolerance> allergyResources = bundle.getEntry().stream().map(r -> r.getResource())
				.filter(r -> (r instanceof AllergyIntolerance)).map(r -> (AllergyIntolerance) r)
				.collect(Collectors.toList());
		Assert.assertEquals("Multiple AllergyIntolerance resources in the bundle", 1, allergyResources.size());
		return allergyResources.get(0);
	}

	static private void verifyAllergyIntoleranceCategory(AllergyProblemAct act, String expected) throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);

		Enumeration<AllergyIntolerance.AllergyIntoleranceCategory> category = allergyIntolerance.getCategory().get(0);
		String actual = category == null ? null : category.asStringValue();
		Assert.assertEquals("Unexpected AllergyIntolerance category", expected, actual);
	}

	static private void verifyAllergyIntoleranceType(AllergyProblemAct act, String expected) throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);

		AllergyIntolerance.AllergyIntoleranceType type = allergyIntolerance.getType();
		String actual = type == null ? null : type.toString();
		Assert.assertEquals("Unexpected AllergyIntolerance type", expected, actual);
	}

	static private AllergyStatusObservationImpl createAllergyStatusObservation(String cdaProblemStatusCode) {
		AllergyStatusObservationImpl allergyStatus = (AllergyStatusObservationImpl) cdaObjFactory
				.createAllergyStatusObservation();

		II templateId = cdaTypeFactory.createII("2.16.840.1.113883.10.20.22.4.28");
		allergyStatus.getTemplateIds().add(templateId);

		CD code = cdaTypeFactory.createCD("33999-4", "2.16.840.1.113883.6.1", null, null);
		allergyStatus.setCode(code);

		CS cs = cdaTypeFactory.createCS("completed");
		allergyStatus.setStatusCode(cs);

		return allergyStatus;
	}

	static private AllergyObservationImpl createAllergyObservation() {
		AllergyObservationImpl observation = (AllergyObservationImpl) cdaObjFactory.createAllergyObservation();

		II templateId = cdaTypeFactory.createII("2.16.840.1.113883.10.20.22.4.7", "2014-06-09");
		observation.getTemplateIds().add(templateId);

		EntryRelationshipImpl entryRelationship = (EntryRelationshipImpl) cdaFactory.createEntryRelationship();
		observation.getEntryRelationships().add(entryRelationship);
		AllergyStatusObservationImpl allergyStatus = createAllergyStatusObservation(null);
		entryRelationship.setObservation(allergyStatus);

		observation.getEntryRelationships().clear();
		observation.getEntryRelationships().add(entryRelationship);

		return observation;
	}

	static private AllergyProblemActImpl createAllergyConcernAct() {
		AllergyProblemActImpl act = (AllergyProblemActImpl) cdaObjFactory.createAllergyProblemAct();

		AllergyObservationImpl observation = createAllergyObservation();

		act.addObservation(observation);
		act.getEntryRelationships().stream().filter(r -> (r.getObservation() == observation))
				.forEach(r -> r.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ));

		return act;
	}

	@Test
	public void testAllergyIntoleranceObservationEffectiveTime() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();
		BundleInfo bundleInfo = new BundleInfo(rt);

		String expected1 = "20171002";

		IVL_TS ivlTs1 = cdaTypeFactory.createIVL_TS();
		IVXB_TS ivxb1 = cdaTypeFactory.createIVXB_TS();
		ivxb1.setValue(expected1);
		ivlTs1.setLow(ivxb1);
		observation.setEffectiveTime(ivlTs1);

		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
		Assert.assertTrue("Invalid Allergy Problem Act in Test", validation);

		Bundle bundle1 = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance1 = findOneResource(bundle1);
		String actual1 = allergyIntolerance1.getOnsetDateTimeType().getValueAsString();
		Assert.assertEquals("Unexpected AllergyIntolerance OnSet", expected1, actual1.replaceAll("-", ""));

		String expected2 = "20161103";
		IVL_TS ivlTs2 = cdaTypeFactory.createIVL_TS();
		ivlTs2.setValue(expected2);
		observation.setEffectiveTime(ivlTs2);

		Bundle bundle2 = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance2 = findOneResource(bundle2);
		String actual2 = allergyIntolerance2.getOnsetDateTimeType().getValueAsString();
		Assert.assertEquals("Unexpected AllergyIntolerance OnSet", expected2, actual2.replaceAll("-", ""));
	}

	@Test
	public void testSubstanceReactantForIntolerance() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();

		Participant2Impl participant = (Participant2Impl) cdaFactory.createParticipant2();
		participant.setTypeCode(ParticipationType.CSM);
		observation.getParticipants().add(participant);

		ParticipantRole role = cdaFactory.createParticipantRole();
		role.setClassCode(RoleClassRoot.MANU);
		participant.setParticipantRole(role);

		PlayingEntityImpl entity = (PlayingEntityImpl) cdaFactory.createPlayingEntity();
		entity.setClassCode(EntityClassRoot.MMAT);
		CE ce = cdaTypeFactory.createCE("2670", "2.16.840.1.113883.6.88", "RxNorm", "Codeine");
		entity.setCode(ce);
		role.setPlayingEntity(entity);

		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
		Assert.assertTrue("Invalid Allergy Problem Act in Test", validation);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		CodeableConcept cc = allergyIntolerance.getCode();
		Coding coding = cc.getCoding().get(0);

		Assert.assertEquals("Unexpected AllergyIntolerance code value", "2670", coding.getCode());
		Assert.assertEquals("Unexpected AllergyIntolerance code display value", "Codeine", coding.getDisplay());
	}

	@Test
	public void testAllergyAndIntoleranceCategory() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observationTop = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();
		verifyAllergyIntoleranceVerificationStatus(act, null);

		for (Map.Entry<String, Object> entry : categoryMap.entrySet()) {
			String cdaType = entry.getKey();
			String fhirCategory = (String) entry.getValue();
			String cdaTypeName = cdaAllergyIntoleranceTypeCodeToName.get(cdaType);

			CE ce = cdaTypeFactory.createCE(cdaType, "2.16.840.1.11388 3.6.96", "SNOMED CT", cdaTypeName);

			observationTop.getValues().clear();
			observationTop.getValues().add(ce);

			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
			Assert.assertTrue("Invalid Allergy Problem Act in Test", validation);

			verifyAllergyIntoleranceCategory(act, fhirCategory);
		}
	}

	@Test
	public void testAllergyAndIntoleranceType() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observationTop = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();
		verifyAllergyIntoleranceVerificationStatus(act, null);

		for (Map.Entry<String, Object> entry : typeMap.entrySet()) {
			String cdaType = entry.getKey();
			String fhirCategory = (String) entry.getValue();
			String cdaTypeName = cdaAllergyIntoleranceTypeCodeToName.get(cdaType);

			CE ce = cdaTypeFactory.createCE(cdaType, "2.16.840.1.11388 3.6.96", "SNOMED CT", cdaTypeName);

			observationTop.getValues().clear();
			observationTop.getValues().add(ce);

			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
			Assert.assertTrue("Invalid Allergy Problem Act in Test", validation);

			verifyAllergyIntoleranceType(act, fhirCategory);
		}
	}

	@Test
	public void testAllergyObservationStatusInactive() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();

		String low = "2018-01-01";
		String high = "2019-01-01";

		IVL_TS interval = cdaTypeFactory.createIVL_TS(low, high);

		observation.setEffectiveTime(interval);
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		AllergyIntoleranceClinicalStatus clinicalStatus = allergyIntolerance.getClinicalStatus();
		String actual = clinicalStatus.toCode();

		Assert.assertEquals("Inactive Allergy with high value", "inactive", actual);
	}

	@Test
	public void testAllergyObservationStatusActive() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();

		String low = "2018-01-01";

		IVL_TS interval = cdaTypeFactory.createIVL_TS(low, null);

		observation.setEffectiveTime(interval);
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		AllergyIntoleranceClinicalStatus clinicalStatus = allergyIntolerance.getClinicalStatus();
		String actual = clinicalStatus.toCode();

		Assert.assertEquals("Active Allergy with low value", "active", actual);
	}

	@Test
	public void testAllergyObservationStatusNoDate() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		// AllergyObservationImpl observation = (AllergyObservationImpl)
		// act.getEntryRelationships().get(0).getObservation();

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		AllergyIntoleranceClinicalStatus clinicalStatus = allergyIntolerance.getClinicalStatus();
		String actual = clinicalStatus.toCode();

		Assert.assertEquals("No effective time defaults to active", "active", actual);
	}

	static private IVL_TS createEffectiveTimeLow(String value) {
		IVL_TS ivlTs = cdaTypeFactory.createIVL_TS();
		IVXB_TS ivxb = cdaTypeFactory.createIVXB_TS();
		ivxb.setValue(value);
		ivlTs.setLow(ivxb);
		return ivlTs;
	}

	static private IVL_TS createEffectiveTimeValue(String value) {
		IVL_TS ivlTs = cdaTypeFactory.createIVL_TS();
		ivlTs.setValue(value);
		return ivlTs;
	}

	@Test
	public void testReactionObservationEffectiveTime() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0)
				.getObservation();

		String expected1 = "20171102";
		ReactionObservationImpl reactionObservation1 = (ReactionObservationImpl) cdaObjFactory
				.createReactionObservation();
		IVL_TS ivlTs1 = createEffectiveTimeLow(expected1);
		reactionObservation1.setEffectiveTime(ivlTs1);

		EntryRelationshipImpl entryRelationship1 = (EntryRelationshipImpl) cdaFactory.createEntryRelationship();
		entryRelationship1.setObservation(reactionObservation1);
		observation.getEntryRelationships().add(entryRelationship1);

		String expected2 = "20171202";
		ReactionObservationImpl reactionObservation2 = (ReactionObservationImpl) cdaObjFactory
				.createReactionObservation();
		IVL_TS ivlTs2 = createEffectiveTimeLow(expected2);
		reactionObservation2.setEffectiveTime(ivlTs2);

		EntryRelationshipImpl entryRelationship2 = (EntryRelationshipImpl) cdaFactory.createEntryRelationship();
		entryRelationship2.setObservation(reactionObservation2);
		observation.getEntryRelationships().add(entryRelationship2);

		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
		Assert.assertTrue("Invalid Allergy Problem Act in Test", validation);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		List<AllergyIntoleranceReactionComponent> reactions = allergyIntolerance.getReaction();

		AllergyIntoleranceReactionComponent reaction1 = reactions.get(0);
		AllergyIntoleranceReactionComponent reaction2 = reactions.get(1);

		String actual1 = reaction1.getOnsetElement().getValueAsString();
		String actual2 = reaction2.getOnsetElement().getValueAsString();

		Assert.assertEquals("Unexpected AllergyIntolerance Reaction Onset value (1):", expected1,
				actual1.replaceAll("-", ""));
		Assert.assertEquals("Unexpected AllergyIntolerance Reaction Onset value (2):", expected2,
				actual2.replaceAll("-", ""));

		String lastActual = allergyIntolerance.getLastOccurrenceElement().getValueAsString();
		Assert.assertEquals("Unexpected AllergyIntolerance Last Occurence:", expected2, lastActual.replaceAll("-", ""));
	}

	@Test
	public void testEffectiveTime() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		DiagnosticChain dxChain = new BasicDiagnostic();
		BundleInfo bundleInfo = new BundleInfo(rt);

		String expected1 = "20171002";
		IVL_TS ivlTs1 = createEffectiveTimeLow(expected1);
		act.setEffectiveTime(ivlTs1);

		Boolean validation1 = act.validateAllergyProblemActEffectiveTime(dxChain, null);
		Assert.assertTrue("Invalid Allergy Problem Act in Test", validation1);

		Bundle bundle1 = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance1 = findOneResource(bundle1);
		String actual1 = allergyIntolerance1.getAssertedDateElement().getValueAsString();
		Assert.assertEquals("Unexpected AllergyIntolerance Asserted Date (1)", expected1, actual1.replaceAll("-", ""));

		String expected2 = "20161103";
		IVL_TS ivlTs2 = createEffectiveTimeValue(expected2);
		act.setEffectiveTime(ivlTs2);

		Boolean validation2 = act.validateAllergyProblemActEffectiveTime(dxChain, null);
		Assert.assertTrue("Invalid Allergy Problem Act in Test", validation2);

		Bundle bundle2 = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance2 = findOneResource(bundle2);
		String actual2 = allergyIntolerance2.getAssertedDateElement().getValueAsString();
		Assert.assertEquals("Unexpected AllergyIntolerance Asserted Date (2)", expected2, actual2.replaceAll("-", ""));
	}

	static private void verifyAllergyIntoleranceVerificationStatus(AllergyProblemAct act, String expected)
			throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo).getBundle();
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);

		AllergyIntoleranceVerificationStatus verificationStatus = allergyIntolerance.getVerificationStatus();
		String actual = verificationStatus == null ? null : verificationStatus.toCode();
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testAllergyIntoleranceStatusCode() throws Exception {
		AllergyProblemActImpl act = (AllergyProblemActImpl) cdaObjFactory.createAllergyProblemAct();
		verifyAllergyIntoleranceVerificationStatus(act, null);

		act.setStatusCode(null);
		verifyAllergyIntoleranceVerificationStatus(act, null);

		act.setStatusCode(cdaTypeFactory.createCS("invalid"));
		Assert.assertFalse("Unexpected Valid Allergy Problem Act in Test",
				act.validateAllergyProblemActStatusCode(null, null));

		for (Map.Entry<String, Object> entry : verificationStatusMap.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();

			CS cs = cdaTypeFactory.createCS(cdaStatusCode);
			act.setStatusCode(cs);
			Assert.assertTrue("Invalid Allergy Problem Act in Test",
					act.validateAllergyProblemActStatusCode(null, null));

			verifyAllergyIntoleranceVerificationStatus(act, fhirStatus);
		}
	}
}
