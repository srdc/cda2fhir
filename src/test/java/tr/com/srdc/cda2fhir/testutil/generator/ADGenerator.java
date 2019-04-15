package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Address;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ADXP;
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

	private List<ADXPGenerator> lines = new ArrayList<>();
	private ADXPGenerator city;
	private ADXPGenerator county;
	private ADXPGenerator country;
	private ADXPGenerator state;
	private ADXPGenerator postalCode;

	public ADGenerator() {
	}

	public void setLine(ADXPGenerator line) {
		lines.clear();
		lines.add(line);
	}

	public void setCity(ADXPGenerator city) {
		this.city = city;
	}

	public void setCounty(ADXPGenerator county) {
		this.county = county;
	}

	public void setCountry(ADXPGenerator country) {
		this.country = country;
	}

	public void setState(ADXPGenerator state) {
		this.state = state;
	}

	public void setPostalCode(ADXPGenerator postalCode) {
		this.postalCode = postalCode;
	}

	@SuppressWarnings("unchecked")
	public ADGenerator(Map<String, Object> json) {
		city = new ADXPGenerator((String) json.get("city"));
		Object rawPostalCode = json.get("postalCode");
		postalCode = rawPostalCode == null ? null : new ADXPGenerator(rawPostalCode.toString());
		county = new ADXPGenerator((String) json.get("county"));
		state = new ADXPGenerator((String) json.get("state"));
		List<Object> lineObject = (List<Object>) json.get("streetAddressLine");
		if (lineObject != null) {
			lineObject.forEach(element -> lines.add(new ADXPGenerator((String) element)));
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
			ADXP adxp = line.generate(factories);
			address.getStreetAddressLines().add(adxp);
		});

		if (city != null) {
			address.getCities().add(city.generate(factories));
		}

		if (county != null) {
			address.getCounties().add(county.generate(factories));
		}

		if (country != null) {
			address.getCountries().add(country.generate(factories));
		}

		if (state != null) {
			address.getStates().add(state.generate(factories));
		}

		if (postalCode != null) {
			address.getPostalCodes().add(postalCode.generate(factories));
		}

		return address;
	}

	public static ADGenerator getDefaultInstance() {
		ADGenerator ag = new ADGenerator();

		ag.lines.add(new ADXPGenerator(LINE));
		ag.city = new ADXPGenerator(CITY);
		ag.state = new ADXPGenerator(STATE);
		ag.postalCode = new ADXPGenerator(POSTAL_CODE);

		return ag;
	}

	public static ADGenerator getFullInstance() {
		ADGenerator ag = new ADGenerator();

		ag.lines.add(new ADXPGenerator(LINE));
		ag.lines.add(new ADXPGenerator(LINE_2));
		ag.city = new ADXPGenerator(CITY);
		ag.state = new ADXPGenerator(STATE);
		ag.county = new ADXPGenerator(COUNTY);
		ag.postalCode = new ADXPGenerator(POSTAL_CODE);
		ag.use = "H";

		return ag;
	}

	public void verify(Address address) {
		if (text == null) {
			Assert.assertTrue("Missing address text", !address.hasText());
		} else {
			Assert.assertEquals("Address text", text, address.getText());
		}

		BaseStringGenerator.verifyList(address.getLine(), lines);

		if (city == null) {
			Assert.assertTrue("Mssing address city", !address.hasCity());
		} else {
			city.verify(address.getCity());
		}

		if (county == null) {
			Assert.assertTrue("Missing address county", !address.hasDistrictElement());
		} else {
			county.verify(address.getDistrict());
		}

		if (country == null) {
			Assert.assertTrue("Missing address country", !address.hasCountry());
		} else {
			country.verify(address.getCountry());
		}

		if (state == null) {
			Assert.assertTrue("Missing address state", !address.hasState());
		} else {
			state.verify(address.getState());
		}

		if (postalCode == null) {
			Assert.assertTrue("Missing address postalCode", !address.hasPostalCode());
		} else {
			postalCode.verify(address.getPostalCode());
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
			List<Object> list = lines.stream().map(r -> r.getValue()).collect(Collectors.toList());
			result.put("line", list);
		}
		if (city != null) {
			result.put("city", city.getValue());
		}
		if (county != null) {
			result.put("district", county.getValue());
		}
		if (country != null) {
			result.put("country", country.getValue());
		}
		if (state != null) {
			result.put("state", state.getValue());
		}
		if (postalCode != null) {
			result.put("postalCode", postalCode.getValue());
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
		if (result.isEmpty()) {
			return null;
		}
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
