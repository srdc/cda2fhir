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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;
import tr.com.srdc.cda2fhir.validation.IValidator;
import tr.com.srdc.cda2fhir.validation.ValidatorImpl;

public class ValidatorTest {

	@BeforeClass
	public static void init() {
		// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar
		// documents will not be recognised.
		// This has to be called before loading the document; otherwise will have no
		// effect.
		CDAUtil.loadPackages();
	}

	// 170.315_b1_toc_gold_sample2_v1.xml without profile
	@Test
	public void testGoldSampleBundleWithoutProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_gold_sample2_v1.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_gold_sample2_v1-wo-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-wo-profile-for-170.315_b1_toc_gold_sample2_v1.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, true);
	}

	// 170.315_b1_toc_gold_sample2_v1.xml with profile
	@Ignore
	public void testGoldSampleBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_gold_sample2_v1.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_gold_sample2_v1-w-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-w-profile-for-170.315_b1_toc_gold_sample2_v1.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// 170.315_b1_toc_inp_ccd_r21_sample1_v5.xml without profile
	@Ignore
	public void testInpSampleBundleWithoutProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_inp_ccd_r21_sample1_v5.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_inp_ccd_r21_sample1_v5-wo-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-wo-profile-for-170.315_b1_toc_inp_ccd_r21_sample1_v5.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// 170.315_b1_toc_inp_ccd_r21_sample1_v5.xml without profile
	@Ignore
	public void testInpSampleBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/170.315_b1_toc_inp_ccd_r21_sample1_v5.xml";
		String targetPathForFHIRResource = "src/test/resources/output/170.315_b1_toc_inp_ccd_r21_sample1_v5-w-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-w-profile-for-170.315_b1_toc_inp_ccd_r21_sample1_v5.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// C-CDA_R2-1_CCD.xml without DAF profile
	@Ignore
	public void testReferenceCCDBundleWithoutProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/C-CDA_R2-1_CCD.xml";
		String targetPathForFHIRResource = "src/test/resources/output/C-CDA_R2-1_CCD-wo-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-wo-profile-for-C-CDA_R2-1_CCD.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// C-CDA_R2-1_CCD.xml with provenance
	@Ignore
	public void testReferenceCCDBundleWithProvenance() throws Exception {
		String cdaResourcePath = "src/test/resources/C-CDA_R2-1_CCD.xml";
		String targetPathForFHIRResource = "src/test/resources/output/C-CDA_R2-1_CCD-w-provenance.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-w-provenance-for-C-CDA_R2-1_CCD.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, true);
	}

	// C-CDA_R2-1_CCD.xml with DAF profile
	@Ignore
	public void testReferenceCCDBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/C-CDA_R2-1_CCD.xml";
		String targetPathForFHIRResource = "src/test/resources/output/C-CDA_R2-1_CCD-w-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-w-profile-for-C-CDA_R2-1_CCD.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// Vitera_CCDA_SMART_Sample.xml without profile
	@Ignore
	public void testViteraBundleWithoutProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/Vitera_CCDA_SMART_Sample.xml";
		String targetPathForFHIRResource = "src/test/resources/output/Vitera_CCDA_SMART_Sample-wo-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-wo-profile-for-Vitera_CCDA_SMART_Sample.html";
		boolean generateDAFProfileMetadata = false;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// Vitera_CCDA_SMART_Sample.xml with profile
	@Ignore
	public void testViteraBundleWithProfile() throws Exception {
		String cdaResourcePath = "src/test/resources/Vitera_CCDA_SMART_Sample.xml";
		String targetPathForFHIRResource = "src/test/resources/output/Vitera_CCDA_SMART_Sample-w-profile-validation.xml";
		String targetPathForResultFile = "src/test/resources/output/validation-result-w-profile-for-Vitera_CCDA_SMART_Sample.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// HannahBanana_EpicCCD.xml
	@Ignore
	public void testHannahBanana() throws Exception {
		String cdaResourcePath = "src/test/resources/Epic/HannahBanana_EpicCCD-pretty.xml";
		String targetPathForFHIRResource = "src/test/resources/output/Epic/HannahBanana_EpicCCD-pretty.fhir.xml";
		String targetPathForResultFile = "src/test/resources/output/Epic/HannahBanana_EpicCCD-pretty.validation-result.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// robust CCD.XML
	@Ignore
	public void testRobust() throws Exception {
		String cdaResourcePath = "src/test/resources/Epic/robust CCD.XML";
		String targetPathForFHIRResource = "src/test/resources/output/Epic/robust CCD.fhir.xml";
		String targetPathForResultFile = "src/test/resources/output/Epic/robust CCD.validation-result.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	// Person-RAKIA_TEST_DOC0001 (1).xml
	@Ignore
	public void testRakia() throws Exception {
		String cdaResourcePath = "src/test/resources/Cerner/Person-RAKIA_TEST_DOC00001 (1).xml";
		String targetPathForFHIRResource = "src/test/resources/output/Cerner/Person-RAKIA_TEST_DOC00001 (1).fhir.xml";
		String targetPathForResultFile = "src/test/resources/output/Cerner/Person-RAKIA_TEST_DOC00001 (1).validation-result.html";
		boolean generateDAFProfileMetadata = true;
		transformAndValidate(cdaResourcePath, targetPathForFHIRResource, targetPathForResultFile,
				generateDAFProfileMetadata, false);
	}

	/**
	 * Transforms a CDA resource to a FHIR resource, validates the FHIR resource and
	 * prints the validation result to the target path.
	 *
	 * @param cdaResourcePath            A file path of the CDA resource that is to
	 *                                   be transformed
	 * @param targetPathForFHIRResource  A file path where the FHIR resource is to
	 *                                   be created
	 * @param targetPathForResultFile    A file path where the validation result
	 *                                   file is to be created
	 * @param generateDAFProfileMetadata A boolean indicating whether the generated
	 *                                   resources will include DAF profile
	 *                                   declarations in meta.profile
	 * @throws Exception
	 */
	private void transformAndValidate(String cdaResourcePath, String targetPathForFHIRResource,
			String targetPathForResultFile, boolean generateDAFProfileMetadata, boolean generateProvenance)
			throws Exception {
		IValidator validator = new ValidatorImpl();
		ByteArrayOutputStream os = null;

		// file to be transformed
		FileInputStream fis = new FileInputStream(cdaResourcePath);

		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());
		ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);

		// set whether DAF Profile URLs will be created in meta.profile of relevant
		// resources
		Config.setGenerateDafProfileMetadata(generateDAFProfileMetadata);

		Bundle bundle = null;

		if (generateProvenance) {

			// CDAUtil closes the stream, re-open it.
			FileInputStream fisStream = new FileInputStream(cdaResourcePath);
			byte[] buffer = new byte[10];
			StringBuilder sb = new StringBuilder();
			while (fisStream.read(buffer) != -1) {
				sb.append(new String(buffer));
				buffer = new byte[10];
			}
			fisStream.close();

			String content = sb.toString();

			Identifier assemblerDevice = new Identifier();
			assemblerDevice.setValue("Higgs");
			assemblerDevice.setSystem("http://www.amida.com");
			bundle = ccdTransformer.transformDocument(cda, BundleType.TRANSACTION, null, content, assemblerDevice);
		} else {
			bundle = ccdTransformer.transformDocument(cda, null, null);
		}

		Assert.assertNotNull(bundle);

		// print the bundle for checking against validation results
		// printed as XML, because HL7 FHIR Validator works with XML encoded resources
		FHIRUtil.printXML(bundle, targetPathForFHIRResource);
		os = (ByteArrayOutputStream) validator.validateBundle(bundle);

		// ValidationResult fileResult =
		// validator.validateFile(targetPathForFHIRResource);
		// Assert.assertNotNull(fileResult);
		// Assert.assertTrue(fileResult.isSuccessful()); TODO: Investigate and fix why
		// this is failing

		if (os != null) {
			File validationFile = new File(targetPathForResultFile);
			validationFile.getParentFile().mkdirs();

			FileOutputStream fos = new FileOutputStream(validationFile);
			os.writeTo(fos);
			os.close();
			fos.close();
		}
	}

}
