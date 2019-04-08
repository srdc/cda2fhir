package tr.com.srdc.cda2fhir.testutil.generator;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public class EDGenerator {
	private static int INDEX = 1;

	private String value;
	private String nullFlavor;

	public EDGenerator(String value) {
		this.value = value;
	}

	public void setNullFlavor(String nullFlavor) {
		this.nullFlavor = nullFlavor;
	}

	public ED generate(CDAFactories factories) {
		ED ed = factories.datatype.createED();

		if (value != null) {
			ed.addText(value);
		}

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			ed.setNullFlavor(nf);
		}

		return ed;
	}

	public static EDGenerator getNextInstance() {
		String value = "ED Text " + INDEX;
		++INDEX;
		return new EDGenerator(value);
	}

	public void verify(byte[] content) {
		if (value == null || nullFlavor != null) {
			Assert.assertNull("No text", content);
		} else {
			Assert.assertEquals("Text", value, new String(content));
		}
	}

	public void verify(String content) {
		if (value == null || nullFlavor != null) {
			Assert.assertNull("No text", content);
		} else {
			Assert.assertEquals("Text", value, content);
		}
	}
}
