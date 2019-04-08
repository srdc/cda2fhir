package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
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

	@SuppressWarnings("unchecked")
	private static void compareConditions(String caseName, Condition condition, Map<String, Object> joltCondition)
			throws Exception {
		Assert.assertNotNull("Jolt condition", joltCondition);
		Assert.assertNotNull("Jolt condition id", joltCondition.get("id"));

		joltCondition.put("id", condition.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltCondition, "subject", condition.getSubject()); // patient is not yet implemented

		if (condition.hasAsserter()) {
			Map<String, Object> asserter = (Map<String, Object>) joltCondition.get("asserter");
			Assert.assertNotNull("Jolt condition asserter", asserter);
			Object reference = asserter.get("reference");
			Assert.assertNotNull("Jolt condition asserter reference", reference);
			Assert.assertTrue("Reference is string", reference instanceof String);
			JoltUtil.putReference(joltCondition, "asserter", condition.getAsserter()); // reference values may not match
		} else {
			Assert.assertNull("No jolt condition asserter", joltCondition.get("asserter"));
		}

		String joltConditionJson = JsonUtils.toPrettyJsonString(joltCondition);
		File joltConditionFile = new File(OUTPUT_PATH + caseName + "JoltCondition.json");
		FileUtils.writeStringToFile(joltConditionFile, joltConditionJson, Charset.defaultCharset());

		String conditionJson = FHIRUtil.encodeToJSON(condition);
		JSONAssert.assertEquals("Jolt condition", conditionJson, joltConditionJson, true);
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

		File xmlFile = CDAUtilExtension.writeAsXML(pca, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProblemConcernAct", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		joltUtil.verifyOrganizations(organizations);
		joltUtil.verifyPractitioners(practitioners);
		joltUtil.verifyPractitionerRoles(practitionerRoles);

		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(joltResult, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No conditions", joltConditions.isEmpty());
		} else {
			for (int index = 0; index < conditions.size(); ++index) {
				Condition condition = conditions.get(index);
				int joltIndex = condition.hasAsserter() ? 1 : 0; // TODO: fix, this is hack

				String indexStr = index == 0 ? "" : String.valueOf(index);
				compareConditions(caseName + indexStr, condition, joltConditions.get(joltIndex));
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
