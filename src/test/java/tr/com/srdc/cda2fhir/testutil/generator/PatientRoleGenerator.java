package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PatientRoleGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public PatientRole generate(CDAFactories factories) {
		PatientRole pr = factories.base.createPatientRole();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			pr.getIds().add(ii);
		});

		return pr;
	}

	public static PatientRoleGenerator getDefaultInstance() {
		PatientRoleGenerator prg = new PatientRoleGenerator();

		prg.idGenerators.add(IDGenerator.getNextInstance());

		return prg;
	}

	public void verify(Patient patient) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No patient identifier", !patient.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(patient.getIdentifier().get(index));
			}
		}
	}
}
