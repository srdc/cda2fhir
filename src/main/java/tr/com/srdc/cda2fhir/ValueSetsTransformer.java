package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.valueset.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;

public interface ValueSetsTransformer {
	
	/**
	* Transforms a CDA AdministrativeGenderCode string to a FHIR AdministrativeGenderEnum.
	* @param cdaAdministrativeGenderCode A CDA AdministrativeGenderCode string
	* @return A value from the FHIR valueset AdministrativeGenderEnum
	*/
	AdministrativeGenderEnum tAdministrativeGenderCode2AdministrativeGenderEnum(String cdaAdministrativeGenderCode);
	
	/**
	 * Transforms a CDA AgeObservationUnit string to a FHIR AgeUnit string.
	 * @param cdaAgeObservationUnit A CDA AgeObservationUnit string
	 * @return A FHIR AgeUnit string
	 */
	String tAgeObservationUnit2AgeUnit(String cdaAgeObservationUnit);
	
	/**
	 * Transforms a CDA AllergyCategoryCode string to a FHIR AllergyIntoleranceCategoryEnum.
	 * @param cdaAllergyCategoryCode A CDA AllergyCategoryCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceCategoryEnum
	 */
	AllergyIntoleranceCategoryEnum tAllergyCategoryCode2AllergyIntoleranceCategoryEnum(String cdaAllergyCategoryCode);
	
	/**
	 * Transforms a CDA CriticalityObservation's value's code string to a FHIR AllergyIntoleranceCriticalityEnum.
	 * @param cdaCriticalityObservationValue A CDA CriticalityObservation's value's code string
	 * @return A value from the FHIR valueset AllergyIntolerancecriticalityEnum
	 */
	AllergyIntoleranceCriticalityEnum tCriticalityObservationValue2AllergyIntoleranceCriticalityEnum(String cdaCriticalityObservationValue);
	
	/**
	 * Transforms a CDA EncounterCode string to a FHIR EncounterClassEnum.
	 * @param cdaEncounterCode A CDA EncounterCode string
	 * @return A value from the FHIR valueset EncounterClassEnum
	 */
	EncounterClassEnum tEncounterCode2EncounterClassEnum(String cdaEncounterCode);
	
	/**
	 * Transforms a CDA EntityClassRoot vocable to a value from the FHIR valueset GroupTypeEnum.
	 * @param cdaEntityClassRoot A CDA EntityClassRoot vocable
	 * @return A value from the FHIR valueset GroupTypeEnum
	 */
	GroupTypeEnum tEntityClassRoot2GroupTypeEnum(EntityClassRoot cdaEntityClassRoot);
	
	/**
	 * Transforms a CDA EntityNameUse vocable to a value from the FHIR valueset NameUseEnum.
	 * @param cdaEntityNameUse A CDA EntityNameUse vocable
	 * @return A value from the FHIR valueset NameUseEnum
	 */
	NameUseEnum tEntityNameUse2NameUseEnum(EntityNameUse cdaEntityNameUse);
	
	/**
	 * Transforms a CDA FamilyHistoryOrganizerStatusCode string to a value from the FHIR valueset FamilyHistoryStatusEnum.
	 * @param cdaFamilyHistoryOrganizerStatusCode A CDA FamilyHistoryOrganizerStatusCode string
	 * @return A value from the FHIR valueset FamilyHistoryStatusEnum
	 */
	FamilyHistoryStatusEnum tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum(String cdaFamilyHistoryOrganizerStatusCode);
	
	/**
	 * Transforms a CDA MaritalStatusCode string to a value from the FHIR valueset MaritalStatusCodesEnum.
	 * @param cdaMaritalStatusCode A CDA MaritalStatusCode string
	 * @return A value from the FHIR valueset MaritalStatusCodesEnum
	 */
	MaritalStatusCodesEnum tMaritalStatusCode2MaritalStatusCodesEnum(String cdaMaritalStatusCode);
	
	/**
	 * Transforms a CDA NullFlavor vocable to a FHIR CodingDt composite datatype which includes the code about DataAbsentReason.
	 * @param cdaNullFlavor A CDA NullFlavor vocable
	 * @return A FHIR CodingDt composite datatype which includes the code about DataAbsentReason.
	 */
	CodingDt tNullFlavor2DataAbsentReasonCode(NullFlavor cdaNullFlavor);
	
	/**
	 * Transforms a CDA ObservationStatusCode string to a value from the FHIR valueset ObservationStatusEnum.
	 * @param cdaObservationStatusCode A CDA ObservationStatusCode string
	 * @return A value from the FHIR valueset ObservationStatusEnum
	 */
	ObservationStatusEnum tObservationStatusCode2ObservationStatusEnum(String cdaObservationStatusCode);
	
