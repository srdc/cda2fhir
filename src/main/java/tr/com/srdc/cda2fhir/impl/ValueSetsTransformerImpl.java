package tr.com.srdc.cda2fhir.impl;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.valueset.AddressTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationAdministrationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.BoundCodeDt;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ValueSetsTransformerImpl implements ValueSetsTransformer {
	///////////////////
	// new codes starts
	

//	public EncounterClassEnum ActClass2EncounterClassEnum( ActClass actClass ){
//		// Visit https://www.hl7.org/fhir/valueset-encounter-class.html
//		// TODO
//		return null;
//	}
	public MaritalStatusCodesEnum MaritalStatusCode2MaritalStatusCodesEnum( String maritalStatusCode ){
		// Visit https://www.hl7.org/fhir/valueset-marital-status.html
		switch(maritalStatusCode){
		case "A": return MaritalStatusCodesEnum.A;
		case "D": return MaritalStatusCodesEnum.D;
		case "I": return MaritalStatusCodesEnum.I;
		case "L": return MaritalStatusCodesEnum.L;
		case "M": return MaritalStatusCodesEnum.M;
		case "P": return MaritalStatusCodesEnum.P;
		case "S": return MaritalStatusCodesEnum.S;
		case "T": return MaritalStatusCodesEnum.T;
		case "W": return MaritalStatusCodesEnum.W;
		case "UNK":
		case "U": 
		default:
			return MaritalStatusCodesEnum.UNK;
		}
	}
	
	public AdministrativeGenderEnum AdministrativeGenderCode2AdministrativeGenderEnum( String administrativeGenderCode ){
		// Visit https://www.hl7.org/fhir/valueset-administrative-gender.html
		switch (administrativeGenderCode) {
		   case "F": // Female
		   case "f":
		   	return AdministrativeGenderEnum.FEMALE;
		   case "M": // Male
		   case "m":
		   	return AdministrativeGenderEnum.MALE;
		   case "U": // Undifferentiated
		   case "u":
		   case "UN":
		   case "UNK":
		   case "un":
		   case "unk":
		   	return AdministrativeGenderEnum.UNKNOWN;
		   default:
		   	return AdministrativeGenderEnum.UNKNOWN;
		} // end of switch block
	}
	
	public MedicationAdministrationStatusEnum StatusCode2MedicationAdministrationStatusEnum( String status){
		switch( status ){
		case "active": return MedicationAdministrationStatusEnum.IN_PROGRESS;
		case "suspended": return MedicationAdministrationStatusEnum.ON_HOLD;
		case "completed": return MedicationAdministrationStatusEnum.COMPLETED;
		case "nullified": return MedicationAdministrationStatusEnum.ENTERED_IN_ERROR;
		case "stopped": return MedicationAdministrationStatusEnum.STOPPED;
		default: return null;
		}
	}
	
	public MedicationDispenseStatusEnum StatusCode2MedicationDispenseStatusEnum( String status){
		switch( status ){
		case "active": return MedicationDispenseStatusEnum.IN_PROGRESS;
		case "suspended": return MedicationDispenseStatusEnum.ON_HOLD;
		case "completed": return MedicationDispenseStatusEnum.COMPLETED;
		case "nullified": return MedicationDispenseStatusEnum.ENTERED_IN_ERROR;
		case "stopped": return MedicationDispenseStatusEnum.STOPPED;
		default: return null;
		}
	}
	
	// new codes ends
	/////////////////
	
	public NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse){
		
		switch(entityNameUse){
		case C: return NameUseEnum.USUAL;
		// Visit https://www.hl7.org/fhir/valueset-name-use.html
		// Trying: case OR: return NameUseEnum.OFFICIAL;
		// .. T, ANON, OLD, M.
		// However, these cases don't exist
		case P: return NameUseEnum.NICKNAME;
		default: return NameUseEnum.USUAL;
		}
	}
	
	public AddressUseEnum PostalAdressUse2AddressUseEnum(PostalAddressUse postalAddressUse){
		
		switch(postalAddressUse){
			case H: return AddressUseEnum.HOME;
			case WP: return AddressUseEnum.WORK;
			case TMP: return AddressUseEnum.TEMPORARY;
			case BAD: return AddressUseEnum.OLD___INCORRECT;
		default:
			return AddressUseEnum.TEMPORARY;
		}
		
	}
	
	public ContactPointUseEnum TelecommunicationAddressUse2ContacPointUseEnum( TelecommunicationAddressUse telecommunicationAddressUse )
	{
		switch(telecommunicationAddressUse){
		case H: return ContactPointUseEnum.HOME;
		// new code start
		case HP: return ContactPointUseEnum.HOME;
		// new code end
		case WP: return ContactPointUseEnum.WORK;
		case TMP: return ContactPointUseEnum.TEMP;
		case BAD: return ContactPointUseEnum.OLD;
		case MC: return ContactPointUseEnum.MOBILE;
		default:
			return ContactPointUseEnum.TEMP;
		}
			
	}
	public AddressTypeEnum PostalAddressUse2AddressTypeEnum( PostalAddressUse postalAddressUse ){
		switch(postalAddressUse){
		// new code starts: notice that some lines are changed
		case PHYS: return AddressTypeEnum.PHYSICAL;
		case PST: return AddressTypeEnum.POSTAL;
		// new code ends
		default: return null;
		}
	}

	
	
	

}
