package tr.com.srdc.cda2fhir.util;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class Constants {
	
    // DAF Profile URLs (based on FHIR DSTU2)
    public static final String PROFILE_DAF_ALLERGY_INTOLERANCE          = "http://hl7.org/fhir/StructureDefinition/daf-allergyintolerance";
    public static final String PROFILE_DAF_ALLERGY_LIST                 = "http://hl7.org/fhir/StructureDefinition/daf-allergylist";
    public static final String PROFILE_DAF_CONDITION                    = "http://hl7.org/fhir/StructureDefinition/daf-condition";
    public static final String PROFILE_DAF_DIAGNOSTIC_ORDER             = "http://hl7.org/fhir/StructureDefinition/daf-diagnosticorder";
    public static final String PROFILE_DAF_DIAGNOSTIC_REPORT            = "http://hl7.org/fhir/StructureDefinition/daf-diagnosticreport";
    public static final String PROFILE_DAF_ENCOUNTER                    = "http://hl7.org/fhir/StructureDefinition/daf-encounter";
    public static final String PROFILE_DAF_ENCOUNTER_LIST               = "http://hl7.org/fhir/StructureDefinition/daf-encounterlist";
    public static final String PROFILE_DAF_FAMILY_MEMBER_HISTORY        = "http://hl7.org/fhir/StructureDefinition/daf-familymemberhistory";
    public static final String PROFILE_DAF_IMMUNIZATION                 = "http://hl7.org/fhir/StructureDefinition/daf-immunization";
    public static final String PROFILE_DAF_IMMUNIZATION_LIST            = "http://hl7.org/fhir/StructureDefinition/daf-immunizationlist";
    public static final String PROFILE_DAF_LOCATION                     = "http://hl7.org/fhir/StructureDefinition/daf-location";
    public static final String PROFILE_DAF_MEDICATION                   = "http://hl7.org/fhir/StructureDefinition/daf-medication";
    public static final String PROFILE_DAF_MEDICATION_ADMINISTRATION    = "http://hl7.org/fhir/StructureDefinition/daf-medicationadministration";
    public static final String PROFILE_DAF_MEDICATION_DISPENSE          = "http://hl7.org/fhir/StructureDefinition/daf-medicationdispense";
    public static final String PROFILE_DAF_MEDICATION_LIST              = "http://hl7.org/fhir/StructureDefinition/daf-medicationlist";
    public static final String PROFILE_DAF_MEDICATION_ORDER             = "http://hl7.org/fhir/StructureDefinition/daf-medicationorder";
    public static final String PROFILE_DAF_MEDICATION_STATEMENT         = "http://hl7.org/fhir/StructureDefinition/daf-medicationstatement";
	public static final String PROFILE_DAF_MEDICATION_REQUEST 			= "http://hl7.org/fhir/StructureDefinition/daf-medicationRequest";

    public static final String PROFILE_DAF_ORGANIZATION                 = "http://hl7.org/fhir/StructureDefinition/daf-organization";
    public static final String PROFILE_DAF_PATIENT                      = "http://hl7.org/fhir/StructureDefinition/daf-patient";
    public static final String PROFILE_DAF_PRACTITIONER                 = "http://hl7.org/fhir/StructureDefinition/daf-pract";
    public static final String PROFILE_DAF_PROBLEM_LIST                 = "http://hl7.org/fhir/StructureDefinition/daf-problemlist";
    public static final String PROFILE_DAF_PROCEDURE                    = "http://hl7.org/fhir/StructureDefinition/daf-procedure";
    public static final String PROFILE_DAF_PROCEDURE_LIST               = "http://hl7.org/fhir/StructureDefinition/daf-procedurelist";
    public static final String PROFILE_DAF_RELATED_PERSON               = "http://hl7.org/fhir/StructureDefinition/daf-relatedperson";
    public static final String PROFILE_DAF_RESULT_LIST                  = "http://hl7.org/fhir/StructureDefinition/daf-resultlist";
    public static final String PROFILE_DAF_RESULT_OBS                   = "http://hl7.org/fhir/StructureDefinition/daf-resultobs";
    public static final String PROFILE_DAF_SMOKING_STATUS               = "http://hl7.org/fhir/StructureDefinition/daf-smokingstatus";
    public static final String PROFILE_DAF_SPECIMEN                     = "http://hl7.org/fhir/StructureDefinition/daf-spec";
    public static final String PROFILE_DAF_SUBSTANCE                    = "http://hl7.org/fhir/StructureDefinition/daf-substance";
    public static final String PROFILE_DAF_VITAL_SIGNS                  = "http://hl7.org/fhir/StructureDefinition/daf-vitalsigns";
    
    // Extension URLs
    public static final String URL_EXTENSION_BIRTHPLACE         = "http://hl7.org/fhir/StructureDefinition/birthPlace";
    public static final String URL_EXTENSION_DATA_ABSENT_REASON = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";
    public static final String URL_EXTENSION_ETHNICITY          = "http://hl7.org/fhir/StructureDefinition/us-core-ethnicity";
    public static final String URL_EXTENSION_RACE               = "http://hl7.org/fhir/StructureDefinition/us-core-race";
    public static final String URL_EXTENSION_RELIGION           = "http://hl7.org/fhir/StructureDefinition/us-core-religion";

}
