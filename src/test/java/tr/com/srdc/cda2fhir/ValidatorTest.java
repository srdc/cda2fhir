package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;
import tr.com.srdc.cda2fhir.validation.IValidator;
import tr.com.srdc.cda2fhir.validation.ValidatorImpl;

public class ValidatorTest {
	@Test
	public void testValidate() throws Exception {
		IValidator validator = new ValidatorImpl();
		java.io.ByteArrayOutputStream os = null;
		
		// file to be transformed
		FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        ClinicalDocument cda = CDAUtil.load(fis);
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        
        // make the transformation
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            os = (java.io.ByteArrayOutputStream)validator.validateBundle(bundle, true);
        
        if(os != null) {
        	java.io.File validationFileDir = new java.io.File("src/test/validation/");
        	if(!validationFileDir.exists())
        		validationFileDir.mkdirs();
        	
        	java.io.File validationFile = new java.io.File("src/test/validation/validation-result.html");
        	java.io.FileWriter validationFileWriter = new java.io.FileWriter(validationFile);
        	validationFileWriter.write(os.toString());
        	validationFileWriter.close();
        	os.close();
        }
	}
}
