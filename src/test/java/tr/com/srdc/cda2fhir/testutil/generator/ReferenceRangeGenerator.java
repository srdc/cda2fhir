package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ObservationRange;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ReferenceRangeGenerator {
	private IVL_PQRangeGenerator rangeGenerator;
	private CEGenerator interpretationCodeGenerator;
	private EDGenerator textGenerator;

	public ReferenceRangeGenerator() {
	}

	public ReferenceRange generate(CDAFactories factories) {
		ReferenceRange rr = factories.base.createReferenceRange();

		ObservationRange or = factories.base.createObservationRange();
		rr.setObservationRange(or);

		if (rangeGenerator != null) {
			IVL_PQ ivlPq = rangeGenerator.generate(factories);
			or.setValue(ivlPq);
		}

		if (interpretationCodeGenerator != null) {
			CE ce = interpretationCodeGenerator.generate(factories);
			or.setInterpretationCode(ce);
		}

		if (textGenerator != null) {
			ED ed = textGenerator.generate(factories);
			or.setText(ed);
		}

		return rr;
	}

	public static ReferenceRangeGenerator getDefaultInstance() {
		ReferenceRangeGenerator rrg = new ReferenceRangeGenerator();

		rrg.rangeGenerator = IVL_PQRangeGenerator.getDefaultInstance();
		rrg.interpretationCodeGenerator = CEGenerator.getNextInstance();
		rrg.textGenerator = EDGenerator.getNextInstance();

		return rrg;
	}

	public void verify(ObservationReferenceRangeComponent range) {
		if (rangeGenerator == null) {
			Assert.assertNull("No range value", range);
		} else {
			rangeGenerator.verify(range);
		}

		if (interpretationCodeGenerator == null) {
			Assert.assertTrue("No range type", !range.hasType());
		} else {
			interpretationCodeGenerator.verify(range.getType());
		}

		if (textGenerator == null) {
			Assert.assertTrue("No range text", !range.hasText());
		} else {
			textGenerator.verify(range.getText());
		}
	}
}
