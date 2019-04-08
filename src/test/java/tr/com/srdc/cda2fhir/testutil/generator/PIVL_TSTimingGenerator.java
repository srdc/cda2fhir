package tr.com.srdc.cda2fhir.testutil.generator;

import java.math.BigDecimal;

import org.hl7.fhir.dstu3.model.Timing;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PIVL_TSTimingGenerator {
	private BigDecimal value;
	private String unit;

	private IVL_TSPeriodGenerator ivlTsPeriodGenerator;

	public PIVL_TSTimingGenerator() {
	}

	public PIVL_TS generate(CDAFactories factories) {
		PIVL_TS pivlTs = factories.datatype.createPIVL_TS();
		PQ pq = factories.datatype.createPQ();
		pivlTs.setPeriod(pq);

		if (value != null) {
			pq.setValue(value);
		}
		if (unit != null) {
			pq.setUnit(unit);
		}
		if (ivlTsPeriodGenerator != null) {
			IVL_TS ivlTs = ivlTsPeriodGenerator.generate(factories);
			pivlTs.setPhase(ivlTs);
		}
		return pivlTs;
	}

	public static PIVL_TSTimingGenerator getDefaultInstance() {
		PIVL_TSTimingGenerator ptg = new PIVL_TSTimingGenerator();

		ptg.value = new BigDecimal(12);
		ptg.unit = "h";
		ptg.ivlTsPeriodGenerator = IVL_TSPeriodGenerator.getDefaultInstance();

		return ptg;
	}

	public static PIVL_TSTimingGenerator getFullInstance() {
		PIVL_TSTimingGenerator ptg = new PIVL_TSTimingGenerator();

		ptg.value = new BigDecimal(120);
		ptg.unit = "h";
		ptg.ivlTsPeriodGenerator = IVL_TSPeriodGenerator.getFullInstance();

		return getDefaultInstance();
	}

	public void verify(Timing timing) {
		if (value == null) {
			Assert.assertTrue("No timing repeat", !timing.hasRepeat());
		} else {
			Assert.assertEquals("Timing repeat period", value, timing.getRepeat().getPeriod());
		}

		if (unit == null) {
			Assert.assertTrue("No timing repeat unit", !timing.hasRepeat() || !timing.getRepeat().hasPeriodUnit());
		} else {
			Assert.assertEquals("Timing repeat unit", unit,
					timing.getRepeat().getPeriodUnit().toString().toLowerCase());
		}

		if (ivlTsPeriodGenerator == null) {
			Assert.assertTrue("No timing repeat", !timing.hasRepeat() || !timing.getRepeat().hasBounds());
		} else {
			ivlTsPeriodGenerator.verify(timing.getRepeat().getBoundsPeriod());
		}
	}
}
