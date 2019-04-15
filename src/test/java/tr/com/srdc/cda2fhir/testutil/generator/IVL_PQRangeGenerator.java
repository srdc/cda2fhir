package tr.com.srdc.cda2fhir.testutil.generator;

import java.math.BigDecimal;

import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Range;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class IVL_PQRangeGenerator {
	private IVXB_PQGenerator low;
	private IVXB_PQGenerator high;

	private BigDecimal value;

	public IVL_PQRangeGenerator() {
	}

	public IVL_PQ generate(CDAFactories factories) {
		IVL_PQ ivlPq = factories.datatype.createIVL_PQ();

		if (low != null) {
			IVXB_PQ pq = low.generate(factories);
			ivlPq.setLow(pq);
		}
		if (high != null) {
			IVXB_PQ pq = high.generate(factories);
			ivlPq.setHigh(pq);
		}
		if (value != null) {
			ivlPq.setValue(value);
		}

		return ivlPq;
	}

	public static IVL_PQRangeGenerator getDefaultInstance() {
		IVL_PQRangeGenerator ptg = new IVL_PQRangeGenerator();

		ptg.low = new IVXB_PQGenerator(150, "ml");
		ptg.high = new IVXB_PQGenerator(500, "ml");

		return ptg;
	}

	public static IVL_PQRangeGenerator getFullInstance() {
		IVL_PQRangeGenerator ptg = new IVL_PQRangeGenerator();

		ptg.low = new IVXB_PQGenerator(150, "ml");
		ptg.high = new IVXB_PQGenerator(500, "ml");

		return ptg;
	}

	public void verify(Range range) {
		if (high == null) {
			Assert.assertTrue("No range high value", !range.hasHigh());
		} else {
			high.verify(range.getHigh());
		}

		if (low == null) {
			Assert.assertTrue("No range low value", !range.hasLow());
		} else {
			low.verify(range.getLow());
		}
	}

	public void verify(ObservationReferenceRangeComponent range) {
		if (high == null) {
			Assert.assertTrue("No range high value", !range.hasHigh());
		} else {
			high.verify(range.getHigh());
		}

		if (low == null) {
			Assert.assertTrue("No range low value", !range.hasLow());
		} else {
			low.verify(range.getLow());
		}
	}
}
