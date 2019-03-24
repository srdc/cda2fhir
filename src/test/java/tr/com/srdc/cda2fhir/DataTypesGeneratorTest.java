package tr.com.srdc.cda2fhir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.ADGenerator;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.testutil.PNGenerator;
import tr.com.srdc.cda2fhir.testutil.TELGenerator;
import tr.com.srdc.cda2fhir.transform.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.transform.IDataTypesTransformer;

public class DataTypesGeneratorTest {
	private static CDAFactories factories;
	private static IDataTypesTransformer dtt;

	private interface Verification {
		Map<String, Object> verify(Map<String, Object> input);
	}

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		dtt = new DataTypesTransformerImpl();
	}

	private static void verify(TELGenerator generator) {
		TEL tel = generator.generate(factories);
		ContactPoint contactPoint = dtt.tTEL2ContactPoint(tel);
		generator.verify(contactPoint);
	}

	private static void verify(ADGenerator generator) {
		AD ad = generator.generate(factories);
		Address address = dtt.AD2Address(ad);
		generator.verify(address);
	}

	private static void verify(PNGenerator generator) {
		PN pn = generator.generate(factories);
		HumanName humanName = dtt.tEN2HumanName(pn);
		generator.verify(humanName);
	}

	private static final Map<String, Verification> verifications = new HashMap<>();
	static {
		verifications.put("TEL", input -> {
			TELGenerator generator = new TELGenerator(input);
			verify(generator);
			return generator.toJson();
		});
		verifications.put("AD", input -> {
			ADGenerator generator = new ADGenerator(input);
			verify(generator);
			return generator.toJson();
		});
		verifications.put("PN", input -> {
			PNGenerator generator = new PNGenerator(input);
			verify(generator);
			return generator.toJson();
		});
	}

	@SuppressWarnings("unchecked")
	private static void runTestCases(String name) throws Exception {
		List<Map<String, Object>> testCases = OrgJsonUtil.getDataTypeGeneratorTestCases(name);
		int numTestCases = testCases.size();
		Assert.assertTrue("Test cases exist", numTestCases > 0);
		for (int index = 0; index < numTestCases; ++index) {
			Map<String, Object> testCase = testCases.get(index);
			Map<String, Object> input = (Map<String, Object>) testCase.get("input");
			Map<String, Object> expectedRaw = (Map<String, Object>) testCase.get("expected");
			Map<String, Object> output = verifications.get(name).verify(input);
			String actual = JsonUtils.toJsonString(output);
			String expected = JsonUtils.toJsonString(expectedRaw);
			JSONAssert.assertEquals(name + " test case " + index, expected, actual, true);
		}
	}

	@Test
	public void testTEL() throws Exception {
		runTestCases("TEL");
		TELGenerator.getAvailableSystems().forEach(system -> {
			TELGenerator generator = new TELGenerator(system + ":" + "somevalue");
			verify(generator);
		});
		Set<String> availableUses = TELGenerator.getAvailableUses();
		availableUses.forEach(use -> {
			TELGenerator generator = TELGenerator.getDefaultInstance();
			generator.set(use);
			verify(generator);
		});
	}

	@Test
	public void testAD() throws Exception {
		runTestCases("AD");
		ADGenerator.getAvailableUses().forEach(use -> {
			ADGenerator generator = ADGenerator.getDefaultInstance();
			generator.setUse(use);
			verify(generator);
		});
	}

	@Test
	public void testPN() throws Exception {
		runTestCases("PN");
		ADGenerator.getAvailableUses().forEach(use -> {
			ADGenerator generator = ADGenerator.getDefaultInstance();
			generator.setUse(use);
			verify(generator);
		});
	}
}
