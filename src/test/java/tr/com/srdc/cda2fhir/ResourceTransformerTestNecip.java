package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ca.uhn.fhir.model.dstu2.resource.*;
import org.eclipse.emf.common.util.EList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.parser.IParser;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.impl.ValueSetsTransformerImpl;

public class ResourceTransformerTestNecip {
	// Test one method at a time. Use annotation @Ignore for the remaining methods.
	
	// context
	private static final FhirContext myCtx = FhirContext.forDstu2();
	
	ResourceTransformerImpl rt = new ResourceTransformerImpl();
	DataTypesTransformerTest dtt = new DataTypesTransformerTest();
	ValueSetsTransformerImpl vsti = new ValueSetsTransformerImpl();
	private FileInputStream fisCDA;
	private FileInputStream fisCCD;
	private ClinicalDocument cda;
	private ContinuityOfCareDocument ccd;
	
	private void printJSON(IResource res) {
	    IParser jsonParser = myCtx.newJsonParser();
	    jsonParser.setPrettyPrint(true);
	    System.out.println(jsonParser.encodeResourceToString(res));
	}
	
	public ResourceTransformerTestNecip() {
        CDAUtil.loadPackages();
        try {
	        fisCDA = new FileInputStream("src/test/resources/SampleCDADocument.xml");
	        fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
//	        fisCCD = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");
		} catch (FileNotFoundException ex) {
	        ex.printStackTrace();
	    }
        
        try {
        	if( fisCDA != null ) { 
        		cda = CDAUtil.load(fisCDA); 
        	}
        	if( fisCCD != null ) {
        		// To validate the file, use the following two lines instead of the third line
//        		ValidationResult result = new ValidationResult();
//        		ccd = (ContinuityOfCareDocument) CDAUtil.load(fisCCD,result);
        		ccd = (ContinuityOfCareDocument) CDAUtil.load(fisCCD);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	// Most of the test methods just print the transformed object in JSON form.
	
	@Ignore
	public void testAllergyProblemAct2AllergyIntolerance(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		for( AllergyProblemAct cdaAPA : test.ccd.getAllergiesSection().getAllergyProblemActs() ){
			System.out.println("Transformation starting..");
			Bundle allergyBundle = rt.AllergyProblemAct2AllergyIntolerance(cdaAPA);
			System.out.println("End of transformation. Printing..");
			printJSON( allergyBundle );
			System.out.println("End of print");
			System.out.println("***");
		}
	}

	@Ignore
	public void testAssignedAuthor2Practitioner(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		if( test.ccd.getAuthors() != null ){
			for( org.openhealthtools.mdht.uml.cda.Author author : test.ccd.getAuthors() ){
				// traversing authors
				if( author != null && author.getAssignedAuthor() != null ){
					
					System.out.println("Transformation starting..");
					Bundle practitionerBundle = rt.AssignedAuthor2Practitioner( author.getAssignedAuthor() );
					System.out.println("End of transformation. Printing the resource as JSON object..");
					printJSON( practitionerBundle );
					System.out.println("End of print.");
				}
			}
		}
	}
	
	@Ignore
	public void testAssignedEntity2Practitioner(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		int procedureCount = 0;
		if( test.ccd.getProceduresSection() != null && !test.ccd.getProceduresSection().isSetNullFlavor() ){
			if( test.ccd.getProceduresSection().getProcedures() != null && !test.ccd.getProceduresSection().getProcedures().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Procedure procedure : test.ccd.getProceduresSection().getProcedures() ){
					// traversing procedures
					System.out.print( "Procedure["+ procedureCount++ +"]" );
					int performerCount = 0;
					if( procedure.getPerformers() != null && !procedure.getPerformers().isEmpty() ){
						for( org.openhealthtools.mdht.uml.cda.Performer2 performer : procedure.getPerformers()  ){
							System.out.print( "-> Performer["+ performerCount++ +"]" );
							if( performer.getAssignedEntity() != null && !performer.getAssignedEntity().isSetNullFlavor() ){
									System.out.println("-> AssignedEntity");
									System.out.println("Transformation starting..");
									
									ca.uhn.fhir.model.dstu2.resource.Practitioner practitioner = null;
									
									Bundle fhirPractitionerBundle = rt.AssignedEntity2Practitioner(performer.getAssignedEntity() );
									for( Entry entry : fhirPractitionerBundle.getEntry() ){
										if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Practitioner){
											practitioner = (Practitioner) entry.getResource();
										}
									}
									
									
									System.out.println("End of transformation. Printing the resource as JSON object..");
									printJSON( practitioner );
									System.out.println("End of print.");
							}
						}
					}
					System.out.print("\n***\n"); // to visualize
				}
			}
		}
	}

	@Ignore
	public void testEncounter2Encounter(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		int encounterCount = 0;
		if( test.ccd.getAllSections() != null && !test.ccd.getAllSections().isEmpty() ){
			for( org.openhealthtools.mdht.uml.cda.Section section : test.ccd.getAllSections() ){
				
				if( section != null && !section.isSetNullFlavor() ){
					
					for( org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter : section.getEncounters() ){
						if( cdaEncounter != null && !cdaEncounter.isSetNullFlavor()  ){
							System.out.println("Encounter["+encounterCount+"]");
							System.out.println("Transformation starting..");
							
							Bundle fhirEncounterBundle = rt.Encounter2Encounter(cdaEncounter);
							
							System.out.println("End of transformation. Printing the resource as JSON object..");
							printJSON( fhirEncounterBundle );
							System.out.println("End of print.");
							System.out.print("\n***\n"); // to visualize
						}
						encounterCount++;
					}
				}
			}
		}
	}

	@Ignore
	public void testGuardian2Contact(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		int patientRoleCount = 0;
		if( test.ccd.getPatientRoles() != null && !test.ccd.getPatientRoles().isEmpty() ){
			for( org.openhealthtools.mdht.uml.cda.PatientRole patientRole : test.ccd.getPatientRoles() ){
				if( patientRole != null && !patientRole.isSetNullFlavor() && patientRole.getPatient() != null && !patientRole.getPatient().isSetNullFlavor() ){
					int guardianCount = 0;
					for( org.openhealthtools.mdht.uml.cda.Guardian guardian : patientRole.getPatient().getGuardians() ){
						if( guardian != null && !guardian.isSetNullFlavor()){
							System.out.println("PatientRole["+patientRoleCount+"], Guardian["+guardianCount+"]");
							
							System.out.println("Transformation starting..");
							ca.uhn.fhir.model.dstu2.resource.Patient.Contact contact = rt.Guardian2Contact(guardian);
							
							System.out.println("End of transformation. Printing the resource as JSON object..");
							
							ca.uhn.fhir.model.dstu2.resource.Patient patient = new Patient().addContact(contact);
							printJSON( patient );
							System.out.println("End of print.");
							System.out.print("\n***\n"); // to visualize
						}
						guardianCount++;
					}
				}
				patientRoleCount++;
			}
		}
	}

	@Ignore
	public void testManufacturedProduct2Medication(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		ImmunizationsSectionEntriesOptional immSection = test.ccd.getImmunizationsSectionEntriesOptional();
		if(immSection != null && !immSection.isSetNullFlavor()){
			if(immSection.getImmunizationActivities() != null && !immSection.getImmunizationActivities().isEmpty()){
				for(ImmunizationActivity immAct : immSection.getImmunizationActivities()){
					if(immAct != null && !immAct.isSetNullFlavor()){
						if(immAct.getConsumable() != null && !immAct.getConsumable().isSetNullFlavor()){
							if(immAct.getConsumable().getManufacturedProduct() != null && !immAct.getConsumable().getManufacturedProduct().isSetNullFlavor()){
								// immAct.immSection.immAct.consumable.manuProd
								
								System.out.println("Transformation starting..");
								Bundle fhirMed = rt.ManufacturedProduct2Medication(immAct.getConsumable().getManufacturedProduct());
								System.out.println("End of transformation. Printing the resource as JSON object..");
								printJSON(fhirMed);
								System.out.println("End of print.");
								System.out.print("\n***\n"); // to visualize
							}
						}
					}
				}
			}
		}
	}
	
	@Ignore
	public void testMedicationActivity2MedicationStatement(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		if(test.ccd.getMedicationsSection() != null && !test.ccd.getMedicationsSection().isSetNullFlavor()) {
			if(test.ccd.getMedicationsSection().getMedicationActivities() != null && !test.ccd.getMedicationsSection().getMedicationActivities().isEmpty()) {
				for(MedicationActivity cdaMedAct : test.ccd.getMedicationsSection().getMedicationActivities()) {
					if(cdaMedAct != null && !cdaMedAct.isSetNullFlavor()) {
						System.out.println("Transformation starting..");
						Bundle fhirMedStBundle = rt.MedicationActivity2MedicationStatement(cdaMedAct);
						System.out.println("End of transformation. Printing the resource as JSON object..");
						printJSON(fhirMedStBundle);
						System.out.println("End of print.");
					}
				}
			}
		}
	}
	
	@Ignore
	public void testMedicationDispense2MedicationDispense(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		// medicationsSection.medicationActivities.medicationDispense
		if(test.ccd.getMedicationsSection() != null && !test.ccd.getMedicationsSection().isSetNullFlavor()){
			org.openhealthtools.mdht.uml.cda.consol.MedicationsSection medSec = test.ccd.getMedicationsSection();
			if(medSec.getMedicationActivities() != null && !medSec.getMedicationActivities().isEmpty()){
				for(MedicationActivity medAct : medSec.getMedicationActivities()){
					if(medAct != null && !medAct.isSetNullFlavor()){
						if(medAct.getMedicationDispenses() != null && !medAct.getMedicationDispenses().isEmpty()){
							for(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense medDisp : medAct.getMedicationDispenses()){
								if(medDisp != null && !medDisp.isSetNullFlavor()){
									System.out.println("Transformation starting..");
									Bundle fhirMedDispBundle = rt.MedicationDispense2MedicationDispense(medDisp);
									System.out.println("End of transformation. Printing the resource as JSON object..");
									printJSON(fhirMedDispBundle);
									System.out.println("End of print.");
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Test
	public void testFamilyMemberOrganizer2FamilyMemberHistory(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		if( test.ccd.getFamilyHistorySection() != null && test.ccd.getFamilyHistorySection().getFamilyHistories() != null ){
			for( org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer familyHistoryOrganizer : test.ccd.getFamilyHistorySection().getFamilyHistories() ){
				if( familyHistoryOrganizer != null ){
					System.out.println("Transformation starting..");
					FamilyMemberHistory fmHistory = rt.FamilyHistoryOrganizer2FamilyMemberHistory( familyHistoryOrganizer );
					System.out.println("End of transformation. Printing the resource as JSON object..");
					printJSON(fmHistory);
					System.out.println("End of print.");
				}
			}
		}
	}
	
	@Ignore
	public void testObservation2Observation(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		if( test.ccd.getSocialHistorySection() != null && !test.ccd.getSocialHistorySection().isSetNullFlavor() ){
			if( test.ccd.getSocialHistorySection().getObservations() != null && !test.ccd.getSocialHistorySection().getObservations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Observation cdaObs : test.ccd.getSocialHistorySection().getObservations() ){
					if( cdaObs != null && !cdaObs.isSetNullFlavor()){
						System.out.println("Transformation starting..");
						Bundle obsBundle = rt.Observation2Observation( cdaObs );
						System.out.println("End of transformation. Printing..");
						printJSON(obsBundle);
						System.out.println("End of print.\n***");
					}
				}
			}
		}
	}

	@Ignore
	public void testOrganization2Organization(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		int patientCount = 0;
		for( org.openhealthtools.mdht.uml.cda.PatientRole patRole : test.ccd.getPatientRoles() ){
			System.out.print("PatientRole["+patientCount++ +"].");
			org.openhealthtools.mdht.uml.cda.Organization cdaOrg = patRole.getProviderOrganization();
			System.out.println( "Transformation starting..." );
	
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrg = null;
			
			Bundle fhirOrgBundle = rt.Organization2Organization(cdaOrg);
			for( Entry entry : fhirOrgBundle.getEntry() ){
				if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Organization){
					fhirOrg = (Organization) entry.getResource();
				}
			}
			
			System.out.println("End of transformation. Printing the resource as JSON object..");
			printJSON( fhirOrg );
			System.out.println("End of print.");
		}
	}

	@Ignore
	public void testPatientRole2Patient(){
			ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
			EList<PatientRole> patientRoles = test.ccd.getPatientRoles();
	
			// We traverse each of the patientRoles included in the document
			// We apply the tests for each of them
			for( PatientRole pr : patientRoles ){
				
				// here we do the transformation by calling the method rt.PatientRole2Patient
				
				Patient patient = null;
				
				Bundle patientBundle = rt.PatientRole2Patient(pr);
				for( Entry entry : patientBundle.getEntry() ){
					if( entry.getResource() instanceof Patient){
						patient = (Patient) entry.getResource();
					}
				}
				
				// ta-ta-ta-taa!
				System.out.println("Printing the resource as JSON object..");
				printJSON( patient );
				System.out.println("End of print");
				
				
				// patient.identifier
				int idCount = 0;
				for( II id : pr.getIds() ){
					// To see the values, you can use the following print lines.
	//				System.out.println( id.getRoot() );
	//				System.out.println( id.getExtension() );
	//				System.out.println( id.getAssigningAuthorityName() );
					
					// cdoeSystem method is changed and tested
	//				Assert.assertEquals("pr.id.root #"+ idCount +" was not transformed",id.getRoot(),  patient.getIdentifier().get(idCount).getSystem() );
					
					Assert.assertEquals("pr.id.assigningAuthorityName #"+ idCount +" was not transformed",id.getAssigningAuthorityName(),  patient.getIdentifier().get(idCount).getAssigner().getReference().getValue() );
					idCount++;
				}
				// patient.name
				// Notice that patient.name is fullfilled by the method EN2HumanName.
				int nameCount = 0;
				for( EN pn : pr.getPatient().getNames() ){
					
					// patient.name.use
					if( pn.getUses() == null || pn.getUses().isEmpty() ){
						Assert.assertNull( patient.getName().get(nameCount).getUse() );
					} else{
						Assert.assertEquals("pr.patient.name["+nameCount+"]"+".use was not transformed", vsti.EntityNameUse2NameUseEnum(pn.getUses().get(0)).toString().toLowerCase(), patient.getName().get(nameCount).getUse() );
					}
					
					// patient.name.text
					Assert.assertEquals("pr.patient.name["+nameCount+"].text was not transformed", pn.getText(),patient.getName().get(nameCount).getText() );
					
					// patient.name.family
					int familyCount = 0;
					for( ENXP family : pn.getFamilies() ){
						if( family == null || family.isSetNullFlavor() ){
							// It can return null or an empty list
							Assert.assertTrue( patient.getName().get(nameCount).getFamily() == null || patient.getName().get(nameCount).getFamily().size() == 0  );
						} else{
							Assert.assertEquals("pr.patient.name["+nameCount+"].family was not transformed", family.getText(),patient.getName().get(nameCount).getFamily().get(familyCount).getValue());
						}
						familyCount++;
					}
					
					// patient.name.given
					int givenCount = 0;
					for( ENXP given : pn.getGivens() ){
						if( given == null || given.isSetNullFlavor() ){
							// It can return null or an empty list
							Assert.assertTrue( patient.getName().get(nameCount).getGiven() == null || patient.getName().get(nameCount).getGiven().size() == 0  );
						} else{
							Assert.assertEquals("pr.patient.name["+nameCount+"].given was not transformed", given.getText(),patient.getName().get(nameCount).getGiven().get(givenCount).getValue());
						}
						givenCount++;
					}
					
					// patient.name.prefix
					int prefixCount = 0;
					for( ENXP prefix : pn.getPrefixes() ){
						if( prefix == null || prefix.isSetNullFlavor() ){
							// It can return null or an empty list
							Assert.assertTrue( patient.getName().get(nameCount).getPrefix() == null || patient.getName().get(nameCount).getPrefix().size() == 0  );
						} else{
							Assert.assertEquals("pr.patient.name["+nameCount+"].prefix was not transformed", prefix.getText(),patient.getName().get(nameCount).getPrefix().get(prefixCount).getValue());
						}
						prefixCount++;
					}
					
					// patient.name.suffix
					int suffixCount = 0;
					for( ENXP suffix : pn.getPrefixes() ){
						if( suffix == null || suffix.isSetNullFlavor() ){
							// It can return null or an empty list
							Assert.assertTrue( patient.getName().get(nameCount).getSuffix() == null || patient.getName().get(nameCount).getSuffix().size() == 0  );
						} else{
							Assert.assertEquals("pr.patient.name["+nameCount+"].suffix was not transformed", suffix.getText(),patient.getName().get(nameCount).getSuffix().get(suffixCount).getValue());
						}
						suffixCount++;
					}
					
					// patient.name.period
					if( pn.getValidTime() == null || pn.getValidTime().isSetNullFlavor() ){
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getPeriod() == null || patient.getName().get(nameCount).getPeriod().isEmpty() );
					} else{ // start of non-null period test
						
						if( pn.getValidTime().getLow() == null || pn.getValidTime().getLow().isSetNullFlavor() ){
							
							Assert.assertTrue( patient.getName().get(nameCount).getPeriod().getStart() == null );
						} else{
							System.out.println("Following lines should contain identical non-null dates:");
							System.out.println("[FHIR] " + patient.getName().get(nameCount).getPeriod().getStart());
							System.out.println("[CDA] "+ pn.getValidTime().getLow());
						}
						
						if( pn.getValidTime().getHigh() == null || pn.getValidTime().getHigh().isSetNullFlavor() ){
							Assert.assertTrue( patient.getName().get(nameCount).getPeriod().getEnd() == null );
						} else{
							System.out.println("Following lines should contain identical non-null dates:");
							System.out.println("[FHIR] " + patient.getName().get(nameCount).getPeriod().getEnd());
							System.out.println("[CDA] "+ pn.getValidTime().getHigh());
						}
					} // end of non-null period test
					nameCount++;
				} // end of patient.name tests
			
				
				// patient.telecom
				// Notice that patient.telecom is fullfilled by the method dtt.TEL2ContactPoint
				if( pr.getTelecoms() == null || pr.getTelecoms().isEmpty() ){
					Assert.assertTrue( patient.getTelecom() == null || patient.getTelecom().isEmpty() );
				} else{
					// size check
					Assert.assertTrue( pr.getTelecoms().size() == patient.getTelecom().size() );
					// We have already tested the method TEL2ContactPoint. Therefore, null-check and size-check is enough for now.
				}
				
				// patient.gender
				// vst.AdministrativeGenderCode2AdministrativeGenderEnum is used in this transformation.
				// Following test aims to test that ValueSetTransformer method.
				if( pr.getPatient().getAdministrativeGenderCode() == null || pr.getPatient().getAdministrativeGenderCode().isSetNullFlavor() ){
					Assert.assertTrue( patient.getGender() == null || patient.getGender().isEmpty() );
				} else{
					System.out.println( "Following lines should contain two lines of non-null gender information which are relevant(male,female,unknown):" );
					System.out.println("  [FHIR]: " + patient.getGender() );
					System.out.println( "  [CDA]: " + pr.getPatient().getAdministrativeGenderCode().getCode() );
				}
				
				// patient.birthDate
				// Notice that patient.birthDate is fullfilled by the method dtt.TS2Date 
				if( pr.getPatient().getBirthTime() == null || pr.getPatient().getBirthTime().isSetNullFlavor() ){
					Assert.assertTrue( patient.getBirthDate() == null );
				} else{
					System.out.println( "Following lines should contain two lines of non-null, equivalent birthdate information:" );
					System.out.println("  [FHIR]: " + patient.getBirthDate() );
					System.out.println("  [CDA]: "+ pr.getPatient().getBirthTime().getValue());
				}
				
				// patient.address
				// Notice that patient.address is fullfilled by the method dtt.AD2Address  
				if( pr.getAddrs() == null || pr.getAddrs().isEmpty() ){
					Assert.assertTrue( patient.getAddress() == null || patient.getAddress().isEmpty() );
				} else{
					// We have already tested the method AD2Address. Therefore, null-check and size-check is enough for now.
					Assert.assertTrue( pr.getAddrs().size() == patient.getAddress().size() );
				}
				
				// patient.maritalStatus
				// vst.MaritalStatusCode2MaritalStatusCodesEnum is used in this transformation.
				// Following test aims to test that ValueSetTransformer method.
				if( pr.getPatient().getMaritalStatusCode() == null || pr.getPatient().getMaritalStatusCode().isSetNullFlavor() ){
					Assert.assertTrue( patient.getMaritalStatus() == null || patient.getMaritalStatus().isEmpty() );
				} else{
					System.out.println( "Following lines should contain two lines of non-null, equivalent marital status information:" );
					System.out.println("  [FHIR]: " + patient.getMaritalStatus().getCoding().get(0).getCode() );
					System.out.println("  [CDA]: "+ pr.getPatient().getMaritalStatusCode().getCode() );
					
				}
				
				
				// patient.languageCommunication
				if( pr.getPatient().getLanguageCommunications() == null || pr.getPatient().getLanguageCommunications().isEmpty() ){
					Assert.assertTrue( patient.getCommunication() == null || patient.getCommunication().isEmpty() );
				} else{
					Assert.assertTrue( pr.getPatient().getLanguageCommunications().size() == patient.getCommunication().size() );
					
					int sizeCommunication = pr.getPatient().getLanguageCommunications().size() ;
					while( sizeCommunication != 0){
						
						//language
						if( pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getLanguageCode() == null ||  
								pr.getPatient().getLanguageCommunications().get(0).getLanguageCode().isSetNullFlavor() ){
							Assert.assertTrue( patient.getCommunication().get(sizeCommunication - 1).getLanguage() == null || patient.getCommunication().get(sizeCommunication -1 ).getLanguage().isEmpty() );
						} else{
							// We have already tested the method CD2CodeableConcept. Therefore, null-check is enough for now.
						}
						
						// preference
						if( pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd() == null ||
								pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().isSetNullFlavor() ){
							Assert.assertTrue( patient.getCommunication().get(sizeCommunication - 1).getPreferred() == null );
						} else{
							Assert.assertEquals(pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().getValue(), patient.getCommunication().get(sizeCommunication - 1).getPreferred());
						}
						sizeCommunication--;
					}
					
				}
				
				// providerOrganization
				if( pr.getProviderOrganization() == null || pr.getProviderOrganization().isSetNullFlavor() ){
					Assert.assertTrue( patient.getManagingOrganization() == null || patient.getManagingOrganization().isEmpty() );
				} else{
					if( pr.getProviderOrganization().getNames() == null ){
						Assert.assertTrue( patient.getManagingOrganization().getDisplay() == null );
					}
					System.out.println("[FHIR] Reference for managing organization: "+  patient.getManagingOrganization().getReference() );
					
				}
				
				// guardian
				if( pr.getPatient().getGuardians() == null || pr.getPatient().getGuardians().isEmpty() ){
					Assert.assertTrue( patient.getContact() == null || patient.getContact().isEmpty() );
				} else{
					// Notice that, inside this mapping, the methods dtt.TEL2ContactPoint and dtt.AD2Address are used.
					// Therefore, null-check and size-check are enough
					Assert.assertTrue( pr.getPatient().getGuardians().size() == patient.getContact().size() );
				}
				
				// extensions
				int extCount = 0;
				for( ExtensionDt extension : patient.getUndeclaredExtensions() ){
					Assert.assertTrue( extension.getUrl() != null );
					Assert.assertTrue( extension.getValue() != null );
					System.out.println("[FHIR] Extension["+extCount+"] url: "+extension.getUrl());
					System.out.println("[FHIR] Extension["+ extCount++ +"] value: "+extension.getValue());
				}
	
				
				
				
			}
	    }
	
	@Ignore
	public void testProblemConcernAct2Condition(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		if(test.ccd.getProblemSection() != null && !test.ccd.getProblemSection().isSetNullFlavor()) {
			if(test.ccd.getProblemSection().getProblemConcerns() != null && !test.ccd.getProblemSection().getProblemConcerns().isEmpty()) {
				for(ProblemConcernAct problemConcernAct : test.ccd.getProblemSection().getProblemConcerns()) {
					if(problemConcernAct != null && !problemConcernAct.isSetNullFlavor()) {
						System.out.println("Transformation starting..");
						Bundle fhirConditionBundle = rt.ProblemConcernAct2Condition(problemConcernAct);
						System.out.println("End of transformation. Printing the resource as JSON object..");
						printJSON(fhirConditionBundle);
						System.out.println("End of print.");
					}
				}
			}
		}
	}
	
	@Ignore
	public void testProcedure2Procedure(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		int procedureCount = 0;
		if( test.ccd.getProceduresSection() != null && !test.ccd.getProceduresSection().isSetNullFlavor() ){
			if( test.ccd.getProceduresSection().getProcedures() != null && !test.ccd.getProceduresSection().getProcedures().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : test.ccd.getProceduresSection().getProcedures() ){
					// traversing procedures
					System.out.println( "Procedure["+ procedureCount++ +"]" );
					
					System.out.println("Transformation starting..");
					
					ca.uhn.fhir.model.dstu2.resource.Procedure fhirProcedure = null;
					
					Bundle fhirProcedureBundle = rt.Procedure2Procedure(cdaProcedure);
					for( Entry entry : fhirProcedureBundle.getEntry() ){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Procedure){
							fhirProcedure = (ca.uhn.fhir.model.dstu2.resource.Procedure) entry.getResource();
						}
					}
					
					System.out.println("End of transformation. Printing the resource as JSON object..");
					printJSON( fhirProcedure );
					System.out.println("End of print.");
					System.out.print("\n***\n"); // to visualize
				}
				
			}
		}
		
		int encounterProceduresCount = 0;
		if( test.ccd.getEncountersSection() != null && !test.ccd.getEncountersSection().isSetNullFlavor() ){
			if( test.ccd.getEncountersSection().getProcedures() != null && !test.ccd.getEncountersSection().getProcedures().isEmpty() ){
				System.out.println("**** ENCOUNTERS -> PROCEDURES *****");
				for( org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure : test.ccd.getEncountersSection().getProcedures() ){
					// traversing procedures
					System.out.println( "Procedure["+ encounterProceduresCount++ +"]" );
					
					System.out.println("Transformation starting..");
	
					ca.uhn.fhir.model.dstu2.resource.Encounter fhirProcedure = null;
					
					Bundle fhirProcedureBundle = rt.Procedure2Procedure(cdaProcedure);
					for( Entry entry : fhirProcedureBundle.getEntry() ){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Procedure){
							fhirProcedure = (Encounter) entry.getResource();
						}
					}
					
					System.out.println("End of transformation. Printing the resource as JSON object..");
					printJSON( fhirProcedure );
					System.out.println("End of print.");
					System.out.print("\n***\n"); // to visualize
				}
				
			}
		}
		
		if( test.ccd.getAllSections() != null && !test.ccd.getAllSections().isEmpty() ){
			System.out.println( "*** SECTIONS ****" );
			int sectionCount = 0;
			for( org.openhealthtools.mdht.uml.cda.Section section : test.ccd.getAllSections() ){
				if( section.getProcedures() != null && !section.getProcedures().isEmpty() ){
					int procedureCount2 = 0;
					for( org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure: section.getProcedures() ){
						// traversing procedures
						System.out.println("Section["+sectionCount+"]"+" -> Procedure["+ procedureCount2++ +"]");
	
						System.out.println("Transformation starting..");
	
						ca.uhn.fhir.model.dstu2.resource.Procedure fhirProcedure = null;
						
						Bundle fhirProcedureBundle = rt.Procedure2Procedure(cdaProcedure);
						for( Entry entry : fhirProcedureBundle.getEntry() ){
							if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Procedure){
								fhirProcedure = (ca.uhn.fhir.model.dstu2.resource.Procedure) entry.getResource();
							}
						}
						
						System.out.println("End of transformation. Printing the resource as JSON object..");
						printJSON( fhirProcedure );
						System.out.println("End of print.");
						System.out.println("\n***\n"); // to visualize
					}
					
				}
				sectionCount++;
			}
		}
		
	}

	@Ignore
	public void testSection2Section(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
	
		org.openhealthtools.mdht.uml.cda.Section sampleSection = null;
		
		// assigning sampleSection to one sample section
		if( test.ccd.getEncountersSection() != null && !test.ccd.getEncountersSection().isSetNullFlavor() ){
			if( test.ccd.getEncountersSection().getAllSections() != null && !test.ccd.getEncountersSection().getAllSections().isEmpty() ){
				if( test.ccd.getEncountersSection().getAllSections().get(0) != null && !test.ccd.getEncountersSection().getAllSections().get(0).isSetNullFlavor() ){
					sampleSection = test.ccd.getEncountersSection().getAllSections().get(0);
				}
			}
		}
		
		if( sampleSection != null ){
			System.out.println("Transformating..");
			ca.uhn.fhir.model.dstu2.resource.Composition.Section fhirSection = rt.section2Section(sampleSection);
			System.out.println("End of transformation. Printing the JSON Object..");
			
			// We need to embed fhirSection to fhirCompositon to use the method printJSON
			ca.uhn.fhir.model.dstu2.resource.Composition fhirComposition = new ca.uhn.fhir.model.dstu2.resource.Composition();
			fhirComposition.addSection(fhirSection);
			
			printJSON( fhirComposition );
			System.out.println("End of print.");
			System.out.print("\n***\n");
		}
	}

	@Ignore
	public void testLanguageCommunication2Communication(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		int patientCount = 0;
		for( org.openhealthtools.mdht.uml.cda.Patient patient : test.ccd.getPatients() ){
			System.out.print("Patient["+patientCount++ +"].");
			int lcCount = 0;
			for( org.openhealthtools.mdht.uml.cda.LanguageCommunication LC : patient.getLanguageCommunications() ){
				System.out.print("LC["+ lcCount++ +"]\n");
				
				System.out.println( "Transformating starting..." );
				ca.uhn.fhir.model.dstu2.resource.Patient.Communication fhirCommunication = rt.LanguageCommunication2Communication(LC);
				System.out.println("End of transformation. Building a patient resource..");
				ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient = new ca.uhn.fhir.model.dstu2.resource.Patient();
				fhirPatient.addCommunication(fhirCommunication);
				System.out.println("End of build. Printing the resource as JSON object..");
				printJSON( fhirPatient );
				System.out.println("End of print.");
			}
		}
	}
}