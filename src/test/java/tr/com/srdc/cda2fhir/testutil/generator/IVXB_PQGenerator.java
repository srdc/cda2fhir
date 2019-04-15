package tr.com.srdc.cda2fhir.testutil.generator;

import java.math.BigDecimal;

import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class IVXB_PQGenerator {
	private BigDecimal value;
	private String unit;

	public IVXB_PQGenerator() {
	}

	public IVXB_PQGenerator(BigDecimal value, String unit) {
		this.value = value;
		this.unit = unit;
	}

	public IVXB_PQGenerator(int value, String unit) {
		this(new BigDecimal(value), unit);
	}

	public IVXB_PQ generate(CDAFactories factories) {
		IVXB_PQ pq = factories.datatype.createIVXB_PQ();

		if (value != null) {
			pq.setValue(value);
		}
		if (unit != null) {
			pq.setUnit(unit);
		}
		return pq;
	}

	public static IVXB_PQGenerator getDefaultInstance() {
		IVXB_PQGenerator ptg = new IVXB_PQGenerator();

		ptg.value = new BigDecimal(124);
		ptg.unit = "kg";

		return ptg;
	}

	public static IVXB_PQGenerator getFullInstance() {
		IVXB_PQGenerator ptg = new IVXB_PQGenerator();

		ptg.value = new BigDecimal(1289);
		ptg.unit = "kg";

		return ptg;
	}

	public void verify(SimpleQuantity quantity) {
		if (value == null) {
			Assert.assertTrue("No value", !quantity.hasValue());
		} else {
			Assert.assertEquals("Quantity value", value, quantity.getValue());
		}

		if (unit == null) {
			Assert.assertTrue("No quantity unit", !quantity.hasUnit());
		} else {
			Assert.assertEquals("Quantity unit", unit, quantity.getUnit());
		}
	}
}
