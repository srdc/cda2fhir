package tr.com.srdc.cda2fhir.impl;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.valueset.AddressTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ValueSetsTransformerImpl implements ValueSetsTransformer {
	
	public NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse){
		
		switch(entityNameUse){
		case C: return NameUseEnum.USUAL;
		// TODO: Visit https://www.hl7.org/fhir/valueset-name-use.html
		// Trying: case OR: return NameUseEnum.OFFICIAL;
		// .. T, ANON, OLD, M.
		// However, these cases don't exist
		case P: return NameUseEnum.NICKNAME;
		default: return NameUseEnum.TEMP;
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
		case PHYS: return AddressTypeEnum.POSTAL;
		// TODO: It maps PHYS to postal, PST to physical. Maybe wrong?
		case PST: return AddressTypeEnum.PHYSICAL;
		// TODO: Check if it is OK to set default as it is
		default: return AddressTypeEnum.POSTAL___PHYSICAL;
		}
	}
	
	

}
