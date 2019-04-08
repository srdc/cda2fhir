package tr.com.srdc.cda2fhir.testutil.generator;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public class BLGenerator {
	private Boolean value;
	private String nullFlavor;

	public BLGenerator(boolean value) {
		this.value = value;
	}

	public void setNullFlavor(String nullFlavor) {
		this.nullFlavor = nullFlavor;
	}

	public BL generate(CDAFactories factories) {
		BL bl = factories.datatype.createBL(value);

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			bl.setNullFlavor(nf);
		}

		return bl;
	}

	public static BLGenerator getNextInstance() {
		return new BLGenerator(true);
	}

	public void verify(Boolean v) {
		if (value == null || nullFlavor != null) {
			Assert.assertNull("No boolean", v);
		} else {
			Assert.assertEquals("BL value", value.booleanValue(), v.booleanValue());
		}
	}
}
