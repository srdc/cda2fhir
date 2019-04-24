package tr.com.srdc.cda2fhir.testutil.generator;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class TSGenerator {
	private static int INDEX = 1;

	private String value;
	private String nullFlavor;

	public TSGenerator(String value) {
		this.value = value;
	}

	public void setNullFlavor(String nullFlavor) {
		this.nullFlavor = nullFlavor;
	}

	protected TS create(CDAFactories factories) {
		return factories.datatype.createTS();
	}

	protected void fill(TS ts) {
		if (value != null) {
			ts.setValue(value);
		}

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			ts.setNullFlavor(nf);
		}
	}

	public TS generate(CDAFactories factories) {
		TS ts = create(factories);
		fill(ts);
		return ts;
	}

	public static TSGenerator getNextInstance() {
		String value = "2019011" + INDEX;
		++INDEX;
		return new TSGenerator(value);
	}

	public void verify(String dateTime) {
		if (value == null || nullFlavor != null) {
			Assert.assertNull("No datetime", dateTime);
		} else {
			String expected = FHIRUtil.toFHIRDatetime(value);
			Assert.assertEquals("Date time", expected, dateTime);
		}
	}
}
