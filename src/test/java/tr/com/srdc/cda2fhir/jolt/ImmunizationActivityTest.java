package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ImmunizationActivityGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ImmunizationActivityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ImmunizationActivity/";

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(ImmunizationActivity ia, String caseName, ImmunizationActivityGenerator generator)
			throws Exception {
		File xmlFile = CDAUtilExtension.writeAsXML(ia, OUTPUT_PATH, caseName);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tImmunizationActivity2Immunization(ia, new BundleInfo(rt));
		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ImmunizationActivity", caseName);

		Bundle bundle = cda2FhirResult.getBundle();
		if (bundle == null) {
			Assert.assertTrue("No immunization", joltResult == null || joltResult.size() == 0);
			return;
		}

		Assert.assertNotNull("Immunization bundle", bundle);

		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRImmunization", "json");
		FHIRUtil.printJSON(immunization, filepath);

		if (generator != null) {
			generator.verify(bundle);
		}

		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		Map<String, Object> joltImmunization = TransformManager.chooseResource(joltResult, "Immunization");
		if (customJoltUpdate != null) {
			customJoltUpdate.accept(joltImmunization);
		}

		joltUtil.verify(immunization);
	}

	private static void runTest(ImmunizationActivityGenerator generator, String caseName) throws Exception {
		ImmunizationActivity iag = generator.generate(factories);
		runTest(iag, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		Optional<ImmunizationsSection> section = cda.getSections().stream()
				.filter(s -> s instanceof ImmunizationsSection).map(s -> (ImmunizationsSection) s).findFirst();

		if (!section.isPresent()) {
			return;
		}

		ImmunizationsSection immSection = section.get();

		int index = 0;
		for (ImmunizationActivity act : immSection.getImmunizationActivities()) {
			String caseName = sourceName.substring(0, sourceName.length() - 4) + "_" + index;
			runTest(act, caseName, null);
			++index;
		}
	}

	@Test
	public void testDefault() throws Exception {
		ImmunizationActivityGenerator generator = ImmunizationActivityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testRefused() throws Exception {
		ImmunizationActivityGenerator generator = ImmunizationActivityGenerator.getDefaultInstance();
		generator.convertToRefused();
		runTest(generator, "refusedCase");
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
		customJoltUpdate = imm -> {
			if (imm != null) {
				Object date = imm.get("date");
				if ("2006-06-28T14:24:00".equals(date)) {
					imm.put("date", "2006-06-28T14:24:00-05:00");
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
