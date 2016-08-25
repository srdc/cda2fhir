package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.AgeDt;
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
	
	/**
	 * Transforms a CDA AgeObservation instance to a FHIR AgeDt composite datatype
	 * @param cdaAgeObservation A CDA AgeObservation instance
	 * @return An AgeDt composite datatype
	 */
	AgeDt tAgeObservation2AgeDt(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation);

	/**
	 * Transforms a CDA AllergyProblemAct instance to a FHIR AllergyIntolerance resource.
	 * @param cdaAllergyProblemAct A CDA AllergyProblemAct instance
	 * @return A Bundle that contains the AllergyIntolerance as the first entry, which can also include other referenced resources such as Practitioner
     */
	Bundle tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProblemAct);

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
	
	/**
	 * Transforms a CDA FamilyHistoryOrganizer instance to a FHIR FamilyMemberHistory resource.
	 * @param cdaFamilyHistoryOrganizer A CDA FamilyHistoryOrganizer instance
	 * @return A FamilyMemberHistory resource
	 */
	FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFamilyHistoryOrganizer);

	/**
	 * Transforms a CDA Observation instance that is included in Functional Status Section to a FHIR Observation resource.
	 * @param cdaObservation A CDA Observation instance that is included in Functional Status Section
	 * @return An Observation resource
	 */
	Bundle tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation);

	/**
	 * Transforms a CDA Indication instance to a FHIR Condition resource.
	 * @param cdaIndication A CDA Indication instance
	 * @return A Condition resource
	 */
	Condition tIndication2Condition(Indication cdaIndication);
	
	/**
	 * Transforms a CDA ManufacturedProduct instance to a FHIR Medication resource.
	 * @param cdaManufacturedProduct A CDA ManufacturedProduct instance
	 * @return A Bundle that contains the Medication as the first entry, which can also include other referenced resources such as Substance, Organization
	 */
	Bundle tManufacturedProduct2Medication(ManufacturedProduct cdaManufacturedProduct);
	
	/**
	 * Transforms a CDA MedicationActivity instance to a FHIR MedicationStatement resource.
	 * @param cdaMedicationActivity A CDA MedicationActivity instance
	 * @return A Bundle that contains the MedicationStatement as the first entry, which can also include other referenced resources such as Practitioner, Medication, Condition
	 */
	Bundle tMedicationActivity2MedicationStatement(MedicationActivity cdaMedicationActivity);
	
	/**
	 * Transforms a CDA MedicationDispense instance to a FHIR MedicationDispense resource.
	 * @param cdaMedicationDispense A CDA MedicationDispense instance
	 * @return A Bundle that contains the MedicationDispense as the first entry, which can also include other referenced resources such as Medication, Practitioner
	 */
	Bundle tMedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMedicationDispense);
	
	/**
	 * Transforms a CDA Observation instance to a FHIR Observation resource.
	 * @param cdaObservation A CDA Observation instance
	 * @return A Bundle that contains the Observation as the first entry, which can also include other referenced resources such as Encounter, Practitioner
	 */
	Bundle tObservation2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation);
	
	/**
	 * Transforms a CDA Organization instance to a FHIR Organization resource.
	 * @param cdaOrganization A CDA Organization instance
	 * @return An Organization resource
	 */
	Organization tOrganization2Organization (org.openhealthtools.mdht.uml.cda.Organization cdaOrganization);
	
	/**
	 * Transforms a CDA ParticipantRole instance to a FHIR Location resource.
	 * @param cdaParticipantRole A CDA ParticipantRole instance
	 * @return A Location Resource
	 */
	Location tParticipantRole2Location(ParticipantRole cdaParticipantRole);
	
	/**
	 * Transforms a CDA PatientRole instance to a FHIR Patient resource.
	 * @param cdaPatientRole A CDA PatientRole instance
	 * @return A Bundle that contains the PatientRole as the first entry, which can also include other referenced resources such as Organization
	 */
	Bundle tPatientRole2Patient(PatientRole cdaPatientRole);
	
	/**
	 * Transforms a CDA Performer2 instance to a FHIR Practitioner resource.
	 * @param cdaPerformer2 A CDA Performer2 instance
	 * @return A Bundle that contains the Practitioner as the first entry, which can also include other referenced resources such as Organization
	 */
	Bundle tPerformer22Practitioner(Performer2 cdaPerformer2);
	
	/**
	 * Transforms a CDA ProblemConcernAct instance to a FHIR Condition resource.
	 * @param cdaProblemConcernAct A CDA ProblemConcernAct instance
	 * @return A Bundle that contains the Condition as the first entry, which can also include other referenced resources such as Encounter, Practitioner
	 */
	Bundle tProblemConcernAct2Condition(ProblemConcernAct cdaProblemConcernAct);

	/**
	 * Transforms a CDA Procedure instance to a FHIR Procedure resource.
	 * @param cdaProcedure A CDA Procedure instance
	 * @return A Bundle that contains the Procedure as the first entry, which can also include other referenced resources such as Practitioner
	 */
	Bundle tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure);
	
	/**
	 * Transforms a CDA ResultObservation instance to a FHIR Observation resource.
	 * @param cdaResultObservation A CDA ResultObservation instance
	 * @return A Bundle that contains the Observation as the first entry, which can also include other referenced resources such as Encounter, Practitioner
	 */
	Bundle tResultObservation2Observation(ResultObservation cdaResultObservation);

	/**
	 * Transforms a CDA ResultOrganizer instance to a FHIR DiagnosticReport resource.
	 * @param cdaResultOrganizer A CDA ResultOrganizer instance
	 * @return A Bundle that contains the DiagnosticReport as the first entry, which can also include other referenced resources such as Practitioner, Observation
	 */
	Bundle tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer);
	
	/**
	 * Transforms a CDA ImmunizationActivity instance to a FHIR Immunization resource.
	 * @param cdaImmunizationActivity A CDA ImmunizationActivity instance
	 * @return A Bundle that contains the Immunization as the first entry, which can also include other referenced resources such as Organization, Practitioner
	 */
	Bundle tImmunizationActivity2Immunization(ImmunizationActivity cdaImmunizationActivity);
    
	/**
	 * Transforms a CDA VitalSignObservation to a FHIR Observation resource.
	 * @param cdaVitalSignObservation A CDA VitalSignObservation instance
	 * @return A Bundle that contains the Observation as the first entry, which can also include other referenced resources such as Encounter, Practitioner
	 */
    Bundle tVitalSignObservation2Observation(VitalSignObservation cdaVitalSignObservation);
    
    /**
     * Transforms a CDA Guardian instance to a FHIR Patient.Contact resource.
     * @param cdaGuardian A CDA Guardian instance
     * @return A Patient.Contact resource
     */
    ca.uhn.fhir.model.dstu2.resource.Patient.Contact tGuardian2Contact(org.openhealthtools.mdht.uml.cda.Guardian cdaGuardian);
    
    /**
     * Transforms a CDA LanguageCommunication instance to a FHIR Communication resource.
     * @param cdaLanguageCommunication A CDA LanguageCommunication instance
     * @return A Communication resource
     */
    Communication tLanguageCommunication2Communication(LanguageCommunication cdaLanguageCommunication);
	
    /**
     * Transforms a CDA ReferenceRange instance to a FHIR Observation.ReferenceRange resource.
     * @param cdaReferenceRange A CDA ReferenceRange instance
     * @return An Observation.ReferenceRange resource
     */
	Observation.ReferenceRange tReferenceRange2ReferenceRange(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange);
    
	/**
	 * Transforms a CDA Section instance to a FHIR Composition.Section resource.
	 * @param cdaSection A CDA Section instance
	 * @return A Composition.Section resource
	 */
	Composition.Section tSection2Section(Section cdaSection);
    
	/**
	 * Transforms a CDA ServiceDeliveryLocation instance to a FHIR Location resource.
	 * @param cdaSDLOC A CDA ServiceDeliveryLocation instance
	 * @return A Location resource
	 */
	Location tServiceDeliveryLocation2Location(ServiceDeliveryLocation cdaSDLOC);
	
	/**
	 * Transforms a CDA Supply instance to a FHIR Device resource.
	 * @param cdaSupply A CDA Supply instance
	 * @return A Device resource
	 */
	ca.uhn.fhir.model.dstu2.resource.Device tSupply2Device(org.openhealthtools.mdht.uml.cda.Supply cdaSupply);
	
	/**
	 * Transforms a CDA ClicinalDocument instance to a FHIR Composition resource.
	 * @param cdaClinicalDocument A CDA ClicinalDocument instance
	 * @return A Bundle that contains the Composition as the first entry, which can also include other referenced resources such as Patient, Practitioner, Organization
	 */
	Bundle tClinicalDocument2Composition(ClinicalDocument cdaClinicalDocument);
	
	/**
	 * Transforms a CDA CustodianOrganization instance to a FHIR Organization resource.
	 * @param cdaCustodianOrganization A CDA CustodianOrganization instance
	 * @return An Organization resource
	 */
	Organization tCustodianOrganization2Organization(org.openhealthtools.mdht.uml.cda.CustodianOrganization cdaCustodianOrganization);
	
}
