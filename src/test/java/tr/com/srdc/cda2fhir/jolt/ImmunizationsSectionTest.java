package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
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

public class ImmunizationsSectionTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ImmunizationsSection/";

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	private static BiConsumer<Map<String, Object>, Resource> customJoltUpdate2; // Hack for now

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

		rt.clearEntries();
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);

		// CDAUtil reorders randomly, follow its order for easy comparison
		rt.reorderSection(section);

		Bundle bundle = sectionResult.getBundle();
		List<Immunization> immunizations = FHIRUtil.findResources(bundle, Immunization.class);

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
			if (customJoltUpdate2 != null) {
				customJoltUpdate2.accept(joltImmunization, immunization);
			}
			joltUtil.verify(immunization, joltImmunization);
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
		customJoltUpdate2 = (r, resource) -> {
			String date = (String) r.get("date");
			if ("2006-06-28T14:24:00".equals(date)) {
				if (resource instanceof Immunization) {
					Immunization imm = (Immunization) resource;
					r.put("date", imm.getDateElement().asStringValue());
				}
			}
		};
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
		customJoltUpdate2 = null;
	}

	// @Ignore
	@Test
	public void testEpicSample1() throws Exception {
		runSampleTest("Epic/DOC0001.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample2() throws Exception {
		runSampleTest("Epic/DOC0001 2.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample3() throws Exception {
		runSampleTest("Epic/DOC0001 3.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample4() throws Exception {
		runSampleTest("Epic/DOC0001 4.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample5() throws Exception {
		runSampleTest("Epic/DOC0001 5.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample6() throws Exception {
		runSampleTest("Epic/DOC0001 6.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample7() throws Exception {
		runSampleTest("Epic/DOC0001 7.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample8() throws Exception {
		runSampleTest("Epic/DOC0001 8.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample9() throws Exception {
		runSampleTest("Epic/DOC0001 9.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample10() throws Exception {
		runSampleTest("Epic/DOC0001 10.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample11() throws Exception {
		runSampleTest("Epic/DOC0001 11.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample12() throws Exception {
		runSampleTest("Epic/DOC0001 12.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample13() throws Exception {
		runSampleTest("Epic/DOC0001 13.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample14() throws Exception {
		runSampleTest("Epic/DOC0001 14.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample15() throws Exception {
		runSampleTest("Epic/DOC0001 15.XML");
	}

	// @Ignore
	@Test
	public void testEpicSample16() throws Exception {
		runSampleTest("Epic/HannahBanana_EpicCCD.xml");
	}

	// @Ignore
	@Test
	public void testCernerSample1() throws Exception {
		runSampleTest("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
	}

	// @Ignore
	@Test
	public void testCernerSample2() throws Exception {
		runSampleTest("Cerner/Encounter-RAKIA_TEST_DOC00001.XML");
	}
}