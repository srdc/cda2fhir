package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;

public class ADGenerator {
	private static final String LINE = "100 Aperture Drive";
	private static final String CITY = "Cleveland";
	private static final String STATE = "Ohio";
	private static final String POSTAL_CODE = "44101";

	private String text;

	private List<String> lines = new ArrayList<>();
	private String city;
	private String county;
	private String country;
	private String state;
	private String postalCode;

	public AD generate(CDAFactories factories) {
		AD address = factories.datatype.createAD();

		if (text != null) {
			address.addText(text);
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
			Assert.assertEquals("Address county", county, address.getDistrictElement());
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
	}
}
