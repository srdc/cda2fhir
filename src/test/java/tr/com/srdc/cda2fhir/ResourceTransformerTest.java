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

import java.io.*;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Composition.Section;
import tr.com.srdc.cda2fhir.transform.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ResourceTransformerTest {
	
	ResourceTransformerImpl rt = new ResourceTransformerImpl();
	DataTypesTransformerImpl dtt = new DataTypesTransformerImpl();
	ValueSetsTransformerImpl vsti = new ValueSetsTransformerImpl();
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

	// Most of the test methods just print the transformed object in JSON form.
	
	@Test
	public void testAllergyProblemAct2AllergyIntolerance() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: AllergyProblemAct2AllergyIntolerance\n");
		// null instance test
		AllergyProblemAct cdaNull = null;
		Bundle fhirNull = rt.tAllergyProblemAct2AllergyIntolerance(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for(AllergyProblemAct cdaAPA : test.ccd.getAllergiesSection().getAllergyProblemActs()) {
			appendToResultFile(transformationStartMsg);
			Bundle allergyBundle = rt.tAllergyProblemAct2AllergyIntolerance(cdaAPA);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(allergyBundle);
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testAssignedAuthor2Practitioner() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: AssignedAuthor2Practitioner\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.AssignedAuthor cdaNull = null;
		Bundle fhirNull = rt.tAssignedAuthor2Practitioner(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getAuthors() != null) {
			for(org.openhealthtools.mdht.uml.cda.Author author : test.ccd.getAuthors()) {
				// traversing authors
				if(author != null && author.getAssignedAuthor() != null) {
					appendToResultFile(transformationStartMsg);
					Bundle practitionerBundle = rt.tAssignedAuthor2Practitioner(author.getAssignedAuthor());
					appendToResultFile(transformationEndMsg);
					appendToResultFile(practitionerBundle);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testAssignedEntity2Practitioner() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: AssignedEntity2Practitioner\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.AssignedEntity cdaNull = null;
		Bundle fhirNull = rt.tAssignedEntity2Practitioner(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getProceduresSection() != null && !test.ccd.getProceduresSection().isSetNullFlavor()) {
			if(test.ccd.getProceduresSection().getProcedures() != null && !test.ccd.getProceduresSection().getProcedures().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Procedure procedure : test.ccd.getProceduresSection().getProcedures()) {
					// traversing procedures
					if(procedure.getPerformers() != null && !procedure.getPerformers().isEmpty()) {
						for(org.openhealthtools.mdht.uml.cda.Performer2 performer : procedure.getPerformers()) {
							if(performer.getAssignedEntity() != null && !performer.getAssignedEntity().isSetNullFlavor()) {
								appendToResultFile(transformationStartMsg);
								Bundle fhirPractitionerBundle = rt.tAssignedEntity2Practitioner(performer.getAssignedEntity());
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
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: ClinicalDocument2Composition\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument cdaNull = null;
		Bundle fhirNull = rt.tClinicalDocument2Composition(cdaNull);
		Assert.assertNull(fhirNull);

		// instance from file
		if(test.ccd != null && !test.ccd.isSetNullFlavor()) {
			appendToResultFile(transformationStartMsg);
			Bundle fhirComp = rt.tClinicalDocument2Composition(test.ccd);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(fhirComp);
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testEncounterActivity2Encounter(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: EncounterActivity2Encounter\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaNull = null;
		Bundle fhirNull = rt.tEncounterActivity2Encounter(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getEncountersSection() != null && !test.ccd.getEncountersSection().isSetNullFlavor()) {
			if(test.ccd.getEncountersSection().getEncounterActivitiess() != null && !test.ccd.getEncountersSection().getEncounterActivitiess().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.consol.EncounterActivities encounterActivity : test.ccd.getEncountersSection().getEncounterActivitiess()) {
					if(encounterActivity != null && !encounterActivity.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirEncounterBundle = rt.tEncounterActivity2Encounter(encounterActivity);
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirEncounterBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testFamilyHistoryOrganizer2FamilyMemberHistory(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: FamilyHistoryOrganizer2FamilyMemberHistory\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer cdaNull = null;
		FamilyMemberHistory fhirNull = rt.tFamilyHistoryOrganizer2FamilyMemberHistory(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getFamilyHistorySection() != null && test.ccd.getFamilyHistorySection().getFamilyHistories() != null) {
			for(org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer familyHistoryOrganizer : test.ccd.getFamilyHistorySection().getFamilyHistories()) {
				if(familyHistoryOrganizer != null) {
					appendToResultFile(transformationStartMsg);
					FamilyMemberHistory fmHistory = rt.tFamilyHistoryOrganizer2FamilyMemberHistory(familyHistoryOrganizer);
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fmHistory);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testFunctionalStatus2Observation() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: FunctionalStatus2Observation\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Observation cdaNull = null;
		Bundle fhirNull = rt.tFunctionalStatus2Observation(cdaNull);
		Assert.assertNull(fhirNull);

		// instance from file
		FunctionalStatusSection funcStatSec = test.ccd.getFunctionalStatusSection();

		if(funcStatSec != null && !funcStatSec.isSetNullFlavor()) {
			if(funcStatSec.getOrganizers() != null && !funcStatSec.getOrganizers().isEmpty()) {
				for(Organizer funcStatOrg : funcStatSec.getOrganizers()) {
					if(funcStatOrg != null && !funcStatOrg.isSetNullFlavor()) {
						if(funcStatOrg instanceof FunctionalStatusResultOrganizer) {
							if(((FunctionalStatusResultOrganizer)funcStatOrg).getObservations() != null && !((FunctionalStatusResultOrganizer)funcStatOrg).getObservations().isEmpty()) {
								for(org.openhealthtools.mdht.uml.cda.Observation cdaObs : ((FunctionalStatusResultOrganizer)funcStatOrg).getObservations()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirObs = rt.tFunctionalStatus2Observation(cdaObs);
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
	public void testGuardian2Contact(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: Guardian2Contact\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Guardian cdaNull = null;
		Patient.Contact fhirNull = rt.tGuardian2Contact(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getPatientRoles() != null && !test.ccd.getPatientRoles().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.PatientRole patientRole : test.ccd.getPatientRoles()) {
				if(patientRole != null && !patientRole.isSetNullFlavor() && patientRole.getPatient() != null
						&& !patientRole.getPatient().isSetNullFlavor()) {
					for(org.openhealthtools.mdht.uml.cda.Guardian guardian : patientRole.getPatient().getGuardians()) {
						if(guardian != null && !guardian.isSetNullFlavor()) {
							appendToResultFile(transformationStartMsg);
							ca.uhn.fhir.model.dstu2.resource.Patient.Contact contact = rt.tGuardian2Contact(guardian);
							appendToResultFile(transformationEndMsg);
							ca.uhn.fhir.model.dstu2.resource.Patient patient = new Patient().addContact(contact);
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
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: ImmunizationActivity2Immunization\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity cdaNull = null;
		Bundle fhirNull = rt.tImmunizationActivity2Immunization(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		ImmunizationsSectionEntriesOptional immSec = test.ccd.getImmunizationsSectionEntriesOptional();
		
		if(immSec != null && !immSec.isSetNullFlavor()) {
			for(ImmunizationActivity immAct : immSec.getImmunizationActivities()) {
				if(immAct != null && !immAct.isSetNullFlavor()) {
					appendToResultFile(transformationStartMsg);
					Bundle fhirImm = rt.tImmunizationActivity2Immunization(immAct);
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirImm);
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testLanguageCommunication2Communication(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: LanguageCommunication2Communication\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.LanguageCommunication cdaNull = null;
		ca.uhn.fhir.model.dstu2.resource.Patient.Communication fhirNull = rt.tLanguageCommunication2Communication(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for(org.openhealthtools.mdht.uml.cda.Patient patient : test.ccd.getPatients()) {
			for(org.openhealthtools.mdht.uml.cda.LanguageCommunication LC : patient.getLanguageCommunications()) {
				appendToResultFile(transformationStartMsg);
				ca.uhn.fhir.model.dstu2.resource.Patient.Communication fhirCommunication = rt.tLanguageCommunication2Communication(LC);
				appendToResultFile(transformationEndMsg);
				ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient = new ca.uhn.fhir.model.dstu2.resource.Patient();
				fhirPatient.addCommunication(fhirCommunication);
				appendToResultFile(fhirPatient);
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testManufacturedProduct2Medication(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: ManufacturedProduct2Medication\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.ManufacturedProduct cdaNull = null;
		Bundle fhirNull = rt.tManufacturedProduct2Medication(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		ImmunizationsSectionEntriesOptional immSection = test.ccd.getImmunizationsSectionEntriesOptional();
		if(immSection != null && !immSection.isSetNullFlavor()) {
			if(immSection.getImmunizationActivities() != null && !immSection.getImmunizationActivities().isEmpty()) {
				for(ImmunizationActivity immAct : immSection.getImmunizationActivities()) {
					if(immAct != null && !immAct.isSetNullFlavor()) {
						if(immAct.getConsumable() != null && !immAct.getConsumable().isSetNullFlavor()) {
							if(immAct.getConsumable().getManufacturedProduct() != null && !immAct.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
								// immAct.immSection.immAct.consumable.manuProd
								appendToResultFile(transformationStartMsg);
								Bundle fhirMed = rt.tManufacturedProduct2Medication(immAct.getConsumable().getManufacturedProduct());
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
	public void testMedicationActivity2MedicationStatement(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: MedicationActivity2MedicationStatement\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.MedicationActivity cdaNull = null;
		Bundle fhirNull = rt.tMedicationActivity2MedicationStatement(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getMedicationsSection() != null && !test.ccd.getMedicationsSection().isSetNullFlavor()) {
			if(test.ccd.getMedicationsSection().getMedicationActivities() != null && !test.ccd.getMedicationsSection().getMedicationActivities().isEmpty()) {
				for(MedicationActivity cdaMedAct : test.ccd.getMedicationsSection().getMedicationActivities()) {
					if(cdaMedAct != null && !cdaMedAct.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirMedStBundle = rt.tMedicationActivity2MedicationStatement(cdaMedAct);
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirMedStBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testMedicationDispense2MedicationDispense(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: MedicationDispense2MedicationDispense\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaNull = null;
		Bundle fhirNull = rt.tMedicationDispense2MedicationDispense(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		// medicationsSection.medicationActivities.medicationDispense
		if(test.ccd.getMedicationsSection() != null && !test.ccd.getMedicationsSection().isSetNullFlavor()) {
			org.openhealthtools.mdht.uml.cda.consol.MedicationsSection medSec = test.ccd.getMedicationsSection();
			if(medSec.getMedicationActivities() != null && !medSec.getMedicationActivities().isEmpty()) {
				for(MedicationActivity medAct : medSec.getMedicationActivities()) {
					if(medAct != null && !medAct.isSetNullFlavor()) {
						if(medAct.getMedicationDispenses() != null && !medAct.getMedicationDispenses().isEmpty()) {
							for(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense medDisp : medAct.getMedicationDispenses()) {
								if(medDisp != null && !medDisp.isSetNullFlavor()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirMedDispBundle = rt.tMedicationDispense2MedicationDispense(medDisp);
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
	public void testObservation2Observation(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: Observation2Observation\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Observation cdaNull = null;
		Bundle fhirNull = rt.tObservation2Observation(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getSocialHistorySection() != null && !test.ccd.getSocialHistorySection().isSetNullFlavor()) {
			if(test.ccd.getSocialHistorySection().getObservations() != null && !test.ccd.getSocialHistorySection().getObservations().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Observation cdaObs : test.ccd.getSocialHistorySection().getObservations()) {
					if(cdaObs != null && !cdaObs.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle obsBundle = rt.tObservation2Observation(cdaObs);
						appendToResultFile(transformationEndMsg);
						appendToResultFile(obsBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testOrganization2Organization(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: Organization2Organization\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Organization cdaNull = null;
		Organization fhirNull = rt.tOrganization2Organization(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for(org.openhealthtools.mdht.uml.cda.PatientRole patRole : test.ccd.getPatientRoles()) {
			org.openhealthtools.mdht.uml.cda.Organization cdaOrg = patRole.getProviderOrganization();
			appendToResultFile(transformationStartMsg);
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrg = rt.tOrganization2Organization(cdaOrg);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(fhirOrg);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testPatientRole2Patient(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: PatientRole2Patient\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.PatientRole cdaNull = null;
		Bundle fhirNull = rt.tPatientRole2Patient(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		for(PatientRole pr : test.ccd.getPatientRoles()) {

			// here we do the transformation by calling the method rt.PatientRole2Patient

			Patient patient = null;

			appendToResultFile(transformationStartMsg);
			Bundle patientBundle = rt.tPatientRole2Patient(pr);
			appendToResultFile(transformationEndMsg);
			appendToResultFile(patientBundle);
			
			for(Entry entry : patientBundle.getEntry()) {
				if(entry.getResource() instanceof Patient) {
					patient = (Patient)entry.getResource();
				}
			}


			// patient.identifier
			int idCount = 0;
			for(II id : pr.getIds()) {
				if(id.getRoot() != null && id.getExtension() != null) {
					// since extension may contain "urn:oid:" or "urn:uuid:", assertion is about containing the value as a piece
					Assert.assertTrue("pr.id.extension #"+ idCount +" was not transformed", patient.getIdentifier().get(idCount).getValue().contains(id.getExtension()));
					Assert.assertTrue("pr.id.root #"+ idCount +" was not transformed",patient.getIdentifier().get(idCount).getSystem().contains(id.getRoot()));
				} else if(id.getRoot() != null) {
					Assert.assertTrue("pr.id.root #"+ idCount +" was not transformed",patient.getIdentifier().get(idCount).getValue().contains(id.getRoot()));
				} else if(id.getExtension() != null) {
					Assert.assertTrue("pr.id.root #"+ idCount +" was not transformed",patient.getIdentifier().get(idCount).getValue().contains(id.getExtension()));
				}
				// codeSystem method is changed and tested
				
				idCount++;
			}
			// patient.name
			// Notice that patient.name is fullfilled by the method EN2HumanName.
			int nameCount = 0;
			for(EN pn : pr.getPatient().getNames()) {

				// patient.name.use
				if(pn.getUses() == null || pn.getUses().isEmpty()) {
					Assert.assertNull(patient.getName().get(nameCount).getUse());
				} else {
					Assert.assertEquals("pr.patient.name["+nameCount+"]"+".use was not transformed", vsti.tEntityNameUse2NameUseEnum(pn.getUses().get(0)).toString().toLowerCase(), patient.getName().get(nameCount).getUse());
				}

				// patient.name.text
				Assert.assertEquals("pr.patient.name["+nameCount+"].text was not transformed", pn.getText(),patient.getName().get(nameCount).getText());

				// patient.name.family
				int familyCount = 0;
				for(ENXP family : pn.getFamilies()) {
					if(family == null || family.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getFamily() == null || patient.getName().get(nameCount).getFamily().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name["+nameCount+"].family was not transformed", family.getText(),patient.getName().get(nameCount).getFamily().get(familyCount).getValue());
					}
					familyCount++;
				}

				// patient.name.given
				int givenCount = 0;
				for(ENXP given : pn.getGivens()) {
					if(given == null || given.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getGiven() == null || patient.getName().get(nameCount).getGiven().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name["+nameCount+"].given was not transformed", given.getText(),patient.getName().get(nameCount).getGiven().get(givenCount).getValue());
					}
					givenCount++;
				}

				// patient.name.prefix
				int prefixCount = 0;
				for(ENXP prefix : pn.getPrefixes()) {
					if(prefix == null || prefix.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue(patient.getName().get(nameCount).getPrefix() == null || patient.getName().get(nameCount).getPrefix().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name["+nameCount+"].prefix was not transformed", prefix.getText(),patient.getName().get(nameCount).getPrefix().get(prefixCount).getValue());
					}
					prefixCount++;
				}

				// patient.name.suffix
				int suffixCount = 0;
				for(ENXP suffix : pn.getPrefixes()) {
					if(suffix == null || suffix.isSetNullFlavor()) {
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getSuffix() == null || patient.getName().get(nameCount).getSuffix().size() == 0);
					} else {
						Assert.assertEquals("pr.patient.name["+nameCount+"].suffix was not transformed", suffix.getText(),patient.getName().get(nameCount).getSuffix().get(suffixCount).getValue());
					}
					suffixCount++;
				}

				// patient.name.period
				if(pn.getValidTime() == null || pn.getValidTime().isSetNullFlavor()) {
					// It can return null or an empty list
					Assert.assertTrue(patient.getName().get(nameCount).getPeriod() == null || patient.getName().get(nameCount).getPeriod().isEmpty());
				}
			}


			// patient.telecom
			// Notice that patient.telecom is fullfilled by the method dtt.TEL2ContactPoint
			if(pr.getTelecoms() == null || pr.getTelecoms().isEmpty()) {
				Assert.assertTrue(patient.getTelecom() == null || patient.getTelecom().isEmpty());
			} else{
				// size check
				Assert.assertTrue(pr.getTelecoms().size() == patient.getTelecom().size());
				// We have already tested the method TEL2ContactPoint. Therefore, null-check and size-check is enough for now.
			}

			// patient.gender
			// vst.AdministrativeGenderCode2AdministrativeGenderEnum is used in this transformation.
			// Following test aims to test that ValueSetTransformer method.
			if(pr.getPatient().getAdministrativeGenderCode() == null || pr.getPatient().getAdministrativeGenderCode().isSetNullFlavor()) {
				Assert.assertTrue(patient.getGender() == null || patient.getGender().isEmpty());
			}

			// patient.birthDate
			// Notice that patient.birthDate is fullfilled by the method dtt.TS2Date
			if(pr.getPatient().getBirthTime() == null || pr.getPatient().getBirthTime().isSetNullFlavor()) {
				Assert.assertTrue(patient.getBirthDate() == null);
			}

			// patient.address
			// Notice that patient.address is fullfilled by the method dtt.AD2Address
			if(pr.getAddrs() == null || pr.getAddrs().isEmpty()) {
				Assert.assertTrue(patient.getAddress() == null || patient.getAddress().isEmpty());
			} else {
				// We have already tested the method AD2Address. Therefore, null-check and size-check is enough for now.
				Assert.assertTrue(pr.getAddrs().size() == patient.getAddress().size());
			}

			// patient.maritalStatus
			// vst.MaritalStatusCode2MaritalStatusCodesEnum is used in this transformation.
			// Following test aims to test that ValueSetTransformer method.
			if(pr.getPatient().getMaritalStatusCode() == null || pr.getPatient().getMaritalStatusCode().isSetNullFlavor()) {
				Assert.assertTrue(patient.getMaritalStatus() == null || patient.getMaritalStatus().isEmpty());
			} else {
				Assert.assertTrue(patient.getMaritalStatus().getCoding().get(0).getCode().toLowerCase().charAt(0) == pr.getPatient().getMaritalStatusCode().getCode().toLowerCase().charAt(0));
			}


			// patient.languageCommunication
			if( pr.getPatient().getLanguageCommunications() == null || pr.getPatient().getLanguageCommunications().isEmpty()) {
				Assert.assertTrue(patient.getCommunication() == null || patient.getCommunication().isEmpty());
			} else {
				Assert.assertTrue(pr.getPatient().getLanguageCommunications().size() == patient.getCommunication().size());

				int sizeCommunication = pr.getPatient().getLanguageCommunications().size();
				while(sizeCommunication != 0) {

					//language
					if(pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getLanguageCode() == null ||
							pr.getPatient().getLanguageCommunications().get(0).getLanguageCode().isSetNullFlavor()) {
						Assert.assertTrue(patient.getCommunication().get(sizeCommunication - 1).getLanguage() == null || patient.getCommunication().get(sizeCommunication -1 ).getLanguage().isEmpty());
					} else {
						// We have already tested the method CD2CodeableConcept. Therefore, null-check is enough for now.
					}

					// preference
					if(pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd() == null ||
							pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().isSetNullFlavor()) {
						Assert.assertTrue(patient.getCommunication().get(sizeCommunication - 1).getPreferred() == null);
					} else {
						Assert.assertEquals(pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().getValue(), patient.getCommunication().get(sizeCommunication - 1).getPreferred());
					}
					sizeCommunication--;
				}

			}

			// providerOrganization
			if(pr.getProviderOrganization() == null || pr.getProviderOrganization().isSetNullFlavor()) {
				Assert.assertTrue(patient.getManagingOrganization() == null || patient.getManagingOrganization().isEmpty());
			} else {
				if(pr.getProviderOrganization().getNames() == null) {
					Assert.assertTrue(patient.getManagingOrganization().getDisplay() == null);
				}
			}

			// guardian
			if(pr.getPatient().getGuardians() == null || pr.getPatient().getGuardians().isEmpty()) {
				Assert.assertTrue(patient.getContact() == null || patient.getContact().isEmpty());
			} else {
				// Notice that, inside this mapping, the methods dtt.TEL2ContactPoint and dtt.AD2Address are used.
				// Therefore, null-check and size-check are enough
				Assert.assertTrue(pr.getPatient().getGuardians().size() == patient.getContact().size());
			}

			// extensions
			for(ExtensionDt extension : patient.getUndeclaredExtensions()) {
				Assert.assertTrue(extension.getUrl() != null);
				Assert.assertTrue(extension.getValue() != null);
			}
		}
		appendToResultFile(endOfTestMsg);
    }
	
	@Test
	public void testProblemConcernAct2Condition() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: ProblemConcernAct2Condition\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct cdaNull = null;
		Bundle fhirNull = rt.tProblemConcernAct2Condition(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getProblemSection() != null && !test.ccd.getProblemSection().isSetNullFlavor()) {
			if(test.ccd.getProblemSection().getProblemConcerns() != null && !test.ccd.getProblemSection().getProblemConcerns().isEmpty()) {
				for(ProblemConcernAct problemConcernAct : test.ccd.getProblemSection().getProblemConcerns()) {
					if(problemConcernAct != null && !problemConcernAct.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirConditionBundle = rt.tProblemConcernAct2Condition(problemConcernAct);
						appendToResultFile(transformationEndMsg);
						appendToResultFile(fhirConditionBundle);
					}
				}
			}
		}
		appendToResultFile(endOfTestMsg);
	}
	
	@Test
	public void testProcedure2Procedure(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: Procedure2Procedure\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Procedure cdaNull = null;
		Bundle fhirNull = rt.tProcedure2Procedure(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		if(test.ccd.getProceduresSection() != null && !test.ccd.getProceduresSection().isSetNullFlavor()) {
			if(test.ccd.getProceduresSection().getProcedures() != null && !test.ccd.getProceduresSection().getProcedures().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : test.ccd.getProceduresSection().getProcedures()) {
					// traversing procedures
					appendToResultFile(transformationStartMsg);
					Bundle fhirProcedureBundle = rt.tProcedure2Procedure(cdaProcedure);
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirProcedureBundle);
				}
			}
		}
		
		if(test.ccd.getEncountersSection() != null && !test.ccd.getEncountersSection().isSetNullFlavor()) {
			if(test.ccd.getEncountersSection().getProcedures() != null && !test.ccd.getEncountersSection().getProcedures().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : test.ccd.getEncountersSection().getProcedures()) {
					// traversing procedures					
					appendToResultFile(transformationStartMsg);
					Bundle fhirProcedureBundle = rt.tProcedure2Procedure(cdaProcedure);
					appendToResultFile(transformationEndMsg);
					appendToResultFile(fhirProcedureBundle);
				}
			}
		}
		
		if(test.ccd.getAllSections() != null && !test.ccd.getAllSections().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Section section : test.ccd.getAllSections()) {
				if(section.getProcedures() != null && !section.getProcedures().isEmpty()) {
					for(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure: section.getProcedures()) {
						// traversing procedures
						appendToResultFile(transformationStartMsg);
						Bundle fhirProcedureBundle = rt.tProcedure2Procedure(cdaProcedure);
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
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: ResultOrganizer2DiagnosticReport\n");
		// null instance test
		ResultOrganizer cdaNull = null;
		Bundle fhirNull = rt.tResultOrganizer2DiagnosticReport(cdaNull);
		Assert.assertNull(fhirNull);

		// instance from file
		ResultsSection resultsSec = test.ccd.getResultsSection();
		
		if(resultsSec != null && !resultsSec.isSetNullFlavor()) {
			if(resultsSec.getOrganizers() != null && !resultsSec.getOrganizers().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Organizer cdaOrganizer : resultsSec.getOrganizers()) {
					if(cdaOrganizer != null && !cdaOrganizer.isSetNullFlavor()) {
						if(cdaOrganizer instanceof ResultOrganizer) {
							appendToResultFile(transformationStartMsg);
							Bundle fhirDiagReport = rt.tResultOrganizer2DiagnosticReport((ResultOrganizer)cdaOrganizer);
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
	public void testSection2Section(){
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: Section2Section\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.Section cdaNull = null;
		Section fhirNull = rt.tSection2Section(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		org.openhealthtools.mdht.uml.cda.Section sampleSection = null;
		
		// assigning sampleSection to one sample section
		if(test.ccd.getEncountersSection() != null && !test.ccd.getEncountersSection().isSetNullFlavor()) {
			if(test.ccd.getEncountersSection().getAllSections() != null && !test.ccd.getEncountersSection().getAllSections().isEmpty()) {
				if(test.ccd.getEncountersSection().getAllSections().get(0) != null && !test.ccd.getEncountersSection().getAllSections().get(0).isSetNullFlavor()) {
					sampleSection = test.ccd.getEncountersSection().getAllSections().get(0);
				}
			}
		}
		if(sampleSection != null) {
			ca.uhn.fhir.model.dstu2.resource.Composition fhirComposition = new ca.uhn.fhir.model.dstu2.resource.Composition();
			appendToResultFile(transformationStartMsg);
			ca.uhn.fhir.model.dstu2.resource.Composition.Section fhirSection = rt.tSection2Section(sampleSection);
			appendToResultFile(transformationEndMsg);
			fhirComposition.addSection(fhirSection);
			appendToResultFile(fhirComposition);
		}
		appendToResultFile(endOfTestMsg);
	}

	@Test
	public void testSocialHistory() {
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: SocialHistory\n");
		SocialHistorySection socialHistSec = test.ccd.getSocialHistorySection();
		
		if(socialHistSec != null && !socialHistSec.isSetNullFlavor()) {
			if(socialHistSec.getObservations() != null && !socialHistSec.getObservations().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Observation cdaObs : socialHistSec.getObservations()) {
					if(cdaObs != null && !cdaObs.isSetNullFlavor()) {
						appendToResultFile(transformationStartMsg);
						Bundle fhirObs = rt.tObservation2Observation(cdaObs);
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
		ResourceTransformerTest test = new ResourceTransformerTest();
		appendToResultFile("## TEST: VitalSignObservation2Observation\n");
		// null instance test
		org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation cdaNull = null;
		Bundle fhirNull = rt.tVitalSignObservation2Observation(cdaNull);
		Assert.assertNull(fhirNull);

		// instances from file
		VitalSignsSectionEntriesOptional vitalSignsSec = test.ccd.getVitalSignsSectionEntriesOptional();
		if(vitalSignsSec != null && !vitalSignsSec.isSetNullFlavor()) {
			if(vitalSignsSec.getVitalSignsOrganizers() != null && !vitalSignsSec.getVitalSignsOrganizers().isEmpty()) {
				for(VitalSignsOrganizer vitalSignOrganizer : vitalSignsSec.getVitalSignsOrganizers()) {
					if(vitalSignOrganizer != null && !vitalSignOrganizer.isSetNullFlavor()) {
						if(vitalSignOrganizer.getVitalSignObservations() != null && !vitalSignOrganizer.getVitalSignObservations().isEmpty()) {
							for(VitalSignObservation vitalSignObservation : vitalSignOrganizer.getVitalSignObservations()) {
								if(vitalSignObservation != null && !vitalSignObservation.isSetNullFlavor()) {
									appendToResultFile(transformationStartMsg);
									Bundle fhirObservation = rt.tVitalSignObservation2Observation((VitalSignObservation)vitalSignObservation);
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
			if(param instanceof String) {
				resultFW.append((String)param);
			}
			else if(param instanceof IResource) {
				FHIRUtil.printJSON((IResource)param, resultFW);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}