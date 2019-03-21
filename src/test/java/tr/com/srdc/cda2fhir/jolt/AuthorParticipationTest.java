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

import tr.com.srdc.cda2fhir.testutil.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
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

	@Test
	public void testDefault() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();
		Author author = authorGenerator.generate(factories);

		IEntityResult cda2FhirResult = rt.tAuthor2Practitioner(author, new BundleInfo(rt));

		Practitioner practitioner = cda2FhirResult.getPractitioner();
		PractitionerRole practitionerRole = cda2FhirResult.getPractitionerRole();
		Organization organization = cda2FhirResult.getOrganization();

		authorGenerator.verify(practitioner);
		authorGenerator.verify(practitionerRole);
		authorGenerator.verify(organization);

		File xmlFile = new File(OUTPUT_PATH + "defaultCase.xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(author, fw);
		fw.close();

		FHIRUtil.printJSON(practitioner, OUTPUT_PATH + "defaultCaseCDA2FHIRPractitioner.json");
		FHIRUtil.printJSON(practitionerRole, OUTPUT_PATH + "defaultCaseCDA2FHIRPractitionerRole.json");
		FHIRUtil.printJSON(organization, OUTPUT_PATH + "defaultCaseCDA2FHIROrganization.json");

		OrgJsonUtil jsonUtil = OrgJsonUtil.readXML(xmlFile.toString());
		JSONObject authorJson = jsonUtil.getJSONObject();
		JSONObject assignedAuthorObject = authorJson.getJSONObject("author");
		File assignedAuthorJsonFile = new File(OUTPUT_PATH + "defaultCase.json");
		FileUtils.writeStringToFile(assignedAuthorJsonFile, assignedAuthorObject.toString(4), Charset.defaultCharset());

		List<Object> joltResult = TransformManager.transformEntryInFile("AuthorParticipation",
				assignedAuthorJsonFile.toString());

		Map<String, Object> joltPractitionerResult = TransformManager.chooseResource(joltResult, "Practitioner");
		Assert.assertNotNull("Jolt Practitioner", joltPractitionerResult);
		Assert.assertNotNull("Jolt Practitioner id", joltPractitionerResult.get("id"));
		joltPractitionerResult.put("id", practitioner.getIdElement().getIdPart()); // ids do not have to match
		String joltPractitionerJson = JsonUtils.toPrettyJsonString(joltPractitionerResult);
		File joltPractitionerFile = new File(OUTPUT_PATH + "defaultCaseJoltPractitioner.json");
		FileUtils.writeStringToFile(joltPractitionerFile, joltPractitionerJson, Charset.defaultCharset());

		String practitionerJson = FHIRUtil.encodeToJSON(practitioner);
		JSONAssert.assertEquals("jolt practitioner output", practitionerJson, joltPractitionerJson, true);

		Map<String, Object> joltOrganizationResult = TransformManager.chooseResource(joltResult, "Organization");
		Assert.assertNotNull("Jolt Organization", joltOrganizationResult);
		Assert.assertNotNull("Jolt Organization id", joltOrganizationResult.get("id"));
		joltOrganizationResult.put("id", organization.getIdElement().getIdPart()); // ids do not have to match
		String joltOrganizationJson = JsonUtils.toPrettyJsonString(joltOrganizationResult);
		File joltOrganizationFile = new File(OUTPUT_PATH + "defaultCaseJoltOrganization.json");
		FileUtils.writeStringToFile(joltOrganizationFile, joltOrganizationJson, Charset.defaultCharset());

		String organizationJson = FHIRUtil.encodeToJSON(organization);
		JSONAssert.assertEquals("jolt practitioner output", organizationJson, joltOrganizationJson, true);

	}
}
