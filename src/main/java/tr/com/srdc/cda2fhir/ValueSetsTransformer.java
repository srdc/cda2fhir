package tr.com.srdc.cda2fhir;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.valueset.AddressTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.FamilyHistoryStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.LocationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationAdministrationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.ProcedureStatusEnum;

public interface ValueSetsTransformer {
	
	String oid2Url(String codeSystem);
	
	// necip start
	GroupTypeEnum EntityClassRoot2GroupTypeEnum( EntityClassRoot entityClassRoot );
	MaritalStatusCodesEnum MaritalStatusCode2MaritalStatusCodesEnum( String maritalStatusCode );
	AdministrativeGenderEnum AdministrativeGenderCode2AdministrativeGenderEnum( String AdministrativeGenderCode );
	ProcedureStatusEnum StatusCode2ProcedureStatusEnum( String statusCodeString );
	CodingDt ParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaPT);
	FamilyHistoryStatusEnum FamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum( String FamilyHistoryOrganizerStatusCode );
	// necip end
	
	// ismail start
	MedicationStatementStatusEnum StatusCode2MedicationStatementStatusEnum( String status);
	MedicationDispenseStatusEnum StatusCode2MedicationDispenseStatusEnum( String status);
	LocationStatusEnum StatusCode2LocationStatusEnum( String status );
	// ismail end
	
	NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
	AddressUseEnum PostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse);
	ContactPointUseEnum TelecommunicationAddressUse2ContacPointUseEnum( TelecommunicationAddressUse telecommunicationAddressUse );
	AddressTypeEnum PostalAddressUse2AddressTypeEnum( PostalAddressUse postalAddressUse );
	
	
}
