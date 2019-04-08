package tr.com.srdc.cda2fhir.testutil.generator;

import java.math.BigDecimal;

import org.hl7.fhir.dstu3.model.Quantity;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PQGenerator {
	private BigDecimal value;
	private String unit;

	public PQGenerator() {
	}

	public PQ generate(CDAFactories factories) {
		PQ pq = factories.datatype.createPQ();

		if (value != null) {
			pq.setValue(value);
		}
		if (unit != null) {
			pq.setUnit(unit);
		}
		return pq;
	}

	public static PQGenerator getDefaultInstance() {
		PQGenerator ptg = new PQGenerator();

		ptg.value = new BigDecimal(124);
		ptg.unit = "kg";

		return ptg;
	}

	public static PQGenerator getFullInstance() {
		PQGenerator ptg = new PQGenerator();

		ptg.value = new BigDecimal(1289);
		ptg.unit = "kg";

		return ptg;
	}

	public void verify(Quantity quantity) {
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
