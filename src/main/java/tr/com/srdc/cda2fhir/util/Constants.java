package tr.com.srdc.cda2fhir.util;

import ca.uhn.fhir.model.dstu2.valueset.ConditionVerificationStatusEnum;

/**
 * Created by mustafa on 8/29/2016.
 */
public class Constants {

    /**
     * Condition.verificationStatus is mandatory, but cannot be mapped from the CDA side. Hence, a default value is selected here.
     */
    public static final ConditionVerificationStatusEnum DEFAULT_CONDITION_VERIFICATION_STATUS = ConditionVerificationStatusEnum.CONFIRMED;

    public static final String URL_EXTENSION_BIRTHPLACE     = "http://hl7.org/fhir/StructureDefinition/birthPlace";
    public static final String URL_EXTENSION_ETHNICITY      = "http://hl7.org/fhir/StructureDefinition/us-core-ethnicity";
    public static final String URL_EXTENSION_RACE           = "http://hl7.org/fhir/StructureDefinition/us-core-race";
    public static final String URL_EXTENSION_RELIGION       = "http://hl7.org/fhir/StructureDefinition/us-core-religion";

}
