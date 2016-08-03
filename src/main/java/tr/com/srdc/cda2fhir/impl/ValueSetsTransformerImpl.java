package tr.com.srdc.cda2fhir.impl;

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
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ValueSetsTransformerImpl implements ValueSetsTransformer {
	
	public String oid2Url(String codeSystem){
		String system = null;
		switch (codeSystem) {
	        case "2.16.840.1.113883.6.96":
	            system = "http://snomed.info/sct";
	            break;
	        case "2.16.840.1.113883.6.88":
	            system = "http://www.nlm.nih.gov/research/umls/rxnorm";
	            break;
	        case "2.16.840.1.113883.6.1":
	            system = "http://loinc.org";
	            break;
	        case "2.16.840.1.113883.6.8":
	            system = "http://unitsofmeasure.org";
	            break;
	        case "2.16.840.1.113883.3.26.1.2":
	            system = "http://ncimeta.nci.nih.gov";
	            break;
	        case "2.16.840.1.113883.6.12":
	            system = "http://www.ama-assn.org/go/cpt";
	            break;
	        case "2.16.840.1.113883.6.209":
	            system = "http://hl7.org/fhir/ndfrt";
	            break;
	        case "2.16.840.1.113883.4.9":
	            system = "http://fdasis.nlm.nih.gov";
	            break;
	        case "2.16.840.1.113883.12.292":
	            system = "http://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=cvx";
	            break;
	        case "1.0.3166.1.2.2":
	            system = "urn:iso:std:iso:3166";
	            break;
	        case "2.16.840.1.113883.6.301.5":
	            system = "http://www.nubc.org/patient-discharge";
	            break;
	        case "2.16.840.1.113883.6.256":
	            system = "http://www.radlex.org";
	            break;
	        case "2.16.840.1.113883.6.3":
	            system = "http://hl7.org/fhir/sid/icd-10";
	            break;
	        case "2.16.840.1.113883.6.4":
	            system = "http://www.icd10data.com/icd10pcs";
	            break;
	        case "2.16.840.1.113883.6.42":
	            system = "http://hl7.org/fhir/sid/icd-9";
	            break;
	        case "2.16.840.1.113883.6.73":
	            system = "http://www.whocc.no/atc";
	            break;
	        case "2.16.840.1.113883.6.24":
	            system = "urn:std:iso:11073:10101";
	            break;
	        case "1.2.840.10008.2.16.4":
	            system = "http://nema.org/dicom/dicm";
	            break;
	        case "2.16.840.1.113883.6.281":
	            system = "http://www.genenames.org";
	            break;
	        case "2.16.840.1.113883.6.280":
	            system = "http://www.ncbi.nlm.nih.gov/nuccore";
	            break;
	        case "2.16.840.1.113883.6.282":
	            system = "http://www.hgvs.org/mutnomen";
	            break;
	        case "2.16.840.1.113883.6.284":
	            system = "http://www.ncbi.nlm.nih.gov/projects/SNP";
	            break;
	        case "2.16.840.1.113883.3.912":
	            system = "http://cancer.sanger.ac.uk/cancergenome/projects/cosmic";
	            break;
	        case "2.16.840.1.113883.6.283":
	            system = "http://www.hgvs.org/mutnomen";
	            break;
	        case "2.16.840.1.113883.6.174":
	            system = "http://www.omim.org";
	            break;
	        case "2.16.840.1.113883.13.191":
	            system = "http://www.ncbi.nlm.nih.gov/pubmed";
	            break;
	        case "2.16.840.1.113883.3.913":
	            system = "http://www.pharmgkb.org";
	            break;
	        case "2.16.840.1.113883.3.1077":
	            system = "http://clinicaltrials.gov";
	            break;
	        default:
	            system = "urn:oid:" + codeSystem;
	            break;
        }
		return system;
	}
	
	public GroupTypeEnum EntityClassRoot2GroupTypeEnum( EntityClassRoot entityClassRoot ){
		switch(entityClassRoot){
			case PSN: return GroupTypeEnum.PERSON;
			case ANM: return GroupTypeEnum.ANIMAL;
			case DEV: return GroupTypeEnum.DEVICE;
			case MMAT: return GroupTypeEnum.MEDICATION;
			default: return null;
		}
	}
	
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
			case PHYS: return AddressTypeEnum.PHYSICAL;
			case PST: return AddressTypeEnum.POSTAL;
			default: return null;
		}
	}

	
	
	

}
