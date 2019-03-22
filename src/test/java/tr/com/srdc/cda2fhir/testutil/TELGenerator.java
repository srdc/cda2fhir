package tr.com.srdc.cda2fhir.testutil;

import java.util.Map;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.json.JSONObject;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.bazaarvoice.jolt.JsonUtils;

public class TELGenerator {
	private static Map<String, Object> contactPointSystemMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactPointSystem.json");
	private static final String VALUE = "tel:+1(555)555-1009";

	private String value;

	public TELGenerator() {
	}

	public TELGenerator(JSONObject json) {
		value = json.optString("value");
		if (value.isEmpty()) {
			value = null;
		}
	}

	public TEL generate(CDAFactories factories) {
		TEL telecom = factories.datatype.createTEL();

		if (value != null) {
			telecom.setValue(value);
		}

		return telecom;
	}

	public static TELGenerator getDefaultInstance() {
		TELGenerator tg = new TELGenerator();

		tg.value = VALUE;

		return tg;
	}

	public void verify(ContactPoint contactPoint) {
		if (value == null) {
			Assert.assertTrue("Contact point value missing", !contactPoint.hasValue());
			Assert.assertTrue("Contact point system missing", !contactPoint.hasSystem());
		} else {
			String[] valuePieces = value.split(":");

			if (valuePieces.length > 1) {
				Assert.assertEquals("Contact point value", contactPoint.getValue(), valuePieces[1]);
				String expected = (String) contactPointSystemMap.get(valuePieces[0]);
				String actual = contactPoint.getSystem().toCode();
				if (expected == null) {
					expected = "phone";
				}
				Assert.assertEquals("Contact value system", expected, actual);
			} else {
				Assert.assertTrue("Contact point system missing", !contactPoint.hasSystem());
				Assert.assertEquals("Contact point value", value, contactPoint.getValue());
			}
		}
	}
}
