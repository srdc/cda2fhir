package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.testutil.OrganizationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AuthorParticipationTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/AuthorParticipation/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareOrganizations(String caseName, Organization organization,
			Map<String, Object> joltOrganization) throws Exception {
		Assert.assertNotNull("Jolt organization", joltOrganization);
		Assert.assertNotNull("Jolt organization id", joltOrganization.get("id"));
		joltOrganization.put("id", organization.getIdElement().getIdPart()); // ids do not have to match

		String joltOrganizationJson = JsonUtils.toPrettyJsonString(joltOrganization);
		File joltOrganizationFile = new File(OUTPUT_PATH + caseName + "JoltOrganization.json");
		FileUtils.writeStringToFile(joltOrganizationFile, joltOrganizationJson, Charset.defaultCharset());

		String organizationJson = FHIRUtil.encodeToJSON(organization);
		JSONAssert.assertEquals("Jolt organization", organizationJson, joltOrganizationJson, true);
	}

	private static void comparePractitioners(String caseName, Practitioner practitioner,
			Map<String, Object> joltPractitioner) throws Exception {
		Assert.assertNotNull("Jolt practitioner", joltPractitioner);
		Assert.assertNotNull("Jolt practitioner id", joltPractitioner.get("id"));
		joltPractitioner.put("id", practitioner.getIdElement().getIdPart()); // ids do not have to match

		String joltPractitionerJson = JsonUtils.toPrettyJsonString(joltPractitioner);
		File joltPractitionerFile = new File(OUTPUT_PATH + caseName + "JoltPractitioner.json");
		FileUtils.writeStringToFile(joltPractitionerFile, joltPractitionerJson, Charset.defaultCharset());

		String practitionerJson = FHIRUtil.encodeToJSON(practitioner);
		JSONAssert.assertEquals("Jolt practitioner", practitionerJson, joltPractitionerJson, true);
	}

	@SuppressWarnings("unchecked")
	private static void checkReference(Map<String, Object> resource, String path, String id) {
		Map<String, Object> parent = (Map<String, Object>) resource.get(path);
		Assert.assertNotNull("Jolt role " + path, parent);
		String actualId = (String) parent.get("reference");
		Assert.assertEquals("Id for " + path, id, actualId);
	}

	private static void compareRoles(String caseName, PractitionerRole practitionerRole,
			Map<String, Object> joltPractitionerRole, String practitionerId, String organizationId) throws Exception {
		Assert.assertNotNull("Jolt practitioner", joltPractitionerRole);
		Assert.assertNotNull("Jolt practitioner id", joltPractitionerRole.get("id"));
		joltPractitionerRole.put("id", practitionerRole.getIdElement().getIdPart()); // ids do not have to match

		String joltPractitionerJson = JsonUtils.toPrettyJsonString(joltPractitionerRole);
		File joltPractitionerFile = new File(OUTPUT_PATH + caseName + "JoltPractitionerRole.json");
		FileUtils.writeStringToFile(joltPractitionerFile, joltPractitionerJson, Charset.defaultCharset());

		checkReference(joltPractitionerRole, "practitioner", practitionerId);
		checkReference(joltPractitionerRole, "organization", organizationId);
	}

	private static File writeAuthorAsXML(String caseName, Author author) throws Exception {
		File xmlFile = new File(OUTPUT_PATH + caseName + ".xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(author, fw);
		fw.close();
		return xmlFile;
	}

	private static List<Object> findJoltResult(File xmlFile, String caseName) throws Exception {
		OrgJsonUtil jsonUtil = OrgJsonUtil.readXML(xmlFile.toString());
		JSONObject authorJson = jsonUtil.getJSONObject();
		JSONObject assignedAuthorObject = authorJson.getJSONObject("author");
		File assignedAuthorJsonFile = new File(OUTPUT_PATH + caseName + ".json");
		FileUtils.writeStringToFile(assignedAuthorJsonFile, assignedAuthorObject.toString(4), Charset.defaultCharset());

		List<Object> joltResult = TransformManager.transformEntryInFile("AuthorParticipation",
				assignedAuthorJsonFile.toString());
		return joltResult;
	}

	private static void runTest(AuthorGenerator authorGenerator, String caseName) throws Exception {
		Author author = authorGenerator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntityResult cda2FhirResult = rt.tAuthor2Practitioner(author, new BundleInfo(rt));

		Practitioner practitioner = cda2FhirResult.getPractitioner();
		PractitionerRole practitionerRole = cda2FhirResult.getPractitionerRole();
		Organization organization = cda2FhirResult.getOrganization();

		authorGenerator.verify(practitioner);
		authorGenerator.verify(practitionerRole);
		authorGenerator.verify(organization);

		File xmlFile = writeAuthorAsXML(caseName, author);

		FHIRUtil.printJSON(practitioner, OUTPUT_PATH + caseName + "CDA2FHIRPractitioner.json");
		if (practitionerRole != null) {
			FHIRUtil.printJSON(practitionerRole, OUTPUT_PATH + caseName + "CDA2FHIRPractitionerRole.json");
		}
		if (organization != null) {
			FHIRUtil.printJSON(organization, OUTPUT_PATH + caseName + "CDA2FHIROrganization.json");
		}

		List<Object> joltResult = findJoltResult(xmlFile, caseName);

		Map<String, Object> joltPractitioner = TransformManager.chooseResource(joltResult, "Practitioner");
		String practitionerId = "Practitioner/" + joltPractitioner.get("id");
		comparePractitioners(caseName, practitioner, joltPractitioner);

		Map<String, Object> joltOrganization = TransformManager.chooseResource(joltResult, "Organization");
		String organizationId = null;
		if (organization == null) {
			Assert.assertNull("Jolt organization", joltOrganization);
		} else {
			organizationId = "Organization/" + joltOrganization.get("id");
			compareOrganizations(caseName, organization, joltOrganization);
		}

		Map<String, Object> joltPractitionerRole = TransformManager.chooseResource(joltResult, "PractitionerRole");
		if (practitionerRole == null) {
			Assert.assertNull("Jolt practitioner role", joltPractitionerRole);
		} else {
			compareRoles(caseName, practitionerRole, joltPractitionerRole, practitionerId, organizationId);
		}
	}

	@Test
	public void testDefault() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();
		runTest(authorGenerator, "defaultCase");
	}

	@Test
	public void testFull() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getFullInstance();
		runTest(authorGenerator, "fullCase");
	}

	@Test
	public void testDefaultNameNullFlavor() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();
		authorGenerator.getPNGenerator().setNullFlavor();
		runTest(authorGenerator, "nameNullFlavorCase");
	}

	@Test
	public void testNullFlavoredOrganization() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();
		OrganizationGenerator og = authorGenerator.getOrganizationGenerator();
		og.setNullFlavor();

		runTest(authorGenerator, "orgNullFlavorCase");
	}
}
