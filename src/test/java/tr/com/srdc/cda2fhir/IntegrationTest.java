
package tr.com.srdc.cda2fhir;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Provenance;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
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
	public void hannahIntegration() throws Exception {

		String sourceName = "Epic/HannahBanana_EpicCCD-pretty.xml";
		String documentBody = "<ClinicalDoc>\n</ClinicalDoc>";
		Identifier assemblerDevice = new Identifier();
		assemblerDevice.setValue("Higgs");
		assemblerDevice.setSystem("http://www.amida.com");
		// create transaction bundle from ccda bundle

		Bundle transactionBundle = ccdTransformer.transformDocument("src/test/resources/" + sourceName,
				BundleType.TRANSACTION, null, documentBody, assemblerDevice);

		// print pre-post bundle
		FHIRUtil.printJSON(transactionBundle, "src/test/resources/output/HannahBanana_EpicCCD-pretty.json");

		// Send transaction bundle to server.
		Bundle resp = client.transaction().withBundle(transactionBundle).execute();

		for (BundleEntryComponent entry : resp.getEntry()) {
			BundleEntryResponseComponent entryResp = entry.getResponse();
			Assert.assertEquals("201 Created", entryResp.getStatus());
		}

		// Re-Send the same transactional bundle to server to test ifNoneExist.
		client.transaction().withBundle(transactionBundle).execute();

		// 1 Patient
		Bundle patientResults = (Bundle) client.search().forResource(Patient.class).prettyPrint().execute();
		Assert.assertEquals(1, patientResults.getTotal());

		// 1 Med request for esomeprazole
		Bundle medicationRequest = (Bundle) client.search().forResource(MedicationRequest.class).prettyPrint()
				.execute();
		Assert.assertEquals(1, medicationRequest.getTotal());

		// 1 Medication statement, esomeprazole
		Bundle medicationStatement = (Bundle) client.search().forResource(MedicationStatement.class).prettyPrint()
				.execute();
		Assert.assertEquals(1, medicationStatement.getTotal());

		// 2 Conditions (4 problem list, 1 imm, 1 med, 1 encounter diagnosis)
		Bundle conditionResults = (Bundle) client.search().forResource(Condition.class).prettyPrint().execute();
		Assert.assertEquals(2, conditionResults.getTotal());

		// 1 Immunization
		Bundle immunizationResults = (Bundle) client.search().forResource(Immunization.class).prettyPrint().execute();
		Assert.assertEquals(1, immunizationResults.getTotal());

		// 1 Allergy
		Bundle allergyResults = (Bundle) client.search().forResource(AllergyIntolerance.class).prettyPrint().execute();
		Assert.assertEquals(1, allergyResults.getTotal());

		// 1 Encounter
		Bundle encounterResults = (Bundle) client.search().forResource(Encounter.class).prettyPrint().execute();
		Assert.assertEquals(1, encounterResults.getTotal());

		// 5 Observations (5 Vitals)
		Bundle observationResults = (Bundle) client.search().forResource(Observation.class).prettyPrint().execute();
		Assert.assertEquals(5, observationResults.getTotal());

		// 1 medication
		Bundle medicationResults = (Bundle) client.search().forResource(Medication.class).prettyPrint().execute();
		Assert.assertEquals(1, medicationResults.getTotal());

		// 2 practitioners
		Bundle practitionerResults = (Bundle) client.search().forResource(Practitioner.class).prettyPrint().execute();
		Assert.assertEquals(2, practitionerResults.getTotal());

		// 2 practitioner roles (re-enable once afsin's fixes are merged).
		// Bundle practitionerRoleResults = (Bundle)
		// client.search().forResource(PractitionerRole.class).prettyPrint()
		// .execute();
		// Assert.assertEquals(2, practitionerRoleResults.getTotal());

		// 2 devices
		Bundle deviceResults = (Bundle) client.search().forResource(Device.class).prettyPrint().execute();
		Assert.assertEquals(2, deviceResults.getTotal());

		// 1 location
		Bundle locationResults = (Bundle) client.search().forResource(Location.class).prettyPrint().execute();
		Assert.assertEquals(1, locationResults.getTotal());

		/**
		 * Document Reference will not de-duplicate, no query mechanism to hash.
		 **/
		Bundle documentReferenceResults = (Bundle) client.search().forResource(DocumentReference.class).prettyPrint()
				.execute();
		Assert.assertEquals(2, documentReferenceResults.getTotal());

		/**
		 * These should duplicate for attribution purposes.
		 **/
		Bundle provenanceResults = (Bundle) client.search().forResource(Provenance.class).prettyPrint().execute();
		Assert.assertEquals(2, provenanceResults.getTotal());

		Bundle compositionResults = (Bundle) client.search().forResource(Composition.class).prettyPrint().execute();
		Assert.assertEquals(2, compositionResults.getTotal());

	}

	@Ignore
	public void CCDIntegration() throws Exception {

		String sourceName = "Epic/HannahBanana_EpicCCD-pretty.xml";
		String documentBody = "<ClinicalDoc>\n</ClinicalDoc>";
		Identifier assemblerDevice = new Identifier();
		assemblerDevice.setValue("Higgs");
		assemblerDevice.setSystem("http://www.amida.com");
		// create transaction bundle from ccda bundle

		Bundle transactionBundle = ccdTransformer.transformDocument("src/test/resources/" + sourceName,
				BundleType.TRANSACTION, null, documentBody, assemblerDevice);

		// print pre-post bundle
		FHIRUtil.printJSON(transactionBundle, "src/test/resources/output/C-CDA_R2-1_CCD.json");

		// Send transaction bundle to server.
		Bundle resp = client.transaction().withBundle(transactionBundle).execute();

		for (BundleEntryComponent entry : resp.getEntry()) {
			BundleEntryResponseComponent entryResp = entry.getResponse();
			Assert.assertEquals("201 Created", entryResp.getStatus());
		}

		// Re-Send the same transactional bundle to server to test ifNoneExist.
		Bundle respTwo = client.transaction().withBundle(transactionBundle).execute();

		// 1 Patient
		Bundle patientResults = (Bundle) client.search().forResource(Patient.class).prettyPrint().execute();
		Assert.assertEquals(1, patientResults.getTotal());

		// 1 Med dispense for proventil
		// Bundle medicationDispense = (Bundle)
		// client.search().forResource(MedicationDispense.class).prettyPrint()
		// .execute();
		// Assert.assertEquals(1, medicationDispense.getTotal());

		// 1 Med request for esomeprazole
		Bundle medicationRequest = (Bundle) client.search().forResource(MedicationRequest.class).prettyPrint()
				.execute();
		Assert.assertEquals(1, medicationRequest.getTotal());

		// 1 Medication statement, esomeprazole
		Bundle medicationStatement = (Bundle) client.search().forResource(MedicationStatement.class).prettyPrint()
				.execute();
		Assert.assertEquals(1, medicationStatement.getTotal());

		// 2 Conditions (4 problem list, 1 imm, 1 med, 1 encounter diagnosis)
		Bundle conditionResults = (Bundle) client.search().forResource(Condition.class).prettyPrint().execute();
		Assert.assertEquals(2, conditionResults.getTotal());

		// 1 Immunization
		Bundle immunizationResults = (Bundle) client.search().forResource(Immunization.class).prettyPrint().execute();
		Assert.assertEquals(1, immunizationResults.getTotal());

		// 1 Allergy
		Bundle allergyResults = (Bundle) client.search().forResource(AllergyIntolerance.class).prettyPrint().execute();
		Assert.assertEquals(1, allergyResults.getTotal());

		// 1 Encounter
		Bundle encounterResults = (Bundle) client.search().forResource(Encounter.class).prettyPrint().execute();
		Assert.assertEquals(1, encounterResults.getTotal());

		// 2 Diagnostic Reports (1 obs on 1, 5 obs on 2)
		// Bundle reportResults = (Bundle)
		// client.search().forResource(DiagnosticReport.class).prettyPrint().execute();
		// Assert.assertEquals(2, reportResults.getTotal());

		// 5 vitals
		// 15 Observations (5 Labs, 8 Vitals, 2 allergy reaction obs,
		Bundle observationResults = (Bundle) client.search().forResource(Observation.class).prettyPrint().execute();
		// Assert.assertEquals(15, observationResults.getTotal());

		// 1 Procedure - 3 total but only 1 in supported format.
		Bundle procedureResults = (Bundle) client.search().forResource(Procedure.class).prettyPrint().execute();
		// Assert.assertEquals(1, procedureResults.getTotal());

		// 4 medications 1 med
		Bundle medicationResults = (Bundle) client.search().forResource(Medication.class).prettyPrint().execute();
		// Assert.assertEquals(4, medicationResults.getTotal());

		// 9 practitioners
		Bundle practitionerResults = (Bundle) client.search().forResource(Practitioner.class).prettyPrint().execute();
		// Assert.assertEquals(9, practitionerResults.getTotal());

		// TODO: prac role, composition, device, doc ref, location, provenance.

	}

	@Ignore
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
		FHIRUtil.printJSON(transactionBundle, "src/test/resources/output/rakia-4-17.json");

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

		Bundle docRefresults = (Bundle) client.search().forResource(DocumentReference.class).prettyPrint().execute();

		Bundle deviceResults = (Bundle) client.search().forResource(Device.class).prettyPrint().execute();

		Bundle organizationResults = (Bundle) client.search().forResource(Organization.class).prettyPrint().execute();

		Assert.assertEquals(1, patientResults.getTotal());
		Assert.assertEquals(32, practitionerResults.getTotal());
		Assert.assertEquals(13, medicationResults.getTotal());
		Assert.assertEquals(1, provenanceResults.getTotal());
		Assert.assertEquals(1, docRefresults.getTotal());
		Assert.assertEquals(2, deviceResults.getTotal());
		Assert.assertEquals(1, organizationResults.getTotal());
	}
}
