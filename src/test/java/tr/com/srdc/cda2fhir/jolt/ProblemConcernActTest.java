package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemConcernActGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProblemConcernAct/";

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	@SuppressWarnings("unchecked")
	private static Consumer<Map<String, Object>> getFloatUpdate(String current, String replacement) {
		return r -> {
			List<Object> codings = JoltUtil.findPathValue(r, "code.coding[]");
			codings.forEach(coding -> {
				Map<String, Object> codingAsMap = (Map<String, Object>) coding;
				String code = (String) codingAsMap.get("code");
				if (current.equals(code)) {
					codingAsMap.put("code", replacement);
				}
			});
		};
	}

	private static void reorderActObservations(ProblemConcernAct act, List<Condition> conditions) {
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		IdentifierMap<Integer> orderMap = IdentifierMapFactory.resourcesToOrder(conditions);
		List<ProblemObservation> observations = new ArrayList<>();
		conditions.forEach(r -> observations.add(null));
		act.getProblemObservations().forEach(r -> {
			II ii = r.getIds().get(0);
			String root = ii.getRoot();
			String extension = ii.getExtension();
			if (extension == null) {
				Integer index = orderMap.get("Condition", root);
				observations.set(index.intValue(), r);
			} else {
				String value = extension;
				String system = vst.tOid2Url(root);
				Integer index = orderMap.get("Condition", system, value);
				observations.set(index.intValue(), r);
			}
		});

		act.getEntryRelationships().clear();
		observations.forEach(po -> {
			EntryRelationship er = factories.base.createEntryRelationship();
			act.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			er.setObservation(po);
		});
	}

	private static void runTest(ProblemConcernAct pca, String caseName, ProblemConcernActGenerator generator)
			throws Exception {
		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tProblemConcernAct2Condition(pca, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Problem bundle", bundle);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		for (int index = 0; index < conditions.size(); ++index) {
			Condition condition = conditions.get(index);
			String filepath = String.format("%s%s%s%s%s", OUTPUT_PATH, caseName, "CDA2FHIRCondition",
					index == 0 ? "" : index, ".json");
			FHIRUtil.printJSON(condition, filepath);
		}
		if (generator != null) {
			generator.verify(bundle);
		}

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderActObservations(pca, conditions);
		File xmlFile = CDAUtilExtension.writeAsXML(pca, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProblemConcernAct", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(joltResult, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No conditions", joltConditions.isEmpty());
		} else {
			for (int index = 0; index < conditions.size(); ++index) {
				Condition condition = conditions.get(index);
				Map<String, Object> joltCondition = joltConditions.get(index);
				if (customJoltUpdate != null) {
					customJoltUpdate.accept(joltCondition);
				}

				joltUtil.verify(condition, joltCondition);
			}
		}
	}

	private static void runTest(ProblemConcernActGenerator generator, String caseName) throws Exception {
		ProblemConcernAct pca = generator.generate(factories);
		runTest(pca, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		ProblemSection section = cda.getProblemSection();

		int index = 0;
		for (ProblemConcernAct act : section.getConsolProblemConcerns()) {
			String caseName = sourceName.substring(0, sourceName.length() - 4) + "_" + index;
			runTest(act, caseName, null);
			++index;
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

	@Test
	public void testSample1() throws Exception {
		runSampleTest("C-CDA_R2-1_CCD.xml");
	}

	@Test
	public void testSample2() throws Exception {
		runSampleTest("170.315_b1_toc_gold_sample2_v1.xml");
	}

	@Test
	public void testSample3() throws Exception {
		customJoltUpdate = getFloatUpdate("346.1", "346.10");
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		customJoltUpdate = r -> {
			getFloatUpdate("346.8", "346.80").accept(r);
			getFloatUpdate("845", "845.00").accept(r);
			getFloatUpdate("300", "300.00").accept(r);
		};
		runSampleTest("Epic/DOC0001.XML");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		customJoltUpdate = getFloatUpdate("300", "300.00");
		runSampleTest("Epic/DOC0001 2.XML");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample3() throws Exception {
		runSampleTest("Epic/DOC0001 3.XML");
	}

	@Ignore
	@Test
	public void testEpicSample4() throws Exception {
		runSampleTest("Epic/DOC0001 4.XML");
	}

	@Ignore
	@Test
	public void testEpicSample5() throws Exception {
		runSampleTest("Epic/DOC0001 5.XML");
	}

	@Ignore
	@Test
	public void testEpicSample6() throws Exception {
		runSampleTest("Epic/DOC0001 6.XML");
	}

	@Ignore
	@Test
	public void testEpicSample7() throws Exception {
		runSampleTest("Epic/DOC0001 7.XML");
	}

	@Ignore
	@Test
	public void testEpicSample8() throws Exception {
		runSampleTest("Epic/DOC0001 8.XML");
	}

	@Ignore
	@Test
	public void testEpicSample9() throws Exception {
		runSampleTest("Epic/DOC0001 9.XML");
	}

	@Ignore
	@Test
	public void testEpicSample10() throws Exception {
		runSampleTest("Epic/DOC0001 10.XML");
	}

	@Ignore
	@Test
	public void testEpicSample11() throws Exception {
		runSampleTest("Epic/DOC0001 11.XML");
	}

	@Ignore
	@Test
	public void testEpicSample12() throws Exception {
		runSampleTest("Epic/DOC0001 12.XML");
	}

	@Ignore
	@Test
	public void testEpicSample13() throws Exception {
		runSampleTest("Epic/DOC0001 13.XML");
	}

	@Ignore
	@Test
	public void testEpicSample14() throws Exception {
		runSampleTest("Epic/DOC0001 14.XML");
	}

	@Ignore
	@Test
	public void testEpicSample15() throws Exception {
		customJoltUpdate = getFloatUpdate("346.9", "346.90");
		runSampleTest("Epic/DOC0001 15.XML");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample16() throws Exception {
		runSampleTest("Epic/HannahBanana_EpicCCD.xml");
	}

	@Ignore
	@Test
	public void testCernerSample1() throws Exception {
		runSampleTest("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
	}

	@Ignore
	@Test
	public void testCernerSample2() throws Exception {
		runSampleTest("Cerner/Encounter-RAKIA_TEST_DOC00001.XML");
	}
}
