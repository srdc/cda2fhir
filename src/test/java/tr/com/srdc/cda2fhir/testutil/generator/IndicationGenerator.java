package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class IndicationGenerator {
	static final public String DEFAULT_ID = "db734647-fc99-424c-a864-7e3cda82e703";

	static final public String DEFAULT_CODE_CODE = "29308-4";
	static final public String DEFAULT_CODE_DISPLAYNAME = "Diagnosis";

	private CD code;

	public Indication generate(CDAFactories factories) {
		Indication indication = factories.consol.createIndication();

		CD codeTBU = code != null ? code
				: factories.datatype.createCD(DEFAULT_CODE_CODE, "2.16.840.1.113883.6.1", "LOINC",
						DEFAULT_CODE_DISPLAYNAME);
		indication.setCode(codeTBU);

		indication.getIds().add(factories.datatype.createII(DEFAULT_ID));

		return indication;
	}
}
