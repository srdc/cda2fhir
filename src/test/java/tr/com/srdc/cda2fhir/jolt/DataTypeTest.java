package tr.com.srdc.cda2fhir.jolt;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.JoltUtil;

public class DataTypeTest {
	private static void runDataTypeTests(String dataType, boolean checkNullflavor) throws Exception {
		List<Object> chainrSpecJSON = JoltUtil.getDataTypeTemplate(dataType);
		Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

		JSONArray testCases = JoltUtil.getDataTypeTestCases(dataType);
		for (int index = 0; index < testCases.length(); ++index) {
			JSONObject testCase = testCases.getJSONObject(index);
			JSONObject inputJSON = testCase.getJSONObject("input");
			JSONObject expectedJSON = testCase.optJSONObject("expected");

			String input = inputJSON.toString();
			Object inputObject = JsonUtils.jsonToObject(input);
			Object actualObject = chainr.transform(inputObject);

			if (expectedJSON == null) {
				Assert.assertNull(dataType + " null test case " + index, actualObject);
			} else {
				Assert.assertNotNull(dataType + " not null test case " + index, actualObject);
				String actual = JsonUtils.toJsonString(actualObject);
				String expected = expectedJSON.toString();
				JSONAssert.assertEquals(dataType + " test case " + index, expected, actual, true);
			}
		}

		if (checkNullflavor) {
			JSONObject testCase = testCases.getJSONObject(0);
			JSONObject inputJSON = testCase.getJSONObject("input");
			inputJSON.put("nullFlavor", "UNK");
			String input = inputJSON.toString();
			Object inputObject = JsonUtils.jsonToObject(input);
			Object actualObject = chainr.transform(inputObject);
			Assert.assertNull("Null flavored data", actualObject);
		}
	}

	@Test
	public void testID() throws Exception {
		runDataTypeTests("ID", false);
	}

	@Test
	public void testCD() throws Exception {
		runDataTypeTests("CD", false);
	}

	@Test
	public void testCD2() throws Exception {
		runDataTypeTests("CD2", false);
	}

	@Test
	public void testTEL() throws Exception {
		runDataTypeTests("TEL", true);
	}

	@Test
	public void testAD() throws Exception {
		runDataTypeTests("AD", true);
	}

	@Test
	public void testPN() throws Exception {
		runDataTypeTests("PN", true);
	}

	@Test
	public void testIVL_TSPeriod() throws Exception {
		runDataTypeTests("IVL_TSPeriod", true);
	}

	@Test
	public void testIndicationEffectiveTime() throws Exception {
		runDataTypeTests("intermediate/IndicationEffectiveTime", true);
	}
}
