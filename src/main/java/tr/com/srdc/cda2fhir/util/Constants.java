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

}
