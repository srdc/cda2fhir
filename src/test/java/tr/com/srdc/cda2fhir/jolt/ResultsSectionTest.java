package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.LocalResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ResultsSectionTest {
	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now
	private static BiConsumer<Map<String, Object>, Resource> customJoltUpdate2; // Hack for now

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ResultsSection/";

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new LocalResourceTransformer(factories);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		ResultsSection section = cda.getResultsSection();

		if (section == null) {
			return;
		}

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		ICDASection cdaSection = CDASectionTypeEnum.RESULTS_SECTION.toCDASection(section);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Map<String, String> idedAnnotations = EMFUtil.findReferences(section.getText());
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		rt.clearEntries();
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);

		Bundle bundle = sectionResult.getBundle();

		List<DiagnosticReport> reports = FHIRUtil.findResources(bundle, DiagnosticReport.class);

		// CDAUtil reorders randomly, follow its order for easy comparison
		rt.reorderSection(section);

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "ResultsSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltReports = joltUtil.findResources("DiagnosticReport");

		int count = reports.size();
		Assert.assertEquals("Diagnostic report resource count", count, joltReports.size());

		for (int index = 0; index < count; ++index) {
			DiagnosticReport report = reports.get(index);
			Map<String, Object> joltReport = joltReports.get(index);
			if (customJoltUpdate != null) {
				joltUtil.setValueChanger(customJoltUpdate);
			}
			if (customJoltUpdate2 != null) {
				joltUtil.setValueChanger(customJoltUpdate2);
			}

			joltUtil.verify(report, joltReport);
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
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
	}

	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void testEpicSample1() throws Exception {
		customJoltUpdate = r -> {
			JoltUtil.getIdentierValueUpdate("3.78165806.1", "378165806.10").accept(r);
		};
		customJoltUpdate2 = (r, resource) -> {
			if (resource instanceof Observation) {
				Observation observation = (Observation) resource;
				if (!observation.hasCode()) {
					r.remove("code");
				}
				if (observation.hasIdentifier()) {
					int count = observation.getIdentifier().size();
					List<Object> joltIdentifiers = (List<Object>) r.get("identifier");
					for (int index = 0; index < count; ++index) {
						Map<String, Object> joltIdentifier = (Map<String, Object>) joltIdentifiers.get(index);
						String value = (String) joltIdentifier.get("value");
						if (value.startsWith("3.78")) {
							String actualValue = observation.getIdentifier().get(index).getValue();
							joltIdentifier.put("value", actualValue);
						}
					}
				}
			}
		};
		runSampleTest("Epic/DOC0001.XML");
		customJoltUpdate = null;
		customJoltUpdate2 = null;
	}

	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void testEpicSample2() throws Exception {
		customJoltUpdate = r -> {
			JoltUtil.getIdentierValueUpdate("3.78165806.1", "378165806.10").accept(r);
		};
		customJoltUpdate2 = (r, resource) -> {
			if (resource instanceof Observation) {
				Observation observation = (Observation) resource;
				if (!observation.hasCode()) {
					r.remove("code");
				}
				if (observation.hasIdentifier()) {
					int count = observation.getIdentifier().size();
					List<Object> joltIdentifiers = (List<Object>) r.get("identifier");
					for (int index = 0; index < count; ++index) {
						Map<String, Object> joltIdentifier = (Map<String, Object>) joltIdentifiers.get(index);
						String value = (String) joltIdentifier.get("value");
						if (value.startsWith("3.78")) {
							String actualValue = observation.getIdentifier().get(index).getValue();
							joltIdentifier.put("value", actualValue);
						}
					}
				}
			}
		};
		runSampleTest("Epic/DOC0001 2.XML");
		customJoltUpdate = null;
		customJoltUpdate2 = null;
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