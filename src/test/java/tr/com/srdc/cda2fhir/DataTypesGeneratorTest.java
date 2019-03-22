package tr.com.srdc.cda2fhir;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.testutil.TELGenerator;
import tr.com.srdc.cda2fhir.transform.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.transform.IDataTypesTransformer;

public class DataTypesGeneratorTest {
	private static CDAFactories factories;
	private static IDataTypesTransformer dtt;

	private static final Map<String, Consumer<JSONObject>> verifications = new HashMap<>();
	static {
		verifications.put("TEL", json -> {
			TELGenerator generator = new TELGenerator(json);
			TEL tel = generator.generate(factories);
			ContactPoint contactPoint = dtt.tTEL2ContactPoint(tel);
			generator.verify(contactPoint);
		});
	}

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		dtt = new DataTypesTransformerImpl();
	}

	private static void runTest(String name) throws Exception {
		JSONArray testCases = OrgJsonUtil.getDataTypeTestCases("TEL");
		for (int index = 0; index < testCases.length(); ++index) {
			JSONObject testCase = testCases.getJSONObject(index);
			JSONObject inputJSON = testCase.getJSONObject("input");
			verifications.get(name).accept(inputJSON);
		}
	}

	@Test
	public void testTEL() throws Exception {
		runTest("TEL");
	}
}
