package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Ratio;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class RTOGenerator {
	private Double numerator;
	private Double denominator;

	public RTOGenerator() {
	}

	public RTO generate(CDAFactories factories) {
		RTO rto = factories.datatype.createRTO();

		if (numerator != null) {
			PQ pq = factories.datatype.createPQ();
			pq.setValue(numerator);
			rto.setNumerator(pq);
		}

		if (denominator != null) {
			PQ pq = factories.datatype.createPQ();
			pq.setValue(denominator);
			rto.setDenominator(pq);
		}

		return rto;
	}

	public static RTOGenerator getDefaultInstance() {
		RTOGenerator rg = new RTOGenerator();

		rg.numerator = new Double(5.5);
		rg.denominator = new Double(9.5);

		return rg;
	}

	public static RTOGenerator getFullInstance() {
		RTOGenerator rg = new RTOGenerator();

		rg.numerator = new Double(5.5);
		rg.denominator = new Double(9.5);

		return rg;
	}

	public void verify(Ratio ratio) {
		if (numerator == null) {
			Assert.assertTrue("No ratio numerator", !ratio.hasNumerator());
		} else {
			Assert.assertEquals("Ration numerator", numerator.doubleValue(), ratio.getNumerator());
		}

		if (denominator == null) {
			Assert.assertTrue("No ratio denominator", !ratio.hasNumerator());
		} else {
			Assert.assertEquals("Ration denominator", denominator.doubleValue(), ratio.getNumerator());
		}
	}
}
