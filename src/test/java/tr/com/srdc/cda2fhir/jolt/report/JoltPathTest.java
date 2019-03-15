package tr.com.srdc.cda2fhir.jolt.report;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltPathTest {
	final private static String PATH = "src/test/resources/jolt-verify/report/jolt-path-0/";

	@Test
	public void test0() throws Exception {
		Map<String, Object> jsonACA = JsonUtils.filepathToMap(PATH + "AllergyConcernAct.json");
		Map<String, Object> jsonAIO = JsonUtils.filepathToMap(PATH + "AllergyIntoleranceObservation.json");
		Map<String, Object> jsonETV = JsonUtils.filepathToMap(PATH + "EffectiveTimeLowOrValue.json");

		RootNode jpathACA = JoltPath.getInstance(jsonACA);
		RootNode jpathAIO = JoltPath.getInstance(jsonAIO);
		RootNode jpathETV = JoltPath.getInstance(jsonETV);

		Map<String, RootNode> expansionMap = new HashMap<String, RootNode>();
		expansionMap.put("AllergyIntoleranceObservation", jpathAIO);
		expansionMap.put("EffectiveTimeLowOrValue", jpathETV);

		jpathACA.expandLinks(expansionMap);
		jpathACA.conditionalize();

		Table table = jpathACA.toTable();

		System.out.println(table.toString());
	}
}
