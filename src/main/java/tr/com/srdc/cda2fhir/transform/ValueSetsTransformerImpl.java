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

import ca.uhn.fhir.model.dstu2.valueset.*;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;

public class ValueSetsTransformerImpl implements IValueSetsTransformer {

	public AdministrativeGenderEnum tAdministrativeGenderCode2AdministrativeGenderEnum(String cdaAdministrativeGenderCode) {
		switch (cdaAdministrativeGenderCode.toLowerCase()) {
			case "f":
				return AdministrativeGenderEnum.FEMALE;
			case "m":
				return AdministrativeGenderEnum.MALE;
			case "un":
				return AdministrativeGenderEnum.UNKNOWN;
			default:
				return AdministrativeGenderEnum.UNKNOWN;
		}
	}

	public String tAgeObservationUnit2AgeUnit(String cdaAgeObservationUnit) {
		if(cdaAgeObservationUnit == null || cdaAgeObservationUnit.isEmpty())
			return null;
		
		switch(cdaAgeObservationUnit.toLowerCase()) {
			case "a":
				return "Year";
			case "mo":
				return "Month";
			case "wk":
				return "Week";
			case "d":
				return "Day";
			case "h":
				return "Hour";
			case "min":
				return "Minute";
			default:
				return null;
		}
	}

	public AllergyIntoleranceCategoryEnum tAllergyCategoryCode2AllergyIntoleranceCategoryEnum(String cdaAllergyCategoryCode) {
		if(cdaAllergyCategoryCode == null)
			return null;
		switch(cdaAllergyCategoryCode) {
			case "416098002":
			case "59037007":
			case "419511003":
				return AllergyIntoleranceCategoryEnum.MEDICATION;
			case "414285001": 
			case "235719002":
			case "418471000":
				return AllergyIntoleranceCategoryEnum.FOOD;
			case "232347008":
				return AllergyIntoleranceCategoryEnum.ENVIRONMENT;
			case "420134006":
			case "418038007":
			case "419199007": 
				return AllergyIntoleranceCategoryEnum.OTHER;
			default:
				return null;
		}
	}
	
	public AllergyIntoleranceCriticalityEnum tCriticalityObservationValue2AllergyIntoleranceCriticalityEnum(String cdaCriticalityObservationValue) {
		if(cdaCriticalityObservationValue == null || cdaCriticalityObservationValue.isEmpty())
			return null;
		switch(cdaCriticalityObservationValue.toLowerCase()) {
			case "critl": 
				return AllergyIntoleranceCriticalityEnum.LOW_RISK;
			case "crith": 
				return AllergyIntoleranceCriticalityEnum.HIGH_RISK;
			case "critu": 
				return AllergyIntoleranceCriticalityEnum.UNABLE_TO_DETERMINE;
			default: 
				return null;
		}
	}
	
	public EncounterClassEnum tEncounterCode2EncounterClassEnum(String cdaEncounterCode) {
		if(cdaEncounterCode == null)
			return null;
		switch(cdaEncounterCode.toLowerCase()) {
			case "amb": 
			case "ambulatory":
				return EncounterClassEnum.AMBULATORY;
			case "out": 
			case "outpatient":
					return EncounterClassEnum.OUTPATIENT;
			case "in":
			case "inp":
			case "inpatient":
					return EncounterClassEnum.INPATIENT;
			case "day":
			case "daytime":
				return EncounterClassEnum.DAYTIME;
			case "em":
			case "eme":
			case "emergency":
				return EncounterClassEnum.EMERGENCY;
			case "hom":
			case "home":
				return EncounterClassEnum.HOME;
			case "vir":
			case "virtual":
				return EncounterClassEnum.VIRTUAL;
			case "fie":
			case "field":
				return EncounterClassEnum.FIELD;
			case "other":
			case "oth":
				return EncounterClassEnum.OTHER;
			default:
				return null;
				
		}
	}
	
	public GroupTypeEnum tEntityClassRoot2GroupTypeEnum(EntityClassRoot cdaEntityClassRoot) {
		switch(cdaEntityClassRoot) {
			case PSN:
				return GroupTypeEnum.PERSON;
			case ANM:
				return GroupTypeEnum.ANIMAL;
			case DEV:
				return GroupTypeEnum.DEVICE;
			case MMAT:
				return GroupTypeEnum.MEDICATION;
			default:
				return null;
		}
	}

