package tr.com.srdc.cda2fhir.testutil.generator;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public class STGenerator {
	private static int INDEX = 1;

	private String value;
	private String nullFlavor;

	public STGenerator(String value) {
		this.value = value;
	}

	public void setNullFlavor(String nullFlavor) {
		this.nullFlavor = nullFlavor;
	}

	public ST generate(CDAFactories factories) {
		ST st = factories.datatype.createST();

		if (value != null) {
			st.addText(value);
		}

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			st.setNullFlavor(nf);
		}

		return st;
	}

	public static STGenerator getNextInstance() {
		String value = "ST Text " + INDEX;
		++INDEX;
		return new STGenerator(value);
	}

	public void verify(String text) {
		if (value == null || nullFlavor != null) {
			Assert.assertNull("No text", text);
		} else {
			Assert.assertEquals("Text", value, text);
		}
	}
}
