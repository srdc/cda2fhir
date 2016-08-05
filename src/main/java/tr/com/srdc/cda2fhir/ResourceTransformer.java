package tr.com.srdc.cda2fhir;

import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;

import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
/**
 * Created by mustafa on 7/28/2016.
 */
public interface ResourceTransformer {
	
	// necip start
	Patient PatientRole2Patient(PatientRole patRole);

	ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( org.openhealthtools.mdht.uml.cda.Guardian guardian );
	
	ca.uhn.fhir.model.dstu2.resource.Procedure Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure);

	Performer Performer22Performer( Performer2 cdaPerformer );

	Practitioner AssignedEntity2Practitioner(AssignedEntity assignedEntity );

	ca.uhn.fhir.model.dstu2.resource.Organization Organization2Organization ( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization );

	Communication LanguageCommunication2Communication( LanguageCommunication LC );

	Practitioner Performer22Practitioner( Performer2 cdaPerformer );
	
	
	// necip end
	
	// ismail start
Bundle Medication2Medication(ManufacturedProduct manPro);
	
	Bundle ProblemConcernAct2Condition(ProblemConcernAct probAct);

	Bundle MedicationActivity2MedicationSatement(
			MedicationActivity subAd);
	
	Bundle MedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense sup);
	
	Location ParticipantRole2Location(ParticipantRole patRole );
	// ismail end

	//tahsin start
    Bundle ResultObservation2Observation(ResultObservation resObs);

    Observation VitalSignObservation2Observation(VitalSignObservation vsObs);
    
    
    /*This is not a fully independent mapping method.*/
    Bundle Performer2Practitioner(Performer2 performer);
    /*It will be called by functions which contain Practitioner as a subresource*/
    
    AllergyIntolerance AllergyProblemAct2AllergyIntolerance(AllergyProblemAct allergyProblemAct);

    Bundle SubstanceAdministration2Immunization(SubstanceAdministration subAd);
    
    //tahsin end

	Composition.Section section2Section(Section cdaSec);
}
