package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class LanguageCommunicationGenerator {
	private static final String[] CODES = { "en", "fr", "tr" };

	private static int NEXT_INDEX = 1;

	private CSCodeGenerator languageCodeGenerator;
	private BLGenerator preferenceIndGenerator;

	public LanguageCommunication generate(CDAFactories factories) {
		LanguageCommunication lc = factories.base.createLanguageCommunication();

		if (languageCodeGenerator != null) {
			CS cs = languageCodeGenerator.generate(factories);
			lc.setLanguageCode(cs);
		}

		if (preferenceIndGenerator != null) {
			BL bl = preferenceIndGenerator.generate(factories);
			lc.setPreferenceInd(bl);
		}

		return lc;
	}

	public static LanguageCommunicationGenerator getNextInstance() {
		int codeIndex = NEXT_INDEX % CODES.length;
		++NEXT_INDEX;

		String code = CODES[codeIndex];

		LanguageCommunicationGenerator lcg = new LanguageCommunicationGenerator();

		lcg.languageCodeGenerator = new CSCodeGenerator(code);
		lcg.preferenceIndGenerator = BLGenerator.getNextInstance();

		return lcg;
	}

	public void verify(PatientCommunicationComponent pcc) {
		if (languageCodeGenerator == null) {
			Assert.assertTrue("No language", !pcc.hasLanguage());
		} else {
			Coding coding = pcc.getLanguage().getCoding().get(0);
			Assert.assertEquals("Communication system", "http://hl7.org/fhir/ValueSet/languages", coding.getSystem());
			languageCodeGenerator.verify(coding.getCode());
		}

		if (preferenceIndGenerator == null) {
			Assert.assertTrue("No preference ind", !pcc.hasPreferred());
		} else {
			preferenceIndGenerator.verify(pcc.getPreferred());
		}
	}

	public static void verifyList(List<PatientCommunicationComponent> actual,
			List<LanguageCommunicationGenerator> expected) {
		Assert.assertEquals("Language communication size", expected.size(), actual.size());
		for (int index = 0; index < actual.size(); ++index) {
			expected.get(index).verify(actual.get(index));
		}
	}
}
