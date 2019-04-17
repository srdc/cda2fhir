package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CEGenerator {
	private static final String[] SYSTEMS = { "2.16.840.1.113883.6.96", "4.5.2.4", "4.1.12.67", "43.45.78.12",
			"3.5.6.8", "12.12.12.1", "9.0.3.6", "12.34.56.78", "3.4.5.2", "9.98.0.9", "12.11.45.33" };

	private static int NEXT_INDEX = 100;

	private String code;
	private String codeSystem;
	private String codeSystemName;
	private String displayName;

	public CEGenerator(String code, String codeSystem, String codeSystemName, String displayName) {
		this.code = code;
		this.codeSystem = codeSystem;
		this.codeSystemName = codeSystemName;
		this.displayName = displayName;
	}

	public CE generate(CDAFactories factories) {
		CE ce = factories.datatype.createCE();

		if (code != null) {
			ce.setCode(code);
		}
		if (codeSystem != null) {
			ce.setCodeSystem(codeSystem);
		}
		if (codeSystemName != null) {
			ce.setCodeSystemName(codeSystemName);
		}
		if (displayName != null) {
			ce.setDisplayName(displayName);
		}
		return ce;
	}

	public void verify(CodeableConcept codeableConcept) {
		List<Coding> codings = codeableConcept.getCoding();
		Assert.assertEquals("Number of coding", 1, codings.size());

		Coding coding = codings.get(0);

		if (code != null) {
			Assert.assertEquals("Coding code", code, coding.getCode());
		} else {
			Assert.assertTrue("Missing coding code", !coding.hasCode());
		}

		if (displayName != null) {
			Assert.assertEquals("Coding display", displayName, coding.getDisplay());
		} else {
			Assert.assertTrue("Missing coding display", !coding.hasDisplay());
		}

		if (codeSystem != null) {
			String system = codeSystem.equals("2.16.840.1.113883.6.96") ? "http://snomed.info/sct"
					: "urn:oid:" + codeSystem;
			Assert.assertEquals("Coding system", system, coding.getSystem());

		} else {
			Assert.assertTrue("Missing coding system", !coding.hasSystem());

		}
	}

	public static CEGenerator getNextInstance() {
		int codeIndex = NEXT_INDEX % SYSTEMS.length;
		++NEXT_INDEX;

		String code = "code_" + String.valueOf(codeIndex);
		String codeSystem = SYSTEMS[codeIndex];
		String codeSystemName = "systemname_" + String.valueOf(codeIndex);
		String displayName = "display_" + codeIndex;

		return new CEGenerator(code, codeSystem, codeSystemName, displayName);
	}
}
