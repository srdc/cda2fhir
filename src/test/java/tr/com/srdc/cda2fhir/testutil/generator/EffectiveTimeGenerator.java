package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.Map;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EffectiveTimeGenerator {
	private String lowNullFlavor;
	private String highNullFlavor;
	private String nullFlavor;

	private String lowValue;
	private String highValue;
	private String value;

	public EffectiveTimeGenerator() {
	}

	public EffectiveTimeGenerator(String lowValue) {
		this.lowValue = lowValue;
	}

	public EffectiveTimeGenerator(String lowValue, String highValue) {
		this.lowValue = lowValue;
		this.highValue = highValue;
	}

	@SuppressWarnings("unchecked")
	public EffectiveTimeGenerator(Map<String, Object> json) {
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

	public String getLowOrValue() {
		if (nullFlavor == null) {
			if (lowValue != null && lowNullFlavor == null) {
				return lowValue;
			}
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public boolean hasHigh() {
		return highValue != null && highNullFlavor == null;
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

	public void verifyLowOrValue(String fhirValue) {
		String lowOrValue = getLowOrValue();
		if (lowOrValue == null) {
			Assert.assertNull("No date or time", fhirValue);
		} else {
			String expected = FHIRUtil.toFHIRDatetime(lowOrValue);
			Assert.assertEquals("Effective time value", expected, fhirValue);
		}
	}

	public void verifyValue(String fhirValue) {
		if (this.value == null) {
			Assert.assertNull("No date or time", fhirValue);
		} else {
			String expected = FHIRUtil.toFHIRDatetime(value);
			Assert.assertEquals("Effective time value", expected, fhirValue);
		}
	}

	public static EffectiveTimeGenerator getValueOnlyInstance(String value) {
		EffectiveTimeGenerator result = new EffectiveTimeGenerator();
		result.value = value;
		return result;
	}
}
