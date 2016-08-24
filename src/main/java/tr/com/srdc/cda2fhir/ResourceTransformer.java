package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.*;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

/**
 * Created by mustafa on 7/28/2016.
 */
public interface ResourceTransformer {

	Bundle tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct);

	/**
	 * Transforms a CDA AssignedAuthor instance to a FHIR Practitioner resource.
	 * @param cdaAssignedAuthor A CDA AssignedAuthor instance
	 * @return A Bundle that contains the Practitioner as the first entry, which can also include other referenced resources such as Organization
     */
	Bundle tAssignedAuthor2Practitioner(AssignedAuthor cdaAssignedAuthor);

	/**
	 * Transforms a CDA AssignedEntity instance to a FHIR Practitioner resource.
	 * @param cdaAssignedEntity A CDA AssignedEntity instance
	 * @return A Bundle that contains the Practitioner as the first entry, which can also include other referenced resources such as Organization
     */
	Bundle tAssignedEntity2Practitioner(AssignedEntity cdaAssignedEntity);

	/**
	 * Transforms a CDA CD instance to a FHIR Substance resource.
	 * @param cdaSubstanceCode A CDA CD instance
	 * @return A Substance resource
	 */
	Substance tCD2Substance(CD cdaSubstanceCode);
	
	/**
	 * Transforms a CDA Encounter instance to a FHIR Encounter resource.
	 * @param cdaEncounterActivity A CDA Encounter instance
	 * @return A Bundle that contains the Encounter as the first entry, which can also include other referenced resources such as Practitioner, Location
	 */
	Bundle tEncounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter);

	/**
	 * Transforms a CDA EncounterActivity instance to a FHIR Encounter resource.
	 * @param cdaEncounterActivity A CDA EncounterActivity instance
	 * @return A Bundle that contains the Encounter as the first entry, which can also include other referenced resources such as Practitioner, Location
	 */
	Bundle tEncounterActivity2Encounter(org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaEncounterActivity);
	
	/**
	 * Transforms a CDA Entity instance to a FHIR Group resource.
	 * @param cdaEntity A CDA Entity instance
	 * @return A Group resource
	 */
	Group tEntity2Group(Entity cdaEntity);
	
	FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO);

	Bundle tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObs);

	Condition tIndication2Condition(Indication indication);
	
	Bundle tManufacturedProduct2Medication(ManufacturedProduct cdaManuProd);
	
	Bundle tMedicationActivity2MedicationStatement(MedicationActivity cdaMedAct);
	
	Bundle tMedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMediDisp);
	
	Bundle tObservation2Observation( org.openhealthtools.mdht.uml.cda.Observation cdaObs);
	
	Organization tOrganization2Organization ( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization);
	
	Location tParticipantRole2Location(ParticipantRole cdaParticipantRole);
	
	Bundle tPatientRole2Patient(PatientRole cdaPatientRole);
	
	Bundle tPerformer22Practitioner(Performer2 cdaPerformer);
	
	Bundle tProblemConcernAct2Condition(ProblemConcernAct cdaProbConcAct);

	Bundle tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure);
	
	Bundle tResultObservation2Observation(ResultObservation cdaResultObs);

	Bundle tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer);
	
	// Instead of using tSocialHistoryObservation2Observation, get observations from the social history section,
	// then, transform observations by using tObservation2Observation
//	Bundle tSocialHistoryObservation2Observation( org.openhealthtools.mdht.uml.cda.consol.SocialHistoryObservation cdaSocialHistoryObs);
	
	Bundle tImmunizationActivity2Immunization(ImmunizationActivity cdaImmAct);
    
    Bundle tVitalSignObservation2Observation(VitalSignObservation cdaVSO);
    
    ca.uhn.fhir.model.dstu2.resource.Patient.Contact tGuardian2Contact(org.openhealthtools.mdht.uml.cda.Guardian cdaGuardian);
    
    Communication tLanguageCommunication2Communication(LanguageCommunication cdaLanguageCommunication);
	
	Observation.ReferenceRange tReferenceRange2ReferenceRange(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaRefRange);
    
	Composition.Section tSection2Section(Section cdaSec);
    
	Bundle tClinicalDocument2Composition(ClinicalDocument cda);
	
	Organization tCustodianOrganization2Organization(org.openhealthtools.mdht.uml.cda.CustodianOrganization cdaOrganization);
	
}