	public NameUseEnum tEntityNameUse2NameUseEnum(EntityNameUse cdaEntityNameUse) {
		switch(cdaEntityNameUse) {
			case C: return NameUseEnum.USUAL;
			case P: return NameUseEnum.NICKNAME;
			default: return NameUseEnum.USUAL;
		}
	}

	public FamilyHistoryStatusEnum tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum(String cdaFamilyHistoryOrganizerStatusCode) {
		switch(cdaFamilyHistoryOrganizerStatusCode.toLowerCase()){
		case "completed":
			return FamilyHistoryStatusEnum.COMPLETED;
		case "error":
			return FamilyHistoryStatusEnum.ENTERED_IN_ERROR;
		case "un":
			return FamilyHistoryStatusEnum.HEALTH_UNKNOWN;
		case "part":
			return FamilyHistoryStatusEnum.PARTIAL;
		default:
			return null;
		}
	}

	public MaritalStatusCodesEnum tMaritalStatusCode2MaritalStatusCodesEnum(String cdaMaritalStatusCode) {
		switch(cdaMaritalStatusCode.toUpperCase()) {
			case "A": return MaritalStatusCodesEnum.A;
			case "D": return MaritalStatusCodesEnum.D;
			case "I": return MaritalStatusCodesEnum.I;
			case "L": return MaritalStatusCodesEnum.L;
			case "M": return MaritalStatusCodesEnum.M;
			case "P": return MaritalStatusCodesEnum.P;
			case "S": return MaritalStatusCodesEnum.S;
			case "T": return MaritalStatusCodesEnum.T;
			case "W": return MaritalStatusCodesEnum.W;
			case "UN":
			default:
				return MaritalStatusCodesEnum.UNK;
		}
	}

	public CodingDt tNullFlavor2DataAbsentReasonCode(NullFlavor cdaNullFlavor) {
		CodingDt DataAbsentReasonCode = new CodingDt();
		String code = null;
		String display = null;

		switch(cdaNullFlavor) {
			case UNK:
				code = "unknown"; display = "Unkown"; break;
			case ASKU:
				code = "asked"; display = "Asked"; break;
			case MSK:
				code = "masked"; display = "Masked"; break;
			case NA:
				code = "unsupported"; display= "Unsupported"; break;
			case NASK:
				code= "not-asked"; display = "Not Asked"; break;
			case NAV:
				code = "temp"; display = "Temp"; break;
			case NI:
				code = "error"; display = "Error"; break;
			case NINF:
				code = "NaN"; display = "Not a Number"; break;
			case NP:
				code = "unknown"; display = "Unkown"; break;
			case OTH:
				code = "error"; display = "Error"; break;
			case PINF:
				code = "NaN"; display = "Not a Number"; break;
			case TRC:
				code = "NaN"; display = "Not a Number"; break;
			default:
				break;
		}

		DataAbsentReasonCode.setSystem("http://hl7.org/fhir/data-absent-reason");
		DataAbsentReasonCode.setCode(code);
		DataAbsentReasonCode.setDisplay(display);

		return DataAbsentReasonCode;
	}

	public CodeableConceptDt ObservationInterpretationCode2ObservationInterpretationCode(CD cdaObservationInterpretationCode) {
		if(cdaObservationInterpretationCode == null)
			return null;
		CodingDt obsIntCode = new CodingDt();
		obsIntCode.setSystem("http://hl7.org/fhir/v2/0078");
		
		String code = null, display = null;
		
		// init code and display with the CDA incomings
		if(cdaObservationInterpretationCode.getCode() != null) 
			code = cdaObservationInterpretationCode.getCode();
		if(cdaObservationInterpretationCode.getDisplayName() != null)
			display = cdaObservationInterpretationCode.getDisplayName();
		
		// if a different code is found, change it
		switch(cdaObservationInterpretationCode.getCode().toUpperCase()) {
			case "AC":
				code = "IE"; display = "Insufficient evidence"; break;
			case "EX":
				code = "IND"; display = "Indeterminate"; break;
			case "HX":
				code = "H"; display = "High"; break;
			case "LX":
				code = "L"; display = "Low"; break;
			case "QCF":
				code = "IND"; display = "Indeterminate"; break;
			case "TOX":
				code = "IND"; display = "Indeterminate"; break;
			case "CAR":
				code = "DET"; display = "Detected"; break;
			case "H>":
				code = "HU"; display = "Very high"; break;
			case "L<":
				code = "LU"; display = "Very low"; break;
			default:
				break;
		}
		
		obsIntCode.setCode(code);
		obsIntCode.setDisplay(display);
		return new CodeableConceptDt().addCoding(obsIntCode);
	}
	
