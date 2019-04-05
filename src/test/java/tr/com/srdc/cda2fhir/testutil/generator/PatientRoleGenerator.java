package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PatientRoleGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();
	private List<ADGenerator> addrGenerators = new ArrayList<>();
	private List<TELGenerator> telecomGenerators = new ArrayList<>();

	public PatientRole generate(CDAFactories factories) {
		PatientRole pr = factories.base.createPatientRole();

		idGenerators.forEach(g -> {
			II ii = g.generate(factories);
			pr.getIds().add(ii);
		});

		addrGenerators.forEach(g -> {
			AD ad = g.generate(factories);
			pr.getAddrs().add(ad);
		});

		telecomGenerators.forEach(g -> {
			TEL tel = g.generate(factories);
			pr.getTelecoms().add(tel);
		});

		return pr;
	}

	public static PatientRoleGenerator getDefaultInstance() {
		PatientRoleGenerator prg = new PatientRoleGenerator();

		prg.idGenerators.add(IDGenerator.getNextInstance());
		prg.addrGenerators.add(ADGenerator.getDefaultInstance());
		prg.telecomGenerators.add(TELGenerator.getDefaultInstance());

		return prg;
	}

	public void verify(Patient patient) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No patient identifier", !patient.hasIdentifier());
		} else {
			IDGenerator.verifyList(patient.getIdentifier(), idGenerators);
		}

		if (addrGenerators.isEmpty()) {
			Assert.assertTrue("No patient address", !patient.hasAddress());
		} else {
			ADGenerator.verifyList(patient.getAddress(), addrGenerators);
		}

		if (telecomGenerators.isEmpty()) {
			Assert.assertTrue("No patient telecom", !patient.hasTelecom());
		} else {
			TELGenerator.verifyList(patient.getTelecom(), telecomGenerators);
		}
	}
}
