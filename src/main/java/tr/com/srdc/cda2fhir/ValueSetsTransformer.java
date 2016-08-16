package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.valueset.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;

public interface ValueSetsTransformer {
	
	AdministrativeGenderEnum AdministrativeGenderCode2AdministrativeGenderEnum( String AdministrativeGenderCode );
	
	String AgeObservationUnit2AgeUnit(String cdaUnit);
	
	AllergyIntoleranceCategoryEnum AllergyCategoryCode2AllergyIntoleranceCategoryEnum(String allergyCategoryCode);
	
	EncounterClassEnum EncounterCode2EncounterClassEnum(String encounterCode);
	
	GroupTypeEnum EntityClassRoot2GroupTypeEnum( EntityClassRoot entityClassRoot );
	
	NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
	
	FamilyHistoryStatusEnum FamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum( String FamilyHistoryOrganizerStatusCode );
	
	MaritalStatusCodesEnum MaritalStatusCode2MaritalStatusCodesEnum( String maritalStatusCode );
	
	CodingDt NullFlavor2DataAbsentReasonCode( NullFlavor nullFlavor );
	
	ObservationStatusEnum ObservationStatusCode2ObservationStatusEnum( String obsStatusCode );
	
	String oid2Url(String codeSystem);
	
	CodingDt ParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaPT);

	UnitsOfTimeEnum PeriodUnit2UnitsOfTimeEnum(String periodUnit);

	ConditionCategoryCodesEnum ProblemType2ConditionCategoryCodesEnum(String problemType);
	
	AddressTypeEnum PostalAddressUse2AddressTypeEnum( PostalAddressUse postalAddressUse );
	
	AddressUseEnum PostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse);
	
	AllergyIntoleranceSeverityEnum SeverityCode2AllergyIntoleranceSeverityEnum(String severityCode);
	
	AllergyIntoleranceStatusEnum StatusCode2AllergyIntoleranceStatusEnum( String status );
	
	EncounterStateEnum StatusCode2EncounterStatusEnum(String status);
	
	MedicationDispenseStatusEnum StatusCode2MedicationDispenseStatusEnum( String status);
	
	MedicationStatementStatusEnum StatusCode2MedicationStatementStatusEnum( String status);
	
	ProcedureStatusEnum StatusCode2ProcedureStatusEnum( String statusCodeString );
	
	ContactPointUseEnum TelecommunicationAddressUse2ContacPointUseEnum( TelecommunicationAddressUse telecommunicationAddressUse );

}