package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

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

		File xmlFile = CDAUtilExtension.writeAsXML(pca, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProblemConcernAct", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(joltResult, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No conditions", joltConditions.isEmpty());
		} else {
			for (int index = 0; index < conditions.size(); ++index) {
				Condition condition = conditions.get(index);
				int joltIndex = condition.hasAsserter() ? 1 : 0; // TODO: fix, this is hack

				joltUtil.verify(condition, joltConditions.get(joltIndex));
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
