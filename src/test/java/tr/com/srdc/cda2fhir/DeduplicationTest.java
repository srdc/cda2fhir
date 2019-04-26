package tr.com.srdc.cda2fhir;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.Custodian;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.AssignedEntityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CDAImmunizationSectionComponentGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CDAMedicationSectionComponentGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.CEGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.EncounterActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ImmunizationActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ImmunizationMedicationInformationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.IndicationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationDispenseGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationInformationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationSupplyOrderGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.OrganizationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PatientRoleGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PerformerGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemConcernActGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemObservationGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class DeduplicationTest {
	static CCDTransformerImpl ccdTransformer;
	static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		ccdTransformer = new CCDTransformerImpl();
		factories = CDAFactories.init();

	}

	private MedicationDispenseGenerator getMedDispenseGeneratorMedInfoSameOrg(
			MedicationInformationGenerator medInfoGen) {
		MedicationDispenseGenerator medDispenseGenerator = MedicationDispenseGenerator.getDefaultInstance();

		OrganizationGenerator orgGen = medInfoGen.getOrganizationGenerator();

		medDispenseGenerator.setMedicationInformationGenerator(medInfoGen);

		AssignedEntityGenerator assignedEntityGenerator = AssignedEntityGenerator.getDefaultInstance();

		PerformerGenerator performerGenerator = PerformerGenerator.getDefaultInstance();

		assignedEntityGenerator.setOrganizationGenerator(orgGen);

		performerGenerator.setAssignedEntityGenerator(assignedEntityGenerator);

		List<PerformerGenerator> performerGenerators = new ArrayList<PerformerGenerator>();

		performerGenerators.add(performerGenerator);

		medDispenseGenerator.setPerformerGenerators(performerGenerators);

		return medDispenseGenerator;
	}

	private MedicationActivityGenerator getMedicationActivityGeneratorOneMedInfoGenerator(
			MedicationInformationGenerator medInfoGenerator) {

		MedicationDispenseGenerator medDispenseGen = getMedDispenseGeneratorMedInfoSameOrg(medInfoGenerator);

		MedicationActivityGenerator medActGenerator = MedicationActivityGenerator.getDefaultInstance();

		MedicationSupplyOrderGenerator medicationSupplyOrderGenerator = MedicationSupplyOrderGenerator
				.getDefaultInstance();

		medDispenseGen.setMedicationInformationGenerator(medInfoGenerator);

		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();

		authorGenerator.setOrganizationGenerator(medInfoGenerator.getOrganizationGenerator());

		medicationSupplyOrderGenerator.setAuthorGenerator(authorGenerator);

		medicationSupplyOrderGenerator.setMedicationInfoGenerator(medInfoGenerator);
		medActGenerator.setAuthorGenerator(authorGenerator);
		medActGenerator.setMedicationInfoGenerator(medInfoGenerator);
		medActGenerator.setMedicationSupplyOrderGenerator(medicationSupplyOrderGenerator);
		medActGenerator.setMedicationDispenseGenerator(medDispenseGen);

		return medActGenerator;

	}

	private ContinuityOfCareDocument getClincalDocNoOrgs() {

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument clinicalDoc = docGenerator.generateClinicalDoc(factories);

		return clinicalDoc;

	}

	private Bundle runMedidicationDeduplicationTest(List<MedicationActivity> medicationActivities, int medicationCount,
			boolean print, String path) throws Exception {
		CDAMedicationSectionComponentGenerator medSectionComponentGenerator = new CDAMedicationSectionComponentGenerator();
		ccdTransformer = new CCDTransformerImpl();

		List<SubstanceAdministration> substanceAdministrations = new ArrayList<SubstanceAdministration>();

		for (MedicationActivity medAct : medicationActivities) {
			substanceAdministrations.add(medAct);
		}

		medSectionComponentGenerator.setSubstanceAdministrations(substanceAdministrations);

		ContinuityOfCareDocument clinicalDoc = getClincalDocNoOrgs();

		Component3 medicationsSectionComponent = medSectionComponentGenerator.generate(factories);

		List<Component3> components = new ArrayList<Component3>();

		components.add(medicationsSectionComponent);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, clinicalDoc, components);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		if (print)
			FHIRUtil.printJSON(resultBundle, path);

		BundleUtil.findResources(resultBundle, Medication.class, medicationCount);

		return resultBundle;
	}

	@Test
	public void testMedicationDeduplicationOneMedicationTwoInstancesNoOrg() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator = getMedicationActivityGeneratorOneMedInfoGenerator(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator.generate(factories));

		runMedidicationDeduplicationTest(medActs, 1, false, null);

	}

	@Test
	public void testMedicationDeduplicationTwoMedicationsFourInstances2Orgs() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		OrganizationGenerator orgGenerator1 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator2 = OrganizationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen1 = MedicationInformationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen2 = MedicationInformationGenerator.getDefaultInstance();

		medInfoGen1.setOrganizationGenerator(orgGenerator1);

		medInfoGen2.setOrganizationGenerator(orgGenerator2);

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen1);

		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen2);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 2, false, null);

		BundleUtil.findResources(resultBundle, Organization.class, 2);

	}

	@Test
	public void testMedicationDeduplicationTwoMedicationsFourInstancesSameOrg() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen1 = MedicationInformationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen2 = MedicationInformationGenerator.getDefaultInstance();

		medInfoGen1.setOrganizationGenerator(orgGenerator);

		medInfoGen2.setOrganizationGenerator(orgGenerator);

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen1);

		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen2);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 2, false, null);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testMedicationDeduplicationTwoMedicationsFourInstancesNoOrg() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationInformationGenerator medInfoGen1 = MedicationInformationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen2 = MedicationInformationGenerator.getDefaultInstance();

		medInfoGen1.setOrganizationGenerator(null);

		medInfoGen2.setOrganizationGenerator(null);

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen1);

		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen2);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 2, false, null);

		BundleUtil.findResources(resultBundle, Organization.class, 0);

	}

	@Test
	public void testMedicationDeduplicationSameMedicationOneWithOrg() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen1 = MedicationInformationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen2 = MedicationInformationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen3 = new MedicationInformationGenerator();

		medInfoGen3.setCodeGenerator(medInfoGen2.getCodeGenerator());

		medInfoGen1.setOrganizationGenerator(null);

		medInfoGen2.setOrganizationGenerator(null);

		medInfoGen3.setOrganizationGenerator(orgGenerator);

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen1);

		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen2);

		MedicationActivityGenerator medActGenerator3 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen3);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		medActs.add(medActGenerator3.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 3, true, "src/test/resources/output/wtf.json");

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testMedicationDeduplicationThreeMedicationsThreeInstances5Orgs() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		OrganizationGenerator orgGen = OrganizationGenerator.getDefaultInstance();

		MedicationInformationGenerator medInfoGen = MedicationInformationGenerator.getDefaultInstance();

		medInfoGen.setOrganizationGenerator(orgGen);

		MedicationDispenseGenerator medDispenseGen = getMedDispenseGeneratorMedInfoSameOrg(medInfoGen);

		MedicationActivityGenerator medActGenerator1 = MedicationActivityGenerator.getDefaultInstance();
		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen);

		medActGenerator1.setMedicationDispenseGenerator(medDispenseGen);

		medActGenerator2.setMedicationDispenseGenerator(medDispenseGen);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 3, true,
				"src/test/resources/output/hello-world.json");

		BundleUtil.findResources(resultBundle, Organization.class, 5);

	}

	@Test
	public void testMedicationDeduplicationOneMedicationTwoOrganizations() throws Exception {

		CEGenerator codeGenerator = CEGenerator.getNextInstance();

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		OrganizationGenerator orgGen1 = OrganizationGenerator.getFullInstance();

		OrganizationGenerator orgGen2 = OrganizationGenerator.getFullInstance();

		MedicationInformationGenerator medInfoGen1 = MedicationInformationGenerator.getDefaultInstance();

		medInfoGen1.setOrganizationGenerator(orgGen1);

		medInfoGen1.setCodeGenerator(codeGenerator);

		MedicationInformationGenerator medInfoGen2 = MedicationInformationGenerator.getDefaultInstance();

		// different organization generator used for each
		medInfoGen2.setOrganizationGenerator(orgGen2);

		// Same code generator instance used means same code
		medInfoGen2.setCodeGenerator(codeGenerator);

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen1);

		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneMedInfoGenerator(medInfoGen2);

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		Bundle resultBundle = runMedidicationDeduplicationTest(medActs, 2, false, null);

		BundleUtil.findResources(resultBundle, Organization.class, 2);

	}

	private List<Component3> getSubstanceAdministrationComponentsOneOrg(OrganizationGenerator orgGenerator) {

		ImmunizationMedicationInformationGenerator immunMedInfoGen = ImmunizationMedicationInformationGenerator
				.getDefaultInstance();

		immunMedInfoGen.setOrganizationGenerator(orgGenerator);

		MedicationInformationGenerator medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();

		medInfoGenerator.setOrganizationGenerator(orgGenerator);

		MedicationDispenseGenerator medDispenseGen = getMedDispenseGeneratorMedInfoSameOrg(medInfoGenerator);

		MedicationActivityGenerator medActGenerator = getMedicationActivityGeneratorOneMedInfoGenerator(
				medInfoGenerator);

		medActGenerator.getMedicationSupplyOrderGenerator().setAuthorGenerator(null);
		medActGenerator.setAuthorGenerator(null);
		medActGenerator.setMedicationInfoGenerator(medInfoGenerator);
		medActGenerator.setMedicationDispenseGenerator(medDispenseGen);

		ImmunizationActivityGenerator immunizationActivityGenerator = ImmunizationActivityGenerator
				.getDefaultInstance();

		immunizationActivityGenerator.setReactionObservationGenerator(null);

		immunizationActivityGenerator.getPerformerGenerators().clear();

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

	private List<Component3> getSubstanceAdministrationComponentsSeparateOrgs(OrganizationGenerator orgGenerator1,
			OrganizationGenerator orgGenerator2) {
		ImmunizationMedicationInformationGenerator immunMedInfoGen = ImmunizationMedicationInformationGenerator
				.getDefaultInstance();

		immunMedInfoGen.setOrganizationGenerator(orgGenerator1);

		MedicationInformationGenerator medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();

		medInfoGenerator.setOrganizationGenerator(orgGenerator2);

		MedicationActivityGenerator medActGenerator = getMedicationActivityGeneratorOneMedInfoGenerator(
				medInfoGenerator);

		medActGenerator.getMedicationSupplyOrderGenerator().setAuthorGenerator(null);
		medActGenerator.setAuthorGenerator(null);
		medActGenerator.setMedicationInfoGenerator(medInfoGenerator);

		ImmunizationActivityGenerator immunizationActivityGenerator = ImmunizationActivityGenerator
				.getDefaultInstance();

		immunizationActivityGenerator.setReactionObservationGenerator(null);

		immunizationActivityGenerator.getPerformerGenerators().clear();

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

	private Author getAuthorWithOrg(OrganizationGenerator orgGenerator) {
		AuthorGenerator authorGen = AuthorGenerator.getFullInstance();
		authorGen.setOrganizationGenerator(orgGenerator);
		Author author = authorGen.generate(factories);
		return author;

	}

	private Custodian getCustodianWithOrg(OrganizationGenerator orgGenerator) {
		CustodianOrganization custodianOrg = orgGenerator.generateCustodianOrg(factories);
		// Not sure why this is set by default.
		custodianOrg.unsetNullFlavor();
		AssignedCustodian assignedCustodian = factories.base.createAssignedCustodian();
		assignedCustodian.setRepresentedCustodianOrganization(custodianOrg);

		Custodian custodian = factories.base.createCustodian();
		custodian.setAssignedCustodian(assignedCustodian);
		return custodian;
	}

	@Test
	public void testClinicalDocGeneratorNoOrgs() throws Exception {
		ccdTransformer = new CCDTransformerImpl();

		ContinuityOfCareDocument clinicalDoc = getClincalDocNoOrgs();

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		BundleUtil.findResources(resultBundle, Organization.class, 0);
	}

	@Test
	public void testOrganizationDeduplicationMedActImmunActSameOrg() throws Exception {
		OrganizationGenerator orgGenerator1 = OrganizationGenerator.getDefaultInstance();

		List<Component3> substanceAdminComponents = getSubstanceAdministrationComponentsOneOrg(orgGenerator1);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument clinicalDoc = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, clinicalDoc, substanceAdminComponents);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganizationDeduplicationMedActImmunActDifferentOrgs() throws Exception {
		OrganizationGenerator orgGenerator1 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator2 = OrganizationGenerator.getDefaultInstance();

		List<Component3> substanceAdminComponents = getSubstanceAdministrationComponentsSeparateOrgs(orgGenerator1,
				orgGenerator2);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument clinicalDoc = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, clinicalDoc, substanceAdminComponents);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);

		BundleUtil.findResources(resultBundle, Organization.class, 2);

	}

	@Test
	public void testOrganizationDeduplicationAssignedEntity() throws Exception {

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		AssignedEntityGenerator assignedEntityGenerator = new AssignedEntityGenerator();

		assignedEntityGenerator.setOrganizationGenerator(orgGenerator);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(assignedEntityGenerator);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganizationDeduplicationPatientRole() throws Exception {

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		PatientRole patientRole = getPatientRoleWithOrg(orgGenerator);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setRecordTarget(factories, document, patientRole);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganizationDeduplicationAuthor() throws Exception {

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		Author author = getAuthorWithOrg(orgGenerator);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setAuthor(factories, document, author);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganizationDeduplicationCustodian() throws Exception {

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		Custodian custodian = getCustodianWithOrg(orgGenerator);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(null);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setCustodian(factories, document, custodian);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganizationDeduplicationOneOrgMultipleInstances() throws Exception {

		OrganizationGenerator orgGenerator = OrganizationGenerator.getDefaultInstance();

		AssignedEntityGenerator assignedEntityGenerator = new AssignedEntityGenerator();

		assignedEntityGenerator.setOrganizationGenerator(orgGenerator);

		PatientRole patientRole = getPatientRoleWithOrg(orgGenerator);

		Author author = getAuthorWithOrg(orgGenerator);

		Custodian custodian = getCustodianWithOrg(orgGenerator);

		List<Component3> substanceAdminComponents1 = getSubstanceAdministrationComponentsOneOrg(orgGenerator);

		List<Component3> substanceAdminComponents2 = getSubstanceAdministrationComponentsOneOrg(orgGenerator);

		List<Component3> substanceAdminComponents3 = getSubstanceAdministrationComponentsOneOrg(orgGenerator);

		List<Component3> components = new ArrayList<Component3>();

		components.addAll(substanceAdminComponents1);
		components.addAll(substanceAdminComponents2);
		components.addAll(substanceAdminComponents3);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(assignedEntityGenerator);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, document, components);

		ClinicalDocumentMetadataGenerator.setRecordTarget(factories, document, patientRole);

		ClinicalDocumentMetadataGenerator.setCustodian(factories, document, custodian);

		ClinicalDocumentMetadataGenerator.setAuthor(factories, document, author);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 1);

	}

	@Test
	public void testOrganization5OrgsMultipleInstances() throws Exception {
		OrganizationGenerator orgGenerator1 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator2 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator3 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator4 = OrganizationGenerator.getDefaultInstance();

		OrganizationGenerator orgGenerator5 = OrganizationGenerator.getDefaultInstance();

		AssignedEntityGenerator assignedEntityGenerator = new AssignedEntityGenerator();

		assignedEntityGenerator.setOrganizationGenerator(orgGenerator1);

		PatientRole patientRole = getPatientRoleWithOrg(orgGenerator1);

		Author author = getAuthorWithOrg(orgGenerator2);

		Custodian custodian = getCustodianWithOrg(orgGenerator3);

		List<Component3> substanceAdminComponents1 = getSubstanceAdministrationComponentsOneOrg(orgGenerator4);

		List<Component3> substanceAdminComponents2 = getSubstanceAdministrationComponentsOneOrg(orgGenerator5);

		List<Component3> substanceAdminComponents3 = getSubstanceAdministrationComponentsOneOrg(orgGenerator5);

		List<Component3> components = new ArrayList<Component3>();

		components.addAll(substanceAdminComponents1);
		components.addAll(substanceAdminComponents2);
		components.addAll(substanceAdminComponents3);

		ccdTransformer = new CCDTransformerImpl();

		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();

		docGenerator.setAssignedEntityGenerator(assignedEntityGenerator);

		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, document, components);

		ClinicalDocumentMetadataGenerator.setRecordTarget(factories, document, patientRole);

		ClinicalDocumentMetadataGenerator.setCustodian(factories, document, custodian);

		ClinicalDocumentMetadataGenerator.setAuthor(factories, document, author);

		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Organization.class, 5);

	}
	
	private List<Component3> create2ProblemConcernActGenerators1Observation() {
		ProblemObservationGenerator probObsGen = ProblemObservationGenerator.getDefaultInstance();
		
		List<ProblemObservationGenerator> probsObsGenList = new ArrayList<ProblemObservationGenerator>();
		
		probsObsGenList.add(probObsGen);
		
		ProblemConcernActGenerator probConcernActGen1 = ProblemConcernActGenerator.getDefaultInstance();
		
		ProblemConcernActGenerator probConcernActGen2 = ProblemConcernActGenerator.getDefaultInstance();

		probConcernActGen1.setProblemObservationGenerators(probsObsGenList);
		
		probConcernActGen2.setProblemObservationGenerators(probsObsGenList);
		
		

	}
		
	private List<Component3> createActComponentsSameIndication(IndicationGenerator indGen) {
		List<Component3> componenents = new ArrayList<Component3>();
		
		List<IndicationGenerator> indicationGenerators = new ArrayList<IndicationGenerator>();
		
		indicationGenerators.add(indGen);
		
		EncounterActivityGenerator encounterActivityGen = EncounterActivityGenerator.getDefaultInstance();
		
		encounterActivityGen.setIndicationGenerator(indicationGenerators);
		
		MedicationActivityGenerator medActGen = MedicationActivityGenerator.getDefaultInstance();
		
		medActGen.setIndicationGenerators(indicationGenerators);
		
		return null;
		
	}
	@Test
	public void conditionTest() throws Exception {
		ccdTransformer = new CCDTransformerImpl();
		
		IndicationGenerator indGenerator = IndicationGenerator.getDefaultInstance();
		
		ClinicalDocumentMetadataGenerator docGenerator = new ClinicalDocumentMetadataGenerator();
		
		ContinuityOfCareDocument document = docGenerator.generateClinicalDoc(factories);
		
		List<Component3> components = createConditionComponenetsSameIndication(indGenerator);

		ClinicalDocumentMetadataGenerator.setStructuredBody(factories, document, components);
		
		Bundle resultBundle = ccdTransformer.transformDocument(document);

		BundleUtil.findResources(resultBundle, Condition.class, 1);
	}

}
