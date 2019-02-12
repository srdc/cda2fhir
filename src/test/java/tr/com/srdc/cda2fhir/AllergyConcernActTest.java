package tr.com.srdc.cda2fhir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus;
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
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.EntryRelationshipImpl;
import org.openhealthtools.mdht.uml.cda.impl.Participant2Impl;
import org.openhealthtools.mdht.uml.cda.impl.PlayingEntityImpl;
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

public class AllergyConcernActTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	
	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;
	
	private static Map<String, String> cdaProblemStatusCodeToName = new HashMap<String, String>();
	private static Map<String, String> cdaAllergyIntolaranceTypeCodeToName = new HashMap<String, String>();
	
	private static Map<String, Object> clinicalStatusMap = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceClinicalStatus.json");
		
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		
		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();		
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();
		
		cdaProblemStatusCodeToName.put("55561003", "Active");
		cdaProblemStatusCodeToName.put("73425007", "Inactive");
		cdaProblemStatusCodeToName.put("413322009", "Resolved");
	}

	static private AllergyIntolerance findOneResource(Bundle bundle) throws Exception {
    	List<AllergyIntolerance> allergyResources = bundle.getEntry().stream()
    			.map(r -> r.getResource())
    			.filter(r -> (r instanceof AllergyIntolerance))
    			.map(r -> (AllergyIntolerance) r)
				.collect(Collectors.toList());
    	Assert.assertEquals(1, allergyResources.size());
    	return allergyResources.get(0);	
	}
	
	static private void verifyAllergyIntoleranceCategory(AllergyProblemAct act, String expected) throws Exception {
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);

    	Enumeration<AllergyIntolerance.AllergyIntoleranceCategory> category = allergyIntolerance.getCategory().get(0);
    	String actual = category == null ? null : category.asStringValue();
		Assert.assertEquals(expected, actual);		
	}
	
	static private AllergyStatusObservationImpl createAllergyStatusObservation(String cdaProblemStatusCode) {
		AllergyStatusObservationImpl allergyStatus = (AllergyStatusObservationImpl) cdaObjFactory.createAllergyStatusObservation();
		
		II templateId = cdaTypeFactory.createII("2.16.840.1.113883.10.20.22.4.28");
		allergyStatus.getTemplateIds().add(templateId);

		CD code = cdaTypeFactory.createCD("33999-4", "2.16.840.1.113883.6.1", null, null);
		allergyStatus.setCode(code);

		CS cs = cdaTypeFactory.createCS("completed");
		allergyStatus.setStatusCode(cs);
		if (cdaProblemStatusCode == null) {
			cdaProblemStatusCode = clinicalStatusMap.entrySet().stream().findFirst().get().getKey();
		}
		String cdaProblemStatusName = cdaProblemStatusCodeToName.get(cdaProblemStatusCode);	
		CE ce = cdaTypeFactory.createCE (cdaProblemStatusCode, "2.16.840.1.11388 3.6.96", "SNOMED CT", cdaProblemStatusName);
		allergyStatus.getValues().add(ce);
		
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
		act.getEntryRelationships().stream()
			.filter(r -> (r.getObservation() == observation))
			.forEach(r -> r.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ));

		return act;
	}

	@Test
	public void testAllergyIntoleranceObservationEffectiveTime() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0).getObservation();					

		String expected1 = "20171002";
		
		IVL_TS ivlTs1 = cdaTypeFactory.createIVL_TS();
		IVXB_TS ivxb1 = cdaTypeFactory.createIVXB_TS();
		ivxb1.setValue(expected1);
		ivlTs1.setLow(ivxb1);
		observation.setEffectiveTime(ivlTs1);
		
		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
		Assert.assertTrue(validation);

		Bundle bundle1 = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance1 = findOneResource(bundle1);
		String actual1 = allergyIntolerance1.getOnsetDateTimeType().getValueAsString();
		Assert.assertEquals(expected1, actual1.replaceAll("-", ""));

		String expected2 = "20161103";
		IVL_TS ivlTs2 = cdaTypeFactory.createIVL_TS();
		ivlTs2.setValue(expected2);
		observation.setEffectiveTime(ivlTs2);
		
		Bundle bundle2 = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance2 = findOneResource(bundle2);
		String actual2 = allergyIntolerance2.getOnsetDateTimeType().getValueAsString();
		Assert.assertEquals(expected2, actual2.replaceAll("-", ""));	
	}
	
	@Test
	public void testSubstanceReactantForIntolerance() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observation = (AllergyObservationImpl) act.getEntryRelationships().get(0).getObservation();					
		
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
		Assert.assertTrue(validation);

		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		CodeableConcept cc = allergyIntolerance.getCode();
		Coding coding = cc.getCoding().get(0);
		
		Assert.assertEquals("2670", coding.getCode());
		Assert.assertEquals("Codeine", coding.getDisplay());
	}
		
	@Test
	public void testAllergyAndIntoleranceType() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observationTop = (AllergyObservationImpl) act.getEntryRelationships().get(0).getObservation();					
		verifyAllergyIntoleranceVerificationStatus(act, null);

		Map<String, Object> map = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceCategory.json");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String cdaType = entry.getKey();
			String fhirCategory = (String) entry.getValue();
			String cdaTypeName = cdaAllergyIntolaranceTypeCodeToName.get(cdaType);

			CE ce = cdaTypeFactory.createCE (cdaType, "2.16.840.1.11388 3.6.96", "SNOMED CT", cdaTypeName);

			observationTop.getValues().clear();
			observationTop.getValues().add(ce);
			
			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
			Assert.assertTrue(validation);
			
			verifyAllergyIntoleranceCategory(act, fhirCategory);
		}
	}

	@Test
	public void testStatusObservation() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();
		AllergyObservationImpl observationTop = (AllergyObservationImpl) act.getEntryRelationships().get(0).getObservation();					
		
		for (Map.Entry<String, Object> entry : clinicalStatusMap.entrySet()) {
			String cdaProblemStatusCode = entry.getKey();
			String fhirClinicalStatus = (String) entry.getValue();
					
					
			AllergyStatusObservationImpl allergyStatus = createAllergyStatusObservation(cdaProblemStatusCode);	
			EntryRelationshipImpl entryRelationship = (EntryRelationshipImpl) cdaFactory.createEntryRelationship();
			entryRelationship.setObservation(allergyStatus);
			
			observationTop.getEntryRelationships().clear();
			observationTop.getEntryRelationships().add(entryRelationship);
										
			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
			Assert.assertTrue(validation);
	
			Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
			AllergyIntolerance allergyIntolerance = findOneResource(bundle);
			AllergyIntoleranceClinicalStatus clinicalStatus = allergyIntolerance.getClinicalStatus();
			String actual = clinicalStatus.toCode();
			Assert.assertEquals(fhirClinicalStatus, actual);
		}
	}
	
	static private void verifyAllergyIntoleranceVerificationStatus(AllergyProblemAct act, String expected) throws Exception {
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		
    	AllergyIntoleranceVerificationStatus verificationStatus = allergyIntolerance.getVerificationStatus();
    	String actual = verificationStatus == null ? null : verificationStatus.toCode();
		Assert.assertEquals(expected, actual);		
	}
	
	@Test
	public void testEffectiveTime() throws Exception {
		AllergyProblemActImpl act = createAllergyConcernAct();

		String expected1 = "20171002";
		
		IVL_TS ivlTs1 = cdaTypeFactory.createIVL_TS();
		IVXB_TS ivxb1 = cdaTypeFactory.createIVXB_TS();
		ivxb1.setValue(expected1);
		ivlTs1.setLow(ivxb1);
		act.setEffectiveTime(ivlTs1);
		
		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateAllergyProblemActAllergyObservation(dxChain, null);
		Assert.assertTrue(validation);

		Bundle bundle1 = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance1 = findOneResource(bundle1);
		String actual1 = allergyIntolerance1.getAssertedDateElement().getValueAsString();
		Assert.assertEquals(expected1, actual1.replaceAll("-", ""));

		String expected2 = "20161103";
		IVL_TS ivlTs2 = cdaTypeFactory.createIVL_TS();
		ivlTs2.setValue(expected2);
		act.setEffectiveTime(ivlTs2);
		
		Bundle bundle2 = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance2 = findOneResource(bundle2);
		String actual2 = allergyIntolerance2.getAssertedDateElement().getValueAsString();
		Assert.assertEquals(expected2, actual2.replaceAll("-", ""));	
	}
		
	@Test
	public void testAllergyIntoleranceStatusCode() throws Exception {
		AllergyProblemActImpl act = (AllergyProblemActImpl) cdaObjFactory.createAllergyProblemAct();
		verifyAllergyIntoleranceVerificationStatus(act, null);
		
		act.setStatusCode(cdaTypeFactory.createCS("invalid"));
		Assert.assertFalse(act.validateAllergyProblemActStatusCode(null, null));
				
		Map<String, Object> map = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceVerificationStatus.json");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();
			
			CS cs = cdaTypeFactory.createCS(cdaStatusCode);
			act.setStatusCode(cs);
			Assert.assertTrue(act.validateAllergyProblemActStatusCode(null, null));

			verifyAllergyIntoleranceVerificationStatus(act, fhirStatus);
		}
	}
}
