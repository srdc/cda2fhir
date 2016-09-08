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

    // Gold Sample r2.1
    @Ignore
    public void testGoldSample() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/170.315_b1_toc_gold_sample2_v1.xml");

        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            FHIRUtil.printJSON(bundle, "src/test/resources/output/170.315_b1_toc_gold_sample2_v1.json");
    }

    // Vitera
    @Ignore
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
    // These instances are removed for the time being, as they are a bit old (i.e. not C-CDA 2.1)

    // HL7
    @Ignore
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

    // NIST
    @Ignore
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

}
