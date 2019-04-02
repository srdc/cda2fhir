package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CDGenerator {
	private static final String[] SYSTEMS = { "2.16.840.1.113883.6.96", "4.5.2.4", "4.1.12.67", "43.45.78.12" };

	private static int NEXT_INDEX = 1;

	private String code;
	private String codeSystem;
	private String codeSystemName;
	private String displayName;

	public CDGenerator(String code, String codeSystem, String codeSystemName, String displayName) {
		this.code = code;
		this.codeSystem = codeSystem;
		this.codeSystemName = codeSystemName;
		this.displayName = displayName;
	}

	public CD generate(CDAFactories factories) {
		CD cd = factories.datatype.createCD();

		if (code != null) {
			cd.setCode(code);
		}
		if (codeSystem != null) {
			cd.setCodeSystem(codeSystem);
		}
		if (codeSystemName != null) {
			cd.setCodeSystemName(codeSystemName);
		}
		if (displayName != null) {
			cd.setDisplayName(displayName);
		}
		return cd;
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

	public static CDGenerator getNextInstance() {
		int codeIndex = NEXT_INDEX % SYSTEMS.length;
		++NEXT_INDEX;

		String code = "code_" + String.valueOf(codeIndex);
		String codeSystem = SYSTEMS[codeIndex];
		String codeSystemName = "systemname_" + String.valueOf(codeIndex);
		String displayName = "display_" + codeIndex;

		return new CDGenerator(code, codeSystem, codeSystemName, displayName);
	}
}
