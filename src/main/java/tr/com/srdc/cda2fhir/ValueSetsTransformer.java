package tr.com.srdc.cda2fhir;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.valueset.AddressTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.AllergyIntoleranceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.FamilyHistoryStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.LocationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ProcedureStatusEnum;

public interface ValueSetsTransformer {
	
	AdministrativeGenderEnum AdministrativeGenderCode2AdministrativeGenderEnum( String AdministrativeGenderCode );
	
	String AgeObservationUnit2AgeUnit(String cdaUnit);
	
	GroupTypeEnum EntityClassRoot2GroupTypeEnum( EntityClassRoot entityClassRoot );
	
	NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
	
	FamilyHistoryStatusEnum FamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum( String FamilyHistoryOrganizerStatusCode );
	
	MaritalStatusCodesEnum MaritalStatusCode2MaritalStatusCodesEnum( String maritalStatusCode );
	
	CodingDt NullFlavor2DataAbsentReasonCode( NullFlavor nullFlavor );
	
	ObservationStatusEnum ObservationStatusCode2ObservationStatusEnum( String obsStatusCode );
	
	String oid2Url(String codeSystem);
	
	CodingDt ParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaPT);
	
	AddressTypeEnum PostalAddressUse2AddressTypeEnum( PostalAddressUse postalAddressUse );
	
	AddressUseEnum PostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse);
	
	AllergyIntoleranceStatusEnum StatusCode2AllergyIntoleranceStatusEnum( String status );
	
	EncounterStateEnum StatusCode2EncounterStatusEnum(String status);
	
	MedicationDispenseStatusEnum StatusCode2MedicationDispenseStatusEnum( String status);
	
	MedicationStatementStatusEnum StatusCode2MedicationStatementStatusEnum( String status);
	
	ProcedureStatusEnum StatusCode2ProcedureStatusEnum( String statusCodeString );
	
	ContactPointUseEnum TelecommunicationAddressUse2ContacPointUseEnum( TelecommunicationAddressUse telecommunicationAddressUse );

}
