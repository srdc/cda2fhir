package tr.com.srdc.cda2fhir;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttestationMode;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerTest {
	private static final Logger logger = LoggerFactory.getLogger(ResourceTransformerImpl.class);
	private final static List<CDASectionTypeEnum> addlSections = new ArrayList<CDASectionTypeEnum>();

	static {
		addlSections.add(CDASectionTypeEnum.VITAL_SIGNS_SECTION);
		addlSections.add(CDASectionTypeEnum.SOCIAL_HISTORY_SECTION);
		addlSections.add(CDASectionTypeEnum.RESULTS_SECTION);
		addlSections.add(CDASectionTypeEnum.FUNCTIONAL_STATUS_SECTION);
		addlSections.add(CDASectionTypeEnum.FAMILY_HISTORY_SECTION);
		addlSections.add(CDASectionTypeEnum.MEDICAL_EQUIPMENT_SECTION);
	};

	@BeforeClass
	public static void init() {
		// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar
		// documents will not be recognised.
		// This has to be called before loading the document; otherwise will have no
		// effect.
		CDAUtil.loadPackages();
	}

	private static List<Reference> getSectionEntriesByCode(Composition composition, String code) {
		for (SectionComponent section : composition.getSection()) {
			String sectionCode = section.getCode().getCoding().get(0).getCode();
			if (code.equals(sectionCode)) {
				return section.getEntry();
			}
		}
		return null;
	}

	private static <T extends Resource> void verifySectionCounts(Bundle bundle, String sectionCode, Class<T> clazz) {
		Composition composition = (Composition) bundle.getEntry().get(0).getResource();
		List<Reference> compositionEntries = getSectionEntriesByCode(composition, sectionCode);
		List<T> resources = FHIRUtil.findResources(bundle, clazz);
		String msg = String.format("Expect only section resources for type %s", clazz.getSimpleName());
		Assert.assertEquals(msg, compositionEntries == null ? 0 : compositionEntries.size(), resources.size());
	}

	private static void verifyNoDuplicatePractitioner(Bundle bundle) {
		IIdentifierMap<String> identifierMap = IdentifierMapFactory.bundleToIds(bundle);
		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		for (Practitioner practitioner : practitioners) {
			if (!practitioner.hasIdentifier()) {
				logger.info("No practioner identifier");
				continue;
			}
			String id = practitioner.getId();
			for (Identifier identifier : practitioner.getIdentifier()) {
				String idInMap = identifierMap.get(practitioner.fhirType(), identifier);
				if (idInMap == null) {
					logger.error("Did not find " + id + " in identifier map");
				} else {
					if (!idInMap.equals(id)) {
						logger.error(id + " is a duplicate of " + idInMap);
					} else {
						logger.error("All good for " + id);
					}
				}
			}
		}
	}

	private static Bundle readVerifyFile(String sourceName, List<CDASectionTypeEnum> addlSections) throws Exception {
		logger.info(String.format("Verifying file %s", sourceName));
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);

		ClinicalDocument cda = CDAUtil.load(fis);
		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		if (addlSections != null) {
			addlSections.stream().forEach(r -> ccdTransformer.addSection(r));
		}
		Config.setGenerateDafProfileMetadata(false);
		Config.setGenerateNarrative(true);
		Bundle bundle = ccdTransformer.transformDocument(cda);
		Assert.assertNotNull("Expect a bundle after transformation", bundle);
		Assert.assertTrue("Expect some entries", bundle.hasEntry());

		Composition composition = BundleUtil.findOneResource(bundle, Composition.class);
		Assert.assertTrue("Expect composition to be the first resource",
				bundle.getEntry().get(0).getResource() == composition);

		// Nothing should create encounters but Encounters Section
		verifySectionCounts(bundle, "46240-8", Encounter.class);

		verifyNoDuplicatePractitioner(bundle);

		String baseName = sourceName.substring(0, sourceName.length() - 4);
		FHIRUtil.printJSON(bundle, "src/test/resources/output/" + baseName + ".json");
		return bundle;
	}

	private static List<Reference> getSectionEntries(Composition composition, String title) {
		for (SectionComponent section : composition.getSection()) {
			if (title.equals(section.getTitle())) {
				return section.getEntry();
			}
		}
		return null;
	}

	private static <T extends Resource> void verifySection(Bundle bundle, String title, Class<T> clazz, int count,
			int referenceCount) throws Exception {
		List<T> resources = BundleUtil.findResources(bundle, clazz, count);
		Set<String> ids = resources.stream().map(r -> r.getId()).collect(Collectors.toSet());
		Composition composition = (Composition) bundle.getEntry().get(0).getResource();
		List<Reference> references = getSectionEntries(composition, title);
		Assert.assertNotNull("Expect references in section " + title, references);
		Assert.assertEquals("Expect " + referenceCount + " references in composition", referenceCount,
				references.size());
		for (int idx = 0; idx < referenceCount; ++idx) {
			String id = references.get(idx).getReference().toString();
			Assert.assertTrue("Expect composition reference to be a resource id", ids.contains(id));
		}
	}

	private static <T extends Resource> void verifySection(Bundle bundle, String title, Class<T> clazz, int count)
			throws Exception {
		verifySection(bundle, title, clazz, count, count);
	}

	// Gold Sample r2.1
	@Ignore
	@Test
	public void testSample1() throws Exception {
		readVerifyFile("170.315_b1_toc_gold_sample2_v1.xml", addlSections);
		Bundle bundle = readVerifyFile("170.315_b1_toc_gold_sample2_v1.xml", null);

		verifySection(bundle, "ALLERGIES AND ADVERSE REACTIONS", AllergyIntolerance.class, 1);
		verifySection(bundle, "PROBLEMS", Condition.class, 1);
		verifySection(bundle, "MEDICATIONS", MedicationStatement.class, 1);
		verifySection(bundle, "IMMUNIZATIONS", Immunization.class, 1);
		verifySection(bundle, "PROCEDURES", Procedure.class, 1);

		// Spot checks
		Patient patient = BundleUtil.findOneResource(bundle, Patient.class);
		Assert.assertTrue("Expect an identifier for patient", patient.hasIdentifier());
		Assert.assertEquals("Expect the patient id in the CCDA file", "414122222",
				patient.getIdentifier().get(0).getValue());
	}

	@Test
	public void testSample2() throws Exception {
		Bundle bundle = readVerifyFile("C-CDA_R2-1_CCD.xml", addlSections);

		verifySection(bundle, "ALLERGIES AND ADVERSE REACTIONS", AllergyIntolerance.class, 2);
		verifySection(bundle, "PROBLEMS", Condition.class, 7, 4);
		verifySection(bundle, "MEDICATIONS", MedicationStatement.class, 2);
		verifySection(bundle, "IMMUNIZATIONS", Immunization.class, 5);
		verifySection(bundle, "PROCEDURES", Procedure.class, 2, 1);
		verifySection(bundle, "ENCOUNTERS", Encounter.class, 1);
		verifySection(bundle, "VITAL SIGNS", Observation.class, 20, 8);
		verifySection(bundle, "SOCIAL HISTORY", Observation.class, 20, 3);
		verifySection(bundle, "RESULTS", DiagnosticReport.class, 2, 2);
		verifySection(bundle, "FUNCTIONAL STATUS", Observation.class, 20, 2);
		verifySection(bundle, "FAMILY HISTORY", FamilyMemberHistory.class, 1, 1);
		verifySection(bundle, "MEDICAL EQUIPMENT", Resource.class, 110, 4);

		// Spot checks
		BundleUtil util = new BundleUtil(bundle);
		util.spotCheckImmunizationPractitioner("e6f1ba43-c0ed-4b9b-9f12-f435d8ad8f92", "Hippocrates", null,
				"Good Health Clinic");
		util.spotCheckEncounterPractitioner("2a620155-9d11-439e-92b3-5d9815ff4de8", null, "59058001", null);
		util.spotCheckProcedurePractitioner("d68b7e32-7810-4f5b-9cc2-acd54b0fd85d", null, null, "Community Health and Hospitals");
		util.spotCheckPractitioner("urn:oid:2.16.840.1.113883.19.5.9999.456", "2981823", null, "1001 Village Avenue");
		util.spotCheckAttesterPractitioner(CompositionAttestationMode.PROFESSIONAL, "Primary", "207QA0505X", null);
		util.spotCheckAttesterPractitioner(CompositionAttestationMode.LEGAL, "Primary", "207QA0505X", null);
		util.spotCheckPractitioner("urn:oid:2.16.840.1.113883.4.6", "5555555555", "Primary", "1004 Healthcare Drive ");
		util.spotCheckAuthorPractitioner("Primary", "207QA0505X", null);
		util.spotCheckObservationPractitioner("b63a8636-cfff-4461-b018-40ba58ba8b32", null, null, null);
		util.spotCheckMedStatementPractitioner("6c844c75-aa34-411c-b7bd-5e4a9f206e29", "Primary", null, null);
		util.spotCheckObservationPractitioner("ed9589fd-fda0-41f7-a3d0-dc537554f5c2", null, null, null);
		util.spotCheckConditionPractitioner("ab1791b0-5c71-11db-b0de-0800200c9a66", null, null, null);
		util.spotCheckAllergyPractitioner("36e3e930-7b14-11db-9fe1-0800200c9a66", null, null, null);
	}

	@Ignore
	@Test
	public void testSample3() throws Exception {
		readVerifyFile("170.315_b1_toc_gold_sample2_v1.xml", addlSections);
		Bundle bundle = readVerifyFile("Vitera_CCDA_SMART_Sample.xml", null);

		verifySection(bundle, "Allergies", AllergyIntolerance.class, 5);
		verifySection(bundle, "Problems", Condition.class, 1);
		verifySection(bundle, "Medications", MedicationStatement.class, 16);
		verifySection(bundle, "Immunizations", Immunization.class, 1);
		verifySection(bundle, "Procedures and Surgical/Medical History", Procedure.class, 4);
		verifySection(bundle, "Encounters", Encounter.class, 13);
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		readVerifyFile("Epic/DOC0001.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		readVerifyFile("Epic/DOC0001 2.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample3() throws Exception {
		readVerifyFile("Epic/DOC0001 3.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample4() throws Exception {
		readVerifyFile("Epic/DOC0001 4.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample5() throws Exception {
		readVerifyFile("Epic/DOC0001 5.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample6() throws Exception {
		readVerifyFile("Epic/DOC0001 6.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample7() throws Exception {
		readVerifyFile("Epic/DOC0001 7.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample8() throws Exception {
		readVerifyFile("Epic/DOC0001 8.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample9() throws Exception {
		readVerifyFile("Epic/DOC0001 9.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample10() throws Exception {
		readVerifyFile("Epic/DOC0001 10.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample11() throws Exception {
		readVerifyFile("Epic/DOC0001 11.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample12() throws Exception {
		readVerifyFile("Epic/DOC0001 12.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample13() throws Exception {
		readVerifyFile("Epic/DOC0001 13.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample14() throws Exception {
		readVerifyFile("Epic/DOC0001 14.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample15() throws Exception {
		readVerifyFile("Epic/DOC0001 15.XML", addlSections);
	}

	@Ignore
	@Test
	public void testEpicSample16() throws Exception {
		readVerifyFile("Epic/HannahBanana_EpicCCD.xml", addlSections);
	}

	@Ignore
	@Test
	public void testCernerSample1() throws Exception {
		readVerifyFile("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML", addlSections);
	}

	@Ignore
	@Test
	public void testCernerSample2() throws Exception {
		readVerifyFile("Cerner/Encounter-RAKIA_TEST_DOC00001.XML", addlSections);
	}
}
