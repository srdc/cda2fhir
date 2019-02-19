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

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
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
    
    // Gold Sample r2.1
    @Test
    public void testGoldSample() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/170.315_b1_toc_gold_sample2_v1.xml");

        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Config.setGenerateDafProfileMetadata(true);
        Config.setGenerateNarrative(true);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            FHIRUtil.printJSON(bundle, "src/test/resources/output/170.315_b1_toc_gold_sample2_v1.json");
    }

//    // Inp Sample r2.1
//    @Test
//    public void testInpSample() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/170.315_b1_toc_inp_ccd_r21_sample1_v5.xml");
//
//        ClinicalDocument cda = CDAUtil.load(fis);
//        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
//        Config.setGenerateDafProfileMetadata(true);
//        Config.setGenerateNarrative(true);
//        Bundle bundle = ccdTransformer.transformDocument(cda);
//        if(bundle != null)
//            FHIRUtil.printJSON(bundle, "src/test/resources/output/170.315_b1_toc_inp_ccd_r21_sample1_v5.json");
//    }
//
//    // C-CDA_R2-1_CCD.xml - with DAF profile in meta.profile
//    @Test
//    public void testReferenceCCDInstance() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
//
//        ClinicalDocument cda = CDAUtil.load(fis);
//        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
//        Config.setGenerateDafProfileMetadata(true);
//        Config.setGenerateNarrative(true);
//        Bundle bundle = ccdTransformer.transformDocument(cda);
//        if(bundle != null) 
//        	FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-w-daf.json");
//    }
//
//    // C-CDA_R2-1_CCD.xml - without DAF profile in meta.profile
//    @Test
//    public void testReferenceCCDInstanceWithoutDAF() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
//
//        ClinicalDocument cda = CDAUtil.load(fis);
//        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
//        Config.setGenerateDafProfileMetadata(false);
//        Config.setGenerateNarrative(true);
//        Bundle bundle = ccdTransformer.transformDocument(cda);
//        if(bundle != null)
//            FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-wo-daf.json");
//    }
//
//    // C-CDA_R2-1_CCD.xml - without DAF profile in meta.profile and without narrative generated in resources
//    @Test
//    public void testReferenceCCDInstanceWithoutDAFAndNarrative() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
//
//        ClinicalDocument cda = CDAUtil.load(fis);
//        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
//        Config.setGenerateDafProfileMetadata(false);
//        Config.setGenerateNarrative(false);
//        Bundle bundle = ccdTransformer.transformDocument(cda);
//        if(bundle != null)
//            FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-wo-daf-narrative.json");
//    }
//
//    // Vitera
//    @Test
//    public void testViteraSample() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");
//
//        ClinicalDocument cda = CDAUtil.load(fis);
//        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
//        Config.setGenerateDafProfileMetadata(true);
//        Config.setGenerateNarrative(true);
//        Bundle bundle = ccdTransformer.transformDocument(cda);
//        if(bundle != null)
//            FHIRUtil.printJSON(bundle, "src/test/resources/output/Vitera_CCDA_SMART_Sample.json");
//    }

}
