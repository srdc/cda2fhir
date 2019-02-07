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
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class IntegrationTest{
	@BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
	
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
    	String serverBase = "http://localhost:8080/baseDstu3";
    	Bundle bundle = generateBundle("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML"); 
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
    	System.out.println(outcome.toString());
	}
}
