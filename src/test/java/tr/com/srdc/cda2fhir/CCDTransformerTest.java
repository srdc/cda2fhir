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

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerTest {

    @BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
    
    private static Bundle readVerifyFile(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
                
        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
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
    
    // Gold Sample r2.1
    @Test
    public void testSample1() throws Exception {
    	Bundle bundle = readVerifyFile("170.315_b1_toc_gold_sample2_v1.xml");
        Assert.assertTrue("Expect some entries", bundle.hasEntry());
        
        // Spot checks
    	Patient patient = BundleUtil.findOneResource(bundle, Patient.class);
    	Assert.assertTrue("Expect an identifier for patient", patient.hasIdentifier());
    	Assert.assertEquals("Expect the patient id in the CCDA file", "414122222", patient.getIdentifier().get(0).getValue());

        BundleUtil.findResources(bundle, AllergyIntolerance.class, 1);
        BundleUtil.findResources(bundle, Condition.class, 1);
        BundleUtil.findResources(bundle, MedicationStatement.class, 1);
        BundleUtil.findResources(bundle, Immunization.class, 1);
        BundleUtil.findResources(bundle, Procedure.class, 1);
    }

    @Test
    public void testSample2() throws Exception {
    	Bundle bundle = readVerifyFile("C-CDA_R2-1_CCD.xml");
        Assert.assertTrue("Expect some entries", bundle.hasEntry());
        
        // Spot checks
    	BundleUtil.findOneResource(bundle, Patient.class);
        BundleUtil.findResources(bundle, AllergyIntolerance.class, 2);
        BundleUtil.findResources(bundle, Condition.class, 6);
        BundleUtil.findResources(bundle, MedicationStatement.class, 2);
        BundleUtil.findResources(bundle, Immunization.class, 5);
        BundleUtil.findResources(bundle, Procedure.class, 1);
    }

    @Test
    public void testSample3() throws Exception {
    	Bundle bundle = readVerifyFile("Vitera_CCDA_SMART_Sample.xml");
        Assert.assertTrue("Expect some entries", bundle.hasEntry());
        
        // Spot checks
    	BundleUtil.findOneResource(bundle, Patient.class);
        BundleUtil.findResources(bundle, AllergyIntolerance.class, 5);
        BundleUtil.findResources(bundle, Condition.class, 1);
        BundleUtil.findResources(bundle, MedicationStatement.class, 16);
        BundleUtil.findResources(bundle, Immunization.class, 1);
        BundleUtil.findResources(bundle, Procedure.class, 4);
    }
}
