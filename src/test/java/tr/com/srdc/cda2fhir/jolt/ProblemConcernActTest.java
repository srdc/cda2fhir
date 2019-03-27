package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemConcernActGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProblemConcernAct/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void putReference(Map<String, Object> joltResult, String property, Reference reference) {
		Map<String, Object> r = new LinkedHashMap<String, Object>();
		r.put("reference", reference.getReference());
		joltResult.put(property, r);
	}

	@SuppressWarnings("unchecked")
	private static void compareConditions(String caseName, Condition condition, Map<String, Object> joltCondition)
			throws Exception {
		Assert.assertNotNull("Jolt condition", joltCondition);
		Assert.assertNotNull("Jolt condition id", joltCondition.get("id"));

		joltCondition.put("id", condition.getIdElement().getIdPart()); // ids do not have to match
		putReference(joltCondition, "subject", condition.getSubject()); // patient is not yet implemented

		if (condition.hasAsserter()) {
			Map<String, Object> asserter = (Map<String, Object>) joltCondition.get("asserter");
			Assert.assertNotNull("Jolt condition asserter", asserter);
			Object reference = asserter.get("reference");
			Assert.assertNotNull("Jolt condition asserter reference", reference);
			Assert.assertTrue("Reference is string", reference instanceof String);
			putReference(joltCondition, "asserter", condition.getAsserter()); // reference values may not match
		} else {
			Assert.assertNull("No jolt condition asserter", joltCondition.get("asserter"));
		}

		String joltConditionJson = JsonUtils.toPrettyJsonString(joltCondition);
		File joltConditionFile = new File(OUTPUT_PATH + caseName + "JoltCondition.json");
		FileUtils.writeStringToFile(joltConditionFile, joltConditionJson, Charset.defaultCharset());

		String conditionJson = FHIRUtil.encodeToJSON(condition);
		JSONAssert.assertEquals("Jolt condition", conditionJson, joltConditionJson, true);
	}

	private static void comparePractitioner(String caseName, Practitioner practitioner,
			Map<String, Object> joltPractitioner) throws Exception {
		joltPractitioner.put("id", practitioner.getIdElement().getIdPart()); // ids do not have to match
		// different
		String expected = FHIRUtil.encodeToJSON(practitioner);
		String actual = JsonUtils.toJsonString(joltPractitioner);
		JSONAssert.assertEquals("Jolt practitioner", expected, actual, true);
	}

	private static void compareOrganization(String caseName, Organization organization,
			Map<String, Object> joltOrganization) throws Exception {
		joltOrganization.put("id", organization.getIdElement().getIdPart()); // ids do not have to match
		// different
		String expected = FHIRUtil.encodeToJSON(organization);
		String actual = JsonUtils.toJsonString(joltOrganization);
		JSONAssert.assertEquals("Jolt organization", expected, actual, true);
	}

	@SuppressWarnings("unchecked")
	private static void compareRoles(String caseName, PractitionerRole practitionerRole,
			Map<String, Object> joltPractitionerRole) throws Exception {
		Assert.assertNotNull("Jolt practitioner", joltPractitionerRole);
		Assert.assertNotNull("Jolt practitioner id", joltPractitionerRole.get("id"));
		joltPractitionerRole.put("id", practitionerRole.getIdElement().getIdPart()); // ids do not have to match

		Map<String, Object> practitioner = (Map<String, Object>) joltPractitionerRole.get("practitioner");
		Assert.assertNotNull("Jolt role practitioner", practitioner);
		Object practitionerReference = practitioner.get("reference");
		Assert.assertNotNull("Jolt role practitioner reference", practitionerReference);
		Assert.assertTrue("practitioner reference is string", practitionerReference instanceof String);
		putReference(joltPractitionerRole, "practitioner", practitionerRole.getPractitioner()); // reference values may
																								// not match

		Map<String, Object> organization = (Map<String, Object>) joltPractitionerRole.get("organization");
		Assert.assertNotNull("Jolt role organization", organization);
		Object organizationReference = organization.get("reference");
		Assert.assertNotNull("Jolt role organization reference", organizationReference);
		Assert.assertTrue("organization reference is string", organizationReference instanceof String);
		putReference(joltPractitionerRole, "organization", practitionerRole.getOrganization()); // reference values may
																								// not match

		String joltPractitionerJson = JsonUtils.toPrettyJsonString(joltPractitionerRole);
		File joltPractitionerFile = new File(OUTPUT_PATH + caseName + "JoltPractitionerRole.json");
		FileUtils.writeStringToFile(joltPractitionerFile, joltPractitionerJson, Charset.defaultCharset());
	}

	private static File writeProblemConcernActAsXML(String caseName, ProblemConcernAct pca) throws Exception {
		File xmlFile = new File(OUTPUT_PATH + caseName + ".xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(pca, fw);
		fw.close();
		return xmlFile;
	}

	private static List<Object> findJoltResult(File xmlFile, String caseName) throws Exception {
		OrgJsonUtil util = OrgJsonUtil.readXML(xmlFile.toString());
		JSONObject json = util.getJSONObject();
		File jsonFile = new File(OUTPUT_PATH + caseName + ".json");
		FileUtils.writeStringToFile(jsonFile, json.toString(4), Charset.defaultCharset());

		List<Object> joltResult = TransformManager.transformEntryInFile("ProblemConcernAct", jsonFile.toString());
		return joltResult;
	}

	private static void runTest(ProblemConcernActGenerator generator, String caseName) throws Exception {
		ProblemConcernAct pca = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tProblemConcernAct2Condition(pca, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Problem bundle", bundle);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		for (int index = 0; index < conditions.size(); ++index) {
			Condition condition = conditions.get(index);
			String filepath = String.format("%s%s%s%s%s", OUTPUT_PATH, caseName, "CDA2FHIRCondition",
					index == 0 ? "" : index, "json");
			FHIRUtil.printJSON(condition, filepath);
		}
		generator.verify(bundle);

		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		List<PractitionerRole> practitionerRoles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		List<Organization> organizations = FHIRUtil.findResources(bundle, Organization.class);

		File xmlFile = writeProblemConcernActAsXML(caseName, pca);

		List<Object> joltResult = findJoltResult(xmlFile, caseName);

		List<Map<String, Object>> joltOrganizations = TransformManager.chooseResources(joltResult, "Organization");
		if (organizations.isEmpty()) {
			Assert.assertTrue("No organizations", joltOrganizations.isEmpty());
		} else {
			for (int index = 0; index < organizations.size(); ++index) {
				String indexStr = index == 0 ? "" : String.valueOf(index);
				compareOrganization(caseName + indexStr, organizations.get(index), joltOrganizations.get(index));
			}
		}

		List<Map<String, Object>> joltPractitioners = TransformManager.chooseResources(joltResult, "Practitioner");
		if (practitioners.isEmpty()) {
			Assert.assertTrue("No practitioner", joltPractitioners.isEmpty());
		} else {
			for (int index = 0; index < practitioners.size(); ++index) {
				String indexStr = index == 0 ? "" : String.valueOf(index);
				comparePractitioner(caseName + indexStr, practitioners.get(index), joltPractitioners.get(index));
			}
		}

		List<Map<String, Object>> joltPractitionerRoles = TransformManager.chooseResources(joltResult,
				"PractitionerRole");
		if (practitionerRoles.isEmpty()) {
			Assert.assertTrue("No jolt practitioner role", joltPractitionerRoles.isEmpty());
		} else {
			for (int index = 0; index < practitionerRoles.size(); ++index) {
				String indexStr = index == 0 ? "" : String.valueOf(index);
				compareRoles(caseName + indexStr, practitionerRoles.get(index), joltPractitionerRoles.get(index));
			}
		}

		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(joltResult, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No conditions", joltConditions.isEmpty());
		} else {
			for (int index = 0; index < conditions.size(); ++index) {
				String indexStr = index == 0 ? "" : String.valueOf(index);
				compareConditions(caseName + indexStr, conditions.get(index), joltConditions.get(index));
			}
		}
	}

	@Test
	public void testDefault() throws Exception {
		ProblemConcernActGenerator generator = ProblemConcernActGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testFull() throws Exception {
		ProblemConcernActGenerator generator = ProblemConcernActGenerator.getFullInstance();
		runTest(generator, "fullCase");
	}
}
