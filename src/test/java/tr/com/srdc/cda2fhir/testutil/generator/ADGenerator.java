package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ADGenerator {
	private static Map<String, Object> ADDRESS_TYPE = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AddressType.json");
	private static Map<String, Object> ADDRESS_USE = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AddressUse.json");

	private static final String LINE = "100 Aperture Drive";
	private static final String LINE_2 = "Suite #1245";
	private static final String CITY = "Cleveland";
	private static final String STATE = "Ohio";
	private static final String COUNTY = "Montgomery";
	private static final String POSTAL_CODE = "44101";

	private String text;
	private String use;

	private List<String> lines = new ArrayList<>();
	private String city;
	private String county;
	private String country;
	private String state;
	private String postalCode;

	public ADGenerator() {
	}

	@SuppressWarnings("unchecked")
	public ADGenerator(Map<String, Object> json) {
		city = (String) json.get("city");
		Object rawPostalCode = json.get("postalCode");
		postalCode = rawPostalCode == null ? null : rawPostalCode.toString();
		county = (String) json.get("county");
		state = (String) json.get("state");
		List<Object> lineObject = (List<Object>) json.get("streetAddressLine");
		if (lineObject != null) {
			lineObject.forEach(element -> lines.add((String) element));
		}
	}

	public void setUse(String use) {
		this.use = use;
	}

	public AD generate(CDAFactories factories) {
		AD address = factories.datatype.createAD();

		if (text != null) {
			address.addText(text);
		}

		if (use != null) {
			PostalAddressUse addressUse = PostalAddressUse.get(use);
			address.getUses().add(addressUse);
		}

		lines.forEach(line -> {
			address.addStreetAddressLine(line);
		});

		if (city != null) {
			address.addCity(city);
		}

		if (county != null) {
			address.addCounty(county);
		}

		if (country != null) {
			address.addCountry(country);
		}

		if (state != null) {
			address.addState(state);
		}

		if (postalCode != null) {
			address.addPostalCode(postalCode);
		}

		return address;
	}

	public static ADGenerator getDefaultInstance() {
		ADGenerator ag = new ADGenerator();

		ag.lines.add(LINE);
		ag.city = CITY;
		ag.state = STATE;
		ag.postalCode = POSTAL_CODE;

		return ag;
	}

	public static ADGenerator getFullInstance() {
		ADGenerator ag = new ADGenerator();

		ag.lines.add(LINE);
		ag.lines.add(LINE_2);
		ag.city = CITY;
		ag.state = STATE;
		ag.county = COUNTY;
		ag.postalCode = POSTAL_CODE;
		ag.use = "H";

		return ag;
	}

	public void verify(Address address) {
		if (text == null) {
			Assert.assertTrue("Missing address text", !address.hasText());
		} else {
			Assert.assertEquals("Address text", text, address.getText());
		}

		List<StringType> addressLines = address.getLine();
		int lineCount = lines.size();
		Assert.assertEquals("Address line count", lineCount, addressLines.size());
		for (int index = 0; index < lineCount; ++index) {
			Assert.assertEquals("Address line " + index, lines.get(index), addressLines.get(index).toString());
		}

		if (city == null) {
			Assert.assertTrue("Mssing address city", !address.hasCity());
		} else {
			Assert.assertEquals("Address city", city, address.getCity());
		}

		if (county == null) {
			Assert.assertTrue("Missing address county", !address.hasDistrictElement());
		} else {
			Assert.assertEquals("Address county", county, address.getDistrict());
		}

		if (country == null) {
			Assert.assertTrue("Missing address country", !address.hasCountry());
		} else {
			Assert.assertEquals("Address country", country, address.getCountry());
		}

		if (state == null) {
			Assert.assertTrue("Missing address state", !address.hasState());
		} else {
			Assert.assertEquals("Address state", state, address.getState());
		}

		if (postalCode == null) {
			Assert.assertTrue("Missing address postalCode", !address.hasPostalCode());
		} else {
			Assert.assertEquals("Address postalCode", postalCode, address.getPostalCode());
		}
		if (use != null) {
			String addressType = (String) ADDRESS_TYPE.get(use);
			if (addressType != null) {
				Assert.assertEquals("Address type", addressType, address.getType().toCode());
			} else {
				String addressUse = (String) ADDRESS_USE.get(use);
				if (addressUse != null) {
					Assert.assertEquals("Address use", addressUse, address.getUse().toCode());
				} else {
					Assert.assertEquals("Address default use", "temp", address.getUse().toCode());
				}
			}
		}
	}

	public Map<String, Object> toJson() {
		Map<String, Object> result = new LinkedHashMap<>();

		if (!lines.isEmpty()) {
			result.put("line", new ArrayList<Object>(lines));
		}
		if (city != null) {
			result.put("city", city);
		}
		if (county != null) {
			result.put("district", county);
		}
		if (country != null) {
			result.put("country", country);
		}
		if (state != null) {
			result.put("state", state);
		}
		if (postalCode != null) {
			result.put("postalCode", postalCode);
		}
		if (use != null) {
			String addressType = (String) ADDRESS_TYPE.get(use);
			if (addressType != null) {
				result.put("type", addressType);
			} else {
				String addressUse = (String) ADDRESS_USE.get(use);
				if (addressUse == null) {
					addressUse = "temp";
				}
				result.put("use", addressUse);
			}
		}
		if (result.isEmpty())
			return null;
		return result;
	}

	public static Set<String> getAvailableUses() {
		Set<String> result = new HashSet<>();
		result.addAll(ADDRESS_TYPE.keySet());
		result.addAll(ADDRESS_USE.keySet());
		return result;
	}

	public static void verifyList(List<Address> actual, List<ADGenerator> expected) {
		Assert.assertEquals("Address count", expected.size(), actual.size());
		for (int index = 0; index < actual.size(); ++index) {
			expected.get(index).verify(actual.get(index));
		}
	}
}
