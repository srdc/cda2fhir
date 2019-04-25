package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PatientRoleGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();
	private List<ADGenerator> addrGenerators = new ArrayList<>();
	private List<TELGenerator> telecomGenerators = new ArrayList<>();
	private OrganizationGenerator providerOrgGenerator;
	private PatientGenerator patientGenerator;

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

		if (providerOrgGenerator != null) {
			pr.setProviderOrganization(providerOrgGenerator.generate(factories));
		}

		if (patientGenerator != null) {
			pr.setPatient(patientGenerator.generate(factories));
		}

		return pr;
	}

	public static PatientRoleGenerator getDefaultInstance() {
		PatientRoleGenerator prg = new PatientRoleGenerator();

		prg.idGenerators.add(IDGenerator.getNextInstance());
		prg.addrGenerators.add(ADGenerator.getDefaultInstance());
		prg.telecomGenerators.add(TELGenerator.getDefaultInstance());
		prg.providerOrgGenerator = OrganizationGenerator.getDefaultInstance();
		prg.patientGenerator = PatientGenerator.getDefaultInstance();

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

		if (providerOrgGenerator == null) {
			Assert.assertTrue("No patient telecom", !patient.hasManagingOrganization());
		} else {
			Assert.assertTrue("Patient telecom exists", patient.hasManagingOrganization());
		}

		if (patientGenerator == null) {
			boolean other = patient.hasName();
			Assert.assertTrue("No patient name or other", !other);
		} else {
			patientGenerator.verify(patient);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		BundleUtil util = new BundleUtil(bundle);
		Patient patient = BundleUtil.findOneResource(bundle, Patient.class);

		verify(patient);

		if (providerOrgGenerator == null) {
			Assert.assertTrue("No patient telecom", !patient.hasManagingOrganization());
		} else {
			String organizationId = patient.getManagingOrganization().getReference();
			Assert.assertNotNull("Patient managing organization", organizationId);
			Organization organization = util.getResourceFromReference(organizationId, Organization.class);
			providerOrgGenerator.verify(organization);
		}
	}

	public void setProviderOrgGenerator(OrganizationGenerator orgGenerator) {
		this.providerOrgGenerator = orgGenerator;
	}
}
