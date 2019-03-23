package tr.com.srdc.cda2fhir.testutil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.json.JSONObject;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;

public class TELGenerator {
	private static Map<String, Object> contactPointSystemMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactPointSystem.json");
	private static Map<String, Object> contactPointUseMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactPointUse.json");

	private static final String VALUE = "tel:+1(555)555-1009";
	private static final String USE = "WP";
	// private static final String START = "20190106";
	// private static final String END = "20190606";

	private String value;
	private String use;
	private String start;
	private String end;

	public TELGenerator() {
	}

	public TELGenerator(String value) {
		this.value = value;
	}

	public TELGenerator(JSONObject json) {
		value = json.optString("value");
		if (value.isEmpty()) {
			value = null;
		}
	}

	public void set(String use) {
		this.use = use;
	}

	public TEL generate(CDAFactories factories) {
		TEL telecom = factories.datatype.createTEL();

		if (value != null) {
			telecom.setValue(value);
		}
		if (use != null) {
			TelecommunicationAddressUse telUse = TelecommunicationAddressUse.get(use);
			Assert.assertNotNull("Translated use", telUse);
			telecom.getUses().add(telUse);
		}
		if (start != null) {
			SXCM_TS sxcm = factories.datatype.createSXCM_TS();
			sxcm.setValue(start);
			telecom.getUseablePeriods().add(sxcm);
		}
		if (end != null) {
			SXCM_TS sxcm = factories.datatype.createSXCM_TS();
			sxcm.setValue(end);
			telecom.getUseablePeriods().add(sxcm);
		}
		return telecom;
	}

	public static TELGenerator getDefaultInstance() {
		TELGenerator tg = new TELGenerator();

		tg.value = VALUE;

		return tg;
	}

	public static TELGenerator getFullInstance() {
		TELGenerator tg = new TELGenerator();

		tg.value = VALUE;
		tg.use = USE;
		// tg.start = START;
		// tg.end = END;

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
					expected = Config.DEFAULT_CONTACT_POINT_SYSTEM.toCode();
				}
				Assert.assertEquals("Contact value system", expected, actual);
			} else {
				String expected = Config.DEFAULT_CONTACT_POINT_SYSTEM.toCode();
				Assert.assertEquals("Contact point system", expected, contactPoint.getSystem().toCode());
				Assert.assertEquals("Contact point value", value, contactPoint.getValue());
			}
		}
		if (use == null) {
			Assert.assertTrue("Contact point use missing", !contactPoint.hasUse());
		} else {
			String actual = contactPoint.getUse().toCode();
			String expected = (String) contactPointUseMap.get(use);
			Assert.assertEquals("Contact point use", expected, actual);
		}
		/*
		 * For some reason xml changes for use attribute when start/end is set. if
		 * (start != null) { String actual =
		 * contactPoint.getPeriod().getStartElement().getValueAsString(); String
		 * expected = start.substring(0, 4) + "-" + start.substring(4, 6) + "-" +
		 * start.substring(6, 8); Assert.assertEquals("Contact point period start",
		 * expected, actual); } if (end != null) { String actual =
		 * contactPoint.getPeriod().getEndElement().getValueAsString(); String expected
		 * = end.substring(0, 4) + "-" + end.substring(4, 6) + "-" + end.substring(6,
		 * 8); Assert.assertEquals("Contact point period end", expected, actual); }
		 */
	}

	public static Set<String> getAvailableSystems() {
		return Collections.unmodifiableSet(contactPointSystemMap.keySet());
	}

	public static Set<String> getAvailableUses() {
		return Collections.unmodifiableSet(contactPointUseMap.keySet());
	}
}
