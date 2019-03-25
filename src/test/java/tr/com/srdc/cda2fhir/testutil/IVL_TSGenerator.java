package tr.com.srdc.cda2fhir.testutil;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Period;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class IVL_TSGenerator {
	private String lowNullFlavor;
	private String highNullFlavor;
	private String nullFlavor;

	private String lowValue;
	private String highValue;
	private String value;

	public IVL_TSGenerator() {
	}

	public IVL_TSGenerator(Map<String, Object> json) {
	}

	public IVL_TS generate(CDAFactories factories) {
		IVL_TS ivlTs = factories.datatype.createIVL_TS();
		if (lowValue != null) {
			IVXB_TS ivxbTs = factories.datatype.createIVXB_TS();
			ivxbTs.setValue(lowValue);
			if (lowNullFlavor != null) {
				NullFlavor nf = NullFlavor.get(lowNullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				ivxbTs.setNullFlavor(nf);
			}
			ivlTs.setLow(ivxbTs);
		}
		if (highValue != null) {
			IVXB_TS ivxbTs = factories.datatype.createIVXB_TS();
			ivxbTs.setValue(highValue);
			if (highNullFlavor != null) {
				NullFlavor nf = NullFlavor.get(highNullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				ivxbTs.setNullFlavor(nf);
			}
			ivlTs.setHigh(ivxbTs);
		}
		if (value != null) {
			ivlTs.setValue(value);
			if (nullFlavor != null) {
				NullFlavor nf = NullFlavor.get(nullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				ivlTs.setNullFlavor(nf);
			}
		}
		return ivlTs;
	}

	public static IVL_TSGenerator getDefaultInstance() {
		IVL_TSGenerator ivlTs = new IVL_TSGenerator();

		ivlTs.lowValue = "20080501114500-0800";
		ivlTs.highValue = "20110812170500-0800";

		return ivlTs;
	}

	public static IVL_TSGenerator getFullInstance() {
		return getDefaultInstance();
	}

	public void verify(Period period) {
		boolean lowChecked = false;
		if (lowNullFlavor == null && nullFlavor == null && lowValue != null) {
			lowChecked = true;
			String startValue = FHIRUtil.toCDADatetime(period.getStartElement().asStringValue());
			Assert.assertEquals("Period start value", lowValue, startValue);
		}
		boolean highChecked = false;
		if (highNullFlavor == null && nullFlavor == null && highValue != null) {
			highChecked = true;
			String endValue = FHIRUtil.toCDADatetime(period.getEndElement().asStringValue());
			Assert.assertEquals("Period end value", highValue, endValue);
		} else {
			Assert.assertTrue("Missing period end", !period.hasEndElement());
		}
		if (lowChecked)
			return;

		if (!highChecked && nullFlavor == null && value != null) {
			String startValue = period.getStartElement().asStringValue();
			Assert.assertEquals("Period start value", value, startValue);
		} else {
			Assert.assertTrue("Missing period start", !period.hasStartElement());
		}
	}

	public Map<String, Object> toJson() {
		return null;
	}
}
