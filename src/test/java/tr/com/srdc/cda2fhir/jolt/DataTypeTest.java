package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class DataTypeTest {
	private static void runDataTypeTests(String dataType) throws Exception {
		String templatePath = String.format("src/test/resources/jolt/data-type/%s.json", dataType);
		List<Object> chainrSpecJSON = JsonUtils.filepathToList(templatePath);
		Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

		String testCasesPath = String.format("src/test/resources/jolt-verify/data-type/%s.json", dataType);
		File file = new File(testCasesPath);
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		JSONArray testCases = new JSONArray(content);
		for (int index = 0; index < testCases.length(); ++index) {
			JSONObject testCase = testCases.getJSONObject(index);
			JSONObject inputJSON = testCase.getJSONObject("input");
			JSONObject expectedJSON = testCase.getJSONObject("expected");

			String input = inputJSON.toString();
			Object inputObject = JsonUtils.jsonToObject(input);
			Object actualObject = chainr.transform(inputObject);

			String actual = JsonUtils.toJsonString(actualObject);
			String expected = expectedJSON.toString();
			JSONAssert.assertEquals(dataType + " test case " + index, expected, actual, true);
		}
	}

	@Test
	public void testID() throws Exception {
		runDataTypeTests("ID");
	}

	@Test
	public void testCD() throws Exception {
		runDataTypeTests("CD");
	}

	@Test
	public void testTEL() throws Exception {
		runDataTypeTests("TEL");
	}
}
