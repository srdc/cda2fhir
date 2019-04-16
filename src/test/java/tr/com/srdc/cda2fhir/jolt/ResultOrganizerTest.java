package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.RTInvocationHandler;
import tr.com.srdc.cda2fhir.testutil.generator.ResultOrganizerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ResultOrganizerTest {
	private static class LocalResourceTransformer extends ResourceTransformerImpl {
		private static final long serialVersionUID = 1L;

		private List<ResultObservation> resultObservations = new ArrayList<>();

		public void clearObservations() {
			resultObservations.clear();
		}

		public List<ResultObservation> getObservations() {
			return Collections.unmodifiableList(resultObservations);
		}

		@Override
		public EntryResult tResultObservation2Observation(ResultObservation cdaResultObservation,
				IBundleInfo bundleInfo) {
			resultObservations.add(cdaResultObservation);
			return super.tResultObservation2Observation(cdaResultObservation, bundleInfo);
		}
	}

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	private static RTInvocationHandler handler;

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now
	private static BiConsumer<Map<String, Object>, Resource> customJoltUpdate2; // Hack for now

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ResultOrganizer/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();

		handler = new RTInvocationHandler(new ResourceTransformerImpl());
		handler.addMethod("tResultObservation2Observation");
		rt = new LocalResourceTransformer();
	}

	private static void reorderOrganizer(ResultOrganizer organizer, List<ResultObservation> observations) {
		organizer.getComponents().clear();
		observations.forEach(observation -> {
			Component4 component = factories.base.createComponent4();
			component.setObservation(observation);
			organizer.getComponents().add(component);
		});
	}

	private static void runTest(ResultOrganizer ro, String caseName, ResultOrganizerGenerator generator)
			throws Exception {
		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		if (generator == null) {
			rt.clearObservations();
		}
		IEntryResult cda2FhirResult = rt.tResultOrganizer2DiagnosticReport(ro, new BundleInfo(rt));
		if (generator == null) {
			reorderOrganizer(ro, rt.getObservations());
		}

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Diagnostic report bundle", bundle);

		DiagnosticReport report = BundleUtil.findOneResource(bundle, DiagnosticReport.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRDiagnosticReport", "json");
		FHIRUtil.printJSON(report, filepath);

		if (generator != null) {
			generator.verify(report);
			generator.verify(bundle);
		}

		File xmlFile = CDAUtilExtension.writeAsXML(ro, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ResultOrganizer", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		if (customJoltUpdate != null) {
			joltUtil.setValueChanger(customJoltUpdate);
		}
		if (customJoltUpdate2 != null) {
			joltUtil.setValueChanger(customJoltUpdate2);
		}

		joltUtil.verify(report);
	}

	private static void runTest(ResultOrganizerGenerator generator, String caseName) throws Exception {
		ResultOrganizer ro = generator.generate(factories);
		runTest(ro, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		ResultsSection section = cda.getResultsSection();
		if (section != null) {
			int index = 0;
			for (ResultOrganizer organizer : section.getResultOrganizers()) {
				String caseName = sourceName.substring(0, sourceName.length() - 4) + "_" + index;
				runTest(organizer, caseName, null);
				++index;
			}
		}
	}

	@Test
	public void testDefault() throws Exception {
		ResultOrganizerGenerator generator = ResultOrganizerGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
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
