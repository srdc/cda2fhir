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
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerTest {

    @BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
    
    private static Bundle readVerifyFile(String sourceName, List<CDASectionTypeEnum> addlSections) throws Exception {
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
    	Assert.assertTrue("Expect composition to be the first resource", bundle.getEntry().get(0).getResource() == composition);

    	String baseName = sourceName.substring(sourceName.length() - 4);
        FHIRUtil.printJSON(bundle, "src/test/resources/output/" + baseName + ".json");
        return bundle;
    }

    private static List<Reference> getSectionEntries(Composition composition, String title) {
        for (SectionComponent section: composition.getSection()) {
        	if (title.equals(section.getTitle())) {
        		return section.getEntry();
        	}
        }
        return null;
    }
    
    private static <T extends Resource> void verifySection(Bundle bundle, String title, Class<T> clazz, int count, int referenceCount) throws Exception {
        List<T> resources = BundleUtil.findResources(bundle, clazz, count);
        Set<String> ids = resources.stream().map(r -> r.getId()).collect(Collectors.toSet());
        Composition composition = (Composition) bundle.getEntry().get(0).getResource();
        List<Reference> references = getSectionEntries(composition, title);
        Assert.assertNotNull("Expect references in section " + title, references);
        Assert.assertEquals("Expect " + referenceCount + " references in composition", referenceCount, references.size());
        for (int idx = 0; idx < referenceCount; ++idx) {
        	String id = references.get(idx).getReference().toString();
        	Assert.assertTrue("Expect composition reference to be a resource id", ids.contains(id));
        }
    }

    private static <T extends Resource> void verifySection(Bundle bundle, String title, Class<T> clazz, int count) throws Exception {
    	verifySection(bundle, title, clazz, count, count);
    }    
    // Gold Sample r2.1
    @Test
    public void testSample1() throws Exception {
    	Bundle bundle = readVerifyFile("170.315_b1_toc_gold_sample2_v1.xml", null);

    	verifySection(bundle, "ALLERGIES AND ADVERSE REACTIONS", AllergyIntolerance.class, 1);
    	verifySection(bundle, "PROBLEMS", Condition.class, 1);
    	verifySection(bundle, "MEDICATIONS", MedicationStatement.class, 1);
    	verifySection(bundle, "IMMUNIZATIONS", Immunization.class, 1);
    	verifySection(bundle, "PROCEDURES", Procedure.class, 1);

        // Spot checks
    	Patient patient = BundleUtil.findOneResource(bundle, Patient.class);
    	Assert.assertTrue("Expect an identifier for patient", patient.hasIdentifier());
    	Assert.assertEquals("Expect the patient id in the CCDA file", "414122222", patient.getIdentifier().get(0).getValue());
    }

    @Test
    public void testSample2() throws Exception {
    	List<CDASectionTypeEnum> addlSections = new ArrayList<CDASectionTypeEnum>();
    	addlSections.add(CDASectionTypeEnum.VITAL_SIGNS_SECTION);
    	addlSections.add(CDASectionTypeEnum.SOCIAL_HISTORY_SECTION);
    	addlSections.add(CDASectionTypeEnum.RESULTS_SECTION);
    	addlSections.add(CDASectionTypeEnum.FUNCTIONAL_STATUS_SECTION);
    	addlSections.add(CDASectionTypeEnum.FAMILY_HISTORY_SECTION);
    	addlSections.add(CDASectionTypeEnum.MEDICAL_EQUIPMENT_SECTION);
    	Bundle bundle = readVerifyFile("C-CDA_R2-1_CCD.xml", addlSections);
         
    	verifySection(bundle, "ALLERGIES AND ADVERSE REACTIONS", AllergyIntolerance.class, 2);
    	verifySection(bundle, "PROBLEMS", Condition.class, 7, 4);
    	verifySection(bundle, "MEDICATIONS", MedicationStatement.class, 2);
    	verifySection(bundle, "IMMUNIZATIONS", Immunization.class, 5);
    	verifySection(bundle, "PROCEDURES", Procedure.class, 2, 1);
    	verifySection(bundle, "ENCOUNTERS", Encounter.class, 1, 1);
    	verifySection(bundle, "VITAL SIGNS", Observation.class, 20, 8);
    	verifySection(bundle, "SOCIAL HISTORY", Observation.class, 20, 3);
    	verifySection(bundle, "RESULTS", DiagnosticReport.class, 2, 2);
    	verifySection(bundle, "FUNCTIONAL STATUS", Observation.class, 20, 2);
    	verifySection(bundle, "FAMILY HISTORY", FamilyMemberHistory.class, 1, 1);
    	verifySection(bundle, "MEDICAL EQUIPMENT", Resource.class, 110, 4);
    }

    @Test
    public void testSample3() throws Exception {
    	Bundle bundle = readVerifyFile("Vitera_CCDA_SMART_Sample.xml", null);
        
    	verifySection(bundle, "Allergies", AllergyIntolerance.class, 5);
    	verifySection(bundle, "Problems", Condition.class, 1);
    	verifySection(bundle, "Medications", MedicationStatement.class, 16);
    	verifySection(bundle, "Immunizations", Immunization.class, 1);
    	verifySection(bundle, "Procedures and Surgical/Medical History", Procedure.class, 4);
    	verifySection(bundle, "Encounters", Encounter.class, 13);
    }
}
