package tr.com.srdc.cda2fhir;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.Custodian;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.AssignedEntityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CDAImmunizationSectionComponentGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CDAMedicationSectionComponentGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ImmunizationActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ImmunizationMedicationInformationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationInformationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationSupplyOrderGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.OrganizationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PatientRoleGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PerformerGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;

public class DeduplicationTest {
	static CCDTransformerImpl ccdTransformer;
	static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		ccdTransformer = new CCDTransformerImpl();
		factories = CDAFactories.init();

	}

	private MedicationActivityGenerator getMedicationActivityGeneratorOneCode(
			MedicationInformationGenerator medInfoGenerator) {

		MedicationActivityGenerator medActGenerator = MedicationActivityGenerator.getDefaultInstance();

		MedicationSupplyOrderGenerator medicationSupplyOrderGenerator = MedicationSupplyOrderGenerator
				.getDefaultInstance();

		medicationSupplyOrderGenerator.setMedicationInfoGenerator(medInfoGenerator);
		medActGenerator.setMedicationInfoGenerator(medInfoGenerator);
		medActGenerator.setMedicationSupplyOrderGenerator(medicationSupplyOrderGenerator);

		return medActGenerator;

	}

	private void runMedidicationDeduplicationTest(List<MedicationActivity> medicationActivities, int medicationCount)
			throws Exception {
		ClinicalDocumentMetadataGenerator metadataGenerator = new ClinicalDocumentMetadataGenerator();
		CDAMedicationSectionComponentGenerator medSectionComponentGenerator = new CDAMedicationSectionComponentGenerator();
		ccdTransformer = new CCDTransformerImpl();

		List<SubstanceAdministration> substanceAdministrations = new ArrayList<SubstanceAdministration>();

		for (MedicationActivity medAct : medicationActivities) {
			substanceAdministrations.add(medAct);
		}

		medSectionComponentGenerator.setSubstanceAdministrations(substanceAdministrations);

		ContinuityOfCareDocument clinicalDoc = metadataGenerator.generateClinicalDoc(factories);

		Component3 medicationsSectionComponent = medSectionComponentGenerator.generate(factories);

		List<Component3> components = new ArrayList<Component3>();

		components.add(medicationsSectionComponent);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, clinicalDoc, components);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		BundleUtil.findResources(resultBundle, Medication.class, medicationCount);

	}

	@Test
	public void testMedicationDeduplicationOneMedicationTwoInstances() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator.generate(factories));

		runMedidicationDeduplicationTest(medActs, 1);

	}

	@Test
	public void testMedicationDeduplicationTwoMedicationsFourInstances() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());
		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		runMedidicationDeduplicationTest(medActs, 2);

	}

	@Test
	public void testMedicationDeduplicationThreeMedicationsTwoInstances() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator1 = MedicationActivityGenerator.getDefaultInstance();
		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		runMedidicationDeduplicationTest(medActs, 3);

	}

	private List<Component3> getSubstanceAdministrationComponentsOneOrg(OrganizationGenerator orgGenerator) {
		ImmunizationMedicationInformationGenerator immunMedInfoGen = ImmunizationMedicationInformationGenerator
				.getDefaultInstance();

		immunMedInfoGen.setOrganizationGenerator(orgGenerator);

		MedicationInformationGenerator medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();

		medInfoGenerator.setOrganizationGenerator(orgGenerator);

		MedicationActivityGenerator medActGenerator = MedicationActivityGenerator.getDefaultInstance();

		medActGenerator.setMedicationInfoGenerator(medInfoGenerator);

		ImmunizationActivityGenerator immunizationActivityGenerator = ImmunizationActivityGenerator
				.getDefaultInstance();

		immunizationActivityGenerator.setMedicationInformationGenerator(immunMedInfoGen);

		List<SubstanceAdministration> medicationSubstanceAdministrations = new ArrayList<SubstanceAdministration>();

		List<SubstanceAdministration> immunSubstanceAdministrations = new ArrayList<SubstanceAdministration>();

		medicationSubstanceAdministrations.add(medActGenerator.generate(factories));

		immunSubstanceAdministrations.add(immunizationActivityGenerator.generate(factories));

		CDAImmunizationSectionComponentGenerator immunSectionComponentGenerator = new CDAImmunizationSectionComponentGenerator();

		CDAMedicationSectionComponentGenerator medSectionComponentGenerator = new CDAMedicationSectionComponentGenerator();

		immunSectionComponentGenerator.setSubstanceAdministrations(immunSubstanceAdministrations);

		medSectionComponentGenerator.setSubstanceAdministrations(medicationSubstanceAdministrations);

		List<Component3> components = new ArrayList<Component3>();

		components.add(medSectionComponentGenerator.generate(factories));

		components.add(immunSectionComponentGenerator.generate(factories));

		return components;
	}

	private PatientRole getPatientRoleWithOrg(OrganizationGenerator orgGenerator) {
		PatientRoleGenerator patientRoleGenerator = PatientRoleGenerator.getDefaultInstance();
		patientRoleGenerator.setProviderOrgGenerator(orgGenerator);
		return patientRoleGenerator.generate(factories);
	}

	private Author getAssignedAuthorWithOrg(OrganizationGenerator orgGenerator) {
		AuthorGenerator authorGen = AuthorGenerator.getFullInstance();
		authorGen.setOrganizationGenerator(orgGenerator);
		Author author = authorGen.generate(factories);
		return author;

	}

	private AssignedEntity getAssignedEntityWithOrg(OrganizationGenerator orgGenerator) {
		AssignedEntityGenerator assignedEntityGen = AssignedEntityGenerator.getFullInstance();
		assignedEntityGen.setOrganizationGenerator(orgGenerator);
		return assignedEntityGen.generate(factories);

	}

	private Performer2 getPeformerWithOrg(OrganizationGenerator orgGenerator) {
		AssignedEntityGenerator assignedEntityGen = AssignedEntityGenerator.getFullInstance();
		assignedEntityGen.setOrganizationGenerator(orgGenerator);
		PerformerGenerator performerGen = PerformerGenerator.getFullInstance();
		performerGen.setAssignedEntityGenerator(assignedEntityGen);
		return performerGen.generate(factories);
	}

	private Custodian getCustodianWithOrg(OrganizationGenerator orgGenerator) {
		CustodianOrganization custodianOrg = factories.base.createCustodianOrganization();

		AssignedCustodian assignedCustodian = factories.base.createAssignedCustodian();
		assignedCustodian.setRepresentedCustodianOrganization()
	}

	@Test
	public void testOrganizationDeduplicationOneOrgTwoInstances() throws Exception {
		OrganizationGenerator orgGenerator1 = OrganizationGenerator.getFullInstance();

		List<Component3> substanceAdminComponents = getSubstanceAdministrationComponentsOneOrg(orgGenerator1);

		ClinicalDocumentMetadataGenerator metadataGenerator = new ClinicalDocumentMetadataGenerator();

		ccdTransformer = new CCDTransformerImpl();

		ContinuityOfCareDocument clinicalDoc = metadataGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, clinicalDoc, components);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	public void testOrganization4Orgs8Instances() {

	}

}
