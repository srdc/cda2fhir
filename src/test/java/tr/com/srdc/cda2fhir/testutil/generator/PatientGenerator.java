package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PatientGenerator {
	private List<PNGenerator> nameGenerators = new ArrayList<>();

	public org.openhealthtools.mdht.uml.cda.Patient generate(CDAFactories factories) {
		org.openhealthtools.mdht.uml.cda.Patient p = factories.base.createPatient();

		nameGenerators.forEach(g -> {
			PN pn = g.generate(factories);
			p.getNames().add(pn);
		});

		return p;
	}

	public static PatientGenerator getDefaultInstance() {
		PatientGenerator prg = new PatientGenerator();

		prg.nameGenerators.add(PNGenerator.getDefaultInstance());

		return prg;
	}

	public void verify(Patient patient) {
		if (nameGenerators.isEmpty()) {
			Assert.assertTrue("No patient name", !patient.hasName());
		} else {
			PNGenerator.verifyList(patient.getName(), nameGenerators);
		}
	}
}
