package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
//import org.testng.annotations.DataProvider;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class IntegrationTest{
	static String hapiURL = "http://localhost:8080";
	@BeforeClass
    public static void init() throws IOException {
//		Process p = Runtime.getRuntime().exec("export PATH = ${}docker run -d -p 8080:8080 --name=hapi-fhir amidatech/amida-hapi-fhir-jpaserver-example:latest");

        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
	
	@ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose.yaml")
            .waitingForService("hapi", HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat(hapiURL)))
            .build();
	
	 private static Bundle generateBundle(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Config.setGenerateDafProfileMetadata(true);
        Config.setGenerateNarrative(true);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        return bundle;
    }
    
    @Test
	public void patientIntegration() throws Exception {
    	FhirContext ctx = FhirContext.forDstu3();
    	String serverBase = hapiURL + "/baseDstu3";
    	Bundle bundle = generateBundle("Cerner/Person-RAKslaIA_TEST_DOC00001 (1).XML"); 
    	List<Patient> patients = bundle.getEntry().stream()
				.map(r -> r.getResource())
				.filter(r -> (r instanceof Patient))
				.map(r -> (Patient) r)
				.collect(Collectors.toList());
    	Patient patient = patients.get(0);
    	IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    	
    	MethodOutcome outcome = client.create()
    			   .resource(patient)
    			   .prettyPrint()
    			   .encodedJson()
    			   .execute();
    	Assert.assertTrue(outcome.getCreated());
//    	
//    	// Perform a search
//    	Bundle results = client
//    	      .search()
//    	      .forResource(Patient.class)
//    	      .where(Patient.FAMILY.matches().value("duck"))
//    	      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
//    	      .execute();
	}
    
    
    
    @Test
    public void patientIntegration2(){
    	
    }
}
