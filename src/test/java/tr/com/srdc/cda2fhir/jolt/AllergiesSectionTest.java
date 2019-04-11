package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.jolt.report.ReportException;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergiesSectionTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/AllergiesSection/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new ResourceTransformerImpl();
		factories = CDAFactories.init();
	}

	private static IdentifierMap<Integer> getEntryOrder(List<AllergyIntolerance> allergies) {
		IdentifierMap<Integer> result = new IdentifierMap<Integer>();
		for (int index = 0; index < allergies.size(); ++index) {
			AllergyIntolerance allergy = allergies.get(index);
			if (!allergy.hasIdentifier()) {
				throw new ReportException("No identfier. Cannot be ordered");
			}
			result.put("AllergyIntolerance", allergy.getIdentifier(), index);
		}
		return result;
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		AllergiesSection section = cda.getAllergiesSection();

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		ICDASection cdaSection = CDASectionTypeEnum.ALLERGIES_SECTION.toCDASection(section);
		ISectionResult sectionResult = cdaSection.transform(new BundleInfo(rt));
		Bundle bundle = sectionResult.getBundle();

		List<AllergyIntolerance> allergyIntolerances = FHIRUtil.findResources(bundle, AllergyIntolerance.class);

		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		IdentifierMap<Integer> orderMap = getEntryOrder(allergyIntolerances);
		List<AllergyProblemAct> acts = new ArrayList<>();
		allergyIntolerances.forEach(r -> acts.add(null));
		section.getAllergyProblemActs().forEach(r -> {
			II ii = r.getIds().get(0);
			String root = ii.getRoot();
			String extension = ii.getExtension();
			if (extension == null) {
				Integer index = orderMap.get("AllergyIntolerance", root);
				acts.set(index.intValue(), r);
			} else {
				String value = extension;
				String system = vst.tOid2Url(root);
				Integer index = orderMap.get("AllergyIntolerance", system, value);
				acts.set(index.intValue(), r);
			}
		});
		section.getEntries().clear();
		acts.forEach(act -> {
			Entry entry = factories.base.createEntry();
			entry.setAct(act);
			section.getEntries().add(entry);
		});

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "AllergiesSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltAllergies = joltUtil.findResources("AllergyIntolerance");

		int count = allergyIntolerances.size();
		Assert.assertEquals("Allergy intolerance resource count", count, joltAllergies.size());

		for (int index = 0; index < count; ++index) {
			AllergyIntolerance allergy = allergyIntolerances.get(index);
			Map<String, Object> joltAllergy = joltAllergies.get(index);

			System.out.println(index);

			joltUtil.verify(allergy, joltAllergy);
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