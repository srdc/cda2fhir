package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ImmunizationsSectionTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ImmunizationsSection/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new ResourceTransformerImpl();
		factories = CDAFactories.init();
	}

	private static void reorderSectionActs(ImmunizationsSection section, List<Immunization> immunizations) {
		IdentifierMap<Integer> orderMap = IdentifierMapFactory.resourcesToOrder(immunizations);
		List<ImmunizationActivity> acts = new ArrayList<>();
		section.getImmunizationActivities().forEach(r -> acts.add(r));
		acts.sort((a, b) -> {
			int aval = CDAUtilExtension.idValue("Immunization", a.getIds(), orderMap);
			int bval = CDAUtilExtension.idValue("Immunization", b.getIds(), orderMap);
			return aval - bval;
		});
		section.getEntries().clear();
		acts.forEach(act -> {
			Entry entry = factories.base.createEntry();
			entry.setSubstanceAdministration(act);
			section.getEntries().add(entry);
		});
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		Optional<ImmunizationsSection> sectionOptional = cda.getSections().stream()
				.filter(s -> s instanceof ImmunizationsSection).map(s -> (ImmunizationsSection) s).findFirst();

		if (!sectionOptional.isPresent()) {
			return;
		}

		ImmunizationsSection section = sectionOptional.get();

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		ICDASection cdaSection = CDASectionTypeEnum.IMMUNIZATIONS_SECTION.toCDASection(section);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Map<String, String> idedAnnotations = EMFUtil.findReferences(section.getText());
		bundleInfo.mergeIdedAnnotations(idedAnnotations);
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);
		Bundle bundle = sectionResult.getBundle();

		List<Immunization> immunizations = FHIRUtil.findResources(bundle, Immunization.class);

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderSectionActs(section, immunizations);

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "ImmunizationsSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltImmunizations = joltUtil.findResources("Immunization");

		int count = immunizations.size();
		Assert.assertEquals("Immunizations resource count", count, joltImmunizations.size());

		for (int index = 0; index < count; ++index) {
			Immunization immunization = immunizations.get(index);
			Map<String, Object> joltImmunization = joltImmunizations.get(index);
			if (customJoltUpdate != null) {
				customJoltUpdate.accept(joltImmunization);
			}

			joltUtil.verify(immunization, joltImmunization);
		}
	}

	@Test
	public void testSample1() throws Exception {
		runSampleTest("C-CDA_R2-1_CCD.xml");
	}

	@Ignore
	@Test
	public void testSample2() throws Exception {
		runSampleTest("170.315_b1_toc_gold_sample2_v1.xml");
	}

	@Ignore
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