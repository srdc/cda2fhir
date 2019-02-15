package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.formats.IParser;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.BundleTypeEnumFactory;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
//import org.testng.annotations.DataProvider;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;
import ca.uhn.fhir.parser.JsonParser;

public class IntegrationTest{
	static String hapiURL = "http://localhost:8080";
	static BundleTypeEnumFactory bundleTypeEnumFactory;
	@BeforeClass
    public static void init() throws IOException {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
        bundleTypeEnumFactory = new BundleTypeEnumFactory();
    }
	
//	@ClassRule
//    public static DockerComposeRule docker = DockerComposeRule.builder()
//            .file("src/test/resources/docker-compose.yaml")
//            .waitingForService("hapi", HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat(hapiURL)))
//            .build();
	
	 private static Bundle generateBundle(String sourceName) throws Exception {
		
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        BundleType bt = bundleTypeEnumFactory.fromCode("transaction");
        Bundle bundle = ccdTransformer.transformDocument(cda, bt, null, new HashMap<String, String>());
        
        bundle.setType(bt);
       
        return bundle;
    }
    

	 
    @Test
	public void patientIntegration() throws Exception {
//    	IParser outputParser;
    	FhirContext ctx = FhirContext.forDstu3();
     	String serverBase = hapiURL + "/baseDstu3";
    	Bundle bundle = generateBundle("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML"); 
//    	outputParser = (IParser) ctx.newJsonParser();
    	FhirValidator validator = new FhirValidator(ctx);
    	ValidationResult result = validator.validateWithResult(bundle);
    	Assert.assertTrue(result.isSuccessful());
    	IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    	
    	Bundle outcome = client.transaction().withBundle(bundle).execute();  	
//    	// Perform a search
    	Bundle results = (Bundle) client
    	      .search()
    	      .forResource(Bundle.class)
    	      .prettyPrint()
    	      .execute();
    	
  
//    	System.out.println(jp.);
    	Assert.assertEquals(1, results.getTotal());
	}
}
