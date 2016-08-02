package tr.com.srdc.cda2fhir.impl;

import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.rim.ActRelationship;
import org.openhealthtools.mdht.uml.hl7.rim.Participation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipType;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.primitive.IdDt;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ResourceTransformerImplNecip {
	
	private static int idHolder = 0;
	private static int getUniqueId(){
		return idHolder++;
	}
	
	public 	ca.uhn.fhir.model.dstu2.resource.Procedure Procedure2Procedure(org.openhealthtools.mdht.uml.cda.consol.Procedure cdaPr){
		
		// cdaPr: Procedure of type CDA
		// fhirPr: Procedure of type FHIR
		// https://www.hl7.org/fhir/daf/procedure-daf.html
		// https://www.hl7.org/fhir/procedure-mappings.html
		
		if( cdaPr == null || cdaPr.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Procedure fhirPr = new ca.uhn.fhir.model.dstu2.resource.Procedure();
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			ValueSetsTransformer vst = new ValueSetsTransformerImpl();
			
			// TODO: Procedure[moodCode=EVN]. Should we check moodCode?
			
			// identifier <-> id
			if( cdaPr.getIds() != null && !cdaPr.getIds().isEmpty() ){
				for( II id : cdaPr.getIds() ){
					if( id == null || id.isSetNullFlavor() ) continue;
					else{
						fhirPr.addIdentifier( dtt.II2Identifier(id) );
					}
				}
			}
			
			
			// subject <-> participation
			if( cdaPr.getParticipations() != null && !cdaPr.getParticipations().isEmpty() ){
				for( Participation participation : cdaPr.getParticipations()  ){
					if( participation.getTypeCode() == ParticipationType.SBJ ){
						// TODO: It accepts "Reference(Patient | Group)" as subject (Visit: https://www.hl7.org/fhir/procedure-definitions.html)
						// Which one of them should we choose?
						participation.getRole();
					}
					// TODO: Resource reference
				}
			}
			
			
			// category <-> outboundRelationship[typeCode="COMP].target[classCode="LIST", moodCode="EVN"].code
			if( cdaPr.getOutboundRelationships() != null && !cdaPr.getOutboundRelationships().isEmpty() ){
				for( ActRelationship rs : cdaPr.getOutboundRelationships() ){
					// following if statement trusts the short-circuit-evaluation feature of java
					if( rs != null && rs.getTypeCode() != null && rs.getTypeCode() == ActRelationshipType.COMP){
						if(  rs.getTarget() != null && rs.getTarget().getClassCode() == ActClass.LIST && rs.getTarget().getMoodCode() == ActMood.EVN ) {
							for( CS cs : rs.getTarget().getRealmCodes() ){
								// TODO: Do we just assert that at most 1 code is included in rs.getTarget().getRealmCodes()?
								fhirPr.setCategory( dtt.CD2CodeableConcept(cs) );
							}
						}
					}
					
				} // end for
			} // end if
			
			
			// code <-> code
			if( cdaPr.getCode() != null && !cdaPr.getCode().isSetNullFlavor() ){
				fhirPr.setCode(  dtt.CD2CodeableConcept( cdaPr.getCode() )  );
			}
			
			
			// notPerformed <-> actionNegationInd
			if( cdaPr.getNegationInd() != null  ){
				fhirPr.setNotPerformed( cdaPr.getNegationInd() );
			}
			
			
			// reasonNotPerformed <-> .reason.Observation.value
			// TODO: cda part couldn't be found
			
			
			// bodySite <-> .targetSiteCode
			if( cdaPr.getTargetSiteCodes() != null && !cdaPr.getTargetSiteCodes().isEmpty() ){
				for( CD cd : cdaPr.getTargetSiteCodes() ){
					if( cd != null && !cd.isSetNullFlavor() ){
						fhirPr.addBodySite( dtt.CD2CodeableConcept(cd) );
					}
				}
			}
			
			// TODO: reason[x] <-> .reasonCode
			
			// TODO: While mapping performer <-> .participation[typeCode=PFM] :
			// There is a method: getPerformers()
			// Which one should we use:b  participation[typeCode=PFM]   or    getPerformers()
			
			return fhirPr;
		}
	}
	
	
	public Patient PatientRole2Patient(PatientRole patRole){
		// https://www.hl7.org/fhir/patient-mappings.html
		
		if( patRole == null || patRole.isSetNullFlavor() ) return null;
		else{
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			ValueSetsTransformer vst = new ValueSetsTransformerImpl();
			Patient patient = new Patient();
			
			// identifier <-> id
			if( patRole.getIds() != null && !patRole.getIds().isEmpty() ){
				for( II id : patRole.getIds() ){
					if( id == null || id.isSetNullFlavor() ) continue;
					else{
						patient.addIdentifier(  dtt.II2Identifier(id)  );
					}
				}
			}
			
			// name <-> patient.name
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && 
					patRole.getPatient().getNames() != null && !patRole.getPatient().getNames().isEmpty() )
			{
				for( PN pn : patRole.getPatient().getNames() ){
					if( pn == null || pn.isSetNullFlavor() ) continue;
					else{
						patient.addName( dtt.EN2HumanName(pn) );
					}
				}
			}
			
			// telecom <-> telecom
			if( patRole.getTelecoms() != null && !patRole.getTelecoms().isEmpty() )
			{
				for( TEL tel : patRole.getTelecoms() ){
					if( tel == null || tel.isSetNullFlavor() ) continue;
					else{
						patient.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}
			
			// gender <-> patient.administrativeGenderCode
			if(     patRole.getPatient() != null &&
					!patRole.getPatient().isSetNullFlavor() &&
					patRole.getPatient().getAdministrativeGenderCode() != null && 
					!patRole.getPatient().getAdministrativeGenderCode().isSetNullFlavor()  )
			{
				
				if( patRole.getPatient().getAdministrativeGenderCode().getCode() != null && 
						!patRole.getPatient().getAdministrativeGenderCode().getCode().isEmpty() )
				{
					patient.setGender(vst.AdministrativeGenderCode2AdministrativeGenderEnum( patRole.getPatient().getAdministrativeGenderCode().getCode() ) );
				}
			}
			
			// birthDate <-> patient.birthTime
			if( patRole.getPatient() != null && 
					!patRole.getPatient().isSetNullFlavor() &&
					patRole.getPatient().getBirthTime() != null &&
					!patRole.getPatient().getBirthTime().isSetNullFlavor() )
			{
				patient.setBirthDate( dtt.TS2Date(patRole.getPatient().getBirthTime()) );
			}
			
			// address <-> addr
			if( patRole.getAddrs() != null && !patRole.getAddrs().isEmpty() ){
				for(AD ad : patRole.getAddrs()){
					if( ad == null || ad.isSetNullFlavor() ) continue;
					else{
						patient.addAddress(dtt.AD2Address(ad));
					}
				}
			}
			
			// maritalStatus <-> patient.maritalStatusCode
			if(patRole.getPatient().getMaritalStatusCode() != null 
					&& !patRole.getPatient().getMaritalStatusCode().isSetNullFlavor())
			{
				if( patRole.getPatient().getMaritalStatusCode().getCode() != null && !patRole.getPatient().getMaritalStatusCode().getCode().isEmpty() )
				{
					patient.setMaritalStatus( vst.MaritalStatusCode2MaritalStatusCodesEnum(patRole.getPatient().getMaritalStatusCode().getCode()) );
				}
			}
			
			// communication <-> patient.languageCommunication
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() &&
					patRole.getPatient().getLanguageCommunications() != null &&
					!patRole.getPatient().getLanguageCommunications().isEmpty() )
			{
				
				for( LanguageCommunication LC : patRole.getPatient().getLanguageCommunications() ){
					if(LC == null || LC.isSetNullFlavor() ) continue;
					else{
						Communication communication = dtt.LanguageCommunication2Communication(LC);
						patient.addCommunication(communication);
					}
				}
			}
			
			// managingOrganization <-> providerOrganization
			if( patRole.getProviderOrganization() != null && !patRole.getProviderOrganization().isSetNullFlavor() ){
				ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = dtt.Organization2Organization( patRole.getProviderOrganization() );

				// See https://www.hl7.org/fhir/references.html#Reference
				ResourceReferenceDt organizationReference = new ResourceReferenceDt();
				String uniqueIdString = "Organization/"+getUniqueId();
				// TODO: The information about the organization should be pushed to database using the uniqueIdString
				organizationReference.setReference( uniqueIdString );
				if( fhirOrganization.getName() != null ){
					organizationReference.setDisplay( fhirOrganization.getName() );
				}
				patient.setManagingOrganization( organizationReference );
			}
			
//			// guardian <-> patient.guardians
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && 
					patRole.getPatient().getGuardians() != null && !patRole.getPatient().getGuardians().isEmpty() )
			{
				for( org.openhealthtools.mdht.uml.cda.Guardian guardian : patRole.getPatient().getGuardians() ){
					patient.addContact( dtt.Guardian2Contact(guardian) );
				}
			}
			
			
			////////////////////
			// extensions start
			
			// extRace <-> patient.raceCode
			ExtensionDt extRace = new ExtensionDt();
			extRace.setModifier(false);
			extRace.setUrl("http://hl7.org/fhir/extension-us-core-race.html");

			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getRaceCode() != null && !patRole.getPatient().getRaceCode().isSetNullFlavor())
			{
				CD raceCode = patRole.getPatient().getRaceCode();
				extRace.setValue( dtt.CD2CodeableConcept(raceCode) );
			}
			patient.addUndeclaredExtension( extRace );
			
			
			// extEthnicity <-> patient.ethnicGroupCode
			ExtensionDt extEthnicity = new ExtensionDt();
			extEthnicity.setModifier(false);
			extEthnicity.setUrl("http://hl7.org/fhir/extension-us-core-ethnicity.html");
			
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getEthnicGroupCode() != null && !patRole.getPatient().getEthnicGroupCode().isSetNullFlavor() )
			{
				CD ethnicGroupCode = patRole.getPatient().getEthnicGroupCode();
				extEthnicity.setValue( dtt.CD2CodeableConcept(ethnicGroupCode) );
			}
			patient.addUndeclaredExtension(extEthnicity);
			
			// extBirthPlace
