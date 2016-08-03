package tr.com.srdc.cda2fhir;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.valueset.AddressTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationAdministrationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;

public interface ValueSetsTransformer {
	
	////////////////////
	// new codes starts
	
	// necip start
	GroupTypeEnum EntityClassRoot2GroupTypeEnum( EntityClassRoot entityClassRoot );
	MaritalStatusCodesEnum MaritalStatusCode2MaritalStatusCodesEnum( String maritalStatusCode );
	AdministrativeGenderEnum AdministrativeGenderCode2AdministrativeGenderEnum( String AdministrativeGenderCode );
	// necip end
	
	// ismail start
	MedicationAdministrationStatusEnum StatusCode2MedicationAdministrationStatusEnum( String status);
	MedicationDispenseStatusEnum StatusCode2MedicationDispenseStatusEnum( String status);
	// ismail end
	
	// new codes ends
	/////////////////
	
	NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
	AddressUseEnum PostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse);
	ContactPointUseEnum TelecommunicationAddressUse2ContacPointUseEnum( TelecommunicationAddressUse telecommunicationAddressUse );
	AddressTypeEnum PostalAddressUse2AddressTypeEnum( PostalAddressUse postalAddressUse );
	
	
}
