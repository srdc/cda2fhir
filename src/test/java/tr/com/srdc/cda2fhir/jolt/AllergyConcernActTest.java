package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergyConcernActTest {
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
	}

	private static void putReference(Map<String, Object> joltResult, String property, Reference reference) {
		Map<String, Object> r = new LinkedHashMap<String, Object>();
		r.put("reference", reference.getReference());
		joltResult.put(property, r);
	}

	private static void compare(Map<String, Object> joltResult, AllergyIntolerance cda2FHIRResult) throws Exception {
		joltResult.put("id", cda2FHIRResult.getId().split("/")[1]); // ids are not expected to be equal
		putReference(joltResult, "patient", cda2FHIRResult.getPatient()); // patient is not yet implemented
		if (cda2FHIRResult.hasRecorder()) {
			putReference(joltResult, "recorder", cda2FHIRResult.getRecorder()); // do not check recorder for now, ids are
		}																// different
		String expected = FHIRUtil.encodeToJSON(cda2FHIRResult);
		String actual = JsonUtils.toJsonString(joltResult);
		JSONAssert.assertEquals("Jolt output vs CDA2FHIR output", expected, actual, true);
	}

	private static void comparePractitioner(Map<String, Object> joltResult, Practitioner cda2FHIRResult) throws Exception {
		joltResult.put("id", cda2FHIRResult.getId().split("/")[1]); // ids are not expected to be equal
																			// different
		String expected = FHIRUtil.encodeToJSON(cda2FHIRResult);
		String actual = JsonUtils.toJsonString(joltResult);
		JSONAssert.assertEquals("Jolt output vs CDA2FHIR output", expected, actual, true);
	}

	@SuppressWarnings("unchecked")
	private static void testAllergies(String sourceName) throws Exception {
		BundleUtil util = BundleUtil.getInstance(sourceName);

		OrgJsonUtil jsonUtil = OrgJsonUtil.readXML("src/test/resources/" + sourceName);

		JSONArray allergies = jsonUtil.getAllergiesSectionEntries();

		int count = allergies == null ? 0 : allergies.length();
		util.checkResourceCount(AllergyIntolerance.class, count);

		String baseName = "src/test/resources/output/jolt/" + sourceName.substring(0, sourceName.length() - 4);

		for (int index = 0; index < count; ++index) {
			JSONObject entry = allergies.getJSONObject(index);
			String cdaJSONFile = baseName + " allergies entry " + index + ".json";
			FileUtils.writeStringToFile(new File(cdaJSONFile), entry.toString(4), Charset.defaultCharset());

			List<Object> joltResultList = (List<Object>) TransformManager.transformEntryInFile("AllergyConcernAct",
					cdaJSONFile);
			String prettyJson = JsonUtils.toPrettyJsonString(joltResultList);
			String resultFile = baseName + " allergies entry " + index + " - result" + ".json";
			FileUtils.writeStringToFile(new File(resultFile), prettyJson, Charset.defaultCharset());

			Map<String, Object> joltResult = TransformManager.chooseResource(joltResultList, "AllergyIntolerance");
			List<Object> identifiers = (List<Object>) joltResult.get("identifier");
			AllergyIntolerance cda2FHIRResult = (AllergyIntolerance) util.getFromJSONArray("AllergyIntolerance",
					identifiers);
			String cda2FHIRFile = baseName + " allergies entry " + index + " - ccda2fhir" + ".json";
			FileUtils.writeStringToFile(new File(cda2FHIRFile), FHIRUtil.encodeToJSON(cda2FHIRResult),
					Charset.defaultCharset());

			compare(joltResult, cda2FHIRResult);

			Map<String, Object> joltPractitioner = TransformManager.chooseResource(joltResultList, "Practitioner");
			if (joltPractitioner == null) {
				Reference recorder = cda2FHIRResult.getRecorder();
				Assert.assertNull("Practitioner reference", recorder.getReference());	
			} else {
				List<Object> identifiersPractitioner = (List<Object>) joltPractitioner.get("identifier");
				Practitioner cda2FHIRPractitioner = (Practitioner) util.getFromJSONArray("Practitioner",
					identifiersPractitioner);
				String cda2FHIRPractitionerFile = baseName + " allergies entry practitioner" + index + " - ccda2fhir"
					+ ".json";
				FileUtils.writeStringToFile(new File(cda2FHIRPractitionerFile), FHIRUtil.encodeToJSON(cda2FHIRPractitioner),
					Charset.defaultCharset());

				comparePractitioner(joltPractitioner, cda2FHIRPractitioner);
			}
		}
	}

	@Test
	public void testSample1() throws Exception {
		testAllergies("C-CDA_R2-1_CCD.xml");
	}

	@Test
	public void testSample2() throws Exception {
		testAllergies("170.315_b1_toc_gold_sample2_v1.xml");
	}

	@Test
	public void testSample3() throws Exception {
		testAllergies("Vitera_CCDA_SMART_Sample.xml");
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		testAllergies("Epic/DOC0001.XML");
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		testAllergies("Epic/DOC0001 2.XML");
	}

	@Ignore
	@Test
	public void testEpicSample3() throws Exception {
		testAllergies("Epic/DOC0001 3.XML");
	}

	@Ignore
	@Test
	public void testEpicSample4() throws Exception {
		testAllergies("Epic/DOC0001 4.XML");
	}

	@Ignore
	@Test
	public void testEpicSample5() throws Exception {
		testAllergies("Epic/DOC0001 5.XML");
	}

	@Ignore
	@Test
	public void testEpicSample6() throws Exception {
		testAllergies("Epic/DOC0001 6.XML");
	}

	@Ignore
	@Test
	public void testEpicSample7() throws Exception {
		testAllergies("Epic/DOC0001 7.XML");
	}

	@Ignore
	@Test
	public void testEpicSample8() throws Exception {
		testAllergies("Epic/DOC0001 8.XML");
	}

	@Ignore
	@Test
	public void testEpicSample9() throws Exception {
		testAllergies("Epic/DOC0001 9.XML");
	}

	@Ignore
	@Test
	public void testEpicSample10() throws Exception {
		testAllergies("Epic/DOC0001 10.XML");
	}

	@Ignore
	@Test
	public void testEpicSample11() throws Exception {
		testAllergies("Epic/DOC0001 11.XML");
	}

	@Ignore
	@Test
	public void testEpicSample12() throws Exception {
		testAllergies("Epic/DOC0001 12.XML");
	}

	@Ignore
	@Test
	public void testEpicSample13() throws Exception {
		testAllergies("Epic/DOC0001 13.XML");
	}

	@Ignore
	@Test
	public void testEpicSample14() throws Exception {
		testAllergies("Epic/DOC0001 14.XML");
	}

	@Ignore
	@Test
	public void testEpicSample15() throws Exception {
		testAllergies("Epic/DOC0001 15.XML");
	}

	@Ignore
	@Test
	public void testEpicSample16() throws Exception {
		testAllergies("Epic/HannahBanana_EpicCCD.xml");
	}

	@Ignore
	@Test
	public void testCernerSample1() throws Exception {
		testAllergies("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
	}

	@Ignore
	@Test
	public void testCernerSample2() throws Exception {
		testAllergies("Cerner/Encounter-RAKIA_TEST_DOC00001.XML");
	}
}
