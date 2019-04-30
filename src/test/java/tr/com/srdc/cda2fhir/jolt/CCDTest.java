package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.LocalResourceTransformer;
import tr.com.srdc.cda2fhir.testutil.generator.AssignedEntityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CCDGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CCDTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/CCD/";

	private static CDAFactories factories;

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	private static void runTest(ContinuityOfCareDocument ccd, String caseName, CCDGenerator generator)
			throws Exception {
		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		CCDTransformerImpl ccdTransform = new CCDTransformerImpl();

		LocalResourceTransformer rt = null;
		if (generator == null) {
			rt = new LocalResourceTransformer(factories);
			ccdTransform.setResourceTransformer(rt);
		}

		Bundle bundle = ccdTransform.transformDocument(ccd);

		Assert.assertNotNull("CCD bundle", bundle);

		Composition composition = BundleUtil.findOneResource(bundle, Composition.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRCCD", "json");
		FHIRUtil.printJSON(bundle, filepath);

		if (generator != null) {
			generator.verify(composition);
			generator.verify(bundle);
		}

		if (rt != null) {
			rt.reorder(ccd);
		}

		File xmlFile = CDAUtilExtension.writeAsXML(ccd, OUTPUT_PATH, caseName);
		List<Object> joltResult = JoltUtil.findJoltDocumentResult(xmlFile, "CCD", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		if (customJoltUpdate != null) {
			joltUtil.setValueChanger(customJoltUpdate);
		}

		joltUtil.verify(composition);
	}

	private static void runTest(CCDGenerator generator, String caseName) throws Exception {
		ContinuityOfCareDocument ccd = generator.generate(factories);
		runTest(ccd, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());
		String caseName = sourceName.substring(0, sourceName.length() - 4);
		runTest(cda, caseName, null);
	}

	@Test
	public void testDefault() throws Exception {
		CCDGenerator generator = CCDGenerator.getDefaultInstance();
		runTest(generator, "default");
	}

	@Test
	public void testSameLegalAuthenticatorAuthor() throws Exception {
		CCDGenerator generator = CCDGenerator.getDefaultInstance();
		AssignedEntityGenerator aeg = new AssignedEntityGenerator(generator.getAuthorGenerator());
		generator.setLegalAuthenticatorGenerator(aeg);
		runTest(generator, "sameLegalAuthenticatorAuthor");
	}

	@Test
	public void testSample1() throws Exception {
		runSampleTest("C-CDA_R2-1_CCD.xml");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSample2() throws Exception {
		customJoltUpdate = resource -> {
			if (resource.get("resourceType").equals("Composition")) {
				List<Object> values = JoltUtil.findPathValue(resource, "event[].period");
				Map<String, Object> period = (Map<String, Object>) values.get(0);
				period.put("end", "2015-07-22T23:00:00+00:00"); // invalid time in file
			}
		};
		runSampleTest("170.315_b1_toc_gold_sample2_v1.xml");
		customJoltUpdate = null;
	}

	@Test
	public void testSample3() throws Exception {
		Consumer<Map<String, Object>> conditionJoltUpdate = JoltUtil.getFloatUpdate("346.1", "346.10");
		customJoltUpdate = resource -> {
			if (resource.get("resourceType").equals("Condition")) {
				conditionJoltUpdate.accept(resource);
			}
			if (resource.get("resourceType").equals("Immunization")) {
				String date = (String) resource.get("date");
				if ("2006-06-28T14:24:00".equals(date)) {
					resource.put("date", "2006-06-28T14:24:00-05:00");
				}
			}
		};
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
		customJoltUpdate = null;
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		runSampleTest("Epic/DOC0001.XML");
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		runSampleTest("Epic/DOC0001 2.XML");
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
		runSampleTest("Epic/DOC0001 15.XML");
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