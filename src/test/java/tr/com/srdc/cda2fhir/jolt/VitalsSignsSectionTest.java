package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
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
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class VitalsSignsSectionTest {
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

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/VitalSignsSection/";

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new LocalResourceTransformer();
		factories = CDAFactories.init();
	}

	private static void reorderSection(VitalSignsSectionEntriesOptional section) {
		Map<VitalSignObservation, Integer> map = new HashMap<>();
		List<VitalSignObservation> list = rt.getObservations();
		for (int index = 0; index < list.size(); ++index) {
			VitalSignObservation observation = list.get(index);
			map.put(observation, index);
		}

		List<VitalSignsOrganizer> organizers = new ArrayList<>(section.getVitalSignsOrganizers());
		organizers.sort((a, b) -> {
			VitalSignObservation obsa = a.getVitalSignObservations().get(0);
			VitalSignObservation obsb = b.getVitalSignObservations().get(0);

			int aval = map.get(obsa).intValue();
			int bval = map.get(obsb).intValue();

			return aval - bval;
		});

		final Map<VitalSignObservation, VitalSignsOrganizer> map2 = new HashMap<>();
		organizers.forEach(organizer -> {
			organizer.getVitalSignObservations().forEach(observation -> {
				map2.put(observation, organizer);
			});
		});

		section.getEntries().clear();
		organizers.forEach(organizer -> {
			organizer.getComponents().clear();
			Entry entry = factories.base.createEntry();
			entry.setOrganizer(organizer);
			section.getEntries().add(entry);
		});

		rt.getObservations().forEach(observation -> {
			Component4 component = factories.base.createComponent4();
			component.setObservation(observation);
			VitalSignsOrganizer organizer = map2.get(observation);
			organizer.getComponents().add(component);
		});
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		ICDASection cdaSection = null;
		VitalSignsSectionEntriesOptional section = cda.getVitalSignsSectionEntriesOptional();
		if (section == null) {
			Optional<VitalSignsSection> sectionOptional = cda.getSections().stream()
					.filter(s -> s instanceof VitalSignsSection).map(s -> (VitalSignsSection) s).findFirst();
			if (!sectionOptional.isPresent()) {
				return;
			}
			section = sectionOptional.get();
			cdaSection = CDASectionTypeEnum.VITAL_SIGNS_SECTION.toCDASection(section);
		} else {
			cdaSection = CDASectionTypeEnum.VITAL_SIGNS_SECTION_ENTRIES_OPTIONAL.toCDASection(section);
		}

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		BundleInfo bundleInfo = new BundleInfo(rt);

		Map<String, String> idedAnnotations = EMFUtil.findReferences(section.getText());
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		rt.clearObservations();
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);

		Bundle bundle = sectionResult.getBundle();

		List<Observation> observations = FHIRUtil.findResources(bundle, Observation.class);

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderSection(section);

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "VitalSignsSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltObservations = joltUtil.findResources("Observation");

		int count = observations.size();
		Assert.assertEquals("Observation resource count", count, joltObservations.size());

		for (int index = 0; index < count; ++index) {
			Observation observation = observations.get(index);
			Map<String, Object> joltObservation = joltObservations.get(index);
			joltUtil.verify(observation, joltObservation);
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