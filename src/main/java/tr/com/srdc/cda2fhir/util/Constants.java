package tr.com.srdc.cda2fhir.util;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.valueset.CompositionStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ConditionVerificationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;

/**
 * Created by mustafa on 8/29/2016.
 */
public class Constants {

    /**
     * Condition.verificationStatus is mandatory, but cannot be mapped from the CDA side. Hence, a default value is selected here.
     */
	public static final String DEFAULT_COMMUNICATION_LANGUAGE_CODE_SYSTEM = "urn:ietf:bcp:47";
	public static final CompositionStatusEnum DEFAULT_COMPOSITION_STATUS = CompositionStatusEnum.PRELIMINARY;
    public static final ConditionVerificationStatusEnum DEFAULT_CONDITION_VERIFICATION_STATUS = ConditionVerificationStatusEnum.CONFIRMED;
    public static final ContactPointSystemEnum DEFAULT_CONTACT_POINT_SYSTEM = ContactPointSystemEnum.PHONE;
    public static final CodingDt DEFAULT_ENCOUNTER_PARTICIPANT_TYPE_CODE = new CodingDt().setSystem("http://hl7.org/fhir/v3/ParticipationType").setCode("PART").setDisplay("Participation");
    public static final EncounterStateEnum DEFAULT_ENCOUNTER_STATUS = EncounterStateEnum.FINISHED;
    public static final CodingDt DEFAULT_DIAGNOSTICREPORT_PERFORMER_DATA_ABSENT_REASON_CODE = new CodingDt().setSystem("http://hl7.org/fhir/data-absent-reason").setCode("unknown").setDisplay("Unknown");
    public static final boolean DEFAULT_IMMUNIZATION_REPORTED = false;

    public static final String URL_EXTENSION_BIRTHPLACE     = "http://hl7.org/fhir/StructureDefinition/birthPlace";
    public static final String URL_EXTENSION_DATA_ABSENT_REASON = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";
    public static final String URL_EXTENSION_ETHNICITY      = "http://hl7.org/fhir/StructureDefinition/us-core-ethnicity";
    public static final String URL_EXTENSION_RACE           = "http://hl7.org/fhir/StructureDefinition/us-core-race";
    public static final String URL_EXTENSION_RELIGION       = "http://hl7.org/fhir/StructureDefinition/us-core-religion";
    
    public static final String PROFILE_ALLERGY_INTOLERANCE = "http://hl7.org/fhir/StructureDefinition/daf-allergyintolerance";
    public static final String PROFILE_DIAGNOSTIC_ORDER = "http://hl7.org/fhir/StructureDefinition/daf-diagnosticorder";
    public static final String PROFILE_DIAGNOSTIC_REPORT = "http://hl7.org/fhir/StructureDefinition/daf-diagnosticreport";
    public static final String PROFILE_ENCOUNTER = "http://hl7.org/fhir/StructureDefinition/daf-encounter";
    public static final String PROFILE_FAMILY_MEMBER_HISTORY = "http://hl7.org/fhir/StructureDefinition/daf-familymemberhistory";
    public static final String PROFILE_IMMUNIZATION = "http://hl7.org/fhir/StructureDefinition/daf-immunization";
    public static final String PROFILE_RESULT_OBS = "http://hl7.org/fhir/StructureDefinition/daf-resultobs";
    public static final String PROFILE_MEDICATION = "http://hl7.org/fhir/StructureDefinition/daf-medication";
    public static final String PROFILE_MEDICATION_STATEMENT = "http://hl7.org/fhir/StructureDefinition/daf-medicationstatement";
    public static final String PROFILE_MEDICATION_ADMINISTRATION = "http://hl7.org/fhir/StructureDefinition/daf-medicationadministration";
    public static final String PROFILE_MEDICATION_DISPENSE = "https://www.hl7.org/fhir/daf/medicationdispense-daf.html";
    public static final String PROFILE_MEDICATION_ORDER = "http://hl7.org/fhir/StructureDefinition/daf-medicationorder";
    public static final String PROFILE_PATIENT = "http://hl7.org/fhir/StructureDefinition/daf-patient";
    public static final String PROFILE_CONDITION = "http://hl7.org/fhir/StructureDefinition/daf-condition";
    public static final String PROFILE_PROCEDURE = "http://hl7.org/fhir/StructureDefinition/daf-procedure";
    public static final String PROFILE_SMOKING_STATUS = "http://hl7.org/fhir/StructureDefinition/daf-smokingstatus";
    public static final String PROFILE_VITAL_SIGNS = "http://hl7.org/fhir/StructureDefinition/daf-vitalsigns";
    public static final String PROFILE_ORGANIZATION = "http://hl7.org/fhir/StructureDefinition/daf-organization";
    public static final String PROFILE_LOCATION = "http://hl7.org/fhir/StructureDefinition/daf-location";
    public static final String PROFILE_PRACTITIONER = "http://hl7.org/fhir/StructureDefinition/daf-pract";
    public static final String PROFILE_SUBSTANCE = "http://hl7.org/fhir/StructureDefinition/daf-substance";
    public static final String PROFILE_RELATED_PERSON = "http://hl7.org/fhir/StructureDefinition/daf-relatedperson";
    public static final String PROFILE_SPECIMEN = "http://hl7.org/fhir/StructureDefinition/daf-spec";
    public static final String PROFILE_ALLERGY_LIST = "http://hl7.org/fhir/StructureDefinition/daf-allergylist";
    public static final String PROFILE_ENCOUNTER_LIST = "http://hl7.org/fhir/StructureDefinition/daf-encounterlist";
    public static final String PROFILE_IMMUNIZATION_LIST = "http://hl7.org/fhir/StructureDefinition/daf-immunizationlist";
    public static final String PROFILE_MEDICATION_LIST = "http://hl7.org/fhir/StructureDefinition/daf-medicationlist";
    public static final String PROFILE_PROBLEM_LIST = "http://hl7.org/fhir/StructureDefinition/daf-problemlist";
    public static final String PROFILE_PROCEDURE_LIST = "http://hl7.org/fhir/StructureDefinition/daf-procedurelist";
    public static final String PROFILE_RESULT_LIST = "http://hl7.org/fhir/StructureDefinition/daf-resultlist";
    
}
