package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.formats.IParser;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.BundleTypeEnumFactory;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.dstu3.model.ProcessResponse;
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
	static String serverBase = hapiURL + "/baseDstu3";
	static BundleTypeEnumFactory bundleTypeEnumFactory;
	static FhirContext ctx;
	static IGenericClient client;
	@BeforeClass
    public static void init() throws IOException {
		
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
        ctx = FhirContext.forDstu3();
        bundleTypeEnumFactory = new BundleTypeEnumFactory();
        client = ctx.newRestfulGenericClient(serverBase);
    }
	
	@ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose.yaml")
            .waitingForService("hapi", HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat(hapiURL)))
            .build();
	
	 private static Bundle generateTransactionBundle(String sourceName, String patientId, Map<String, String> map) throws Exception {
		
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);
        
        CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        BundleType bt = bundleTypeEnumFactory.fromCode("transaction");
        Bundle bundle = ccdTransformer.transformDocument(cda, bt, patientId, map);
        bundle.setType(bt);
       
        return bundle;
    }
	 
	 private static Bundle generatePatientBundle(String sourceName) throws Exception {
			
	        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
	        ClinicalDocument cda = CDAUtil.load(fis);

	        CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
	        BundleType bt = bundleTypeEnumFactory.fromCode("transaction");
	        Bundle bundle = ccdTransformer.transformDocument(cda);
	        bundle.setType(bt);
	       
	        return bundle;
	 }
	 
	private static void validate(Bundle bundle) {
		FhirValidator validator = new FhirValidator(ctx);
    	ValidationResult result = validator.validateWithResult(bundle);
    	Assert.assertTrue(result.isSuccessful());
	}
    
	private Map<String, String> getResourceProfileMap(Bundle bundle) {
		HashMap<String, String> map = new HashMap<String, String>();
		
		for( BundleEntryComponent entry : bundle.getEntry()) {
			if(entry != null && !entry.getResource().getResourceType().name().equals("Patient")) {
				map.put(entry.getResource().getResourceType().name() ,"");
			}
		}
		
		return map;
	}

	private String rakiaPatientIntegration(Bundle bundle) throws Exception {
		
		List<Patient> patients = bundle.getEntry().stream()
				.map(r -> r.getResource())
				.filter(r -> (r instanceof Patient))
				.map(r -> (Patient) r)
				.collect(Collectors.toList());
		
    	Patient patient = patients.get(0);
    	
    	MethodOutcome outcome = client.create()
 			   .resource(patient)
 			   .prettyPrint()
 			   .encodedJson()
 			   .execute();
    	
    	Assert.assertTrue(outcome.getCreated());
    	return outcome.getResource().getIdElement().getIdPart();
	}
    @Test
	public void rakiaIntegration() throws Exception {
    	String sourceName = "Cerner/Person-RAKIA_TEST_DOC00001 (1).XML";
		Bundle patientBundle  = generatePatientBundle(sourceName);
		
		// Create bundle with patient and test patient creation
    	String id = rakiaPatientIntegration(patientBundle);
    	
		Map<String, String> resourceProfileMap = getResourceProfileMap(patientBundle);

    	// Generate transaction bundle sans patient entry
    	Bundle bundle = generateTransactionBundle(sourceName, id, resourceProfileMap); 
    
    	// Send transaction bundle to server.
    	Bundle resp = client.transaction().withBundle(bundle).execute();
    	
    	for(BundleEntryComponent entry : resp.getEntry()) {
    		BundleEntryResponseComponent entryResp =entry.getResponse();
    		
    		Assert.assertEquals(200, entryResp.getStatus());
    	}
    	
//    	for( BundleEntryComponent entry : bundle.getEntry()) {
//    		System.out.println(entry.getClass());
//    		Bundle results = (Bundle) client
//    	    	      .search()
//    	    	      .forResource((Class<? extends IBaseResource>) entry.getClass())
//    	    	      .prettyPrint()
//    	    	      .execute();
//    		
//    		Assert.assertEquals(1,results.getTotal());
//    	}
    	
	}
}
