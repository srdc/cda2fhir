package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.VitalSignsOrganizerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.section.impl.CDASectionCommon;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class VitalSignsOrganizerTest {
	private static class LocalResourceTransformer extends ResourceTransformerImpl {
		private static final long serialVersionUID = 1L;

		private List<VitalSignObservation> observations = new ArrayList<>();

		public void clearObservations() {
			observations.clear();
		}

		public List<VitalSignObservation> getObservations() {
			return Collections.unmodifiableList(observations);
		}

		@Override
		public EntryResult tVitalSignObservation2Observation(VitalSignObservation vitalSignObservation,
				IBundleInfo bundleInfo) {
			observations.add(vitalSignObservation);
			return super.tVitalSignObservation2Observation(vitalSignObservation, bundleInfo);
		}
	}

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/VitalSignsOrganizer/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new LocalResourceTransformer();
	}

	private static void reorderOrganizer(VitalSignsOrganizer organizer, List<VitalSignObservation> observations) {
		organizer.getComponents().clear();
		observations.forEach(observation -> {
			Component4 component = factories.base.createComponent4();
			component.setObservation(observation);
			organizer.getComponents().add(component);
		});
	}

	private static void runTest(VitalSignsOrganizer vso, String caseName, VitalSignsOrganizerGenerator generator)
			throws Exception {
		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		EList<VitalSignsOrganizer> list = new BasicEList<>();
		list.add(vso);

		rt.clearObservations();
		ISectionResult sectionResult = CDASectionCommon.transformVitalSignsOrganizerList(list, new BundleInfo(rt));

		Bundle bundle = sectionResult.getBundle();
		Assert.assertNotNull("Observation bundle", bundle);

		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRBundle", "json");
		FHIRUtil.printJSON(bundle, filepath);

		if (generator != null) {
			generator.verify(bundle);
		}

		reorderOrganizer(vso, rt.getObservations());

		File xmlFile = CDAUtilExtension.writeAsXML(vso, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "VitalSignsOrganizer", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Observation> observations = FHIRUtil.findResources(bundle, Observation.class);
		joltUtil.verifyObservations(observations);
	}

	private static void runTest(VitalSignsOrganizerGenerator generator, String caseName) throws Exception {
		VitalSignsOrganizer vso = generator.generate(factories);
		runTest(vso, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		EList<VitalSignsOrganizer> organizers;
		VitalSignsSectionEntriesOptional section = cda.getVitalSignsSectionEntriesOptional();
		if (section != null) {
			organizers = section.getVitalSignsOrganizers();
		} else {
			Optional<VitalSignsSection> sectionOptional = cda.getSections().stream()
					.filter(s -> s instanceof VitalSignsSection).map(s -> (VitalSignsSection) s).findFirst();
			if (!sectionOptional.isPresent()) {
				return;
			}
			organizers = sectionOptional.get().getVitalSignsOrganizers();
		}
		int index = 0;
		for (VitalSignsOrganizer organizer : organizers) {
			String caseName = sourceName.substring(0, sourceName.length() - 4) + "_" + index;
			runTest(organizer, caseName, null);
			++index;
		}
	}

	@Test
	public void testDefault() throws Exception {
		VitalSignsOrganizerGenerator generator = VitalSignsOrganizerGenerator.getDefaultInstance();
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
