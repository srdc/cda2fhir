package tr.com.srdc.cda2fhir;

import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;

import org.openhealthtools.mdht.uml.cda.Informant12;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
/**
 * Created by mustafa on 7/28/2016.
 */
public interface ResourceTransformer {

	///////////////////
	// new code starts
	
	// necip start
	ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( org.openhealthtools.mdht.uml.cda.Guardian guardian );
	
	ca.uhn.fhir.model.dstu2.resource.Procedure Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure);
	// necip end
	
	// ismail start
	Medication ManufacturedProduct2Medication(ManufacturedProduct manPro);
	
	List<Condition> ProblemConcernAct2Condition(ProblemConcernAct probAct);

	MedicationAdministration SubstanceAdministration2MedicationAdministration(
			SubstanceAdministration subAd);
	
	MedicationDispense Supply2MedicationDispense(Supply sup);
	// ismail end
	
	// new code ends
	////////////////
	
	//tahsin start
    Observation ResultObservation2Observation(ResultObservation resObs);

    Observation VitalSignObservation2Observation(VitalSignObservation vsObs);
    
    
    /*This is not a fully independent mapping method.*/
    Practitioner Performer2Practitioner(Performer2 performer,int id);
    /*It will be called by functions which contain Practitioner as a subresource*/
    
    AllergyIntolerance AllergyProblemAct2AllergyIntolerance(AllergyProblemAct allProb);
    //tahsin end
}
