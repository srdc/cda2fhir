package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemConcernActGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemSectionTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProblemSection/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new ResourceTransformerImpl();
		factories = CDAFactories.init();
	}

	private static void reorderSectionActs(ProblemSection section, List<Condition> conditions) {
		IdentifierMap<Integer> orderMap = IdentifierMapFactory.resourcesToOrder(conditions);
		List<ProblemConcernAct> acts = new ArrayList<>();
		section.getConsolProblemConcerns().forEach(act -> acts.add(act));
		acts.sort((a, b) -> {
			EList<ProblemObservation> alist = a.getProblemObservations();
			EList<ProblemObservation> blist = b.getProblemObservations();
			if (alist.size() == 0) {
				return -1;
			}
			if (blist.size() == 0) {
				return 1;
			}
			ProblemObservation apo = alist.get(0);
			ProblemObservation bpo = blist.get(0);
			int aval = CDAUtilExtension.idValue("Condition", apo.getIds(), orderMap);
			int bval = CDAUtilExtension.idValue("Condition", bpo.getIds(), orderMap);
			return aval - bval;
		});

		section.getEntries().clear();
		acts.forEach(act -> {
			ProblemConcernActGenerator.reorderActObservations(factories, act, conditions);
			Entry entry = factories.base.createEntry();
			entry.setAct(act);
			section.getEntries().add(entry);
		});
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		ProblemSection section = cda.getProblemSection();

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		ICDASection cdaSection = CDASectionTypeEnum.PROBLEM_SECTION.toCDASection(section);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Map<String, String> idedAnnotations = EMFUtil.findReferences(section.getText());
		bundleInfo.mergeIdedAnnotations(idedAnnotations);
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);
		Bundle bundle = sectionResult.getBundle();

		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderSectionActs(section, conditions);

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "ProblemSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltConditions = joltUtil.findResources("Condition");

		int count = conditions.size();
		Assert.assertEquals("Allergy intolerance resource count", count, joltConditions.size());

		for (int index = 0; index < count; ++index) {
			Condition condition = conditions.get(index);
			Map<String, Object> joltCondition = joltConditions.get(index);
			if (customJoltUpdate != null) {
				customJoltUpdate.accept(joltCondition);
			}

			joltUtil.verify(condition, joltCondition);
		}
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
		customJoltUpdate = JoltUtil.getFloatUpdate("346.1", "346.10");
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		customJoltUpdate = r -> {
			JoltUtil.getFloatUpdate("346.8", "346.80").accept(r);
			JoltUtil.getFloatUpdate("845", "845.00").accept(r);
			JoltUtil.getFloatUpdate("300", "300.00").accept(r);
		};
		runSampleTest("Epic/DOC0001.XML");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		customJoltUpdate = JoltUtil.getFloatUpdate("300", "300.00");
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
		customJoltUpdate = JoltUtil.getFloatUpdate("346.9", "346.90");
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