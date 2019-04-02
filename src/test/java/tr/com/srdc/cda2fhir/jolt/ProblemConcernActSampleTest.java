package tr.com.srdc.cda2fhir.jolt;

import java.util.List;

import org.hl7.fhir.dstu3.model.Resource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActSampleTest {
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
	}

	private static int getProblemObservationCount(JSONObject problem) throws JSONException {
		JSONObject act = problem.getJSONObject("act");
		JSONArray ers = act.getJSONArray("entryRelationship");
		int result = 0;
		for (int index = 0; index < ers.length(); ++index) {
			JSONObject er = ers.getJSONObject(index);
			String typeCode = er.optString("typeCode");
			if ("SUBJ".equals(typeCode)) {
				JSONObject observation = er.optJSONObject("observation");
				if (observation != null) {
					++result;
				}
			}
		}
		return result;
	}

	private static int getActualResourceCount(JSONArray problems) throws JSONException {
		if (problems == null) {
			return 0;
		}
		int result = 0;
		for (int index = 0; index < problems.length(); ++index) {
			result += getProblemObservationCount(problems.getJSONObject(index));
		}
		return result;
	}

	private static void testAllergies(String sourceName) throws Exception {
		String baseName = "src/test/resources/output/jolt/" + sourceName.substring(0, sourceName.length() - 4);
		BundleUtil util = BundleUtil.getInstance(sourceName);
		FHIRUtil.printJSON(util.getBundle(), baseName + ".json");

		OrgJsonUtil jsonUtil = OrgJsonUtil.readXML("src/test/resources/" + sourceName);

		JSONArray problems = jsonUtil.getProblemSectionEntries();

		List<Resource> sectionResources = util.getSectionResources("11450-4");

		int count = getActualResourceCount(problems);
		Assert.assertEquals("Section resource count", count, sectionResources.size());
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
