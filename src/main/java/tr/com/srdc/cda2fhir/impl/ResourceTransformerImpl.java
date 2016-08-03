package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.Entity;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CR;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.rim.ActRelationship;
import org.openhealthtools.mdht.uml.hl7.rim.Participation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipType;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentProcedureMood;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Group;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Organization.Contact;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ResourceTransformerImpl implements tr.com.srdc.cda2fhir.ResourceTransformer{
	
	///////////////
	// necip start
	private static int idHolder = 0;
	private static int getUniqueId(){
		return idHolder++;
	}
	
	
	// incomplete
	public ca.uhn.fhir.model.dstu2.resource.Encounter Encounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter){
		
		if( cdaEncounter == null || cdaEncounter.isSetNullFlavor() ) return null;
		else if( cdaEncounter.getMoodCode() != org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentEncounterMood.EVN ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = new ca.uhn.fhir.model.dstu2.resource.Encounter();
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			
			// identifier <-> id
			if( cdaEncounter.getIds() != null && !cdaEncounter.getIds().isEmpty() ){
				for( II id : cdaEncounter.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						fhirEncounter.addIdentifier(  dtt.II2Identifier(id) );
					}
				}
			}
			
			// status <-> statusCode
			if( cdaEncounter.getStatusCode() != null && !cdaEncounter.getStatusCode().isSetNullFlavor() ){
				if( cdaEncounter.getStatusCode().getCode().equals("active") ){
					fhirEncounter.setStatus(EncounterStateEnum.IN_PROGRESS);
				} else if( cdaEncounter.getStatusCode().getCode().equals("completed") ){
					fhirEncounter.setStatus( EncounterStateEnum.FINISHED );
				}
			}
			
			// class
			
			
			// type <-> code
			if( cdaEncounter.getCode() != null && !cdaEncounter.getCode().isSetNullFlavor() ){
				fhirEncounter.addType( dtt.CD2CodeableConcept( cdaEncounter.getCode() ) );
			}
			
			// priority <-> priorityCode
			if( cdaEncounter.getPriorityCode() != null && !cdaEncounter.getPriorityCode().isSetNullFlavor() ){
				fhirEncounter.setPriority( dtt.CD2CodeableConcept( cdaEncounter.getPriorityCode() ) );
			}
			
			// patient
			if( cdaEncounter.getParticipations() != null && !cdaEncounter.getParticipations().isEmpty()){
				for( Participation participation : cdaEncounter.getParticipations() ){
					if( participation.getTypeCode() == org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType.SBJ  ){
						if( participation.getRole() != null && participation.getRole().getClassCode() == org.openhealthtools.mdht.uml.hl7.vocab.RoleClass.PAT ){
							// PatientRole extends Role
							Patient fhirPatient = PatientRole2Patient( (PatientRole) participation.getRole() );
							if( fhirPatient != null ){
								ResourceReferenceDt patientReference = new ResourceReferenceDt();
								String uniqueIdString = "Patient/"+getUniqueId();
								// TODO: The information about the patient should be pushed to database using the uniqueIdString
								patientReference.setReference( uniqueIdString );
								// TODO: Do we need to set display? What to set, name?
//								patientReference.setDisplay( THE VALUE TO SET AS DISPLAY );
								fhirEncounter.setPatient( patientReference );
							}
						}
					}
				}
			}
			
			// participant
			
			// appointment <-> .outboundRelationship[typeCode=FLFS].target[classCode=ENC, moodCode=APT] 
			
			
			// period <-> .effectiveTime (low & high)
			if( cdaEncounter.getEffectiveTime() != null && !cdaEncounter.getEffectiveTime().isSetNullFlavor() ){
				fhirEncounter.setPeriod( dtt.IVL_TS2Period( cdaEncounter.getEffectiveTime() ) );
			}
			
			
			// indication <-> .outboundRelationship[typeCode=RSON].target

			
			// hospitalization <-> .outboundRelationship[typeCode=COMP].target[classCode=ENC, moodCode=EVN]

			
			// location <-> .participation[typeCode=LOC]
//			if( cdaEncounter.getParticipations() != null && !cdaEncounter.getParticipations().isEmpty() ){
//				for( Participation participation : cdaEncounter.getParticipations() ){
//					if( participation.getTypeCode() == org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType.LOC ){
//						Location locationToAdd = new Location();
//						
//						Role2Location
//						// https://www.hl7.org/fhir/location-mappings.html
//						// locationToAdd.setLocation(  Role2Location( participation.getRole() )  );
//						
//						// period <-> time?
//						
//						fhirEncounter.addLocation( locationToAdd );
//					}
//				}
//			}
			
			// serviceProvider Reference(Organizaton) <-> 	.particiaption[typeCode=PFM].role
//			if( cdaEncounter.getParticipations() != null && !cdaEncounter.getParticipations().isEmpty() ){
//				for( Participation participation : cdaEncounter.getParticipations() ){
//					if( participation.getTypeCode() == org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType.PPRF /* PFM? NOT SURE */ ){
//						 Role2Organization
//						fhirEncounter.setServiceProvider( Organization2Organization( participation.getRole() ) );
//					}
						// typeCode pfm ?
//				}
//			}
			
			
			// partOf <-> .inboundRelationship[typeCode=COMP].source[classCode=COMP, moodCode=EVN]
//			if( cdaEncounter.getInboundRelationships() != null && !cdaEncounter.getInboundRelationships().isEmpty() ){
//				for( ActRelationship actRelationship : cdaEncounter.getInboundRelationships() ){
//					if( actRelationship != null && actRelationship ){
//						actRelationship
//					}
//				}
//			}
			
			
			
			
			
			
			
			return fhirEncounter;
		}
	}
	
	// incomplete
	public Group Entity2Group( Entity entity ){
		if( entity == null || entity.isSetNullFlavor() ) return null;
		else if( entity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND ) return null;
		else{
			Group group = new Group();
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			ValueSetsTransformer vst = new ValueSetsTransformerImpl();
			
			// identifier <-> id
			if( entity.getIds() != null && !entity.getIds().isEmpty() ){
				for( II id : entity.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						if( id.getDisplayable() ){
							// unique
							group.addIdentifier( dtt.II2Identifier(id) );
						}
					}
				}
			}
			
			// type
			if( entity.getClassCode() != null ){
				GroupTypeEnum groupTypeEnum = vst.EntityClassRoot2GroupTypeEnum( entity.getClassCode() );
				if( groupTypeEnum != null ){
					group.setType( groupTypeEnum );
				}
				
			}
			
			// actual
			if( entity.isSetDeterminerCode() && entity.getDeterminerCode() != null ){
				if( entity.getDeterminerCode() == EntityDeterminer.KIND ){
					group.setActual(false);
				} else{
					group.setActual(true);
				}
			}
			
			// code
			if( entity.getCode() != null && !entity.getCode().isSetNullFlavor() ){
				group.setCode( dtt.CD2CodeableConcept(entity.getCode()) );
			}
			
			// name
			
			
			// quantity
			
			
			// characteristic
			
			// member
//			if( entity.getScopedRoles() != null && !entity.getScopedRoles().isEmpty() ){
//				for( Role role: entity.getScopedRoles() ){
//					if( role != null && role.getClassCode() == org.openhealthtools.mdht.uml.hl7.vocab.RoleClass.MBR ){
//						if( role.getPlayer() != null ){
//							group.addMember( role.getPlayer() );
//									Group.MEMBER <-> ENTITY
//						}
//					}
//				}
//			}
			
			
			return group;
		}
	}
	
	// incomplete
	public 	ca.uhn.fhir.model.dstu2.resource.Procedure Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaPr){
		
		
		// cdaPr: Procedure of type CDA
		// fhirPr: Procedure of type FHIR
		// https://www.hl7.org/fhir/daf/procedure-daf.html
		// https://www.hl7.org/fhir/procedure-mappings.html
		
		if( cdaPr == null || cdaPr.isSetNullFlavor() ) return null;
		else if( cdaPr.getMoodCode() == null || cdaPr.getMoodCode() != x_DocumentProcedureMood.EVN ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Procedure fhirPr = new ca.uhn.fhir.model.dstu2.resource.Procedure();
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			
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
						// It accepts "Reference(Patient | Group)" as subject (Visit: https://www.hl7.org/fhir/procedure-definitions.html)
						// Cannot map from participation to patient or group
						participation.getRole();
					}
					// Resource reference
				}
			}
			
			
			// category <-> outboundRelationship[typeCode="COMP].target[classCode="LIST", moodCode="EVN"].code
			if( cdaPr.getOutboundRelationships() != null && !cdaPr.getOutboundRelationships().isEmpty() ){
				for( ActRelationship rs : cdaPr.getOutboundRelationships() ){
					// following if statement trusts the short-circuit-evaluation feature of java
					if( rs != null && rs.getTypeCode() != null && rs.getTypeCode() == ActRelationshipType.COMP){
						if(  rs.getTarget() != null && rs.getTarget().getClassCode() == ActClass.LIST && rs.getTarget().getMoodCode() == ActMood.EVN ) {
							for( CS cs : rs.getTarget().getRealmCodes() ){
								// Asserted that at most 1 code is included in rs.getTarget().getRealmCodes()
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
			// cda part couldn't be found
			
			
			// bodySite <-> .targetSiteCode
			if( cdaPr.getTargetSiteCodes() != null && !cdaPr.getTargetSiteCodes().isEmpty() ){
				for( CD cd : cdaPr.getTargetSiteCodes() ){
					if( cd != null && !cd.isSetNullFlavor() ){
						fhirPr.addBodySite( dtt.CD2CodeableConcept(cd) );
					}
				}
			}
			
			// reason[x] <-> .reasonCode
			
			

			// performer <-> .participation[typeCode=PFM]
//			if( cdaPr.getParticipations() != null && !cdaPr.getParticipations().isEmpty() ){
//				for( Participation participation : cdaPr.getParticipations() ){
//					if( participation != null && participation.getTypeCode() == org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType.PRF ){
//						// Not sure if ParticipationType.PRF means [typeCode=PFM]. Check in tests.
//						Performer fhirPerformer = new Performer();
//							// performer	.participation[typeCode=PFM]
//							//	        actor	.role
//							//	        role	.functionCode
//						fhirPr.addPerformer( fhirPerformer );
//					}
//				}
//			}
			
			// performed[x] <-> .effectiveTime
			if( cdaPr.getEffectiveTime() != null && !cdaPr.getEffectiveTime().isSetNullFlavor() ){
				fhirPr.setPerformed( dtt.IVL_TS2Period( cdaPr.getEffectiveTime() ) );
			}
			
			// encounter <-> .inboundRelationship[typeCode=COMP].source[classCode=ENC, moodCode=EVN]
//			if( cdaPr.getEncounters() != null && cdaPr.getEncounters().isEmpty() ){
//				for( org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter : cdaPr.getEncounters() ){
//					if( cdaEncounter != null ){
//						Encounter2Encounter( cdaEncounter );
//						fhirPr.setEncounter(  ); /* list of encounters? */
//					}
//				}
//				// encounter mapping https://www.hl7.org/fhir/encounter-mappings.html 
//			}
			
			// location
			
			// outcome
//			if( cdaPr.getOutboundRelationships() != null && !cdaPr.getOutboundRelationships().isEmpty() ){
//				for( ActRelationship actRelationship : cdaPr.getOutboundRelationships() ){
//					if( actRelationship != null && actRelationship.getTypeCode() == org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipType.OUTC ){
//						if( actRelationship.getTarget() != null ){
//							fhirPr.setOutcome(  actRelationship.getTarget()   );
//						}
//					}
//				}
//			}
			
			// report 
			// mapping needed https://www.hl7.org/fhir/diagnosticreport-mappings.html
			
			return fhirPr;
		}
	}
	
	// not tested
	public ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( Guardian guardian ){
		
		// There doesn't exist a well specified mapping between contact and guardian
		// If found, control the mapping
		if( guardian == null || guardian.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Patient.Contact contact = new ca.uhn.fhir.model.dstu2.resource.Patient.Contact();
			
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			ValueSetsTransformer vst = new ValueSetsTransformerImpl();
			
			// addr
			if( guardian.getAddrs() != null && !guardian.getAddrs().isEmpty() ){
				contact.setAddress( dtt.AD2Address(guardian.getAddrs().get(0)) );
			} 
			
			// tel
			if( guardian.getTelecoms() != null && !guardian.getTelecoms().isEmpty() ){
				for( TEL tel : guardian.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						contact.addTelecom( dtt.TEL2ContactPoint( tel ) );
					}
				}
			}
			
			// relationship
			if( guardian.getCode() != null && !guardian.getCode().isSetNullFlavor() ){
				contact.addRelationship( dtt.CD2CodeableConcept( guardian.getCode() ) );
			}

			
			
//			if( guardian.getIds() != null && !guardian.getIds().isEmpty() ){
//				if( guardian.getIds().get(0) != null && !guardian.getIds().get(0).isSetNullFlavor() ){
//					guardian.getIds().get(0);
//				}
//			}
			

			return contact;
		}
	}
	
	// tested
	public ca.uhn.fhir.model.dstu2.resource.Organization Organization2Organization ( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization ){
		if( cdaOrganization == null || cdaOrganization.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = new ca.uhn.fhir.model.dstu2.resource.Organization();
			
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			ValueSetsTransformer vst = new ValueSetsTransformerImpl();
			
			if( cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty() )
			{
				List<IdentifierDt> idList = new ArrayList<IdentifierDt>();
				for( II id : cdaOrganization.getIds()  ){
					if( id.getRoot() != null && !id.getRoot().isEmpty() ){
						idList.add(dtt.II2Identifier(id));
					}
				}
				if( !idList.isEmpty() ){
					fhirOrganization.setIdentifier(idList);
				}
				
			}
			
			if( cdaOrganization.getNames() != null && !cdaOrganization.isSetNullFlavor() ){
				for( ON name:cdaOrganization.getNames() ){
					if( name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty() ){
						fhirOrganization.setName( name.getText() );
					}
//					if( name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty() ){
//						String nameToSet = "";
//						if( name.getFamilies() != null && !name.getFamilies().isEmpty() ){
//							nameToSet = nameToSet + name.getFamilies().get(0).getText();
//						}
//						if( name.getGivens() != null && !name.getGivens().isEmpty() ){
//							nameToSet = nameToSet + name.getGivens().get(0).getText();
//						}
//						if( !nameToSet.equals("") ){
//							fhirOrganization.setName( nameToSet );
//						}
//					}
				}
			}
			if( cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty() ){
				for(TEL tel : cdaOrganization.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor()){
						Contact c = new Contact();
						ContactPointDt contactPoint = dtt.TEL2ContactPoint(tel);
						if( contactPoint != null && !contactPoint.isEmpty() ){
							c.addTelecom( contactPoint );
							fhirOrganization.addContact( c );
						}
					}
				}
			}
			
			if( cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty() ){
				for( AD ad : cdaOrganization.getAddrs()  ){
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirOrganization.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			return fhirOrganization;
		}
	}

	// tested
	public Communication LanguageCommunication2Communication( LanguageCommunication LC ){
		if(LC == null || LC.isSetNullFlavor()) return null;
		else{
			Communication communication = new Communication();
			
			DataTypesTransformer dtt = new DataTypesTransformerImpl();
			if( LC.getLanguageCode() != null && !LC.getLanguageCode().isSetNullFlavor() ){
				communication.setLanguage(  dtt.CD2CodeableConcept( LC.getLanguageCode() )  );
			}
			if( LC.getPreferenceInd() != null && !LC.getPreferenceInd().isSetNullFlavor() ){
				communication.setPreferred(  dtt.BL2Boolean( LC.getPreferenceInd() )  );
			}
			return communication;
		}
	}
	
	// tested
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
						Communication communication = LanguageCommunication2Communication(LC);
						patient.addCommunication(communication);
					}
				}
			}
			
			// managingOrganization <-> providerOrganization
			if( patRole.getProviderOrganization() != null && !patRole.getProviderOrganization().isSetNullFlavor() ){
				ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = Organization2Organization( patRole.getProviderOrganization() );
				
				// See https://www.hl7.org/fhir/references.html#Reference
				ResourceReferenceDt organizationReference = new ResourceReferenceDt();
				String uniqueIdString = "Organization/"+getUniqueId();
				// TODO: The information about the organization should be pushed to database using the uniqueIdString
				// Also, the id of the organization should be set
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
					patient.addContact( Guardian2Contact(guardian) );
				}
			}
			
			
			////////////////////
			// extensions start
			
			// extRace <-> patient.raceCode
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getRaceCode() != null && !patRole.getPatient().getRaceCode().isSetNullFlavor())
			{
				ExtensionDt extRace = new ExtensionDt();
				extRace.setModifier(false);
				extRace.setUrl("http://hl7.org/fhir/extension-us-core-race.html");
				CD raceCode = patRole.getPatient().getRaceCode();
				extRace.setValue( dtt.CD2CodeableConcept(raceCode) );
				patient.addUndeclaredExtension( extRace );
			}

			// extEthnicity <-> patient.ethnicGroupCode
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getEthnicGroupCode() != null && !patRole.getPatient().getEthnicGroupCode().isSetNullFlavor() )
			{
				ExtensionDt extEthnicity = new ExtensionDt();
				extEthnicity.setModifier(false);
				extEthnicity.setUrl("http://hl7.org/fhir/extension-us-core-ethnicity.html");
				CD ethnicGroupCode = patRole.getPatient().getEthnicGroupCode();
				extEthnicity.setValue( dtt.CD2CodeableConcept(ethnicGroupCode) );
				patient.addUndeclaredExtension(extEthnicity);
			}
			
			
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
			if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getReligiousAffiliationCode() != null && !patRole.getPatient().getReligiousAffiliationCode().isSetNullFlavor() )
			{
				ExtensionDt extReligion = new ExtensionDt();
				extReligion.setModifier(false);
				// TODO: This url doesn't exist. Look for a existing one.
				extReligion.setUrl("http://hl7.org/fhir/extension-religion.html");
				CD religiousAffiliationCode = patRole.getPatient().getReligiousAffiliationCode();
				extReligion.setValue( dtt.CD2CodeableConcept(religiousAffiliationCode) );
				patient.addUndeclaredExtension(extReligion);
			}
			
			// extensions end
			/////////////////
			
			
			// Visit https://www.hl7.org/fhir/daf/patient-daf.html
			
			return patient;
		}
	}

	// necip end
	////////////
	
	
	///////////////
	// ismail start
	
	static int counter = 0;
	DataTypesTransformer dtt = new DataTypesTransformerImpl();

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public List<Condition> ProblemConcernAct2Condition(ProblemConcernAct probAct) {
		
		List<Condition> conditionList = new ArrayList<Condition>();
		
		for(EntryRelationship entryRelationship : probAct.getEntryRelationships())
		{	
			Condition condition = new Condition();
			List<IdentifierDt> IdList = new ArrayList<IdentifierDt>();
		
			if( probAct.getIds() != null & !probAct.getIds().isEmpty() ){
				
				IdentifierDt identifier = dtt.II2Identifier( probAct.getIds().get(0) );
				IdList.add(identifier);
				
			}
			condition.setIdentifier( IdList );
			
			ResourceReferenceDt resourceReferencePatient = new ResourceReferenceDt();
			//IdentifierDt identifierPatient = dtt.II2Identifier( probAct.getSubject().getRole().getPlayer().getTypeId());
			resourceReferencePatient.setReference( "patient/" + counter );
			condition.setPatient( resourceReferencePatient );
			
			ResourceReferenceDt resourceReferenceEncounter = new ResourceReferenceDt();
			//IdentifierDt identifierEncounter = dtt.II2Identifier( 
					//probAct.getEncounters().get(0).getInboundRelationships().get(0).getSource().getTypeId());
			resourceReferenceEncounter.setReference( "encounter/" + counter );
			condition.setEncounter( resourceReferenceEncounter );
			
			ResourceReferenceDt resourceReferenceAsserter = new ResourceReferenceDt();
			//IdentifierDt identifierAsserter = dtt.II2Identifier( probAct.getAuthors().get(0).getRole().getPlayer().getTypeId());
			resourceReferenceAsserter.setReference( "asserter/" + counter ); 
			condition.setAsserter( resourceReferenceAsserter );
			
			counter++;
			
			
			//DATE-RECORDED
			DateDt dateRecorded = dtt.TS2Date(entryRelationship.getObservation().getAuthors().get(0).getTime());
			condition.setDateRecorded( dateRecorded );
			/////
		
			
			
			/*if( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName().equals("Problem") ){
				
			codingForSetCode.setCode( cd.getTranslations().get(0).getCode() );
			codingForSetCode.setDisplay( cd.getTranslations().get(0).getDisplayName() );
			codingForSetCode.setSystem( codeSystem2System( cd.getTranslations().get(0).getCodeSystem() ) );
			
			codingForCategory.setCode( probAct.getEntryRelationships().get(0).getObservation().getCode().getCode() );
			codingForCategory.setDisplay( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( codeSystem2System( probAct.getEntryRelationships().get(0).getObservation().getCode().getCodeSystem() ) );
			
			codingForCategory2.setCode( "finding" );
			codingForCategory2.setDisplay( "Finding" );
			codingForCategory2.setSystem( "http://hl7.org/fhir/condition-category" );
			
			boundCodeableConceptDt.addCoding( codingForCategory );
			boundCodeableConceptDt.addCoding( codingForCategory2 );
			codeableConcept.addCoding( codingForSetCode );
			
			}*/
			
			//CODE AND CATEGORY
			CodingDt codingForCategory = new CodingDt();
			//CodingDt codingForCategory2 = new CodingDt();
			CodingDt codingForSetCode = new CodingDt();
			CodeableConceptDt codeableConcept = new CodeableConceptDt();
			BoundCodeableConceptDt boundCodeableConceptDt = new BoundCodeableConceptDt();
			//TODO: VALIDATE: Casting ANY to CD.
			if( entryRelationship.getObservation().getValues() != null ){
				CD cd = (CD) ( entryRelationship.getObservation().getValues().get(0) );
	
				codingForSetCode.setCode( cd.getCode() );
				codingForSetCode.setDisplay( cd.getDisplayName() );
				codingForSetCode.setSystem( codeSystem2System( cd.getCodeSystem() ) );
				codeableConcept.addCoding( codingForSetCode );
			}
			
			codingForCategory.setCode( entryRelationship.getObservation().getCode().getCode() );
			codingForCategory.setDisplay(  entryRelationship.getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( codeSystem2System( entryRelationship.getObservation().getCode().getCodeSystem() ) );
			boundCodeableConceptDt.addCoding( codingForCategory );
			
			condition.setCategory( boundCodeableConceptDt );
			condition.setCode( codeableConcept);
			/////
			
		
			
			//ONSET and ABATEMENT
			// It is not clear which effectiveTime getter to call: 
			//                  ...getObservation().getEffectiveTime()  OR  probAct.getEffectiveTime()
			if(entryRelationship.getObservation().getEffectiveTime() != null || 
					!entryRelationship.getObservation().getEffectiveTime().isSetNullFlavor())
			{
				PeriodDt period = dtt.IVL_TS2Period(entryRelationship.getObservation().getEffectiveTime());
				DateTimeDt dateStart = new DateTimeDt();
				DateTimeDt dateEnd = new DateTimeDt();
				if(period.getStart() != null)
				{
					dateStart.setValue( period.getStart() );
					
				}
		        
				if(period.getEnd() != null )
				{
			        dateEnd.setValue( period.getEnd() );
			        
				}
			    
				condition.setOnset(  dateStart );
				condition.setAbatement(  dateEnd );
			}
	        
	        
	        
	        // BODYSITE, SEVERITY and NOTES
	        CodeableConceptDt codeableConceptBodySite = new CodeableConceptDt();
	        CodeableConceptDt codeableConceptSeverity = new CodeableConceptDt();
	        CodingDt codingBodysite = new CodingDt();
	        CodingDt codingSeverity = new CodingDt();
	        
	        if( entryRelationship.getObservation().getValues() != null ){
				CD cd = (CD) ( entryRelationship.getObservation().getValues().get(0) );
				
		        if(cd.getQualifiers() != null && !cd.getQualifiers().isEmpty()){
		        	for(CR cr : cd.getQualifiers()){
				        if(cr.getName().getDisplayName().toLowerCase().equals("finding site")){
				        	
				        	codingBodysite.setDisplay( cr.getValue().getDisplayName() );
				        	codingBodysite.setCode( cr.getValue().getCode() );
				        	
				        }
//				        if(cr.getName().getDisplayName().toLowerCase().equals("problem severity")) {
//				        	
//				        	codingSeverity.setCode( cr.getValue().getCode() );
//				        	codingSeverity.setDisplay( cr.getValue().getDisplayName() );
//				        	
//				        }
		        	}
		        	
		        	//Notes
//		        	for(CD cdInner : cd.getTranslations() ){
//		        		
//		        		if(cdInner.getDisplayName().toLowerCase().equals("annotation")){
//		        			//TODO : What to write in Notes.
//		        			conditionList[i].setNotes( cdInner.getCode() );
//		        			
//		        		}
//		        		
//		        	}
		        		
		        }
	        }
	        
	        codeableConceptBodySite.addCoding( codingBodysite );
	        //codeableConceptSeverity.addCoding( codingSeverity );
	        condition.addBodySite(codeableConceptBodySite);
	        //condition.setSeverity(codeableConceptSeverity);
	        ////
	        
	        
	      //VERIFICATION_STATUS
//	        BoundCodeDt boundcodeVerif = new BoundCodeDt();
//	       
//	        for ( CR cr :entryRelationship.getObservation().getCode().getQualifiers()){
//	        	if(cr.getName().getDisplayName().toLowerCase().equals("verification status")
//	        			  || cr.getName().getDisplayName().toLowerCase().equals("verificationstatus")
//	        			  || cr.getName().getDisplayName().toLowerCase().equals("verification")
//	        				)
//	        	{
//	        		
//	        		boundcodeVerif.setValueAsString( cr.getValue().getDisplayName() );
//	        		
//	        	}
//	        }
	        
	        
	        
	        //TODO: Stage type does not exist.
	        conditionList.add(condition);
	        
		}
		
		return conditionList;
	}

	public Observation ResultObservation2Observation(ResultObservation resObs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Observation VitalSignObservation2Observation(
			VitalSignObservation vsObs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String codeSystem2System(String codeSystem){
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

	@SuppressWarnings("deprecation")
	public Medication ManufacturedProduct2Medication(ManufacturedProduct manPro) {
		
		
		Medication medication = new Medication();
		
		// TODO : VALIDATE: manufacturedMaterial.code IS USED INSTEAD OF medication.code ( which does not exist).
		//CODE
		CodeableConceptDt codeableConcept = new CodeableConceptDt();
		CodingDt coding = new CodingDt();
		if(manPro.getManufacturedMaterial() != null & manPro.getManufacturedMaterial().getCode() != null
				& !manPro.getManufacturedMaterial().getCode().isSetNullFlavor()){
			
			CE ce = manPro.getManufacturedMaterial().getCode();
			if(ce.getCode() != null )
				coding.setCode( ce.getCode());
			if( ce.getDisplayName() != null )
			coding.setDisplay( ce.getDisplayName() );
			if( ce.getCodeSystem() != null )
				coding.setSystem( codeSystem2System( ce.getCodeSystem() ) );
			if( ce.getCodeSystemVersion() != null )
				coding.setVersion( ce.getCodeSystemVersion() );
			
		}
		codeableConcept.addCoding(coding);
		for( CD cd : manPro.getManufacturedMaterial().getCode().getTranslations() )
		{
			CodingDt codingTrans = new CodingDt();
			if(cd.getCode() != null )
				codingTrans.setCode( cd.getCode());
			if( cd.getDisplayName() != null )
				codingTrans.setDisplay( cd.getDisplayName() );
			if( cd.getCodeSystem() != null )
				codingTrans.setSystem( codeSystem2System( cd.getCodeSystem() ) );
			if( cd.getCodeSystemVersion() != null )
				codingTrans.setVersion( cd.getCodeSystemVersion() );
			codeableConcept.addCoding(codingTrans);
		}
		
		medication.setCode( codeableConcept );
		
		
		//IS_BRAND and MANUFACTURER
		ResourceReferenceDt resourceReferenceManu = new ResourceReferenceDt();
		if( manPro.getManufacturerOrganization() != null ){
			
			if( !manPro.getManufacturerOrganization().isSetNullFlavor()  )
			{
				medication.setIsBrand(true);
				
				//MANUFACTURER
				
				if( manPro.getManufacturerOrganization().getIds() != null ){
					if(manPro.getManufacturerOrganization().getIds().size() != 0){
						IdentifierDt identifierManu = dtt.II2Identifier( manPro.getManufacturerOrganization().getIds().get(0) );
						resourceReferenceManu.setReference( identifierManu.getId() );
					}
				}
				if( manPro.getManufacturerOrganization().getNames() != null){
					if( manPro.getManufacturerOrganization().getNames().size() != 0 )
						resourceReferenceManu.setDisplay( manPro.getManufacturerOrganization().getNames().get(0).getText() );
				}
				
			}
			else
			{
				medication.setIsBrand(false);
			}
		}else{
			medication.setIsBrand(false);
		}
		medication.setManufacturer( resourceReferenceManu );
		
		
		
		
		//TODO Product.
		//TODO Package.
		
		
		return medication;
	}

	public MedicationAdministration SubstanceAdministration2MedicationAdministration(
			SubstanceAdministration subAd) {
		
		MedicationAdministration medAd = new MedicationAdministration();
		List<IdentifierDt> idList = new ArrayList<IdentifierDt>();
		medAd.setIdentifier( idList );
		if( subAd.getIds().isEmpty() == false){
			IdentifierDt identifier = dtt.II2Identifier( subAd.getIds().get(0) );
			medAd.addIdentifier( identifier ); 
		}
		//MedicationAdministrationStatusEnum
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		medAd.setStatus( vst.StatusCode2MedicationAdministrationStatusEnum( subAd.getStatusCode().getDisplayName()) );
		
		// TODO : Complete.
			
		return null;
	}

	public MedicationDispense Supply2MedicationDispense(Supply sup) {
		
		if( sup.getMoodCode().getLiteral() == "EVN" ){
			
			MedicationDispense meDis = new MedicationDispense();
			
			if( sup.getIds() != null &  !sup.getIds().isEmpty() )
				meDis.setIdentifier( dtt.II2Identifier( sup.getIds().get(0) ) );
			
			ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
			meDis.setStatus( vst.StatusCode2MedicationDispenseStatusEnum( sup.getStatusCode().getDisplayName() ) );
			
			ResourceReferenceDt performerRef = new ResourceReferenceDt();
			performerRef.setReference( "practitioner/" + counter );
			meDis.setDispenser( performerRef );
			
			
			CodeableConceptDt type = new CodeableConceptDt();
			CodingDt coding = new CodingDt();
			if(sup.getCode() != null & !sup.getCode().isSetNullFlavor()){
				
				CD cd = sup.getCode();
				if(cd.getCode() != null )
					coding.setCode( cd.getCode() );
				if(cd.getDisplayName() != null )
					coding.setDisplay( cd.getDisplayName() );
				if( cd.getCodeSystem() != null )
					coding.setSystem( codeSystem2System( cd.getCodeSystem() ) );
				if( cd.getCodeSystemVersion() != null )
					coding.setVersion( cd.getCodeSystemVersion() );
				
				 for( CD trans : cd.getTranslations() ){
					
					CodingDt codingTr  = new CodingDt();
					
					if(trans.getCode() != null )
						codingTr.setCode( trans.getCode() );
					if(trans.getDisplayName() != null )
						codingTr.setDisplay( trans.getDisplayName() );
					if( trans.getCodeSystem() != null )
						codingTr.setSystem( codeSystem2System( trans.getCodeSystem() ) );
					if( trans.getCodeSystemVersion() != null )
						codingTr.setVersion( trans.getCodeSystemVersion() );
					
					type.addCoding( codingTr );
				}
			}
			
			type.addCoding( coding );
			meDis.setType( type );
			
			SimpleQuantityDt quantity = new SimpleQuantityDt();
			if( sup.getQuantity().getValue() != null )
				quantity.setValue( sup.getQuantity().getValue() );
			if( sup.getQuantity().getUnit() != null)
				quantity.setUnit( sup.getQuantity().getUnit() );
			meDis.setQuantity( quantity );
			
			ResourceReferenceDt medRef = new ResourceReferenceDt();
			medRef.setReference( "medication/" + counter );
			medRef.setDisplay( sup.getProduct().getManufacturedProduct().getManufacturedMaterial().getName().getText() );
			meDis.setMedication( medRef );
			
			DateTimeDt prepDate = new DateTimeDt();
			if( sup.getEffectiveTimes() != null & sup.getEffectiveTimes().size() != 0 ){
				prepDate.setValueAsString( sup.getEffectiveTimes().get(0).getValue() );
				
				if(sup.getEffectiveTimes().get(1) != null ){
					DateTimeDt handDate = new DateTimeDt();
					handDate.setValueAsString( sup.getEffectiveTimes().get(1).getValue() );
					meDis.setWhenHandedOver(handDate);
				}
				
			}
			meDis.setWhenPrepared( prepDate );
			
			// TODO : No dosage section exists in CCD example.
			meDis.setDosageInstruction(null);
						
		}
		
		return null;
	}

	
	// ismail end
	/////////////
}
