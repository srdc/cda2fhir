package tr.com.srdc.cda2fhir;

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

	@Test
	public void testBundleWithProfile() throws Exception {
		IValidator validator = new ValidatorImpl();
		java.io.ByteArrayOutputStream os = null;
		
		// file to be transformed
		FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        
        // make the transformation
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null) {
			// print the bundle for checking against validation results
			FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-for-profile-validation.json");
			os = (java.io.ByteArrayOutputStream) validator.validateBundle(bundle, true);
		}
        
        if(os != null) {
        	java.io.File validationFileDir = new java.io.File("src/test/resources/output/");
        	if(!validationFileDir.exists())
        		validationFileDir.mkdirs();

			FileOutputStream fos = new FileOutputStream(new File("src/test/resources/output/validation-result-profile.html"));
			os.writeTo(fos);
			os.close();
			fos.close();
        }
	}
	
	@Test
	public void testBundleWithoutProfile() throws Exception {
		IValidator validator = new ValidatorImpl();
		java.io.ByteArrayOutputStream os = null;
		
		// file to be transformed
		FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        
        // make the transformation
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null) {
			// print the bundle for checking against validation results
			FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-for-nonprofile-validation.json");
			os = (java.io.ByteArrayOutputStream) validator.validateBundle(bundle, false);
		}
        
        if(os != null) {
        	java.io.File validationFileDir = new java.io.File("src/test/resources/output/");
        	if(!validationFileDir.exists())
        		validationFileDir.mkdirs();

			FileOutputStream fos = new FileOutputStream(new File("src/test/resources/output/validation-result-nonprofile.html"));
			os.writeTo(fos);
			os.close();
			fos.close();
        }
	}
}
