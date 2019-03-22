package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

public class TELGenerator {
	private static final String VALUE = "tel:+1(555)555-1009";

	private String value;

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
		String[] valuePieces = value == null ? null : value.split(":");
		String actualValue = value == null ? null : valuePieces[1];
		Assert.assertEquals("Phone number", contactPoint.getValue(), actualValue);
	}
}
