package tr.com.srdc.cda2fhir.testutil.generator;

import java.math.BigDecimal;

import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class IVL_PQSimpleQuantityGenerator {
	private BigDecimal value;
	private String unit;

	public IVL_PQSimpleQuantityGenerator() {
	}

	public IVL_PQ generate(CDAFactories factories) {
		IVL_PQ pq = factories.datatype.createIVL_PQ();

		if (value != null) {
			pq.setValue(value);
		}
		if (unit != null) {
			pq.setUnit(unit);
		}
		return pq;
	}

	public static IVL_PQSimpleQuantityGenerator getDefaultInstance() {
		IVL_PQSimpleQuantityGenerator ptg = new IVL_PQSimpleQuantityGenerator();

		ptg.value = new BigDecimal(124);
		ptg.unit = "kg";

		return ptg;
	}

	public static IVL_PQSimpleQuantityGenerator getFullInstance() {
		IVL_PQSimpleQuantityGenerator ptg = new IVL_PQSimpleQuantityGenerator();

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
