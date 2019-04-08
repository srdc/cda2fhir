package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Range;
import org.hl7.fhir.dstu3.model.Ratio;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AnyGenerator {
	private CDGenerator cdGenerator;
	private PQGenerator pqGenerator;
	private STGenerator stGenerator;
	private IVL_PQRangeGenerator ivlPqGenerator;
	private RTOGenerator rtoGenerator;
	private EDGenerator edGenerator;
	private TSGenerator tsGenerator;
	private BLGenerator blGenerator;

	public AnyGenerator(CDGenerator cdGenerator) {
		this.cdGenerator = cdGenerator;
	}

	public AnyGenerator(PQGenerator pqGenerator) {
		this.pqGenerator = pqGenerator;
	}

	public AnyGenerator(STGenerator stGenerator) {
		this.stGenerator = stGenerator;
	}

	public AnyGenerator(IVL_PQRangeGenerator ivlPqGenerator) {
		this.ivlPqGenerator = ivlPqGenerator;
	}

	public AnyGenerator(RTOGenerator rtoGenerator) {
		this.rtoGenerator = rtoGenerator;
	}

	public AnyGenerator(EDGenerator edGenerator) {
		this.edGenerator = edGenerator;
	}

	public AnyGenerator(TSGenerator tsGenerator) {
		this.tsGenerator = tsGenerator;
	}

	public AnyGenerator(BLGenerator blGenerator) {
		this.blGenerator = blGenerator;
	}

	public ANY generate(CDAFactories factories) {
		if (cdGenerator != null) {
			return cdGenerator.generate(factories);
		}
		if (pqGenerator != null) {
			return pqGenerator.generate(factories);
		}
		if (stGenerator != null) {
			return stGenerator.generate(factories);
		}
		if (ivlPqGenerator != null) {
			return ivlPqGenerator.generate(factories);
		}
		if (rtoGenerator != null) {
			return rtoGenerator.generate(factories);
		}
		if (edGenerator != null) {
			return edGenerator.generate(factories);
		}
		if (tsGenerator != null) {
			return tsGenerator.generate(factories);
		}
		if (blGenerator != null) {
			return blGenerator.generate(factories);
		}

		return null;
	}

	public void verify(CodeableConcept codeableConcept) {
		if (cdGenerator == null) {
			Assert.assertNull("No codeable concept", codeableConcept);
		} else {
			cdGenerator.verify(codeableConcept);
		}
	}

	public void verify(Quantity quantity) {
		if (pqGenerator == null) {
			Assert.assertNull("No quantity", quantity);
		} else {
			pqGenerator.verify(quantity);
		}
	}

	public void verify(String string) {
		if (stGenerator == null) {
			Assert.assertNull("No string", string);
		} else {
			stGenerator.verify(string);
		}
	}

	public void verify(Range range) {
		if (ivlPqGenerator == null) {
			Assert.assertNull("No range", range);
		} else {
			ivlPqGenerator.verify(range);
		}
	}

	public void verify(Ratio ratio) {
		if (rtoGenerator == null) {
			Assert.assertNull("No ratio", ratio);
		} else {
			rtoGenerator.verify(ratio);
		}
	}

	public void verify(Attachment attachment) {
		if (edGenerator == null) {
			Assert.assertNull("No attachement", attachment);
		} else {
			edGenerator.verify(attachment.getData());
		}
	}

	public void verify(DateTimeType dateTime) {
		if (tsGenerator == null) {
			Assert.assertNull("No date time", dateTime);
		} else {
			tsGenerator.verify(dateTime.getValueAsString());
		}
	}

	public void verify(boolean bl) {
		if (blGenerator == null) {
			Assert.assertNull("No boolean", bl);
		} else {
			blGenerator.verify(bl);
		}
	}
}
