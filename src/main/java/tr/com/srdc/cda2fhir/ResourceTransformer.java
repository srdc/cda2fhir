package tr.com.srdc.cda2fhir;

import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;

import org.openhealthtools.mdht.uml.cda.Informant12;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
/**
 * Created by mustafa on 7/28/2016.
 */
public interface ResourceTransformer {

    /*Patient PatientRole2Patient(PatientRole patRole);

    Condition ProblemConcernAct2Condition(ProblemConcernAct probAct);*/

    Observation ResultObservation2Observation(ResultObservation resObs);

    Observation VitalSignObservation2Observation(VitalSignObservation vsObs);
    
    Practitioner Performer2Practitioner(Performer2 performer,int id);

	Medication ManufacturedProduct2Medication(ManufacturedProduct manPro);

	

	List<Condition> ProblemConcernAct2Condition(ProblemConcernAct probAct);

	MedicationAdministration SubstanceAdministration2MedicationAdministration(
			SubstanceAdministration subAd);
}
