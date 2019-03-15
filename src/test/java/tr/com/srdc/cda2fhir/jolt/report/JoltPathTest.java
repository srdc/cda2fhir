package tr.com.srdc.cda2fhir.jolt.report;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.bazaarvoice.jolt.JsonUtils;

public class JoltPathTest {
	final private static String PATH = "src/test/resources/jolt-verify/report/jolt-path-0/";

	@Test
	public void test0() throws Exception {
		Map<String, Object> jsonACA = JsonUtils.filepathToMap(PATH + "AllergyConcernAct.json");
		Map<String, Object> jsonAIO = JsonUtils.filepathToMap(PATH + "AllergyIntoleranceObservation.json");
		Map<String, Object> jsonETV = JsonUtils.filepathToMap(PATH + "EffectiveTimeLowOrValue.json");

		JoltPath jpathACA = JoltPath.getInstance(jsonACA);
		JoltPath jpathAIO = JoltPath.getInstance(jsonAIO);
		JoltPath jpathETV = JoltPath.getInstance(jsonETV);

		Map<String, JoltPath> expansionMap = new HashMap<String, JoltPath>();
		expansionMap.put("AllergyIntoleranceObservation", jpathAIO);
		expansionMap.put("EffectiveTimeLowOrValue", jpathETV);

		jpathACA.expandLinks(expansionMap);
		jpathACA.conditionalize();

		Table table = jpathACA.toTable();

		System.out.println(table.toString());
	}
}
