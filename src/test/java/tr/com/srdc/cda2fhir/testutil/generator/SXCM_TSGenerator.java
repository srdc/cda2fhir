package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class SXCM_TSGenerator extends TSGenerator {
	private static int INDEX = 1;

	public SXCM_TSGenerator(String value) {
		super(value);
	}

	@Override
	protected SXCM_TS create(CDAFactories factories) {
		return factories.datatype.createSXCM_TS();
	}

	@Override
	public SXCM_TS generate(CDAFactories factories) {
		SXCM_TS ts = create(factories);
		fill(ts);
		return ts;
	}

	public static SXCM_TSGenerator getNextInstance() {
		String value = "2019011" + INDEX;
		++INDEX;
		return new SXCM_TSGenerator(value);
	}
}
