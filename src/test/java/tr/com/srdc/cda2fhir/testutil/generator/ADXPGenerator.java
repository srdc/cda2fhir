package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.hl7.datatypes.ADXP;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ADXPGenerator extends BaseStringGenerator<ADXP> {
	public ADXPGenerator(boolean nullFlavorOK) {
		super(nullFlavorOK);
	}

	public ADXPGenerator(String value) {
		super(value);
	}

	public ADXPGenerator(String value, boolean nullFlavorOK) {
		super(value, nullFlavorOK);
	}

	@Override
	public ADXP create(CDAFactories factories) {
		return factories.datatype.createADXP();
	}

	public static ADXPGenerator getNextInstance(boolean nullFlavorOK) {
		String value = BaseStringGenerator.getNextValue();
		return new ADXPGenerator(value, nullFlavorOK);
	}
}
