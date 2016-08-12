package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
/**
 * Created by mustafa on 7/28/2016.
 */
public interface ResourceTransformer {

	Bundle AllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct);
	
	Bundle AssignedAuthor2Practitioner( AssignedAuthor cdaAssignedAuthor );
	
	Bundle AssignedEntity2Practitioner(AssignedEntity cdaAssignedEntity );
	
	Bundle Encounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter);
	
	Bundle Entity2Group( Entity cdaEntity );
	
	Bundle FamilyMemberOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO);
	
	Bundle ManufacturedProduct2Medication(ManufacturedProduct cdaManuProd);
	
	Bundle MedicationActivity2MedicationStatement(MedicationActivity cdaMedAct);
	
	Bundle MedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMediDisp);
	
	Bundle Observation2Observation( org.openhealthtools.mdht.uml.cda.Observation cdaObs );
	
	Bundle Organization2Organization ( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization );
	
	Bundle ParticipantRole2Location(ParticipantRole cdaParticipantRole );
	
	Bundle PatientRole2Patient(PatientRole cdaPatientRole);
	
	Bundle Performer22Practitioner( Performer2 cdaPerformer );
	
	Bundle ProblemConcernAct2Condition(ProblemConcernAct cdaProbConcAct);

	Bundle Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure);
	
	Bundle ResultObservation2Observation(ResultObservation cdaResultObs);
	
	Bundle SocialHistoryObservation2Observation( org.openhealthtools.mdht.uml.cda.consol.SocialHistoryObservation cdaSocialHistoryObs );
	
	Bundle SubstanceAdministration2Immunization(SubstanceAdministration cdaSubAdm);
    
    Bundle VitalSignObservation2Observation(VitalSignObservation cdaVSO);
    
    
    ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( org.openhealthtools.mdht.uml.cda.Guardian cdaGuardian );
    
    Communication LanguageCommunication2Communication( LanguageCommunication cdaLanguageCommunication );
	
    ca.uhn.fhir.model.dstu2.resource.Procedure.Performer Performer22Performer( Performer2 cdaPerformer );

	Observation.ReferenceRange ReferenceRange2ReferenceRange( org.openhealthtools.mdht.uml.cda.ReferenceRange cdaRefRange);
    
	Composition.Section section2Section(Section cdaSec);
    
}
