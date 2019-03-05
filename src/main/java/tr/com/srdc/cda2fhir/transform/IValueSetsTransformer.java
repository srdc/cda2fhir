package tr.com.srdc.cda2fhir.transform;

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

import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCriticality;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceSeverity;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.dstu3.model.Condition.ConditionVerificationStatus;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory.FamilyHistoryStatus;
import org.hl7.fhir.dstu3.model.Group.GroupType;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.dstu3.model.MedicationDispense.MedicationDispenseStatus;
import org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Timing.UnitsOfTime;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

public interface IValueSetsTransformer {
	
	/**
	* Transforms a CDA AdministrativeGenderCode string to a FHIR AdministrativeGender.
	* @param cdaAdministrativeGenderCode A CDA AdministrativeGenderCode string
	* @return A value from the FHIR valueset AdministrativeGender
	*/
	AdministrativeGender tAdministrativeGenderCode2AdministrativeGender(String cdaAdministrativeGenderCode);
	
	/**
	 * Transforms a CDA AgeObservationUnit string to a FHIR AgeUnit string.
	 * @param cdaAgeObservationUnit A CDA AgeObservationUnit string
	 * @return A FHIR AgeUnit string
	 */
	String tAgeObservationUnit2AgeUnit(String cdaAgeObservationUnit);
	
	/**
	 * Transforms a CDA AllergyCategoryCode string to a FHIR AllergyIntoleranceCategory.
	 * @param cdaAllergyCategoryCode A CDA AllergyCategoryCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceCategory
	 */
	AllergyIntoleranceCategory tAllergyCategoryCode2AllergyIntoleranceCategory(String cdaAllergyCategoryCode);
	
	/**
	 * Transforms a CDA CriticalityObservation's value's code string to a FHIR AllergyIntoleranceCriticality.
	 * @param cdaCriticalityObservationValue A CDA CriticalityObservation's value's code string
	 * @return A value from the FHIR valueset AllergyIntolerancecriticalityEnum
	 */
	AllergyIntoleranceCriticality tCriticalityObservationValue2AllergyIntoleranceCriticality(String cdaCriticalityObservationValue);
	
	/**
	 * Transforms a CDA EncounterCode string to a FHIR Coding.
	 * @param cdaEncounterCode A CDA EncounterCode string
	 * @return A value from the FHIR valueset Coding
	 */
	Coding tEncounterCode2EncounterCode(String cdaEncounterCode);
	
	/**
	 * Transforms a CDA EntityClassRoot vocable to a value from the FHIR valueset GroupType.
	 * @param cdaEntityClassRoot A CDA EntityClassRoot vocable
	 * @return A value from the FHIR valueset GroupType
	 */
	GroupType tEntityClassRoot2GroupType(EntityClassRoot cdaEntityClassRoot);
	
	/**
	 * Transforms a CDA EntityNameUse vocable to a value from the FHIR valueset NameUse.
	 * @param cdaEntityNameUse A CDA EntityNameUse vocable
	 * @return A value from the FHIR valueset NameUse
	 */
	NameUse tEntityNameUse2NameUse(EntityNameUse cdaEntityNameUse);
	
	/**
	 * Transforms a CDA FamilyHistoryOrganizerStatusCode string to a value from the FHIR valueset FamilyHistoryStatus.
	 * @param cdaFamilyHistoryOrganizerStatusCode A CDA FamilyHistoryOrganizerStatusCode string
	 * @return A value from the FHIR valueset FamilyHistoryStatus
	 */
	FamilyHistoryStatus tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatus(String cdaFamilyHistoryOrganizerStatusCode);
	
	/**
	 * Transforms a CDA MaritalStatusCode string to a value from the FHIR valueset Coding.
	 * @param cdaMaritalStatusCode A CDA MaritalStatusCode string
	 * @return A value from the FHIR valueset Coding
	 */
	Coding tMaritalStatusCode2MaritalStatusCode(String cdaMaritalStatusCode);
	
	/**
	 * Transforms a CDA NullFlavor vocable to a FHIR Coding composite datatype which includes the code about DataAbsentReason.
	 * @param cdaNullFlavor A CDA NullFlavor vocable
	 * @return A FHIR Coding composite datatype which includes the code about DataAbsentReason.
	 */
	Coding tNullFlavor2DataAbsentReasonCode(NullFlavor cdaNullFlavor);
	
	/**
	 * Transforms a CDA Observation Interpretation Code to a FHIR CodeableConcept composite datatype which includes the code about Observation Interpretation.
	 * @param cdaObservationInterpretationCode A CDA Observation Interpretation Code
	 * @return A FHIR CodeableConcept composite datatype which includes the code about Observation Interpretation
	 */
	CodeableConcept tObservationInterpretationCode2ObservationInterpretationCode(CD cdaObservationInterpretationCode);
	
	/**
	 * Transforms a CDA ObservationStatusCode string to a value from the FHIR valueset ObservationStatus.
	 * @param cdaObservationStatusCode A CDA ObservationStatusCode string
	 * @return A value from the FHIR valueset ObservationStatus
	 */
	ObservationStatus tObservationStatusCode2ObservationStatus(String cdaObservationStatusCode);
	
	/**
	 * Transforms a CodeSystem string to a URL string.
	 * @param codeSystem a CodeSystem string
	 * @return A URL string
	 */
	String tOid2Url(String codeSystem);
	
