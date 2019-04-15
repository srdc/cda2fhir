package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
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

public class MedicationsSectionTest {
	private static class LocalResourceTransformer extends ResourceTransformerImpl {
		private static final long serialVersionUID = 1L;

		private List<MedicationActivity> medActivities = new ArrayList<>();

		public void clearEntries() {
			medActivities.clear();
		}

		public List<MedicationActivity> getEntries() {
			return Collections.unmodifiableList(medActivities);
		}

		@Override
		public EntryResult tMedicationActivity2MedicationStatement(MedicationActivity medActivity,
				IBundleInfo bundleInfo) {
			medActivities.add(medActivity);
			return super.tMedicationActivity2MedicationStatement(medActivity, bundleInfo);
		}
	}

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationsSection/";

	private static CDAFactories factories;
	private static LocalResourceTransformer rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new LocalResourceTransformer();
		factories = CDAFactories.init();
	}

	private static void reorderSection(MedicationsSection section) {
		section.getEntries().clear();
		rt.getEntries().forEach(ma -> {
			Entry entry = factories.base.createEntry();
			entry.setSubstanceAdministration(ma);
			section.getEntries().add(entry);
		});
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		MedicationsSection section = cda.getMedicationsSection();
		ICDASection cdaSection = CDASectionTypeEnum.MEDICATIONS_SECTION.toCDASection(section);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		BundleInfo bundleInfo = new BundleInfo(rt);

		Map<String, String> idedAnnotations = EMFUtil.findReferences(section.getText());
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		rt.clearEntries();
		ISectionResult sectionResult = cdaSection.transform(bundleInfo);

		Bundle bundle = sectionResult.getBundle();

		List<MedicationStatement> medStatements = FHIRUtil.findResources(bundle, MedicationStatement.class);
		List<MedicationRequest> medRequests = FHIRUtil.findResources(bundle, MedicationRequest.class);

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderSection(section);

		String caseName = sourceName.substring(0, sourceName.length() - 4);
		File xmlFile = CDAUtilExtension.writeAsXML(section, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltSectionResult(xmlFile, "MedicationsSection", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltMedStatements = joltUtil.findResources("MedicationStatement");

		int count = medStatements.size();
		Assert.assertEquals("Med statement resource count", count, joltMedStatements.size());

		for (int index = 0; index < count; ++index) {
			MedicationStatement medStatement = medStatements.get(index);
			Map<String, Object> joltMedStatement = joltMedStatements.get(index);
			joltUtil.verify(medStatement, joltMedStatement);
		}

		List<Map<String, Object>> joltMedRequests = joltUtil.findResources("MedicationRequest");

		int countRequest = medRequests.size();
		Assert.assertEquals("Med request resource count", countRequest, joltMedRequests.size());

		for (int index = 0; index < countRequest; ++index) {
			MedicationRequest medRequest = medRequests.get(index);
			Map<String, Object> joltMedRequest = joltMedRequests.get(index);
			joltUtil.verify(medRequest, joltMedRequest);
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

	@Ignore
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