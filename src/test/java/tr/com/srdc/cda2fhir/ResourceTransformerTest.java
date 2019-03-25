package tr.com.srdc.cda2fhir;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Patient.ContactComponent;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import ca.uhn.fhir.model.api.IResource;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ResourceTransformerTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static final ValueSetsTransformerImpl vsti = new ValueSetsTransformerImpl();
	private static FileInputStream fisCCD;
	private static FileWriter resultFW;
	private static ContinuityOfCareDocument ccd;
	private static final String resultFilePath = "src/test/resources/output/ResourceTransformerTest.txt";
	private static final String transformationStartMsg = "\n# TRANSFORMATION STARTING..\n";
	private static final String transformationEndMsg = "# END OF TRANSFORMATION.\n";
	private static final String endOfTestMsg = "\n## END OF TEST\n";
	

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		// read the input test file
		try {
			fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
			ccd = (ContinuityOfCareDocument) CDAUtil.load(fisCCD);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// init the output file writer
		File resultFile = new File(resultFilePath);
		resultFile.getParentFile().mkdirs();
		try {
			resultFW = new FileWriter(resultFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void finalise() {
		try {
			resultFW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	public void referenceTest() {
		Reference ref;
		
		Identifier identifier = new Identifier();
		identifier.setId("identifierId");
		identifier.setValue("identifierValue");
		
		Medication med = new Medication();
		
		IdType id = new IdType("testID", "0");
		med.setId(id);
		
		ref = rt.getReference(med);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Medication Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		
		MedicationStatement medStatement = new MedicationStatement();
		
		medStatement.setId(id);
		medStatement.addIdentifier(identifier);
		
		ref = rt.getReference(medStatement);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("MedicationStatement Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));

		Patient patient = new Patient();
		
		patient.setId(id);
		patient.addIdentifier(identifier);
		
		ref = rt.getReference(patient);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Patient Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));
		
		Practitioner prac = new Practitioner();
		
		prac.setId(id);
		prac.addIdentifier(identifier);
		
		ref = rt.getReference(prac);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Practitioner Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));
		
		Organization org = new Organization();
		
		org.setId(id);
		org.addIdentifier(identifier);
		
		ref = rt.getReference(org);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Organization Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));
		
		Immunization immunization = new Immunization();
		
		immunization.setId(id);
		immunization.addIdentifier(identifier);
		
		ref = rt.getReference(immunization);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Immunization Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));
		
		Condition condition = new Condition();
		
		condition.setId(id);
		condition.addIdentifier(identifier);
		
		ref = rt.getReference(condition);
		
		Assert.assertTrue(ref.getDisplay().contentEquals("Condition Reference"));
		Assert.assertTrue(ref.getReference().contentEquals("testID/0"));
		Assert.assertTrue(ref.getIdentifier().getId().contentEquals("identifierId"));
		Assert.assertTrue(ref.getIdentifier().getValue().contentEquals("identifierValue"));
	}

	// Most of the test methods just print the transformed object in JSON form.

	@Test
	public void testAllergyProblemAct2AllergyIntolerance() {
		appendToResultFile("## TEST: AllergyProblemAct2AllergyIntolerance\n");
		// null instance test
		AllergyProblemAct cdaNull = null;
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirNull = rt.tAllergyProblemAct2AllergyIntolerance(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		for (AllergyProblemAct cdaAPA : ResourceTransformerTest.ccd.getAllergiesSection().getAllergyProblemActs()) {
			appendToResultFile(transformationStartMsg);
			Bundle allergyBundle = rt.tAllergyProblemAct2AllergyIntolerance(cdaAPA, bundleInfo).getBundle();
			appendToResultFile(transformationEndMsg);
			appendToResultFile(allergyBundle);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testAssignedAuthor2Practitioner() {
		appendToResultFile("## TEST: AssignedAuthor2Practitioner\n");
		// null instance test
		BundleInfo bundleInfo = new BundleInfo(rt);
		org.openhealthtools.mdht.uml.cda.AssignedAuthor cdaNull = null;
		Bundle fhirNull = rt.tAssignedAuthor2Practitioner(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getAuthors() != null) {
			for (org.openhealthtools.mdht.uml.cda.Author author : ResourceTransformerTest.ccd.getAuthors()) {
				// traversing authors
				if (author != null && author.getAssignedAuthor() != null) {
					appendToResultFile(transformationStartMsg);
					Bundle practitionerBundle = rt.tAssignedAuthor2Practitioner(author.getAssignedAuthor(), bundleInfo)
							.getBundle();
					appendToResultFile(transformationEndMsg);
					appendToResultFile(practitionerBundle);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testAssignedEntity2Practitioner() {
		appendToResultFile("## TEST: AssignedEntity2Practitioner\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.AssignedEntity cdaNull = null;
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirNull = rt.tAssignedEntity2Practitioner(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getProceduresSection() != null
				&& !ResourceTransformerTest.ccd.getProceduresSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getProceduresSection().getProcedures() != null
					&& !ResourceTransformerTest.ccd.getProceduresSection().getProcedures().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Procedure procedure : ResourceTransformerTest.ccd
						.getProceduresSection().getProcedures()) {
					// traversing procedures
					if (procedure.getPerformers() != null && !procedure.getPerformers().isEmpty()) {
						for (org.openhealthtools.mdht.uml.cda.Performer2 performer : procedure.getPerformers()) {
							if (performer.getAssignedEntity() != null
									&& !performer.getAssignedEntity().isSetNullFlavor()) {
								appendToResultFile(transformationStartMsg);
								Bundle fhirPractitionerBundle = rt
										.tAssignedEntity2Practitioner(performer.getAssignedEntity(), bundleInfo)
										.getBundle();
								appendToResultFile(transformationEndMsg);
								appendToResultFile(fhirPractitionerBundle);
							}
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testClinicalDocument2Composition() {
		appendToResultFile("## TEST: ClinicalDocument2Composition\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument cdaNull = null;
		Bundle fhirNull = rt.tClinicalDocument2Composition(cdaNull).getBundle();
		Assert.assertNull(fhirNull);

		// instance from file
		if (ResourceTransformerTest.ccd != null && !ResourceTransformerTest.ccd.isSetNullFlavor()) {
			appendToResultFile(transformationStartMsg);
			Bundle fhirComp = rt.tClinicalDocument2Composition(ResourceTransformerTest.ccd).getBundle();
			appendToResultFile(transformationEndMsg);
			appendToResultFile(fhirComp);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testEncounterActivity2Encounter() {
		appendToResultFile("## TEST: EncounterActivity2Encounter\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaNull = null;
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirNull = rt.tEncounterActivity2Encounter(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getEncountersSection() != null
				&& !ResourceTransformerTest.ccd.getEncountersSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getEncountersSection().getEncounterActivitiess() != null
					&& !ResourceTransformerTest.ccd.getEncountersSection().getEncounterActivitiess().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.consol.EncounterActivities encounterActivity : ResourceTransformerTest.ccd
						.getEncountersSection().getEncounterActivitiess()) {
					if (encounterActivity != null && !encounterActivity.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirEncounterBundle = rt.tEncounterActivity2Encounter(encounterActivity, bundleInfo)
								.getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirEncounterBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testFamilyHistoryOrganizer2FamilyMemberHistory() {
		appendToResultFile("## TEST: FamilyHistoryOrganizer2FamilyMemberHistory\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer cdaNull = null;
		FamilyMemberHistory fhirNull = rt.tFamilyHistoryOrganizer2FamilyMemberHistory(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getFamilyHistorySection() != null
				&& ResourceTransformerTest.ccd.getFamilyHistorySection().getFamilyHistories() != null) {
			for (org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer familyHistoryOrganizer : ResourceTransformerTest.ccd
					.getFamilyHistorySection().getFamilyHistories()) {
				if (familyHistoryOrganizer != null) {
					appendToResultFile(transformationStartMsg);
					FamilyMemberHistory fmHistory = rt
							.tFamilyHistoryOrganizer2FamilyMemberHistory(familyHistoryOrganizer);
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fmHistory);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testFunctionalStatus2Observation() {
		appendToResultFile("## TEST: FunctionalStatus2Observation\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Observation cdaNull = null;
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirNull = rt.tFunctionalStatus2Observation(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instance from file
		FunctionalStatusSection funcStatSec = ResourceTransformerTest.ccd.getFunctionalStatusSection();

		if (funcStatSec != null && !funcStatSec.isSetNullFlavor()) {
			if (funcStatSec.getOrganizers() != null && !funcStatSec.getOrganizers().isEmpty()) {
				for (Organizer funcStatOrg : funcStatSec.getOrganizers()) {
					if (funcStatOrg != null && !funcStatOrg.isSetNullFlavor()) {
						if (funcStatOrg instanceof FunctionalStatusResultOrganizer) {
							if (((FunctionalStatusResultOrganizer) funcStatOrg).getObservations() != null
									&& !((FunctionalStatusResultOrganizer) funcStatOrg).getObservations().isEmpty()) {
								for (org.openhealthtools.mdht.uml.cda.Observation cdaObs : ((FunctionalStatusResultOrganizer) funcStatOrg)
										.getObservations()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirObs = rt.tFunctionalStatus2Observation(cdaObs, bundleInfo).getBundle();
									appendToResultFile(transformationEndMsg);
									appendToResultFile(fhirObs);
								}
							}
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testGuardian2Contact() {
		appendToResultFile("## TEST: Guardian2Contact\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Guardian cdaNull = null;
		ContactComponent fhirNull = rt.tGuardian2Contact(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getPatientRoles() != null
				&& !ResourceTransformerTest.ccd.getPatientRoles().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.PatientRole patientRole : ResourceTransformerTest.ccd
					.getPatientRoles()) {
				if (patientRole != null && !patientRole.isSetNullFlavor() && patientRole.getPatient() != null
						&& !patientRole.getPatient().isSetNullFlavor()) {
					for (org.openhealthtools.mdht.uml.cda.Guardian guardian : patientRole.getPatient().getGuardians()) {
						if (guardian != null && !guardian.isSetNullFlavor()) {
							appendToResultFile(transformationStartMsg);
							ContactComponent contact = rt.tGuardian2Contact(guardian);
							appendToResultFile(transformationEndMsg);
							org.hl7.fhir.dstu3.model.Patient patient = new Patient().addContact(contact);
							appendToResultFile(patient);
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testImmunizationActivity2Immunization() {
		appendToResultFile("## TEST: ImmunizationActivity2Immunization\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity cdaNull = null;
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirNull = rt.tImmunizationActivity2Immunization(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		ImmunizationsSectionEntriesOptional immSec = ResourceTransformerTest.ccd
				.getImmunizationsSectionEntriesOptional();

		if (immSec != null && !immSec.isSetNullFlavor()) {
			for (ImmunizationActivity immAct : immSec.getImmunizationActivities()) {
				if (immAct != null && !immAct.isSetNullFlavor()) {
					appendToResultFile(transformationStartMsg);
					Bundle fhirImm = rt.tImmunizationActivity2Immunization(immAct, bundleInfo).getBundle();
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirImm);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testLanguageCommunication2Communication() {
		appendToResultFile("## TEST: LanguageCommunication2Communication\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.LanguageCommunication cdaNull = null;
		PatientCommunicationComponent fhirNull = rt.tLanguageCommunication2Communication(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for (org.openhealthtools.mdht.uml.cda.Patient patient : ResourceTransformerTest.ccd.getPatients()) {
			for (org.openhealthtools.mdht.uml.cda.LanguageCommunication LC : patient.getLanguageCommunications()) {
				appendToResultFile(transformationStartMsg);
				PatientCommunicationComponent fhirCommunication = rt.tLanguageCommunication2Communication(LC);
				appendToResultFile(transformationEndMsg);
				org.hl7.fhir.dstu3.model.Patient fhirPatient = new org.hl7.fhir.dstu3.model.Patient();
				fhirPatient.addCommunication(fhirCommunication);
				appendToResultFile(fhirPatient);
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testManufacturedProduct2Medication() {
		appendToResultFile("## TEST: ManufacturedProduct2Medication\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.ManufacturedProduct cdaNull = null;
		Bundle fhirNull = rt.tManufacturedProduct2Medication(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		ImmunizationsSectionEntriesOptional immSection = ResourceTransformerTest.ccd
				.getImmunizationsSectionEntriesOptional();
		if (immSection != null && !immSection.isSetNullFlavor()) {
			if (immSection.getImmunizationActivities() != null && !immSection.getImmunizationActivities().isEmpty()) {
				for (ImmunizationActivity immAct : immSection.getImmunizationActivities()) {
					if (immAct != null && !immAct.isSetNullFlavor()) {
						if (immAct.getConsumable() != null && !immAct.getConsumable().isSetNullFlavor()) {
							if (immAct.getConsumable().getManufacturedProduct() != null
									&& !immAct.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
								// immAct.immSection.immAct.consumable.manuProd
								appendToResultFile(transformationStartMsg);
								Bundle fhirMed = rt.tManufacturedProduct2Medication(
										immAct.getConsumable().getManufacturedProduct());
								appendToResultFile(transformationEndMsg);
								appendToResultFile(fhirMed);
							}
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testMedicationActivity2MedicationStatement() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: MedicationActivity2MedicationStatement\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.MedicationActivity cdaNull = null;
		Bundle fhirNull = rt.tMedicationActivity2MedicationStatement(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getMedicationsSection() != null
				&& !ResourceTransformerTest.ccd.getMedicationsSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getMedicationsSection().getMedicationActivities() != null
					&& !ResourceTransformerTest.ccd.getMedicationsSection().getMedicationActivities().isEmpty()) {
				for (MedicationActivity cdaMedAct : ResourceTransformerTest.ccd.getMedicationsSection()
						.getMedicationActivities()) {
					if (cdaMedAct != null && !cdaMedAct.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirMedStBundle = rt.tMedicationActivity2MedicationStatement(cdaMedAct, bundleInfo)
								.getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirMedStBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testMedicationDispense2MedicationDispense() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: MedicationDispense2MedicationDispense\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaNull = null;
		Bundle fhirNull = rt.tMedicationDispense2MedicationDispense(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		// medicationsSection.medicationActivities.medicationDispense
		if (ResourceTransformerTest.ccd.getMedicationsSection() != null
				&& !ResourceTransformerTest.ccd.getMedicationsSection().isSetNullFlavor()) {
			org.openhealthtools.mdht.uml.cda.consol.MedicationsSection medSec = ResourceTransformerTest.ccd
					.getMedicationsSection();
			if (medSec.getMedicationActivities() != null && !medSec.getMedicationActivities().isEmpty()) {
				for (MedicationActivity medAct : medSec.getMedicationActivities()) {
					if (medAct != null && !medAct.isSetNullFlavor()) {
						if (medAct.getMedicationDispenses() != null && !medAct.getMedicationDispenses().isEmpty()) {
							for (org.openhealthtools.mdht.uml.cda.consol.MedicationDispense medDisp : medAct
									.getMedicationDispenses()) {
								if (medDisp != null && !medDisp.isSetNullFlavor()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirMedDispBundle = rt
											.tMedicationDispense2MedicationDispense(medDisp, bundleInfo).getBundle();
									appendToResultFile(transformationEndMsg);
									appendToResultFile(fhirMedDispBundle);
								}
							}
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testObservation2Observation() {
		appendToResultFile("## TEST: Observation2Observation\n");
		BundleInfo bundleInfo = new BundleInfo(rt);
		// null instance test
		org.openhealthtools.mdht.uml.cda.Observation cdaNull = null;
		Bundle fhirNull = rt.tObservation2Observation(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getSocialHistorySection() != null
				&& !ResourceTransformerTest.ccd.getSocialHistorySection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getSocialHistorySection().getObservations() != null
					&& !ResourceTransformerTest.ccd.getSocialHistorySection().getObservations().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Observation cdaObs : ResourceTransformerTest.ccd
						.getSocialHistorySection().getObservations()) {
					if (cdaObs != null && !cdaObs.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle obsBundle = rt.tObservation2Observation(cdaObs, bundleInfo).getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(obsBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testOrganization2Organization() {
		appendToResultFile("## TEST: Organization2Organization\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Organization cdaNull = null;
		Organization fhirNull = rt.tOrganization2Organization(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for (org.openhealthtools.mdht.uml.cda.PatientRole patRole : ResourceTransformerTest.ccd.getPatientRoles()) {
			org.openhealthtools.mdht.uml.cda.Organization cdaOrg = patRole.getProviderOrganization();
			appendToResultFile(transformationStartMsg);
			org.hl7.fhir.dstu3.model.Organization fhirOrg = rt.tOrganization2Organization(cdaOrg);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(fhirOrg);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testPatientRole2Patient() {
		appendToResultFile("## TEST: PatientRole2Patient\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.PatientRole cdaNull = null;
		Bundle fhirNull = rt.tPatientRole2Patient(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for (PatientRole pr : ResourceTransformerTest.ccd.getPatientRoles()) {

			// here we do the transformation by calling the method rt.PatientRole2Patient

			Patient patient = null;

			appendToResultFile(transformationStartMsg);
			Bundle patientBundle = rt.tPatientRole2Patient(pr);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(patientBundle);

			for (BundleEntryComponent entry : patientBundle.getEntry()) {
				if (entry.getResource() instanceof Patient) {
					patient = (Patient) entry.getResource();
				}
			}

			// patient.identifier
			int idCount = 0;
			for (II id : pr.getIds()) {
				if (id.getRoot() != null && id.getExtension() != null) {
					// since extension may contain "urn:oid:" or "urn:uuid:", assertion is about
					// containing the value as a piece
					Assert.assertTrue("pr.id.extension #" + idCount + " was not transformed",
							patient.getIdentifier().get(idCount).getValue().contains(id.getExtension()));
					Assert.assertTrue("pr.id.root #" + idCount + " was not transformed",
							patient.getIdentifier().get(idCount).getSystem().contains(id.getRoot()));
				} else if (id.getRoot() != null) {
					Assert.assertTrue("pr.id.root #" + idCount + " was not transformed",
							patient.getIdentifier().get(idCount).getValue().contains(id.getRoot()));
				} else if (id.getExtension() != null) {
					Assert.assertTrue("pr.id.root #" + idCount + " was not transformed",
							patient.getIdentifier().get(idCount).getValue().contains(id.getExtension()));
				}
				// codeSystem method is changed and tested

				idCount++;
			}
			// patient.name
			// Notice that patient.name is fullfilled by the method EN2HumanName.
			int nameCount = 0;
			for (EN pn : pr.getPatient().getNames()) {

				// patient.name.use
				if (pn.getUses() == null || pn.getUses().isEmpty()) {
					Assert.assertNull(patient.getName().get(nameCount).getUse().toCode());
				} else {
					Assert.assertEquals("pr.patient.name[" + nameCount + "]" + ".use was not transformed",
							vsti.tEntityNameUse2NameUse(pn.getUses().get(0)).toString().toLowerCase(),
							patient.getName().get(nameCount).getUse().toCode());
				}

				// patient.name.text
				Assert.assertEquals("pr.patient.name[" + nameCount + "].text was not transformed", pn.getText(),
						patient.getName().get(nameCount).getText());

				// patient.name.family
				for (ENXP family : pn.getFamilies()) {
					if (family == null || family.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getFamily() == null
								|| !patient.getName().get(nameCount)
										.hasFamily()/* patient.getName().get(nameCount).getFamily().size() == 0 */);
					} else {
						Assert.assertEquals("pr.patient.name[" + nameCount + "].family was not transformed",
								family.getText(),
								patient.getName().get(nameCount).getFamily()/* .get(familyCount).getValue() */);
					}
				}

				// patient.name.given
				int givenCount = 0;
				for (ENXP given : pn.getGivens()) {
					if (given == null || given.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getGiven() == null
								|| patient.getName().get(nameCount).getGiven().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name[" + nameCount + "].given was not transformed",
								given.getText(),
								patient.getName().get(nameCount).getGiven().get(givenCount).getValue());
					}
					givenCount++;
				}

				// patient.name.prefix
				int prefixCount = 0;
				for (ENXP prefix : pn.getPrefixes()) {
					if (prefix == null || prefix.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getPrefix() == null
								|| patient.getName().get(nameCount).getPrefix().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name[" + nameCount + "].prefix was not transformed",
								prefix.getText(),
								patient.getName().get(nameCount).getPrefix().get(prefixCount).getValue());
					}
					prefixCount++;
				}

				// patient.name.suffix
				int suffixCount = 0;
				for (ENXP suffix : pn.getPrefixes()) {
					if (suffix == null || suffix.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getSuffix() == null
								|| patient.getName().get(nameCount).getSuffix().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name[" + nameCount + "].suffix was not transformed",
								suffix.getText(),
								patient.getName().get(nameCount).getSuffix().get(suffixCount).getValue());
					}
					suffixCount++;
				}

				// patient.name.period
				if (pn.getValidTime() == null || pn.getValidTime().isSetNullFlavor()) {
					// It can return null or an empty list
					Assert.assertTrue(patient.getName().get(nameCount).getPeriod() == null
							|| patient.getName().get(nameCount).getPeriod().isEmpty());
				}
			}

			// patient.telecom
			// Notice that patient.telecom is fullfilled by the method dtt.TEL2ContactPoint
			if (pr.getTelecoms() == null || pr.getTelecoms().isEmpty()) {
				Assert.assertTrue(patient.getTelecom() == null || patient.getTelecom().isEmpty());
			} else {
				// size check
				Assert.assertTrue(pr.getTelecoms().size() == patient.getTelecom().size());
				// We have already tested the method TEL2ContactPoint. Therefore, null-check and
				// size-check is enough for now.
			}

			// patient.gender
			// vst.AdministrativeGenderCode2AdministrativeGenderEnum is used in this
			// transformation.
			// Following test aims to test that ValueSetTransformer method.
			if (pr.getPatient().getAdministrativeGenderCode() == null
					|| pr.getPatient().getAdministrativeGenderCode().isSetNullFlavor()) {
				Assert.assertTrue(patient.getGender() == null || !patient.getGenderElement().hasValue());
			}

			// patient.birthDate
			// Notice that patient.birthDate is fullfilled by the method dtt.TS2Date
			if (pr.getPatient().getBirthTime() == null || pr.getPatient().getBirthTime().isSetNullFlavor()) {
				Assert.assertTrue(patient.getBirthDate() == null);
			}

			// patient.address
			// Notice that patient.address is fullfilled by the method dtt.AD2Address
			if (pr.getAddrs() == null || pr.getAddrs().isEmpty()) {
				Assert.assertTrue(patient.getAddress() == null || patient.getAddress().isEmpty());
			} else {
				// We have already tested the method AD2Address. Therefore, null-check and
				// size-check is enough for now.
				Assert.assertTrue(pr.getAddrs().size() == patient.getAddress().size());
			}

			// patient.maritalStatus
			// vst.MaritalStatusCode2MaritalStatusCodesEnum is used in this transformation.
			// Following test aims to test that ValueSetTransformer method.
			if (pr.getPatient().getMaritalStatusCode() == null
					|| pr.getPatient().getMaritalStatusCode().isSetNullFlavor()) {
				Assert.assertTrue(patient.getMaritalStatus() == null || patient.getMaritalStatus().isEmpty());
			} else {
				Assert.assertTrue(patient.getMaritalStatus().getCoding().get(0).getCode().toLowerCase().charAt(0) == pr
						.getPatient().getMaritalStatusCode().getCode().toLowerCase().charAt(0));
			}

			// patient.languageCommunication
			if (pr.getPatient().getLanguageCommunications() == null
					|| pr.getPatient().getLanguageCommunications().isEmpty()) {
				Assert.assertTrue(patient.getCommunication() == null || patient.getCommunication().isEmpty());
			} else {
				Assert.assertTrue(
						pr.getPatient().getLanguageCommunications().size() == patient.getCommunication().size());

				int sizeCommunication = pr.getPatient().getLanguageCommunications().size();
				while (sizeCommunication != 0) {

					// language
					if (pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getLanguageCode() == null
							|| pr.getPatient().getLanguageCommunications().get(0).getLanguageCode().isSetNullFlavor()) {
						Assert.assertTrue(patient.getCommunication().get(sizeCommunication - 1).getLanguage() == null
								|| patient.getCommunication().get(sizeCommunication - 1).getLanguage().isEmpty());
					} else {
						// We have already tested the method CD2CodeableConcept. Therefore, null-check
						// is enough for now.
					}

					// preference
					if (pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1)
							.getPreferenceInd() == null
							|| pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd()
									.isSetNullFlavor()) {
						// Assert.assertTrue(patient.getCommunication().get(sizeCommunication -
						// 1).getPreferred() == null);
						Assert.assertTrue(!patient.getCommunication().get(sizeCommunication - 1).hasPreferred());
					} else {
						Assert.assertEquals(
								pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1)
										.getPreferenceInd().getValue(),
								patient.getCommunication().get(sizeCommunication - 1).getPreferred());
					}
					sizeCommunication--;
				}

			}

			// providerOrganization
			if (pr.getProviderOrganization() == null || pr.getProviderOrganization().isSetNullFlavor()) {
				Assert.assertTrue(
						patient.getManagingOrganization() == null || patient.getManagingOrganization().isEmpty());
			} else {
				if (pr.getProviderOrganization().getNames() == null) {
					Assert.assertTrue(patient.getManagingOrganization().getDisplay() == null);
				}
			}

			// guardian
			if (pr.getPatient().getGuardians() == null || pr.getPatient().getGuardians().isEmpty()) {
				Assert.assertTrue(patient.getContact() == null || patient.getContact().isEmpty());
			} else {
				// Notice that, inside this mapping, the methods dtt.TEL2ContactPoint and
				// dtt.AD2Address are used.
				// Therefore, null-check and size-check are enough
				Assert.assertTrue(pr.getPatient().getGuardians().size() == patient.getContact().size());
			}

			// extensions
			for (Extension extension : patient.getExtension()) {
				Assert.assertTrue(extension.getUrl() != null);
				Assert.assertTrue(extension.getValue() != null);
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testProblemConcernAct2Condition() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: ProblemConcernAct2Condition\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct cdaNull = null;
		Bundle fhirNull = rt.tProblemConcernAct2Condition(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		if (ResourceTransformerTest.ccd.getProblemSection() != null
				&& !ResourceTransformerTest.ccd.getProblemSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getProblemSection().getProblemConcerns() != null
					&& !ResourceTransformerTest.ccd.getProblemSection().getProblemConcerns().isEmpty()) {
				for (ProblemConcernAct problemConcernAct : ResourceTransformerTest.ccd.getProblemSection()
						.getProblemConcerns()) {
					if (problemConcernAct != null && !problemConcernAct.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirConditionBundle = rt.tProblemConcernAct2Condition(problemConcernAct, bundleInfo)
								.getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirConditionBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testProcedure2Procedure() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: Procedure2Procedure\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Procedure cdaNull = null;
		EntryResult entryResultNull = rt.tProcedure2Procedure(cdaNull, bundleInfo);
		Assert.assertNull(entryResultNull.getBundle());

		// instances from file
		if (ResourceTransformerTest.ccd.getProceduresSection() != null
				&& !ResourceTransformerTest.ccd.getProceduresSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getProceduresSection().getProcedures() != null
					&& !ResourceTransformerTest.ccd.getProceduresSection().getProcedures().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : ResourceTransformerTest.ccd
						.getProceduresSection().getProcedures()) {
					// traversing procedures
					appendToResultFile(transformationStartMsg);
					EntryResult entryResult = rt.tProcedure2Procedure(cdaProcedure, bundleInfo);
					Bundle fhirProcedureBundle = entryResult.getBundle();
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirProcedureBundle);
				}
			}
		}

		if (ResourceTransformerTest.ccd.getEncountersSection() != null
				&& !ResourceTransformerTest.ccd.getEncountersSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getEncountersSection().getProcedures() != null
					&& !ResourceTransformerTest.ccd.getEncountersSection().getProcedures().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : ResourceTransformerTest.ccd
						.getEncountersSection().getProcedures()) {
					// traversing procedures
					appendToResultFile(transformationStartMsg);
					EntryResult entryResult = rt.tProcedure2Procedure(cdaProcedure, bundleInfo);
					Bundle fhirProcedureBundle = entryResult.getBundle();
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirProcedureBundle);
				}
			}
		}

		if (ResourceTransformerTest.ccd.getAllSections() != null
				&& !ResourceTransformerTest.ccd.getAllSections().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Section section : ResourceTransformerTest.ccd.getAllSections()) {
				if (section.getProcedures() != null && !section.getProcedures().isEmpty()) {
					for (org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : section.getProcedures()) {
						// traversing procedures
						appendToResultFile(transformationStartMsg);
						EntryResult entryResult = rt.tProcedure2Procedure(cdaProcedure, bundleInfo);
						Bundle fhirProcedureBundle = entryResult.getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirProcedureBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testResultOrganizer2DiagnosticReport() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: ResultOrganizer2DiagnosticReport\n");
		// null instance test
		ResultOrganizer cdaNull = null;
		Bundle fhirNull = rt.tResultOrganizer2DiagnosticReport(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instance from file
		ResultsSection resultsSec = ResourceTransformerTest.ccd.getResultsSection();

		if (resultsSec != null && !resultsSec.isSetNullFlavor()) {
			if (resultsSec.getOrganizers() != null && !resultsSec.getOrganizers().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Organizer cdaOrganizer : resultsSec.getOrganizers()) {
					if (cdaOrganizer != null && !cdaOrganizer.isSetNullFlavor()) {
						if (cdaOrganizer instanceof ResultOrganizer) {
							appendToResultFile(transformationStartMsg);
							Bundle fhirDiagReport = rt
									.tResultOrganizer2DiagnosticReport((ResultOrganizer) cdaOrganizer, bundleInfo)
									.getBundle();
							appendToResultFile(transformationEndMsg);
							appendToResultFile(fhirDiagReport);
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testSection2Section() {
		appendToResultFile("## TEST: Section2Section\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Section cdaNull = null;
		SectionComponent fhirNull = rt.tSection2Section(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		org.openhealthtools.mdht.uml.cda.Section sampleSection = null;

		// assigning sampleSection to one sample section
		if (ResourceTransformerTest.ccd.getEncountersSection() != null
				&& !ResourceTransformerTest.ccd.getEncountersSection().isSetNullFlavor()) {
			if (ResourceTransformerTest.ccd.getEncountersSection().getAllSections() != null
					&& !ResourceTransformerTest.ccd.getEncountersSection().getAllSections().isEmpty()) {
				if (ResourceTransformerTest.ccd.getEncountersSection().getAllSections().get(0) != null
						&& !ResourceTransformerTest.ccd.getEncountersSection().getAllSections().get(0)
								.isSetNullFlavor()) {
					sampleSection = ResourceTransformerTest.ccd.getEncountersSection().getAllSections().get(0);
				}
			}
		}
		if (sampleSection != null) {
			org.hl7.fhir.dstu3.model.Composition fhirComposition = new org.hl7.fhir.dstu3.model.Composition();
			appendToResultFile(transformationStartMsg);
			SectionComponent fhirSection = rt.tSection2Section(sampleSection);
			appendToResultFile(transformationEndMsg);
			fhirComposition.addSection(fhirSection);
			appendToResultFile(fhirComposition);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testSocialHistory() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: SocialHistory\n");
		SocialHistorySection socialHistSec = ResourceTransformerTest.ccd.getSocialHistorySection();

		if (socialHistSec != null && !socialHistSec.isSetNullFlavor()) {
			if (socialHistSec.getObservations() != null && !socialHistSec.getObservations().isEmpty()) {
				for (org.openhealthtools.mdht.uml.cda.Observation cdaObs : socialHistSec.getObservations()) {
					if (cdaObs != null && !cdaObs.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirObs = rt.tObservation2Observation(cdaObs, bundleInfo).getBundle();
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirObs);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testVitalSignObservation2Observation() {
		BundleInfo bundleInfo = new BundleInfo(rt);
		appendToResultFile("## TEST: VitalSignObservation2Observation\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation cdaNull = null;
		Bundle fhirNull = rt.tVitalSignObservation2Observation(cdaNull, bundleInfo).getBundle();
		Assert.assertNull(fhirNull);

		// instances from file
		VitalSignsSectionEntriesOptional vitalSignsSec = ResourceTransformerTest.ccd
				.getVitalSignsSectionEntriesOptional();
		if (vitalSignsSec != null && !vitalSignsSec.isSetNullFlavor()) {
			if (vitalSignsSec.getVitalSignsOrganizers() != null && !vitalSignsSec.getVitalSignsOrganizers().isEmpty()) {
				for (VitalSignsOrganizer vitalSignOrganizer : vitalSignsSec.getVitalSignsOrganizers()) {
					if (vitalSignOrganizer != null && !vitalSignOrganizer.isSetNullFlavor()) {
						if (vitalSignOrganizer.getVitalSignObservations() != null
								&& !vitalSignOrganizer.getVitalSignObservations().isEmpty()) {
							for (VitalSignObservation vitalSignObservation : vitalSignOrganizer
									.getVitalSignObservations()) {
								if (vitalSignObservation != null && !vitalSignObservation.isSetNullFlavor()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirObservation = rt
											.tVitalSignObservation2Observation(
													(VitalSignObservation) vitalSignObservation, bundleInfo)
											.getBundle();
									appendToResultFile(transformationEndMsg);
									appendToResultFile(fhirObservation);
								}
							}
						}
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	private void appendToResultFile(Object param) {
		try {
			if (param instanceof String) {
				resultFW.append((String) param);
			} else if (param instanceof IResource) {
				FHIRUtil.printJSON((IResource) param, resultFW);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}