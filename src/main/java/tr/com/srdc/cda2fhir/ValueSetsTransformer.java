package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.valueset.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;

public interface ValueSetsTransformer {
	
	AdministrativeGenderEnum tAdministrativeGenderCode2AdministrativeGenderEnum(String AdministrativeGenderCode);
	
	String tAgeObservationUnit2AgeUnit(String cdaUnit);
	
	AllergyIntoleranceCategoryEnum tAllergyCategoryCode2AllergyIntoleranceCategoryEnum(String allergyCategoryCode);
	
	EncounterClassEnum tEncounterCode2EncounterClassEnum(String encounterCode);
	
	GroupTypeEnum tEntityClassRoot2GroupTypeEnum(EntityClassRoot entityClassRoot);
	
	NameUseEnum tEntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
	
	FamilyHistoryStatusEnum tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum(String FamilyHistoryOrganizerStatusCode);
	
	MaritalStatusCodesEnum tMaritalStatusCode2MaritalStatusCodesEnum(String maritalStatusCode);
	
	CodingDt tNullFlavor2DataAbsentReasonCode(NullFlavor nullFlavor);
	
	ObservationStatusEnum tObservationStatusCode2ObservationStatusEnum(String obsStatusCode);
	
	String tOid2Url(String codeSystem);
	
	CodingDt tParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaPT);

	UnitsOfTimeEnum tPeriodUnit2UnitsOfTimeEnum(String periodUnit);

	ConditionCategoryCodesEnum tProblemType2ConditionCategoryCodesEnum(String problemType);
	
	AddressTypeEnum tPostalAddressUse2AddressTypeEnum(PostalAddressUse postalAddressUse);
	
	AddressUseEnum tPostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse);
	
	AllergyIntoleranceSeverityEnum tSeverityCode2AllergyIntoleranceSeverityEnum(String severityCode);
	
	AllergyIntoleranceStatusEnum tStatusCode2AllergyIntoleranceStatusEnum(String status);
	
	EncounterStateEnum tStatusCode2EncounterStatusEnum(String status);
	
	MedicationDispenseStatusEnum tStatusCode2MedicationDispenseStatusEnum(String status);
	
	MedicationStatementStatusEnum tStatusCode2MedicationStatementStatusEnum(String status);
	
	ProcedureStatusEnum tStatusCode2ProcedureStatusEnum(String statusCodeString);
	
	ContactPointUseEnum tTelecommunicationAddressUse2ContacPointUseEnum(TelecommunicationAddressUse telecommunicationAddressUse);

	ContactPointSystemEnum tTelValue2ContactPointSystemEnum(String telValue);
}