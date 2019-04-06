package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PatientGenerator {
	private static final Map<String, Object> GENDER = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/Gender.json");
	private static final Map<String, Object> MARITAL_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/MaritalStatus.json");

	private List<PNGenerator> nameGenerators = new ArrayList<>();
	private CECodeGenerator genderGenerator;
	private TSGenerator birthTimeGenerator;
	private CECodeGenerator maritalStatusGenerator;
	private List<LanguageCommunicationGenerator> languageCommunicationGenerators = new ArrayList<>();
	private List<GuardianGenerator> guardianGenerators = new ArrayList<>();

	public org.openhealthtools.mdht.uml.cda.Patient generate(CDAFactories factories) {
		org.openhealthtools.mdht.uml.cda.Patient p = factories.base.createPatient();

		nameGenerators.forEach(g -> {
			PN pn = g.generate(factories);
			p.getNames().add(pn);
		});

		if (genderGenerator != null) {
			CE ce = genderGenerator.generate(factories);
			p.setAdministrativeGenderCode(ce);
		}

		if (birthTimeGenerator != null) {
			TS ts = birthTimeGenerator.generate(factories);
			p.setBirthTime(ts);
		}

		if (maritalStatusGenerator != null) {
			CE ce = maritalStatusGenerator.generate(factories);
			p.setMaritalStatusCode(ce);
		}

		languageCommunicationGenerators.forEach(lcg -> {
			LanguageCommunication lc = lcg.generate(factories);
			p.getLanguageCommunications().add(lc);
		});

		guardianGenerators.forEach(g -> {
			Guardian guardian = g.generate(factories);
			p.getGuardians().add(guardian);
		});

		return p;
	}

	public static PatientGenerator getDefaultInstance() {
		PatientGenerator prg = new PatientGenerator();

		prg.nameGenerators.add(PNGenerator.getDefaultInstance());
		prg.genderGenerator = new CECodeGenerator(GENDER, "unknown");
		prg.genderGenerator.set("f");
		prg.birthTimeGenerator = new TSGenerator("20040502");
		prg.maritalStatusGenerator = new CECodeGenerator(MARITAL_STATUS, "UNK");
		prg.maritalStatusGenerator.set("M");
		prg.languageCommunicationGenerators.add(LanguageCommunicationGenerator.getNextInstance());
		prg.guardianGenerators.add(GuardianGenerator.getDefaultInstance());

		return prg;
	}

	public void verify(Patient patient) {
		if (nameGenerators.isEmpty()) {
			Assert.assertTrue("No patient name", !patient.hasName());
		} else {
			PNGenerator.verifyList(patient.getName(), nameGenerators);
		}

		if (genderGenerator == null) {
			Assert.assertTrue("No patient gender", !patient.hasGender());
		} else {
			genderGenerator.verify(patient.getGender().toCode());
		}

		if (birthTimeGenerator == null) {
			Assert.assertTrue("No patient birthday", !patient.hasBirthDate());
		} else {
			birthTimeGenerator.verify(patient.getBirthDateElement().asStringValue());
		}

		if (maritalStatusGenerator == null) {
			Assert.assertTrue("No patient marital status", !patient.hasMaritalStatus());
		} else {
			maritalStatusGenerator.verify(patient.getMaritalStatus());
		}

		if (languageCommunicationGenerators.isEmpty()) {
			Assert.assertTrue("No patient language communications", !patient.hasCommunication());
		} else {
			LanguageCommunicationGenerator.verifyList(patient.getCommunication(), languageCommunicationGenerators);
		}

		if (guardianGenerators.isEmpty()) {
			Assert.assertTrue("No patient guardians", !patient.hasContact());
		} else {
			GuardianGenerator.verifyList(patient.getContact(), guardianGenerators);
		}
	}
}