	/**
	 * Transforms a CodeSystem string to a URL string.
	 * @param codeSystem a CodeSystem string
	 * @return A URL string
	 */
	String tOid2Url(String codeSystem);
	
	/**
	 * Transforms a CDA ParticipationType vocable to a FHIR CodingDt composite datatype which includes the code about ParticipationType.
	 * @param cdaParticipationType A CDA ParticipationType vocable
	 * @return A FHIR CodingDt composite datatype which includes the code about ParticipationType
	 */
	CodingDt tParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaParticipationType);

	/**
	 * Transforms a CDA PeriodUnit string to a value from the FHIR valueset UnitsOfTimeEnum.
	 * @param cdaPeriodUnit A CDA PeriodUnit string
	 * @return A value from the FHIR valueset UnitsOfTimeEnum.
	 */
	UnitsOfTimeEnum tPeriodUnit2UnitsOfTimeEnum(String cdaPeriodUnit);

	/**
	 * Transforms a CDA ProblemType string to a value from the FHIR valuset ConditionCategoryCodesEnum.
	 * @param cdaProblemType A CDA ProblemType string
	 * @return A value from the FHIR valuset ConditionCategoryCodesEnum
	 */
	ConditionCategoryCodesEnum tProblemType2ConditionCategoryCodesEnum(String cdaProblemType);
	 
	/**
	 * Transforms a CDA PostalAddressUse vocable to a value from the FHIR valueset AddressTypeEnum.
	 * @param cdaPostalAddressUse A CDA PostalAddressUse vocable
	 * @return A value from the FHIR valueset AddressTypeEnum
	 */
	AddressTypeEnum tPostalAddressUse2AddressTypeEnum(PostalAddressUse cdaPostalAddressUse);
	
	/**
	 * Transforms a CDA PostalAddressUse vocable to a value from the FHIR valueset AddressUseEnum.
	 * @param cdaPostalAddressUse A CDA PostalAddressUse vocable
	 * @return A value from the FHIR valueset AddressUseEnum
	 */
	AddressUseEnum tPostalAdressUse2AddressUseEnum(PostalAddressUse cdaPostalAddressUse);
	
	/**
	 * Transforms a CDA RoleCode string to a FHIR CodingDt composite datatype which includes the code about PatientContactRelationship.
	 * @param cdaRoleCode A CDA RoleCode string
	 * @return A FHIR CodingDt composite datatype which includes the code about PatientContactRelationship
	 */
	CodingDt tRoleCode2PatientContactRelationshipCode(String cdaRoleCode);
	
	/**
	 * Transforms a CDA SeverityCode string to a value from the FHIR valueset AllergyIntoleranceSeverityEnum.
	 * @param cdaSeverityCode A CDA SeverityCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceSeverityEnum.
	 */
	AllergyIntoleranceSeverityEnum tSeverityCode2AllergyIntoleranceSeverityEnum(String cdaSeverityCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset AllergyIntoleranceStatusEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceStatusEnum
	 */
	AllergyIntoleranceStatusEnum tStatusCode2AllergyIntoleranceStatusEnum(String cdaStatusCode);

	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset ConditionClinicalStatusCodesEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset ConditionClinicalStatusCodesEnum
     */
	ConditionClinicalStatusCodesEnum tStatusCode2ConditionClinicalStatusCodesEnum(String cdaStatusCode);

	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset EncounterStateEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset EncounterStateEnum
	 */
	EncounterStateEnum tStatusCode2EncounterStatusEnum(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset MedicationDispenseStatusEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset MedicationDispenseStatusEnum
	 */
	MedicationDispenseStatusEnum tStatusCode2MedicationDispenseStatusEnum(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset MedicationStatementStatusEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset MedicationStatementStatusEnum
	 */
	MedicationStatementStatusEnum tStatusCode2MedicationStatementStatusEnum(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset ProcedureStatusEnum.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset ProcedureStatusEnum
	 */
	ProcedureStatusEnum tStatusCode2ProcedureStatusEnum(String cdaStatusCode);
	
	/**
	 * Transforms a CDA TelecommunicationAddressUse vocable to a value from the FHIR valueset ContactPointUseEnum.
	 * @param cdaTelecommunicationAddressUse A CDA TelecommunicationAddressUse vocable
	 * @return A value from the FHIR valueset ContactPointUseEnum
	 */
	ContactPointUseEnum tTelecommunicationAddressUse2ContacPointUseEnum(TelecommunicationAddressUse cdaTelecommunicationAddressUse);

	/**
	 * Transforms a CDA TelValue string to a value from the FHIR valueset ContactPointSystemEnum.
	 * @param cdaTelValue A CDA TelValue string
	 * @return A value from the FHIR valueset ContactPointSystemEnum.
	 */
	ContactPointSystemEnum tTelValue2ContactPointSystemEnum(String cdaTelValue);
}