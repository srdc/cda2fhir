package tr.com.srdc.cda2fhir;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.CDAMedicationSectionComponentGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationInformationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationSupplyOrderGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;

public class DeduplicationTest {
	static CCDTransformerImpl ccdTransformer;
	static CDAFactories factories;
	static ClinicalDocumentMetadataGenerator metadataGenerator;
	static CDAMedicationSectionComponentGenerator medSectionComponentGenerator;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		ccdTransformer = new CCDTransformerImpl();
		factories = CDAFactories.init();
		metadataGenerator = new ClinicalDocumentMetadataGenerator();
		medSectionComponentGenerator = new CDAMedicationSectionComponentGenerator();
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

	private void runDeduplicationTest(List<MedicationActivity> medicationActivities, int medicationCount)
			throws Exception {
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
	public void testMedicationDeduplicationOneMedication() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator.generate(factories));

		runDeduplicationTest(medActs, 1);

	}

	@Test
	public void testMedicationDeduplicationTwoMedications() throws Exception {
		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator1 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());
		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		runDeduplicationTest(medActs, 2);

	}

	@Test
	public void testMedicationDeduplicationFourMedications() throws Exception {

		List<MedicationActivity> medActs = new ArrayList<MedicationActivity>();

		MedicationActivityGenerator medActGenerator1 = MedicationActivityGenerator.getDefaultInstance();
		MedicationActivityGenerator medActGenerator2 = getMedicationActivityGeneratorOneCode(
				MedicationInformationGenerator.getDefaultInstance());

		medActs.add(medActGenerator1.generate(factories));

		medActs.add(medActGenerator2.generate(factories));

		runDeduplicationTest(medActs, 3);

	}
}
