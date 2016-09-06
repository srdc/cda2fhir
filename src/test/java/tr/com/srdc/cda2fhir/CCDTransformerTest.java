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

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by mustafa on 8/4/2016.
 */
public class CCDTransformerTest {

    @BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }

    // C-CDA_R2-1_CCD.xml
    @Test
    public void testReferenceCCDInstance() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");

        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD.json");
    }
    
    
    // Vitera
    @Test
    public void testVitera() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");

        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            FHIRUtil.printJSON(bundle, "src/test/resources/output/Vitera_CCDA_SMART_Sample.json");
    }
    
   
    // Traversing all the resources in src/test/resources/sample_ccdas/*,
    //  .. transforming and writing the result to src/test/resources/output/*
    
    // Allscripts/Enterprise_EHR
    @Test
    public void testAllscriptsEnterpriseEHR() throws Exception {
    	File allscriptsEnterpriseEHRDir = new File("src/test/resources/sample_ccdas/Allscripts_Samples/Enterprise_EHR");
    	for(File allscriptsEnterpriseEHRExample : allscriptsEnterpriseEHRDir.listFiles()) {
    		System.out.println("Transforming "+allscriptsEnterpriseEHRExample.getPath());
    		FileInputStream fis = new FileInputStream(allscriptsEnterpriseEHRExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Allscripts_Samples/Enterprise_EHR/"+allscriptsEnterpriseEHRExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    // Allscripts/Internal_Test_with_MU_2_data
    @Test
    public void testAllscriptsMU2Data() throws Exception {
    	File allscriptsMU2DataDir = new File("src/test/resources/sample_ccdas/Allscripts_Samples/Internal_Test_with_MU_2_data");
    	for(File allscriptsMU2DataExample : allscriptsMU2DataDir.listFiles()) {
    		System.out.println("Transforming "+allscriptsMU2DataExample.getPath());
    		FileInputStream fis = new FileInputStream(allscriptsMU2DataExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Allscripts_Samples/Internal_Test_with_MU_2_data/"+allscriptsMU2DataExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    // Allscripts/Professional_EHR
    @Test
    public void testAllscriptsProfessionalEHR() throws Exception {
    	File allscriptsProfessionalEHRDir = new File("src/test/resources/sample_ccdas/Allscripts_Samples/Professional_EHR");
    	for(File allscriptsProfessionalEHRExample : allscriptsProfessionalEHRDir.listFiles()) {
    		System.out.println("Transforming "+allscriptsProfessionalEHRExample.getPath());
    		FileInputStream fis = new FileInputStream(allscriptsProfessionalEHRExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Allscripts_Samples/Professional_EHR/"+allscriptsProfessionalEHRExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    // Allscripts/Sunrise_Clinical_Manager
    @Test
    public void testAllscriptsSunriseClinicalManager() throws Exception {
    	File allscriptsSunriseClinicalManagerDir = new File("src/test/resources/sample_ccdas/Allscripts_Samples/Sunrise_Clinical_Manager");
    	for(File allscriptsSunriseClinicalManagerExample : allscriptsSunriseClinicalManagerDir.listFiles()) {
    		System.out.println("Transforming "+allscriptsSunriseClinicalManagerExample.getPath());
    		FileInputStream fis = new FileInputStream(allscriptsSunriseClinicalManagerExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Allscripts_Samples/Sunrise_Clinical_Manager/"+allscriptsSunriseClinicalManagerExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    
    // Cerner
    @Test
    public void testCerner() throws Exception {
    	File cernerDir = new File("src/test/resources/sample_ccdas/Cerner/");
    	for(File cernerExample : cernerDir.listFiles()) {
    		System.out.println("Transforming "+cernerExample.getPath());
    		FileInputStream fis = new FileInputStream(cernerExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Cerner/"+cernerExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    // Notice that the following test may use resources that cause errors while transforming the includings of text attributes
    // Therefore, you may want to make dtt.tStrucDocText2Narrative method return null before using the following test method
    // EMERGE
    @Test
    public void testEMERGE() throws Exception {
    	File emergeDir = new File("src/test/resources/sample_ccdas/EMERGE/");
    	for(File emergeExample : emergeDir.listFiles()) {
    		System.out.println("Transforming "+emergeExample.getPath());
    		FileInputStream fis = new FileInputStream(emergeExample);

            ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/EMERGE/"+emergeExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }     
    	}
    }
    
    // Notice that the following test may use resources that cause errors while transforming the includings of text attributes
    // Therefore, you may want to make dtt.tStrucDocText2Narrative method return null before using the following test method
    // Greenway
    @Test
    public void testGreenway() throws Exception {
    	File greenwayDir = new File("src/test/resources/sample_ccdas/Greenway/");
    	for(File greenwayExample : greenwayDir.listFiles()) {
    		System.out.println("Transforming "+greenwayExample.getPath());
    		FileInputStream fis = new FileInputStream(greenwayExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Greenway/"+greenwayExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // HL7
    @Test
    public void testHL7() throws Exception {
    	File hl7Dir = new File("src/test/resources/sample_ccdas/HL7/");
    	for(File hl7Example : hl7Dir.listFiles()) {
    		System.out.println("Transforming "+hl7Example.getPath());
    		FileInputStream fis = new FileInputStream(hl7Example);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/HL7/"+hl7Example.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // Kareo
    @Test
    public void testKareo() throws Exception {
    	File kareoDir = new File("src/test/resources/sample_ccdas/Kareo/");
    	for(File kareoExample : kareoDir.listFiles()) {
    		System.out.println("Transforming "+kareoExample.getPath());
    		FileInputStream fis = new FileInputStream(kareoExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Kareo/"+kareoExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // Kinsights
    @Test
    public void testKinsights() throws Exception {
    	File kinsightsDir = new File("src/test/resources/sample_ccdas/Kinsights/");
    	for(File kinsightsExample : kinsightsDir.listFiles()) {
    		System.out.println("Transforming "+kinsightsExample.getPath());
    		FileInputStream fis = new FileInputStream(kinsightsExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Kinsights/"+kinsightsExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // mTuitive_OpNote
    @Test
    public void testmTuitive_OpNote() throws Exception {
    	File mTuitive_OpNoteDir = new File("src/test/resources/sample_ccdas/mTuitive_OpNote/");
    	for(File mTuitive_OpNoteExample : mTuitive_OpNoteDir.listFiles()) {
    		System.out.println("Transforming "+mTuitive_OpNoteExample.getPath());
    		FileInputStream fis = new FileInputStream(mTuitive_OpNoteExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/mTuitive_OpNote/"+mTuitive_OpNoteExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // NextGen
    @Test
    public void testNextGen() throws Exception {
    	File nextGenDir = new File("src/test/resources/sample_ccdas/NextGen/");
    	for(File nextGenExample : nextGenDir.listFiles()) {
    		System.out.println("Transforming "+nextGenExample.getPath());
    		FileInputStream fis = new FileInputStream(nextGenExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/NextGen/"+nextGenExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // NIST
    @Test
    public void testNIST() throws Exception {
    	File NISTDir = new File("src/test/resources/sample_ccdas/NIST/");
    	for(File NISTExample : NISTDir.listFiles()) {
    		System.out.println("Transforming "+NISTExample.getPath());
    		FileInputStream fis = new FileInputStream(NISTExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/NIST/"+NISTExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // Notice that the following test may use resources that cause errors while transforming the includings of text attributes
    // Therefore, you may want to make dtt.tStrucDocText2Narrative method return null before using the following test method
    // Partners_HealthCare
    @Test
    public void testPartners() throws Exception {
    	File partnersDir = new File("src/test/resources/sample_ccdas/Partners_HealthCare/");
    	for(File partnersExample : partnersDir.listFiles()) {
    		System.out.println("Transforming "+partnersExample.getPath());
    		FileInputStream fis = new FileInputStream(partnersExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Partners_HealthCare/"+partnersExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // PracticeFusion
    @Test
    public void testPracticeFusion() throws Exception {
    	File practiceFusionDir = new File("src/test/resources/sample_ccdas/PracticeFusion/");
    	for(File practiceFusionExample : practiceFusionDir.listFiles()) {
    		System.out.println("Transforming "+practiceFusionExample.getPath());
    		FileInputStream fis = new FileInputStream(practiceFusionExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/PracticeFusion/"+practiceFusionExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    // Transitions_of_Care
    @Test
    public void testTransitions_of_Care() throws Exception {
    	File transitionsOfCareDir = new File("src/test/resources/sample_ccdas/Transitions_of_Care/");
    	for(File transitionsOfCareExample : transitionsOfCareDir.listFiles()) {
    		System.out.println("Transforming "+transitionsOfCareExample.getPath());
    		FileInputStream fis = new FileInputStream(transitionsOfCareExample);
    		ClinicalDocument cda = CDAUtil.load(fis);
            ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
            Bundle bundle = ccdTransformer.transformDocument(cda);
            if(bundle != null) {
            	FHIRUtil.printJSON(bundle, "src/test/resources/output/Transitions_of_Care/"+transitionsOfCareExample.getName().replaceAll(".xml", "")+".json");
            	System.out.println("Result was written");
            }
    	}
    }
    
    
}
