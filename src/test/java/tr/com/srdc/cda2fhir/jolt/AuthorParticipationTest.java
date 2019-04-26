package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ADGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.OrganizationGenerator;
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

		File xmlFile = CDAUtilExtension.writeAsXML(author, OUTPUT_PATH, caseName);

		FHIRUtil.printJSON(practitioner, OUTPUT_PATH + caseName + "CDA2FHIRPractitioner.json");
		if (practitionerRole != null) {
			FHIRUtil.printJSON(practitionerRole, OUTPUT_PATH + caseName + "CDA2FHIRPractitionerRole.json");
		}
		if (organization != null) {
			FHIRUtil.printJSON(organization, OUTPUT_PATH + caseName + "CDA2FHIROrganization.json");
		}

		List<Object> joltResult = findJoltResult(xmlFile, caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, cda2FhirResult.getBundle(), caseName, OUTPUT_PATH);

		joltUtil.verifyEntity(practitioner, practitionerRole, organization);
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

	@Test
	public void testNoNameIdentifierOrganization() throws Exception {
		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();

		OrganizationGenerator og = new OrganizationGenerator();
		og.setADGenerator(ADGenerator.getDefaultInstance()); // something that is not name/identifier
		authorGenerator.setOrganizationGenerator(og);

		runTest(authorGenerator, "orgNoNameIdentifier");
	}
}
