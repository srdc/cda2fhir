package tr.com.srdc.cda2fhir;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Organization.OrganizationContactComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.codesystems.ContactentityType;
import org.hl7.fhir.dstu3.model.codesystems.OrganizationType;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentType;
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
		// create transaction bundle from ccda bundle

		// Consider extract into its own function later if needed.
		Organization org = new Organization();
		org.setName("Aperture Science");
		org.setActive(true);

		Coding typeCoding = new Coding(OrganizationType.BUS.getSystem(), OrganizationType.BUS.toCode(),
				OrganizationType.BUS.getDisplay());
		org.addType(new CodeableConcept().addCoding(typeCoding));

		OrganizationContactComponent occ = new OrganizationContactComponent();
		Coding purposeCoding = new Coding(ContactentityType.ADMIN.getSystem(), ContactentityType.ADMIN.toCode(),
				ContactentityType.ADMIN.getDisplay());
		occ.setPurpose(new CodeableConcept().addCoding(purposeCoding));

		Address address = new Address();
		address.addLine("A place");
		address.setCity("Unknown");
		address.setState("Arizona");
		address.setPostalCode("99999");
		occ.setAddress(address);

		org.addContact(occ);

		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		pac.setId(org.getId());

		Coding agentTypeCoding = new Coding(ProvenanceAgentType.ORGANIZATION.getSystem(),
				ProvenanceAgentType.ORGANIZATION.toCode(), ProvenanceAgentType.ORGANIZATION.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentTypeCoding));

		Coding agentRoleCoding = new Coding(ProvenanceAgentRole.ASSEMBLER.getSystem(),
				ProvenanceAgentRole.ASSEMBLER.toCode(), ProvenanceAgentRole.ASSEMBLER.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentRoleCoding));

		Provenance transactionProvenance = new Provenance();
		transactionProvenance.addAgent(pac);
		transactionProvenance.addTarget(new Reference(org));

		Bundle transactionBundle = ccdTransformer.transformDocument("src/test/resources/" + sourceName,
				BundleType.TRANSACTION, null, transactionProvenance);

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

		Assert.assertEquals(1, patientResults.getTotal());
		Assert.assertEquals(18, practitionerResults.getTotal());
		Assert.assertEquals(14, medicationResults.getTotal());

	}

}