//			ExtensionDt extBirthPlace = new ExtensionDt();
//			extBirthPlace.setModifier(false);
//			extBirthPlace.setUrl("http://hl7.org/fhir/extension-birthplace.html");
//			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getBirthplace() != null && !patRole.getPatient().getBirthplace().isSetNullFlavor() )
//			{
////				extBirthPlace.setValue(  dtt.sometransformer( patRole.getPatient().getBirthplace() ) );
//				// Birthplace mapping
//				// We can get the Birthplace info from ccd
//				// However, there is no type to put it
//			}
//			patient.addUndeclaredExtension(extBirthPlace);
			
			// extReligion
			ExtensionDt extReligion = new ExtensionDt();
			extReligion.setModifier(false);
			// TODO: This url doesn't exist. Look for a existing one.
			extReligion.setUrl("http://hl7.org/fhir/extension-religion.html");
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getReligiousAffiliationCode() != null && !patRole.getPatient().getReligiousAffiliationCode().isSetNullFlavor() )
			{
				CD religiousAffiliationCode = patRole.getPatient().getReligiousAffiliationCode();
				extReligion.setValue( dtt.CD2CodeableConcept(religiousAffiliationCode) );
			}
			patient.addUndeclaredExtension(extReligion);
			
			// extensions end
			/////////////////
			
			
			// Visit https://www.hl7.org/fhir/daf/patient-daf.html
			
			return patient;
		}
	}


}
