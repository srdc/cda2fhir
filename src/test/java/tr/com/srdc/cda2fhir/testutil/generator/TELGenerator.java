package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class TELGenerator {
	private static Map<String, Object> contactPointSystemMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactPointSystem.json");
	private static Map<String, Object> contactPointUseMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactPointUse.json");

	private static final String VALUE = "tel:+1(555)555-1009";
	private static final String USE = "WP";

	private String value;
	private List<String> uses = new ArrayList<>();

	String nullFlavor;

	public TELGenerator() {
	}

	public TELGenerator(String value) {
		this.value = value;
	}

	public TELGenerator(Map<String, Object> json) {
		value = (String) json.get("value");
		String multiUse = (String) json.get("use");
		if (multiUse != null) {
			String[] pieces = multiUse.split(" ");
			for (int index = 0; index < pieces.length; ++index) {
				uses.add(pieces[index]);
			}
		}
		nullFlavor = (String) json.get("nullFlavor");
	}

	public void addUse(String use) {
		uses.add(use);
	}

	public TEL generate(CDAFactories factories) {
		TEL telecom = factories.datatype.createTEL();

		if (value != null) {
			telecom.setValue(value);
		}
		uses.forEach(use -> {
			TelecommunicationAddressUse telUse = TelecommunicationAddressUse.get(use);
			if (telUse == null) {
				throw new TestSetupException("Invalid CDA TEL use enumeration.");
			}
			telecom.getUses().add(telUse);
		});
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
		tg.uses.add(USE);

		return tg;
	}

	public void verify(ContactPoint contactPoint) {
		if (value == null || nullFlavor != null) {
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
		if (uses.isEmpty() || nullFlavor != null) {
			Assert.assertTrue("Contact point use missing", !contactPoint.hasUse());
		} else {
			String actual = contactPoint.getUse().toCode();
			String expected = (String) contactPointUseMap.get(uses.get(uses.size() - 1));
			if (expected == null) {
				expected = "temp";
			}
			Assert.assertEquals("Contact point use", expected, actual);
		}
	}

	public Map<String, Object> toJson() {
		if (nullFlavor != null) {
			return null;
		}

		Map<String, Object> result = new LinkedHashMap<>();

		if (value != null) {
			String[] valuePieces = value.split(":");

			if (valuePieces.length > 1) {
				String system = (String) contactPointSystemMap.get(valuePieces[0]);
				if (system == null) {
					system = Config.DEFAULT_CONTACT_POINT_SYSTEM.toCode();
				}
				result.put("system", system);
				result.put("value", valuePieces[1]);
			} else {
				String system = Config.DEFAULT_CONTACT_POINT_SYSTEM.toCode();
				result.put("system", system);
				result.put("value", value);
			}
		}
		if (!uses.isEmpty()) {
			String use = uses.get(uses.size() - 1);
			String jsonUse = (String) contactPointUseMap.get(use);
			if (jsonUse == null) {
				jsonUse = "temp";
			}
			result.put("use", jsonUse);
		}
		if (result.isEmpty())
			return null;
		return result;
	}

	public static Set<String> getAvailableSystems() {
		return Collections.unmodifiableSet(contactPointSystemMap.keySet());
	}

	public static Set<String> getAvailableUses() {
		return Collections.unmodifiableSet(contactPointUseMap.keySet());
	}
}
