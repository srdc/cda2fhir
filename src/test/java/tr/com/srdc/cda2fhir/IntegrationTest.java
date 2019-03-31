package tr.com.srdc.cda2fhir;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Provenance;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;
import tr.com.srdc.cda2fhir.validation.ValidatorImpl;

public class IntegrationTest {
	static String hapiURL = "http://localhost:1137";
	static String serverBase = hapiURL + "/fhir";
	static FhirContext ctx;
	static IGenericClient client;
	static CCDTransformerImpl ccdTransformer;
	static Logger logger;

	@BeforeClass
	public static void init() throws IOException {

		// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar
		// documents will not be recognised.
		// This has to be called before loading the document; otherwise will have no
		// effect.
		CDAUtil.loadPackages();
		ctx = FhirContext.forDstu3();
		client = ctx.newRestfulGenericClient(serverBase);
		ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		logger = LoggerFactory.getLogger(ValidatorImpl.class);
	}

	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.builder().file("src/test/resources/docker-compose.yaml")
			.waitingForService("hapi", HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat(hapiURL))).build();

	@Test
	public void rakiaIntegration() throws Exception {
		String sourceName = "Cerner/Person-RAKIA_TEST_DOC00001 (1).XML";
		String documentBody = "<ClinicalDoc>\n</ClinicalDoc>";
		Identifier assemblerDevice = new Identifier();
		assemblerDevice.setValue("Higgs");
		assemblerDevice.setSystem("http://www.amida.com");
		// create transaction bundle from ccda bundle

		Bundle transactionBundle = ccdTransformer.transformDocument("src/test/resources/" + sourceName,
				BundleType.TRANSACTION, null, documentBody, assemblerDevice);

		// print pre-post bundle
		FHIRUtil.printJSON(transactionBundle, "src/test/resources/output/rakia_bundle.json");

		// Send transaction bundle to server.
		Bundle resp = client.transaction().withBundle(transactionBundle).execute();

		for (BundleEntryComponent entry : resp.getEntry()) {
			BundleEntryResponseComponent entryResp = entry.getResponse();

			Assert.assertEquals("201 Created", entryResp.getStatus());
		}

		Bundle patientResults = (Bundle) client.search().forResource(Patient.class).prettyPrint().execute();

		Bundle practitionerResults = (Bundle) client.search().forResource(Practitioner.class).prettyPrint().execute();

		Bundle medicationResults = (Bundle) client.search().forResource(Medication.class).prettyPrint().execute();

		Bundle provenanceResults = (Bundle) client.search().forResource(Provenance.class).prettyPrint().execute();

		Bundle binaryresults = (Bundle) client.search().forResource(Binary.class).prettyPrint().execute();

		Bundle deviceResults = (Bundle) client.search().forResource(Device.class).prettyPrint().execute();

		Assert.assertEquals(1, patientResults.getTotal());
		Assert.assertEquals(19, practitionerResults.getTotal());
		Assert.assertEquals(28, medicationResults.getTotal());
		Assert.assertEquals(1, provenanceResults.getTotal());
		Assert.assertEquals(1, binaryresults.getTotal());
		Assert.assertEquals(1, deviceResults.getTotal());
	}
}
