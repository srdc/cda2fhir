package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Period;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class IVL_TSPeriodGenerator {
	private String lowNullFlavor;
	private String highNullFlavor;
	private String nullFlavor;

	private String lowValue;
	private String highValue;
	private String value;

	public IVL_TSPeriodGenerator() {
	}

	public IVL_TSPeriodGenerator(String lowValue, String highValue) {
		this.lowValue = lowValue;
		this.highValue = highValue;
	}

	@SuppressWarnings("unchecked")
	public IVL_TSPeriodGenerator(Map<String, Object> json) {
		nullFlavor = (String) json.get("nullFlavor");
		value = (String) json.get("value");
		Map<String, Object> low = (Map<String, Object>) json.get("low");
		if (low != null) {
			lowNullFlavor = (String) low.get("nullFlavor");
			lowValue = (String) low.get("value");
		}
		Map<String, Object> high = (Map<String, Object>) json.get("high");
		if (high != null) {
			highNullFlavor = (String) high.get("nullFlavor");
			highValue = (String) high.get("value");
		}
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

	public static IVL_TSPeriodGenerator getDefaultInstance() {
		IVL_TSPeriodGenerator ivlTs = new IVL_TSPeriodGenerator();

		ivlTs.lowValue = "20080501114500-0800";
		ivlTs.highValue = "20110812170500-0800";

		return ivlTs;
	}

	public static IVL_TSPeriodGenerator getFullInstance() {
		return getDefaultInstance();
	}

	public void verify(Period period) {
		if (nullFlavor != null) {
			Assert.assertTrue("Missing period start", !period.hasStartElement());
			Assert.assertTrue("Missing period end", !period.hasEndElement());
			return;
		}

		if (lowNullFlavor == null && lowValue != null) {
			String startValue = FHIRUtil.toCDADatetime(period.getStartElement().asStringValue());
			Assert.assertEquals("Period start value", lowValue, startValue);
		} else if (lowNullFlavor == null && lowValue == null && highNullFlavor == null && highValue == null
				&& value != null) {
			String startValue = FHIRUtil.toCDADatetime(period.getStartElement().asStringValue());
			Assert.assertEquals("Period start value", value, startValue);
		} else {
			Assert.assertTrue("Missing period start", !period.hasStartElement());
		}

		if (highNullFlavor == null && nullFlavor == null && highValue != null) {
			String endValue = FHIRUtil.toCDADatetime(period.getEndElement().asStringValue());
			Assert.assertEquals("Period end value", highValue, endValue);
		} else {
			Assert.assertTrue("Missing period end", !period.hasEndElement());
		}
	}

	public Map<String, Object> toJson() {
		if (nullFlavor != null) {
			return null;
		}

		Map<String, Object> result = new LinkedHashMap<>();

		if (lowNullFlavor == null && lowValue != null) {
			result.put("start", FHIRUtil.toFHIRDatetime(lowValue));
		}
		if (highNullFlavor == null && highValue != null) {
			result.put("end", FHIRUtil.toFHIRDatetime(highValue));
		}
		if (lowNullFlavor == null && lowValue == null && highNullFlavor == null && highValue == null && value != null) {
			result.put("start", FHIRUtil.toFHIRDatetime(value));
		}
		if (result.isEmpty())
			return null;
		return result;
	}
}
