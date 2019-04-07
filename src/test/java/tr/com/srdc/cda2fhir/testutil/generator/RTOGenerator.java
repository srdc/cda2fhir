package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Ratio;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
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
			REAL real = factories.datatype.createREAL();
			real.setValue(numerator);
			rto.setNumerator(real);
		}

		if (denominator != null) {
			REAL real = factories.datatype.createREAL();
			real.setValue(denominator);
			rto.setDenominator(real);
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
			double expected = numerator.doubleValue();
			double actual = ratio.getNumerator().getValueElement().getValue().doubleValue();
			Assert.assertTrue("Ration numerator", Math.abs(expected - actual) < 0.001);
		}

		if (denominator == null) {
			Assert.assertTrue("No ratio denominator", !ratio.hasNumerator());
		} else {
			double expected = denominator.doubleValue();
			double actual = ratio.getDenominator().getValueElement().getValue().doubleValue();
			Assert.assertTrue("Ratio denominator", Math.abs(expected - actual) < 0.001);
		}
	}
}
