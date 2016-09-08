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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;
import tr.com.srdc.cda2fhir.validation.IValidator;
import tr.com.srdc.cda2fhir.validation.ValidatorImpl;

public class ValidatorTest {

	@BeforeClass
	public static void init() {
		// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
		// This has to be called before loading the document; otherwise will have no effect.
		CDAUtil.loadPackages();
	}

	// C-CDA_R2-1_CCD.xml with DAF profile
	@Test
	public void testReferenceCCDBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/C-CDA_R2-1_CCD.xml";
		String targetPathForFHIRResource = "src/test/resources/output/C-CDA_R2-1_CCD-for-profile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-profile-for-C-CDA_R2-1_CCD.html";
		boolean validateProfile = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// C-CDA_R2-1_CCD.xml without DAF profile
	@Test
	public void testReferenceCCDBundleWithoutProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/C-CDA_R2-1_CCD.xml";
		String targetPathForFHIRResource = "src/test/resources/output/C-CDA_R2-1_CCD-for-nonprofile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-nonprofile-for-C-CDA_R2-1_CCD.html";
		boolean validateProfile = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// Vitera_CCDA_SMART_Sample.xml with profile
	@Ignore
	public void testViteraBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/Vitera_CCDA_SMART_Sample.xml";
		String targetPathForFHIRResource = "src/test/resources/output/Vitera_CCDA_SMART_Sample-for-profile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-profile-for-Vitera_CCDA_SMART_Sample.html";
		boolean validateProfile = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// Vitera_CCDA_SMART_Sample.xml without profile
	@Ignore
	public void testViteraBundleWithoutProfile() throws Exception {	
		String cdaResourcePath = "src/test/resources/Vitera_CCDA_SMART_Sample.xml";
		String targetPathForFHIRResource = "src/test/resources/output/Vitera_CCDA_SMART_Sample-for-nonprofile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-nonprofile-for-Vitera_CCDA_SMART_Sample.html";
		boolean validateProfile = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// 170.315_b1_toc_gold_sample2_v1.xml with profile
	@Ignore
	public void testGoldSampleBundleWithProfile() throws Exception {	
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_gold_sample2_v1.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_gold_sample2_v1-for-profile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-profile-for-170.315_b1_toc_gold_sample2_v1.html";
		boolean validateProfile = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// 170.315_b1_toc_gold_sample2_v1.xml without profile
	@Ignore
	public void testGoldSampleBundleWithoutProfile() throws Exception {	
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_gold_sample2_v1.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_gold_sample2_v1-for-nonprofile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-nonprofile-for-170.315_b1_toc_gold_sample2_v1.html";
		boolean validateProfile = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// 170.315_b1_toc_inp_ccd_r21_sample1_v5.xml without profile
	@Ignore
	public void testSampleBundleWithoutProfile() throws Exception {	
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_inp_ccd_r21_sample1_v5.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_inp_ccd_r21_sample1_v5-for-nonprofile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-nonprofile-for-170.315_b1_toc_inp_ccd_r21_sample1_v5.html";
		boolean validateProfile = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	// 170.315_b1_toc_inp_ccd_r21_sample1_v5.xml without profile
	@Ignore
	public void testSampleBundleWithProfile() throws Exception {	
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_inp_ccd_r21_sample1_v5.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_inp_ccd_r21_sample1_v5-for-profile-validation.json";
		String targetPathForResultFile = "src/test/resources/output/validation-result-profile-for-170.315_b1_toc_inp_ccd_r21_sample1_v5.html";
		boolean validateProfile = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile, validateProfile);
	}
	
	/**
	 * Transforms a CDA resource to a FHIR resource, validates the FHIR resource and prints the validation result to the target path.
	 * @param cdaResourcePath A file path of the CDA resource that is to be transformed
	 * @param targetPathForFHIRResource A file path where the FHIR resource is to be created
	 * @param targetPathForResultFile A file path where the validation result file is to be created
	 * @param validateProfile A boolean indicating that the validation will be done using daf profile
	 * @throws Exception
	 */
	private void transformAndValidate(String cdaResourcePath, String targetPathForFHIRResource, String targetPathForResultFile, boolean validateProfile) throws Exception {
		IValidator validator = new ValidatorImpl();
		java.io.ByteArrayOutputStream os = null;
		
		// file to be transformed
		FileInputStream fis = new FileInputStream(cdaResourcePath);

        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        
        // make the transformation
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null) {
			// print the bundle for checking against validation results
			FHIRUtil.printJSON(bundle, targetPathForFHIRResource);
			os = (java.io.ByteArrayOutputStream) validator.validateBundle(bundle, validateProfile);
		}
        
        if(os != null) {
        	java.io.File validationFile = new java.io.File(targetPathForResultFile);
        	if(!validationFile.getParentFile().exists())
        		validationFile.getParentFile().mkdirs();

			FileOutputStream fos = new FileOutputStream(new File(targetPathForResultFile));
			os.writeTo(fos);
			os.close();
			fos.close();
        }
	}

}