	public ObservationStatusEnum tObservationStatusCode2ObservationStatusEnum(String cdaObservationStatusCode) {
		switch(cdaObservationStatusCode.toLowerCase()) {
		// TODO: https://www.hl7.org/fhir/valueset-observation-status.html and pdf page 476
		// Check the following mapping
			case "new":	
			case "held":
				return ObservationStatusEnum.REGISTERED;
			case "normal":
			case "active":
				return ObservationStatusEnum.PRELIMINARY;
			case "completed":
				return ObservationStatusEnum.FINAL;
			case "error":
				return ObservationStatusEnum.ENTERED_IN_ERROR;
			case "cancelled":
			case "aborted":
			case "nullified":
			case "suspended":
				return ObservationStatusEnum.CANCELLED;
			case "obsolete":
			default:
				return ObservationStatusEnum.UNKNOWN_STATUS;
		}
	}

	public String tOid2Url(String codeSystem) {
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

	public CodingDt tParticipationType2ParticipationTypeCode(org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType cdaParticipationType) {
		CodingDt fhirParticipationType = new CodingDt();
		fhirParticipationType.setSystem("http://hl7.org/fhir/v3/ParticipationType");
		String code = null;
		String display = null;

		switch(cdaParticipationType) {
			case PRF: code = "PRF"; display = "performer";
					break;
			case SBJ: code = "SBJ"; display ="subject";
					break;
			case ADM: code = "ADM"; display = "admitter";
					break;
			case ATND: code = "ATND"; display = "attender";
					break;
			case AUT: code = "AUT"; display = "author";
					break;
			case AUTHEN: code = "AUTHEN"; display = "authenticator";
				break;
			case BBY: code = "BBY"; display = "baby";
				break;
			case BEN: code = "BEN"; display = "beneficiary";
				break;
			case CALLBCK: code = "CALLBCK"; display = "callback contact";
				break;
			case CON: code = "CON"; display = "consultant";
				break;
			case COV: code = "COV"; display = "coverage target";
				break;
			case CSM: code = "CSM"; display = "consumable";
				break;
			case CST: code = "CST"; display = "custodian";
				break;
			case DEV: code = "DEV"; display = "device";
				break;
			case DIR: code = "DIR"; display = "direct target";
				break;
			case DIS: code = "DIS"; display = "discharger";
				break;
			case DIST: code = "DIST"; display = "distributor";
				break;
			case DON: code = "DON"; display = "donor";
				break;
			case DST: code = "DST"; display = "destination";
				break;
			case ELOC: code = "ELOC"; display = "entry location";
				break;
			case ENT: code = "ENT"; display = " data entry person";
				break;
			case ESC: code = "ESC"; display = "escort";
				break;
			case HLD: code = "HLD"; display = "holder";
				break;
			case IND: code = "IND"; display = "indirect target";
				break;
			case INF: code = "INF"; display = "informant";
				break;
			case IRCP: code = "IRCP"; display = "information recipient";
				break;
			case LA: code = "LA"; display = "legal authenticator";
				break;
			case LOC: code = "LOC"; display = "location";
				break;
			case NOT: code = "NOT"; display = "ugent notification contact";
				break;
			case NRD: code = "NRD"; display = "non-reuseable device";
				break;
			case ORG: code = "ORG"; display = "origin";
				break;
			case PPRF: code = "PPRF"; display = "primary performer";
				break;
			case PRCP: code = "PRCP"; display = "primary information recipient";
				break;
			case PRD: code = "PRD"; display = "product";
				break;
			case RCT: code = "RCT"; display = "record target";
				break;
			case RCV: code = "RCV"; display = "receiver";
				break;
			case RDV: code = "RDV"; display = "reusable device";
				break;
			case REF: code = "REF"; display = "referrer";
				break;
			case REFB: code = "REFB"; display = "Referred By";
				break;
			case REFT: code = "REFT"; display = "Referred to";
				break;
			case RESP: code = "RESP"; display = "responsible party";
				break;
			case RML: code = "RML"; display = "remote";
				break;
			case SPC: code = "SPC"; display = "specimen";
				break;
			case SPRF: code = "SPRF"; display = "secondary performer";
				break;
			case TRC: code = "TRC"; display = "tracker";
				break;
			case VIA: code = "VIA"; display = "via";
				break;
			case VRF: code = "VRF"; display = "verifier";
				break;
			case WIT: code = "WIT"; display = "witness";
				break;
			default:
				break;
		}
		if(code != null && display != null) {
			fhirParticipationType.setCode(code);
			fhirParticipationType.setDisplay(display);
		}
		return fhirParticipationType;
	}

	public UnitsOfTimeEnum tPeriodUnit2UnitsOfTimeEnum(String cdaPeriodUnit) {
		switch(cdaPeriodUnit.toLowerCase()) {
			case "a":
				return UnitsOfTimeEnum.A;
			case "d":
				return UnitsOfTimeEnum.D;
			case "h":
				return UnitsOfTimeEnum.H;
			case "min":
				return UnitsOfTimeEnum.MIN;
			case "mo":
				return UnitsOfTimeEnum.MO;
			case "s":
				return UnitsOfTimeEnum.S;
			case "wk":
				return UnitsOfTimeEnum.WK;
			default:
				return null;
		}
	}

	public ConditionCategoryCodesEnum tProblemType2ConditionCategoryCodesEnum(String cdaProblemType) {
		if(cdaProblemType == null)
			return null;
		switch(cdaProblemType) {
			case "248536006":
			case "373930000":
			case "404684003": 
			case "75321-0":
			case "75312-9":
				return ConditionCategoryCodesEnum.FINDING;
			case "409586006": 
			case "75322-8":
			case "75313-7":
				return ConditionCategoryCodesEnum.COMPLAINT;
			case "282291009": 
			case "29308-4":
			case "75314-5":
			case "55607006": // problem
			case "75318-6": 
			case "75323-6": // condition
			case "75315-2":
			case "64572001": 
				return ConditionCategoryCodesEnum.DIAGNOSIS;
			case "418799008": 
			case "75325-1":
			case "75317-8":
				return ConditionCategoryCodesEnum.SYMPTOM;
				
			default: 
				return null;
		}
	}
	
	public AddressTypeEnum tPostalAddressUse2AddressTypeEnum(PostalAddressUse cdaPostalAddressUse) {
		switch(cdaPostalAddressUse) {
			case PHYS:
				return AddressTypeEnum.PHYSICAL;
			case PST:
				return AddressTypeEnum.POSTAL;
			default:
				return null;
		}
	}

	public AddressUseEnum tPostalAdressUse2AddressUseEnum(PostalAddressUse cdaPostalAddressUse) {
		switch(cdaPostalAddressUse) {
			case HP:
			case H:
				return AddressUseEnum.HOME;
			case WP:
				return AddressUseEnum.WORK;
			case TMP:
				return AddressUseEnum.TEMPORARY;
			case BAD:
				return AddressUseEnum.OLD___INCORRECT;
			default:
				return AddressUseEnum.TEMPORARY;
		}
	}

	public DiagnosticReportStatusEnum tResultOrganizerStatusCode2DiagnosticReportStatusEnum(String cdaResultOrganizerStatusCode) {
		if(cdaResultOrganizerStatusCode == null)
			return null;
		
		switch(cdaResultOrganizerStatusCode.toLowerCase()) {
			case "aborted":
				return DiagnosticReportStatusEnum.CANCELLED;
			case "active":
				return DiagnosticReportStatusEnum.PARTIAL;
			case "cancelled":
				return DiagnosticReportStatusEnum.CANCELLED;
			case "completed":
				return DiagnosticReportStatusEnum.FINAL;
			case "held":
				return DiagnosticReportStatusEnum.REGISTERED;
			case "suspended":
				return DiagnosticReportStatusEnum.ENTERED_IN_ERROR;
			default:
				return null;
		}
	}
	
	public CodingDt tRoleCode2PatientContactRelationshipCode(String cdaRoleCode) {
		if(cdaRoleCode == null)
			return null;
		
		CodingDt fhirPatientContactRelationshipCode = new CodingDt();
		fhirPatientContactRelationshipCode.setSystem("http://hl7.org/fhir/patient-contact-relationship");
		String code = null;
		String display = null;
		
		switch(cdaRoleCode.toLowerCase()) {
			case "econ": // emergency contact
				code="emergency"; display = "Emergency"; break;
			case "ext": // extended family member
			case "fammemb": // family member
				code = "family"; display = "Family"; break;
			case "guard": // guardian
				code = "guardian"; display = "Guardian"; break;
			case "frnd": // friend
				code = "friend"; display = "Friend"; break;
			case "sps": // spouse
			case "dompart": // domestic partner
			case "husb": // husband
			case "wife": // wife
				code = "partner"; display = "Partner"; break;
			case "work": 
				code = "work"; display = "Work"; break;
			case "gt": 
				code = "guarantor"; display = "Guarantor"; break;
			case "prn": // parent
			case "fth": // father
			case "mth": // mother
			case "nprn": // natural parent
			case "nfth": // natural father
			case "nmth": // natural mother
			case "prinlaw": // parent in-law
			case "fthinlaw": // father in-law
			case "mthinlaw": // mother in-law
			case "stpprn": // step parent
			case "stpfth": // stepfather
			case "stpmth": // stepmother
				code = "parent"; display = "Parent"; break;
			case "powatt":
				code = "agent"; display = "Agent"; break;
			default:
				return null;
		}
		fhirPatientContactRelationshipCode.setCode(code);
		fhirPatientContactRelationshipCode.setDisplay(display);
		return fhirPatientContactRelationshipCode;
	}
	
	public AllergyIntoleranceSeverityEnum tSeverityCode2AllergyIntoleranceSeverityEnum(String cdaSeverityCode) {
		if(cdaSeverityCode == null)
			return null;
		switch(cdaSeverityCode) {
			case "255604002": 
				return AllergyIntoleranceSeverityEnum.MILD;
			case "371923003": 
				return AllergyIntoleranceSeverityEnum.MILD;
			case "6736007": 
				return AllergyIntoleranceSeverityEnum.MODERATE;
			case "371924009": 
				return AllergyIntoleranceSeverityEnum.MODERATE;
			case "24484000": 
				return AllergyIntoleranceSeverityEnum.SEVERE;
			case "399166001": 
				return AllergyIntoleranceSeverityEnum.SEVERE;
			default: 
				return null;
		}
	}
	
	public AllergyIntoleranceStatusEnum tStatusCode2AllergyIntoleranceStatusEnum(String cdaStatusCode) {
		switch(cdaStatusCode.toLowerCase()) {
			case "active":
				return AllergyIntoleranceStatusEnum.ACTIVE;
			case "nullified":
			case "error":
				return AllergyIntoleranceStatusEnum.ENTERED_IN_ERROR;
			case "confirmed":
				return AllergyIntoleranceStatusEnum.CONFIRMED;
			case "unconfirmed":
				return AllergyIntoleranceStatusEnum.UNCONFIRMED;
			case "refuted":
				return AllergyIntoleranceStatusEnum.REFUTED;
			case "inactive":
				return AllergyIntoleranceStatusEnum.INACTIVE;
			case "resolved":
				return AllergyIntoleranceStatusEnum.RESOLVED;
			default:
				return null;
		}
	}

	public ConditionClinicalStatusCodesEnum tStatusCode2ConditionClinicalStatusCodesEnum(String cdaStatusCode) {
		switch (cdaStatusCode.toLowerCase()) {
			// semantically not the same, but at least outcome-wise it is similar
			case "aborted":
				return ConditionClinicalStatusCodesEnum.RESOLVED;
			case "active":
				return ConditionClinicalStatusCodesEnum.ACTIVE;
			case "completed":
				return ConditionClinicalStatusCodesEnum.RESOLVED;
			case "suspended":
				return ConditionClinicalStatusCodesEnum.REMISSION;
			default:
				return null;
		}
	}

	public EncounterStateEnum tStatusCode2EncounterStatusEnum(String cdaStatusCode) {
		switch(cdaStatusCode.toLowerCase()) {
			case "in-progress":
			case "active":
				return EncounterStateEnum.IN_PROGRESS;
			case "onleave":
				return EncounterStateEnum.ON_LEAVE;
			case "finished":
			case "completed":
				return EncounterStateEnum.FINISHED;
			case "cancelled":
				return EncounterStateEnum.CANCELLED;
			case "planned":
				return EncounterStateEnum.PLANNED;
			case "arrived":
				return EncounterStateEnum.ARRIVED;
			default:
				return null;
		}
	}

	public MedicationDispenseStatusEnum tStatusCode2MedicationDispenseStatusEnum(String cdaStatusCode) {
		switch(cdaStatusCode.toLowerCase()) {
			case "active":
			case "in-progress":
			case "inprogress":
				return MedicationDispenseStatusEnum.IN_PROGRESS;
			case "on-hold":
			case "onhold":
			case "suspended":
				return MedicationDispenseStatusEnum.ON_HOLD;
			case "completed":
				return MedicationDispenseStatusEnum.COMPLETED;
			case "nullified":
			case "error":
			case "entered-in-error":
				return MedicationDispenseStatusEnum.ENTERED_IN_ERROR;
			case "stopped":
				return MedicationDispenseStatusEnum.STOPPED;
			default:
				return null;
		}
	}

	public MedicationStatementStatusEnum tStatusCode2MedicationStatementStatusEnum(String cdaStatusCode) {
		switch(cdaStatusCode.toLowerCase()) {
			case "active":
				return MedicationStatementStatusEnum.ACTIVE;
			case "intended":
				return MedicationStatementStatusEnum.INTENDED;
			case "completed":
				return MedicationStatementStatusEnum.COMPLETED;
			case "nullified":
				return MedicationStatementStatusEnum.ENTERED_IN_ERROR;
			default:
				return null;
		}
	}

	public ProcedureStatusEnum tStatusCode2ProcedureStatusEnum(String cdaStatusCode) {
		switch(cdaStatusCode.toLowerCase()) {
			case "active":
				return ProcedureStatusEnum.IN_PROGRESS;
			case "completed":
				return ProcedureStatusEnum.COMPLETED;
			case "aborted":
			case "aboted":
				return ProcedureStatusEnum.ABOTED;
			case "error":
				return ProcedureStatusEnum.ENTERED_IN_ERROR;
			default:
				return null;
		}
	}

	public ContactPointUseEnum tTelecommunicationAddressUse2ContactPointUseEnum(TelecommunicationAddressUse cdaTelecommunicationAddressUse) {
		switch(cdaTelecommunicationAddressUse) {
			case H:
			case HP:
				return ContactPointUseEnum.HOME;
			case WP:
				return ContactPointUseEnum.WORK;
			case TMP:
				return ContactPointUseEnum.TEMP;
			case BAD:
				return ContactPointUseEnum.OLD;
			case MC:
				return ContactPointUseEnum.MOBILE;
			default:
				return ContactPointUseEnum.TEMP;
		}

	}

	public ContactPointSystemEnum tTelValue2ContactPointSystemEnum(String cdaTelValue) {
		if(cdaTelValue == null)
			return null;
		
		switch(cdaTelValue.toLowerCase()) {
			case "phone":
			case "tel":
				return ContactPointSystemEnum.PHONE;
			case "email":
				return ContactPointSystemEnum.EMAIL;
			case "fax":
				return ContactPointSystemEnum.FAX;
			case "http":
			case "https":
				return ContactPointSystemEnum.URL;
			default:
				return null;
		}
	}
}