	/**
	 * Transforms a CDA ParticipationType vocable to a FHIR Coding composite datatype which includes the code about ParticipationType.
	 * @param cdaParticipationType A CDA ParticipationType vocable
	 * @return A FHIR Coding composite datatype which includes the code about ParticipationType
	 */
	Coding tParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaParticipationType);

	/**
	 * Transforms a CDA PeriodUnit string to a value from the FHIR valueset UnitsOfTime.
	 * @param cdaPeriodUnit A CDA PeriodUnit string
	 * @return A value from the FHIR valueset UnitsOfTime.
	 */
	UnitsOfTime tPeriodUnit2UnitsOfTime(String cdaPeriodUnit);

	/**
	 * Transforms a CDA PostalAddressUse vocable to a value from the FHIR valueset AddressType.
	 * @param cdaPostalAddressUse A CDA PostalAddressUse vocable
	 * @return A value from the FHIR valueset AddressType
	 */
	AddressType tPostalAddressUse2AddressType(PostalAddressUse cdaPostalAddressUse);
	
	/**
	 * Transforms a CDA PostalAddressUse vocable to a value from the FHIR valueset AddressUse.
	 * @param cdaPostalAddressUse A CDA PostalAddressUse vocable
	 * @return A value from the FHIR valueset AddressUse
	 */
	AddressUse tPostalAdressUse2AddressUse(PostalAddressUse cdaPostalAddressUse);
	
	/**
	 * Transforms a CDA ProblemType string to a value from the FHIR valuset Coding.
	 * @param cdaProblemType A CDA ProblemType string
	 * @return A value from the FHIR valuset Coding
	 */
	Coding tProblemType2ConditionCategoryCodes(String cdaProblemType);
	
	/**
	 * Transforms a CDA ResultOrganizer StatusCode string to a value from the FHIR valueset DiagnosticReportStatus
	 * @param cdaResultOrganizerStatusCode A CDA ResultOrganizer StatusCode string
	 * @return A value from the FHIR valueset DiagnosticReportStatus
	 */
	DiagnosticReportStatus tResultOrganizerStatusCode2DiagnosticReportStatus(String cdaResultOrganizerStatusCode);
	/**
	 * Transforms a CDA RoleCode string to a FHIR Coding composite datatype which includes the code about PatientContactRelationship.
	 * @param cdaRoleCode A CDA RoleCode string
	 * @return A FHIR Coding composite datatype which includes the code about PatientContactRelationship
	 */
	Coding tRoleCode2PatientContactRelationshipCode(String cdaRoleCode);
	
	/**
	 * Transforms a CDA SeverityCode string to a value from the FHIR valueset AllergyIntoleranceSeverity.
	 * @param cdaSeverityCode A CDA SeverityCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceSeverity.
	 */
	AllergyIntoleranceSeverity tSeverityCode2AllergyIntoleranceSeverity(String cdaSeverityCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset AllergyIntoleranceVerificationStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset AllergyIntoleranceVerificationStatus
	 */
	AllergyIntoleranceVerificationStatus tStatusCode2AllergyIntoleranceVerificationStatus(String cdaStatusCode);

	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset ConditionVerificationStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset ConditionVerificationStatus
     */
	ConditionVerificationStatus tStatusCode2ConditionVerificationStatus(String cdaStatusCode);

	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset EncounterStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset EncounterStatus
	 */
	EncounterStatus tStatusCode2EncounterStatusEnum(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset MedicationDispenseStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset MedicationDispenseStatus
	 */
	MedicationDispenseStatus tStatusCode2MedicationDispenseStatus(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset MedicationStatementStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset MedicationStatementStatus
	 */
	MedicationStatementStatus tStatusCode2MedicationStatementStatus(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset ImmunizationStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR ImmunizationStatus
	 */
	ImmunizationStatus tStatusCode2ImmunizationStatus(String cdaStatusCode);
	
	/**
	 * Transforms a CDA StatusCode string to a value from the FHIR valueset ProcedureStatus.
	 * @param cdaStatusCode A CDA StatusCode string
	 * @return A value from the FHIR valueset ProcedureStatus
	 */
	ProcedureStatus tStatusCode2ProcedureStatus(String cdaStatusCode);
	
	/**
	 * Transforms a CDA TelecommunicationAddressUse vocable to a value from the FHIR valueset ContactPointUse.
	 * @param cdaTelecommunicationAddressUse A CDA TelecommunicationAddressUse vocable
	 * @return A value from the FHIR valueset ContactPointUse
	 */
	ContactPointUse tTelecommunicationAddressUse2ContactPointUse(TelecommunicationAddressUse cdaTelecommunicationAddressUse);

	/**
	 * Transforms a CDA TelValue string to a value from the FHIR valueset ContactPointSystem.
	 * @param cdaTelValue A CDA TelValue string
	 * @return A value from the FHIR valueset ContactPointSystem.
	 */
	ContactPointSystem tTelValue2ContactPointSystem(String cdaTelValue);
	
	/**
	 * Transforms a CDA ProblemStatus code to a AllergyIntoleranceClinicalStatus
	 * @param code ProblemStatus code
	 * @return A value from the FHIR AllergyIntoleranceClinicalStatus 
	 */
	AllergyIntoleranceClinicalStatus tProblemStatus2AllergyIntoleranceClinicalStatus(String code);

	/**
	 * Transforms a CDA ProblemStatus code to a COnditionClinicalStatus
	 * @param code ProblemStatus code
	 * @return A value from the FHIR ConditionClinicalStatus 
	 */
	ConditionClinicalStatus tProblemStatus2ConditionClinicalStatus(String code);
}