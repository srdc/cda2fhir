package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.TimingDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Organization.Contact;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.dstu2.resource.Practitioner.PractitionerRole;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;
import ca.uhn.fhir.model.dstu2.valueset.AllergyIntoleranceCategoryEnum;
import ca.uhn.fhir.model.dstu2.valueset.AllergyIntoleranceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.AllergyIntoleranceTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ProcedureStatusEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.IdDt;
import tr.com.srdc.cda2fhir.CCDATransformer;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ResourceTransformerImpl implements tr.com.srdc.cda2fhir.ResourceTransformer{

	private DataTypesTransformer dtt;
	private ValueSetsTransformer vst;
	private CCDATransformer cct;
	private IdDt patientId;

	public ResourceTransformerImpl() {
		dtt = new DataTypesTransformerImpl();
		vst = new ValueSetsTransformerImpl();
		cct = null;
		// This is a default patient reference to be used when ResourceTransformer is not initiated with a CCDATransformer
		patientId = new IdDt("Patient", "0");
	}

	public ResourceTransformerImpl(CCDATransformer ccdaTransformer) {
		this();
		cct = ccdaTransformer;
		// Refresh the patientId to get the real value transformed from the recordTarget of the CDA document
		patientId = cct.getPatientId();
	}

	protected String getUniqueId() {
		if(cct != null)
			return cct.getUniqueId();
		else
			return UUID.randomUUID().toString();
	}
	
// necip start
	
	
	// Necip: I am not sure where to map AssignedAuthor. AssignedAuthor2Practitioner makes more sense.
	public Bundle AssignedAuthor2Practitioner( AssignedAuthor cdaAssignedAuthor ){
		if( cdaAssignedAuthor == null || cdaAssignedAuthor.isSetNullFlavor() ) return null;
		else{
			Practitioner fhirPractitioner = new Practitioner();
			
			// bundle
			Bundle fhirPractitionerBundle = new Bundle();
			fhirPractitionerBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner));
			
			// id
			IdDt resourceId = new IdDt("Practitioner",getUniqueId());
			fhirPractitioner.setId(resourceId);
			
			// identifier
			if( cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty() ){
				for( II ii : cdaAssignedAuthor.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirPractitioner.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// name <-> assignedAuthor.assignedPerson.name
			if( cdaAssignedAuthor.getAssignedPerson() != null && !cdaAssignedAuthor.getAssignedPerson().isSetNullFlavor() ){
				if( cdaAssignedAuthor.getAssignedPerson().getNames() != null && !cdaAssignedAuthor.getAssignedPerson().getNames().isEmpty()){
					for( PN pn : cdaAssignedAuthor.getAssignedPerson().getNames() ){
						if( pn != null && !pn.isSetNullFlavor() ){
							// Asserting that at most one name exists
							fhirPractitioner.setName( dtt.EN2HumanName(pn) );
						}
					}
				}
			}
			
			// address
			if( cdaAssignedAuthor.getAddrs() != null && !cdaAssignedAuthor.getAddrs().isEmpty() ){
				for( AD ad : cdaAssignedAuthor.getAddrs() ){
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirPractitioner.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			// telecom
			if( cdaAssignedAuthor.getTelecoms() != null && !cdaAssignedAuthor.getTelecoms().isEmpty() ){
				for( TEL tel : cdaAssignedAuthor.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirPractitioner.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}
			
			// Adding a practitionerRole
			Practitioner.PractitionerRole fhirPractitionerRole = fhirPractitioner.addPractitionerRole();
			
			// practitionerRole.role <-> assignedAuthor.code
			if( cdaAssignedAuthor.getCode() != null && !cdaAssignedAuthor.isSetNullFlavor() ){
				fhirPractitionerRole.setRole( dtt.CD2CodeableConcept(cdaAssignedAuthor.getCode()) );
				}
			
			// practitionerRole.organization <-> organization
			if( cdaAssignedAuthor.getRepresentedOrganization() != null && !cdaAssignedAuthor.getRepresentedOrganization().isSetNullFlavor() ){
				Organization fhirOrganization = null;
				Bundle fhirOrganizationBundle = Organization2Organization( cdaAssignedAuthor.getRepresentedOrganization() );
				
				for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirOrganizationBundle.getEntry()){
					if( entry.getResource() instanceof Organization )
						fhirOrganization = (Organization) entry.getResource();
				}
				
				fhirPractitionerRole.setManagingOrganization( new ResourceReferenceDt(fhirOrganization.getId()) );
				fhirPractitionerBundle.addEntry( new Bundle.Entry().setResource(fhirOrganization));
			}
			
			
			
			return fhirPractitionerBundle;
		}
	}

	// You may delete following method after determining where to map AssignedAuthor
	public Bundle AssignedAuthor2RelatedPerson( AssignedAuthor cdaAssignedAuthor ){
		if( cdaAssignedAuthor == null || cdaAssignedAuthor.isSetNullFlavor() ) return null;
		else{
			RelatedPerson fhirRelatedPerson = new RelatedPerson();
			
			// bundle
			Bundle fhirRelatedPersonBundle = new Bundle();
			fhirRelatedPersonBundle.addEntry( new Bundle.Entry().setResource(fhirRelatedPerson));
			
			// id
			IdDt resourceId = new IdDt("RelatedPerson",getUniqueId());
			fhirRelatedPerson.setId(resourceId);
			
			// identifier
			if( cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty() ){
				for( II ii : cdaAssignedAuthor.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirRelatedPerson.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// relationship <-> assignedAuthor.code
			if( cdaAssignedAuthor.getCode() != null && !cdaAssignedAuthor.isSetNullFlavor() ){
				fhirRelatedPerson.setRelationship( dtt.CD2CodeableConcept(cdaAssignedAuthor.getCode()) );
			}
			
			// name <-> assignedAuthor.assignedPerson.name
			if( cdaAssignedAuthor.getAssignedPerson() != null && !cdaAssignedAuthor.getAssignedPerson().isSetNullFlavor() ){
				if( cdaAssignedAuthor.getAssignedPerson().getNames() != null && !cdaAssignedAuthor.getAssignedPerson().getNames().isEmpty()){
					for( PN pn : cdaAssignedAuthor.getAssignedPerson().getNames() ){
						if( pn != null && !pn.isSetNullFlavor() ){
							// Asserting that at most one name exists
							fhirRelatedPerson.setName( dtt.EN2HumanName(pn) );
						}
					}
				}
			}
			
			// address
			if( cdaAssignedAuthor.getAddrs() != null && !cdaAssignedAuthor.getAddrs().isEmpty() ){
				for( AD ad : cdaAssignedAuthor.getAddrs() ){
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirRelatedPerson.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			// telecom
			if( cdaAssignedAuthor.getTelecoms() != null && !cdaAssignedAuthor.getTelecoms().isEmpty() ){
				for( TEL tel : cdaAssignedAuthor.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirRelatedPerson.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}
			
			// organization?
			
			
			return fhirRelatedPersonBundle;
		}
	}
	
	// needs testing
	public Bundle SocialHistoryObservation2Observation( org.openhealthtools.mdht.uml.cda.consol.SocialHistoryObservation socialHistoryObs ){
		if( socialHistoryObs == null || socialHistoryObs.isSetNullFlavor() ) return null;
		else{
			Bundle fhirObsBundle = new Bundle();
			for( org.openhealthtools.mdht.uml.cda.Observation cdaObs : socialHistoryObs.getObservations() ){
				if( cdaObs == null || cdaObs.isSetNullFlavor() ) continue;
				else{
					Observation fhirObs = null;
					Bundle tempObsBundle = Observation2Observation(cdaObs);
					for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : tempObsBundle.getEntry() ){
						if( entry.getResource() instanceof Observation ){
							fhirObs = (Observation) entry.getResource();
						}
					}
					fhirObsBundle.addEntry( new Bundle.Entry().setResource( fhirObs ) );
				}
			}
			return fhirObsBundle;
		}
	}

	// needs testing
	public Bundle Observation2Observation( org.openhealthtools.mdht.uml.cda.Observation cdaObs ){
		if( cdaObs == null || cdaObs.isSetNullFlavor() ) return null;
		else{
			Observation fhirObs = new Observation();
			
			// bundle
			Bundle fhirObsBundle = new Bundle();
			fhirObsBundle.addEntry( new Bundle.Entry().setResource(fhirObs) );
			
			// id
			IdDt resourceId = new IdDt("Observation", getUniqueId());
			fhirObs.setId(resourceId);
			
			// subject
			fhirObs.setSubject( new ResourceReferenceDt( patientId ) );
			
			// identifier
			if( cdaObs.getIds() != null && !cdaObs.getIds().isEmpty() ){
				for( II ii : cdaObs.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirObs.addIdentifier(dtt.II2Identifier(ii));
					}
				}
			}
			
			// code
			if( cdaObs.getCode() != null && !cdaObs.getCode().isSetNullFlavor() ){
				fhirObs.setCode( dtt.CD2CodeableConcept( cdaObs.getCode() ) );
			}
			
			// status
			if( cdaObs.getStatusCode() != null && !cdaObs.getStatusCode().isSetNullFlavor() ){
				if( cdaObs.getStatusCode().getCode() != null ){
					fhirObs.setStatus(vst.ObservationStatusCode2ObservationStatusEnum( cdaObs.getStatusCode().getCode() ));
				}
			}
			
			// effective
			if( cdaObs.getEffectiveTime() != null && !cdaObs.getEffectiveTime().isSetNullFlavor() ){
				fhirObs.setEffective( dtt.IVL_TS2Period(cdaObs.getEffectiveTime()) );
			}
			
			// targetSiteCode <-> bodySite
			if( cdaObs.getTargetSiteCodes() != null && !cdaObs.getTargetSiteCodes().isEmpty()){
				for(CD cd : cdaObs.getTargetSiteCodes())
				{
					if( cd != null && !cd.isSetNullFlavor() ){
						fhirObs.setBodySite( dtt.CD2CodeableConcept(cd) );
					}
				}
			}
			
			// value or dataAbsentReason
			if( cdaObs.getValues() != null && !cdaObs.getValues().isEmpty() ){
				// We traverse the values in cdaObs
				for( ANY value : cdaObs.getValues()){
					if( value == null ) continue; // If the value is null, continue
					else if( value.isSetNullFlavor() ){
						// If a null flavor exists, then we set dataAbsentReason by looking at the null-flavor value
						CodingDt DataAbsentReasonCode = vst.NullFlavor2DataAbsentReasonCode( value.getNullFlavor() );
						if( DataAbsentReasonCode != null ){
							if( fhirObs.getDataAbsentReason() == null || fhirObs.getDataAbsentReason().isEmpty() ){
								// If DataAbsentReason was not set, create a new CodeableConcept and add our code into it
								fhirObs.setDataAbsentReason( new CodeableConceptDt().addCoding(DataAbsentReasonCode));
							} else{
								// If DataAbsentReason was set, just get the CodeableConcept and add our code into it
								fhirObs.getDataAbsentReason().addCoding( DataAbsentReasonCode );
							}
						}
					} else{
						// If a non-null value which has no null-flavor exists, then we can get the value
						// Checking the type of value
						if( value instanceof CD ){
							fhirObs.setValue( dtt.CD2CodeableConcept( (CD) value ) );
						} else if(value instanceof PQ){
							fhirObs.setValue(dtt.PQ2Quantity( (PQ) value ));
						} else if(value instanceof ST){
							fhirObs.setValue(dtt.ST2String( (ST) value ));
						} else if(value instanceof IVL_PQ){
							fhirObs.setValue(dtt.IVL_PQ2Range( (IVL_PQ) value ));
						} else if(value instanceof RTO){
							fhirObs.setValue(dtt.RTO2Ratio( (RTO) value ));
						} else if(value instanceof ED){
							fhirObs.setValue(dtt.ED2Attachment( (ED) value ));
						}
						else if(value instanceof TS){
							if(((TS)value).getValue().length()>12) {
								fhirObs.setValue(dtt.TS2DateTime( (TS) value));
							} else {
								fhirObs.setValue(dtt.TS2Date( (TS) value));
							}
						}
					}
				}
			}
			
			// encounter
			if( cdaObs.getEncounters() != null && !cdaObs.getEncounters().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter : cdaObs.getEncounters() ){
					if( cdaEncounter != null && !cdaEncounter.isSetNullFlavor() ){
						// Asserting at most one encounter exists
						ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = null;
						Bundle fhirEncounterBundle = Encounter2Encounter(cdaEncounter);
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirEncounterBundle.getEntry() ){
							if( entity.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter ){
								fhirEncounter = (ca.uhn.fhir.model.dstu2.resource.Encounter) entity.getResource();
							}
						}
						
						if( fhirEncounter != null ){
							ResourceReferenceDt fhirEncounterReference = new ResourceReferenceDt();
							fhirEncounterReference.setReference( fhirEncounter.getId() );
							fhirObs.setEncounter( fhirEncounterReference );
							fhirObsBundle.addEntry( new Bundle().addEntry().setResource(fhirEncounter) );
						}
					}
				}
			}
			
			// performer
			if( cdaObs.getAuthors() != null && !cdaObs.getAuthors().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Author author : cdaObs.getAuthors() ){
					if( author != null && !author.isSetNullFlavor() ){
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = AssignedAuthor2Practitioner( author.getAssignedAuthor() );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry() ){
							if( entry.getResource() instanceof Practitioner ){
								
								fhirPractitioner = (Practitioner) entry.getResource();
							}
						}
					
						fhirObs.addPerformer().setReference(fhirPractitioner.getId());
						fhirObsBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner) );
					}
				}
			}
			
			
			
			// performer -old
			// decided that we fill performer with the information of author
			// following commented code is the former one
//			if( cdaObs.getPerformers() != null && !cdaObs.getPerformers().isEmpty() ){
//				for( org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaObs.getPerformers() ){
//					if( cdaPerformer != null && !cdaPerformer.isSetNullFlavor() ) {
//						
//						// Usage of Performer22Practitioner
//						Practitioner fhirPractitioner = null;
//						Bundle fhirPractitionerBundle = Performer22Practitioner( cdaPerformer );
//						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirPractitionerBundle.getEntry() ){
//							if( entity.getResource() instanceof Practitioner ){
//								fhirPractitioner = (Practitioner) entity.getResource();
//							}
//						}
//
//						if( fhirPractitioner != null ){
//							// Notice the usage of fhirObs.addPerformer()
//							ResourceReferenceDt practitionerReference = fhirObs.addPerformer();
//							practitionerReference.setReference( fhirPractitioner.getId() );
//							fhirObsBundle.addEntry( new Bundle().addEntry().setResource( fhirPractitioner ) );
//						}
//					}
//				}
//			}
			
			
			
			// method
			if( cdaObs.getMethodCodes() != null && !cdaObs.getMethodCodes().isEmpty() ){
				for( org.openhealthtools.mdht.uml.hl7.datatypes.CE method : cdaObs.getMethodCodes() ){
					if( method != null && !method.isSetNullFlavor() ){
						// Asserting that only one method exists
						fhirObs.setMethod( dtt.CD2CodeableConcept(method) );
					}
				}
			}
			
			// issued
			if( cdaObs.getAuthors() != null && !cdaObs.getAuthors().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Author author : cdaObs.getAuthors() ){
					if( author != null && !author.isSetNullFlavor() ){
						// get time from author
						if( author.getTime() != null && !author.getTime().isSetNullFlavor() ){
							fhirObs.setIssued( dtt.TS2Instant(author.getTime()) );
						}
					}
				}
			}
			
			// interpretation
			if( cdaObs.getInterpretationCodes() != null && !cdaObs.getInterpretationCodes().isEmpty() ){
				for( org.openhealthtools.mdht.uml.hl7.datatypes.CE cdaInterprCode : cdaObs.getInterpretationCodes() ){
					if( cdaInterprCode != null && !cdaInterprCode.isSetNullFlavor()){
						// Asserting that only one interpretation code exists
						fhirObs.setInterpretation( dtt.CD2CodeableConcept(cdaInterprCode) );
					}
				}
			}
			
			// reference range
			if( cdaObs.getReferenceRanges() != null && !cdaObs.getReferenceRanges().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange : cdaObs.getReferenceRanges() ){
					if( cdaReferenceRange != null && !cdaReferenceRange.isSetNullFlavor() ){
						fhirObs.addReferenceRange( ReferenceRange2ReferenceRange(cdaReferenceRange) );
					}
				}
			}
			return fhirObsBundle;
		}
	}
	
	// tested
	public Bundle AssignedEntity2Practitioner( AssignedEntity assignedEntity ){
		if( assignedEntity == null || assignedEntity.isSetNullFlavor() ) return null;
		else{
			Practitioner practitioner = new Practitioner();
			Bundle practitionerBundle = new Bundle();
			practitionerBundle.addEntry( new Bundle.Entry().setResource(practitioner) );
			
			// id
			IdDt resourceId = new IdDt("Practitioner", getUniqueId());
			practitioner.setId(resourceId);
			
			// identifier
			if( assignedEntity.getIds() != null && !assignedEntity.getIds().isEmpty() ){
				for( II id : assignedEntity.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						practitioner.addIdentifier( dtt.II2Identifier(id) );
					}
				}
			}
			
			// name
			if( assignedEntity.getAssignedPerson() != null && !assignedEntity.getAssignedPerson().isSetNullFlavor() ){
				for( PN pn : assignedEntity.getAssignedPerson().getNames() ){
					if( pn != null && !pn.isSetNullFlavor() ){
						// asserting that at most one name exists
						practitioner.setName( dtt.EN2HumanName( pn ) );
					}
				}
			}
			
			// address
			if( assignedEntity.getAddrs() != null && !assignedEntity.getAddrs().isEmpty() ){
				for( AD ad : assignedEntity.getAddrs() ){
					if( ad != null && !ad.isSetNullFlavor() ){
						practitioner.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			// telecom
			if( assignedEntity.getTelecoms() != null && ! assignedEntity.getTelecoms().isEmpty() ){
				for( TEL tel : assignedEntity.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						practitioner.addTelecom( dtt.TEL2ContactPoint( tel ) );
					}
				}
			}
			
			// practitionerRole.organization
			if( assignedEntity.getRepresentedOrganizations() != null && !assignedEntity.getRepresentedOrganizations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization : assignedEntity.getRepresentedOrganizations() ){
					if( cdaOrganization != null && !cdaOrganization.isSetNullFlavor() ){
						// Notice that for every organization we add, we create a new practitioner role
						
						ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = null;
						Bundle fhirOrganizationBundle = Organization2Organization( cdaOrganization );
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity :	fhirOrganizationBundle.getEntry() ){
							if( entity.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Organization ){
								fhirOrganization = (Organization) entity.getResource();
							}
						}
						
						
						if( fhirOrganization != null ){
							ResourceReferenceDt organizationReference = new ResourceReferenceDt();
							organizationReference.setReference( fhirOrganization.getId() );
							if( fhirOrganization.getName() != null ){
								organizationReference.setDisplay(fhirOrganization.getName());
							}
							practitioner.addPractitionerRole().setManagingOrganization( organizationReference );	
							practitionerBundle.addEntry( new Bundle.Entry().setResource(fhirOrganization) );
							
						}		
					}
				}	
			}
			return practitionerBundle;
		}
	}
	
	// needs testing
	public Bundle FamilyMemberOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO){
		if( cdaFHO == null || cdaFHO.isSetNullFlavor() ) return null;
		else{
			FamilyMemberHistory fhirFMH = new FamilyMemberHistory();
			Bundle fhirFMHBundle = new Bundle();
			fhirFMHBundle.addEntry( new Bundle.Entry().setResource(fhirFMH) );
			// id
			IdDt resourceId = new IdDt("FamilyMemberHistory",getUniqueId() );
			fhirFMH.setId(resourceId);
			
			// identifier
			if( cdaFHO.getIds() != null && !cdaFHO.getIds().isEmpty() ){
				for( II id : cdaFHO.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						fhirFMH.addIdentifier( dtt.II2Identifier(id) );
					}
				}
			}
			
			// patient
			fhirFMH.setPatient( new ResourceReferenceDt( patientId ) );
			
			// statusCode
			if( cdaFHO.getStatusCode() != null && !cdaFHO.getStatusCode().isSetNullFlavor() ){
				fhirFMH.setStatus( vst.FamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum( cdaFHO.getStatusCode().getCode() ) );
			}
			
			// condition <-> observation
			if( cdaFHO.getObservations() != null && !cdaFHO.getObservations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Observation observation : cdaFHO.getObservations() ){
					if( observation != null && !observation.isSetNullFlavor() ){
						ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory.Condition condition = new ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory.Condition();
						
						// onset
						if( observation.getEffectiveTime() != null && !observation.getEffectiveTime().isSetNullFlavor() ){
							condition.setOnset( dtt.IVL_TS2Period( observation.getEffectiveTime() ) );
						}
						
						// condition code
						if( observation.getValues() != null && !observation.getValues().isEmpty() ){
							for( ANY value : observation.getValues() ){
								if( value instanceof CD ){
									condition.setCode( dtt.CD2CodeableConcept( (CD) value ) );
								}
							}
						}
						
						// traversing entryRelationShips
						if( observation.getEntryRelationships() != null && !observation.getEntryRelationships().isEmpty() ){
							for( org.openhealthtools.mdht.uml.cda.EntryRelationship ERS : observation.getEntryRelationships() ){
								if( ERS != null && !ERS.isSetNullFlavor() ){
									// two kinds of typeCode for ERS
									// CAUS & SUBJ
									if( ERS.getTypeCode() == x_ActRelationshipEntryRelationship.CAUS  ){
										// CAUS
										if( ERS.getObservation() != null && !ERS.getObservation().isSetNullFlavor() ){
											if( ERS.getObservation().getCode() != null && !ERS.getObservation().getCode().isSetNullFlavor() ){
												if( ERS.getObservation().getValues() != null && !ERS.getObservation().getValues().isEmpty() ){
													for( ANY value : ERS.getObservation().getValues()){
														if( value instanceof CD ){
															if( ((CD) value).getCode().equals("419099009")){
																// TODO
																// Check if it is OK to use code value to understand if dead
																fhirFMH.setDeceased(new BooleanDt(true));
															}
															
														}
													}
												}
											}
										}
									} else if(ERS.getTypeCode() == x_ActRelationshipEntryRelationship.SUBJ ){
										// SUBJ
										if( ERS.getObservation() != null && !ERS.getObservation().isSetNullFlavor() ){
											if( ERS.getObservation().getValues() != null && !ERS.getObservation().isSetNullFlavor() ){
												if( ERS.getObservation().getCode() != null && !ERS.getObservation().getCode().isSetNullFlavor()){
													condition.setOutcome( dtt.CD2CodeableConcept(ERS.getObservation().getCode()) );
												}
											}
										}
									}
								}
							}
						}
						fhirFMH.addCondition(condition);
					}
				}
			}
			
			
			// getting information from cda->subject->relatedSubject
			if( cdaFHO.getSubject() != null && !cdaFHO.isSetNullFlavor() && cdaFHO.getSubject().getRelatedSubject() != null && !cdaFHO.getSubject().getRelatedSubject().isSetNullFlavor() ){
				org.openhealthtools.mdht.uml.cda.RelatedSubject cdaRelatedSubject = cdaFHO.getSubject().getRelatedSubject();
				
				// relationship: mother, father etc.
				if( cdaRelatedSubject.getCode() != null && !cdaRelatedSubject.getCode().isSetNullFlavor() ){
					fhirFMH.setRelationship( dtt.CD2CodeableConcept(cdaRelatedSubject.getCode()) );
				}
				
				// subject person
				if( cdaRelatedSubject.getSubject() != null && !cdaRelatedSubject.getSubject().isSetNullFlavor() ){
					org.openhealthtools.mdht.uml.cda.SubjectPerson subjectPerson = cdaRelatedSubject.getSubject();
					
					//gender
					if( subjectPerson.getAdministrativeGenderCode() != null && !subjectPerson.getAdministrativeGenderCode().isSetNullFlavor() &&
							subjectPerson.getAdministrativeGenderCode().getCode() != null){
						fhirFMH.setGender( vst.AdministrativeGenderCode2AdministrativeGenderEnum( subjectPerson.getAdministrativeGenderCode().getCode() ) );
					}
					
					// birtTime
					if( subjectPerson.getBirthTime() != null && !subjectPerson.getBirthTime().isSetNullFlavor() ){
						fhirFMH.setBorn( dtt.TS2Date(subjectPerson.getBirthTime()));
					}
					
				}
			}
			
			return fhirFMHBundle;
		}
	}
	
	public Bundle Encounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter){
		
		if( cdaEncounter == null || cdaEncounter.isSetNullFlavor() ) return null;
		else if( cdaEncounter.getMoodCode() != org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentEncounterMood.EVN ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = new ca.uhn.fhir.model.dstu2.resource.Encounter();
			
			Bundle fhirEncounterBundle = new Bundle();
			fhirEncounterBundle.addEntry( new Bundle.Entry().setResource(fhirEncounter) );
			
			// patient
			fhirEncounter.setPatient( new ResourceReferenceDt( patientId ));
			
			// id
			IdDt resourceId = new IdDt("Encounter",getUniqueId() );
			fhirEncounter.setId(resourceId);
						
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
				if( vst.StatusCode2EncounterStatusEnum( cdaEncounter.getStatusCode().getCode() ) != null ){
					fhirEncounter.setStatus( vst.StatusCode2EncounterStatusEnum(cdaEncounter.getStatusCode().getCode()) );
				}
			}
			
			// type <-> code
			if( cdaEncounter.getCode() != null && !cdaEncounter.getCode().isSetNullFlavor() ){
				fhirEncounter.addType( dtt.CD2CodeableConcept( cdaEncounter.getCode() ) );
			}
			
			// priority <-> priorityCode
			if( cdaEncounter.getPriorityCode() != null && !cdaEncounter.getPriorityCode().isSetNullFlavor() ){
				fhirEncounter.setPriority( dtt.CD2CodeableConcept( cdaEncounter.getPriorityCode() ) );
			}
			
			// performer
			if( cdaEncounter.getPerformers() != null && !cdaEncounter.getPerformers().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaEncounter.getPerformers()  ){
					if( cdaPerformer != null && !cdaPerformer.isSetNullFlavor() ){
						ca.uhn.fhir.model.dstu2.resource.Encounter.Participant fhirParticipant = new ca.uhn.fhir.model.dstu2.resource.Encounter.Participant();
						
						fhirParticipant.addType().addCoding( vst.ParticipationType2ParticipationTypeCode( ParticipationType.PRF ) );
						
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = Performer22Practitioner( cdaPerformer );
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirPractitionerBundle.getEntry() ){
							if( entity.getResource() instanceof Practitioner ){
								fhirPractitioner = (Practitioner) entity.getResource();
							}
						}
						
						if( fhirPractitioner != null ){
							ResourceReferenceDt practitionerReference = new ResourceReferenceDt();
							practitionerReference.setReference( fhirPractitioner.getId() );
							fhirParticipant.setIndividual( practitionerReference );
							fhirEncounterBundle.addEntry( new Bundle().addEntry().setResource( fhirPractitioner ) );
							
						}
						fhirEncounter.addParticipant(fhirParticipant);
					}
				}
			}
			
			// period <-> .effectiveTime (low & high)
			if( cdaEncounter.getEffectiveTime() != null && !cdaEncounter.getEffectiveTime().isSetNullFlavor() ){
				fhirEncounter.setPeriod( dtt.IVL_TS2Period( cdaEncounter.getEffectiveTime() ) );
			}
			
			// location <-> .participant[typeCode=LOC]
			if( cdaEncounter.getParticipants() != null && !cdaEncounter.getParticipants().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Participant2 cdaParticipant : cdaEncounter.getParticipants() ){
					if( cdaParticipant != null && !cdaParticipant.isSetNullFlavor() ){
						
						// checking if the participant is location
						if( cdaParticipant.getTypeCode() == ParticipationType.LOC ){
							if( cdaParticipant.getParticipantRole() != null && !cdaParticipant.getParticipantRole().isSetNullFlavor() ){
								// We first make the mapping to a resource.location
								// then, we create a resource.encounter.location
								// then, we add the resource.location to resource.encounter.location
								ca.uhn.fhir.model.dstu2.resource.Location fhirLocation = null;
								// usage of ParticipantRole2Location
								Bundle fhirLocationBundle = ParticipantRole2Location( cdaParticipant.getParticipantRole() );

								for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirLocationBundle.getEntry() ){
									
									if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Location ){
										System.out.println(2);
										fhirLocation = (ca.uhn.fhir.model.dstu2.resource.Location) entry.getResource();
									}
								}
								
								fhirEncounterBundle.addEntry( new Bundle.Entry().setResource(fhirLocation) );
								fhirEncounter.addLocation().setLocation( new ResourceReferenceDt( fhirLocation.getId() ) );
							}
						}
					}
				}
			}
			
			return fhirEncounterBundle;
		}
	}
	
	// never used
	public Bundle Entity2Group( Entity entity ){
		if( entity == null || entity.isSetNullFlavor() ) return null;
		else if( entity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND ) return null;
		else{
			Group group = new Group();
			
			Bundle groupBundle = new Bundle();
			groupBundle.addEntry( new Bundle.Entry().setResource(group) );
			
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

			return groupBundle;
		}
	}

	// tested
	public Bundle Organization2Organization ( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization ){
		if( cdaOrganization == null || cdaOrganization.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = new ca.uhn.fhir.model.dstu2.resource.Organization();
			
			Bundle fhirOrganizationBundle  = new Bundle();
			fhirOrganizationBundle.addEntry( new Bundle.Entry().setResource(fhirOrganization) );
			
			// id
			IdDt resourceId = new IdDt("Organization",getUniqueId() );
			fhirOrganization.setId(resourceId);
			
			// identifier
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
			
			// name
			if( cdaOrganization.getNames() != null && !cdaOrganization.isSetNullFlavor() ){
				for( ON name:cdaOrganization.getNames() ){
					if( name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty() ){
						fhirOrganization.setName( name.getText() );
					}
				}
			}
			
			// contact <-> telecom
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
			
			// address
			if( cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty() ){
				for( AD ad : cdaOrganization.getAddrs()  ){
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirOrganization.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			return fhirOrganizationBundle;
		}
	}

	// tested
	public Bundle PatientRole2Patient(PatientRole patRole){
			if( patRole == null || patRole.isSetNullFlavor() ) return null;
			else{
				Patient patient = new Patient();
				
				Bundle patientBundle  = new Bundle();
				patientBundle.addEntry( new Bundle.Entry().setResource(patient) );
				
				// id
				patient.setId(patientId);
//				IdDt resourceId = new IdDt("Patient", getUniqueId());
//				patient.setId(resourceId);
				
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
					
					ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = null;
					Bundle fhirOrganizationBundle = Organization2Organization( patRole.getProviderOrganization() );
					for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirOrganizationBundle.getEntry() ){
						if( entity.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Organization ){
							fhirOrganization = (Organization) entity.getResource();
						}
					}
					
					if( fhirOrganization != null && !fhirOrganization.isEmpty() ){
						ResourceReferenceDt organizationReference = new ResourceReferenceDt();
						organizationReference.setReference(fhirOrganization.getId());
						if( fhirOrganization.getName() != null ){
							organizationReference.setDisplay( fhirOrganization.getName() );
						}
						patientBundle.addEntry( new Bundle().addEntry().setResource( fhirOrganization ) );
						
						patient.setManagingOrganization(organizationReference);
					}
				}
				
				// guardian <-> patient.guardians
				if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && 
						patRole.getPatient().getGuardians() != null && !patRole.getPatient().getGuardians().isEmpty() )
				{
					for( org.openhealthtools.mdht.uml.cda.Guardian guardian : patRole.getPatient().getGuardians() ){
						patient.addContact( Guardian2Contact(guardian) );
					}
				}
				
				
				// extensions
				
				// extRace <-> patient.raceCode
				if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getRaceCode() != null && !patRole.getPatient().getRaceCode().isSetNullFlavor())
				{
					ExtensionDt extRace = new ExtensionDt();
					extRace.setModifier(false);
					extRace.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
					CD raceCode = patRole.getPatient().getRaceCode();
					extRace.setValue( dtt.CD2CodeableConcept(raceCode) );
					patient.addUndeclaredExtension( extRace );
				}
	
				// extEthnicity <-> patient.ethnicGroupCode
				if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getEthnicGroupCode() != null && !patRole.getPatient().getEthnicGroupCode().isSetNullFlavor() )
				{
					ExtensionDt extEthnicity = new ExtensionDt();
					extEthnicity.setModifier(false);
					extEthnicity.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
					CD ethnicGroupCode = patRole.getPatient().getEthnicGroupCode();
					extEthnicity.setValue( dtt.CD2CodeableConcept(ethnicGroupCode) );
					patient.addUndeclaredExtension(extEthnicity);
				}
				
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
				
				// extBirthPlace
	//			ExtensionDt extBirthPlace = new ExtensionDt();
	//			extBirthPlace.setModifier(false);
	//			extBirthPlace.setUrl("http://hl7.org/fhir/extension-birthplace.html");
	//				if( patRole.getPatient() != null && !patRole.getPatient().isSetNullFlavor() && patRole.getPatient().getBirthplace() != null && !patRole.getPatient().getBirthplace().isSetNullFlavor() )
	//			{
	//				extBirthPlace.setValue(  dtt.sometransformer( patRole.getPatient().getBirthplace() ) );
	//				// Birthplace mapping
	//				// We can get the Birthplace info from ccd
	//				// However, there is no type to put it
	//			}
	//			patient.addUndeclaredExtension(extBirthPlace);
				
				return patientBundle;
			}
		}

	// tested
	public Bundle Performer22Practitioner( Performer2 cdaPerformer ){
		if( cdaPerformer == null || cdaPerformer.isSetNullFlavor() ) return null;
		else{
			return AssignedEntity2Practitioner( cdaPerformer.getAssignedEntity() );
		}
	}
	
	// tested
	public Bundle Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaPr){
		// TODO: used <-> device
		if( cdaPr == null || cdaPr.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Procedure fhirPr = new ca.uhn.fhir.model.dstu2.resource.Procedure();
			Bundle fhirPrBundle  = new Bundle();
			fhirPrBundle.addEntry( new Bundle.Entry().setResource(fhirPr) );
			
			// subject
			fhirPr.setSubject( new ResourceReferenceDt( patientId ) );
			
			// id
			IdDt resourceId = new IdDt("Procedure",getUniqueId() );
			fhirPr.setId(resourceId);
	
			// identifier
			if( cdaPr.getIds() != null && !cdaPr.getIds().isEmpty() ){
				for( II id : cdaPr.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						fhirPr.addIdentifier( dtt.II2Identifier(id) );
					}
				}
			}
			
			// performed
			if( cdaPr.getEffectiveTime() != null && !cdaPr.getEffectiveTime().isSetNullFlavor() ){
				fhirPr.setPerformed( dtt.IVL_TS2Period( cdaPr.getEffectiveTime() )  );
			}
			
			// bodySite
			if( cdaPr.getTargetSiteCodes() != null && !cdaPr.getTargetSiteCodes().isEmpty() ){
				for( CD cd : cdaPr.getTargetSiteCodes() ){
					if( cd != null && !cd.isSetNullFlavor() ){
						fhirPr.addBodySite( dtt.CD2CodeableConcept(cd) );
					}
				}
			}
			
			// performer
			if( cdaPr.getPerformers() != null && !cdaPr.getPerformers().isEmpty() ){
				for( Performer2 performer : cdaPr.getPerformers() ){
					if( performer != null && !performer.isSetNullFlavor() ){
						fhirPr.addPerformer( Performer22Performer(performer) );
					}
				}
			}
			
			// status
			if( cdaPr.getStatusCode() != null && !cdaPr.getStatusCode().isSetNullFlavor() && cdaPr.getStatusCode().getCode() != null ){
				ProcedureStatusEnum status = vst.StatusCode2ProcedureStatusEnum( cdaPr.getStatusCode().getCode() );
				if( status != null ){
					fhirPr.setStatus( status ); 
				}
			}
			
			// code
			if( cdaPr.getCode() != null && !cdaPr.getCode().isSetNullFlavor() ){
				fhirPr.setCode( dtt.CD2CodeableConcept( cdaPr.getCode() ) );
			}
			 
			
			
			// used <-> device
			// no example found in example cda.xml file
			// however, it exists in transformed version
			
			return fhirPrBundle;
		}
	}

	// tested
	public ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( Guardian guardian ){
			
		if( guardian == null || guardian.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Patient.Contact contact = new ca.uhn.fhir.model.dstu2.resource.Patient.Contact();
			
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
			return contact;
		}
	}

	// tested
	public Communication LanguageCommunication2Communication( LanguageCommunication LC ){
		if(LC == null || LC.isSetNullFlavor()) return null;
		else{
			Communication communication = new Communication();
			
			// language
			if( LC.getLanguageCode() != null && !LC.getLanguageCode().isSetNullFlavor() ){
				communication.setLanguage(  dtt.CD2CodeableConcept( LC.getLanguageCode() )  );
			}
			
			// preferred
			if( LC.getPreferenceInd() != null && !LC.getPreferenceInd().isSetNullFlavor() ){
				communication.setPreferred(  dtt.BL2Boolean( LC.getPreferenceInd() )  );
			}
			return communication;
		}
	}

	// tested
	public ca.uhn.fhir.model.dstu2.resource.Procedure.Performer Performer22Performer( Performer2 cdaPerformer ){
		if( cdaPerformer == null || cdaPerformer.isSetNullFlavor() || cdaPerformer.getAssignedEntity() == null || cdaPerformer.getAssignedEntity().isSetNullFlavor() ) return null;
		else{
			Performer fhirPerformer = new Performer();
			
			Practitioner fhirPractitioner = null;
			Bundle fhirPractitionerBundle = AssignedEntity2Practitioner( cdaPerformer.getAssignedEntity() );
			for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirPractitionerBundle.getEntry() ){
				if( entity.getResource() instanceof Practitioner ){
					fhirPractitioner = (Practitioner) entity.getResource();
				}
			}
			
			if( fhirPractitioner != null && !fhirPractitioner.isEmpty() ){
				ResourceReferenceDt actorReference = new ResourceReferenceDt();
				actorReference.setReference(fhirPractitioner.getId());
				if( fhirPractitioner.getName() != null && fhirPractitioner.getName().getText() != null ){
					actorReference.setDisplay( fhirPractitioner.getName().getText() );
				}
				fhirPractitionerBundle.addEntry( new Bundle().addEntry().setResource( fhirPractitioner ) );
				fhirPerformer.setActor(actorReference);
			}
			return fhirPerformer;
		}
	}
	
	// needs testing
	// Following method ( ReferenceRange2ReferenceRange ) will be used as helper for Observation transformation methods
	public Observation.ReferenceRange ReferenceRange2ReferenceRange( org.openhealthtools.mdht.uml.cda.ReferenceRange cdaRefRange){
		if( cdaRefRange == null || cdaRefRange.isSetNullFlavor() ) return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange fhirRefRange = new ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange();
			
			// Notice that we get all the desired information from cdaRefRange.ObservationRange
			// We may think of transforming ObservationRange instead of ReferenceRange
				if( cdaRefRange.getObservationRange() != null && !cdaRefRange.isSetNullFlavor()){
				
				// low - high
				if( cdaRefRange.getObservationRange().getValue() != null && !cdaRefRange.getObservationRange().getValue().isSetNullFlavor()){
					if( cdaRefRange.getObservationRange().getValue() instanceof IVL_PQ ){
						IVL_PQ cdaRefRangeValue = ( (IVL_PQ) cdaRefRange.getObservationRange().getValue() );
						// low
						if( cdaRefRangeValue.getLow() != null && !cdaRefRangeValue.getLow().isSetNullFlavor() ){
							fhirRefRange.setLow( dtt.PQ2SimpleQuantityDt( cdaRefRangeValue.getLow() ) );
						}
						// high
						if( cdaRefRangeValue.getHigh() != null && !cdaRefRangeValue.getHigh().isSetNullFlavor() ){
							fhirRefRange.setHigh( dtt.PQ2SimpleQuantityDt( cdaRefRangeValue.getHigh() ) );
						}
					}
				}
				
				// meaning
				if( cdaRefRange.getObservationRange().getInterpretationCode() != null && !cdaRefRange.getObservationRange().getInterpretationCode().isSetNullFlavor() ){
					fhirRefRange.setMeaning( dtt.CD2CodeableConcept(cdaRefRange.getObservationRange().getInterpretationCode()) );
				}
				
				// text
				if( cdaRefRange.getObservationRange().getText() != null && !cdaRefRange.getObservationRange().getText().isSetNullFlavor()){
					if( cdaRefRange.getObservationRange().getText().getText() != null && !cdaRefRange.getObservationRange().getText().getText().isEmpty() ){
						fhirRefRange.setText( cdaRefRange.getObservationRange().getText().getText() );
					}
				}
			}
			return fhirRefRange;
		}
	}
	
// necip end

	

// ismail start
	
	// needs testing
	public Bundle ProblemConcernAct2Condition(ProblemConcernAct probAct) {
		
		if( probAct == null || probAct.isSetNullFlavor()) return null;
		
		Bundle conditionBundle = new Bundle();

		for(EntryRelationship entryRelationship : probAct.getEntryRelationships()){
			
			Condition condition = new Condition();
			
			conditionBundle.addEntry( new Bundle.Entry().setResource(condition) );
			
			// id
			IdDt resourceId = new IdDt("Condition", getUniqueId());
			condition.setId( resourceId );
			
			// identifier
			if( probAct.getIds() != null && !probAct.getIds().isEmpty() ){
				for( II ii : probAct.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						condition.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// patient
			condition.setPatient( new ResourceReferenceDt( patientId ) );

			
			// TODO: Severity
			// See https://www.hl7.org/fhir/daf/condition-daf.html
			// Couldn't found in the CDA example
			
			// encounter
			if( probAct.getEncounters() != null && !probAct.getEncounters().isEmpty() ){
				if(probAct.getEncounters().get(0) != null && probAct.getEncounters().get(0).isSetNullFlavor() ){
					ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = null;
					Bundle fhirEncounterBundle = Encounter2Encounter(probAct.getEncounters().get(0));
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirEncounterBundle.getEntry()){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter )
							fhirEncounter = (ca.uhn.fhir.model.dstu2.resource.Encounter) entry.getResource();
					}
					
					condition.setEncounter( new ResourceReferenceDt(fhirEncounter.getId()) );
					conditionBundle.addEntry( new Bundle.Entry().setResource(fhirEncounter));
				}
			}
			
			// asserter <-> author
			if( probAct.getAuthors() != null && !probAct.getAuthors().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Author author : probAct.getAuthors() ){
					if( author != null && !author.isSetNullFlavor() ){
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = AssignedAuthor2Practitioner( author.getAssignedAuthor() );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry() ){
							if( entry.getResource() instanceof Practitioner ){
								
								fhirPractitioner = (Practitioner) entry.getResource();
							}
						}
					
					condition.setAsserter( new ResourceReferenceDt( fhirPractitioner.getId() ) );
					conditionBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner) );	
					}
				}
			}
			
			// date recorded
			// if nullCheckForDateRecorded evaluates to true, then it can be stated that it is not null and null-flavor is NOT set
			boolean nullCheckForDateRecorded =  entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor()
					&& entryRelationship.getObservation().getAuthors() != null && !entryRelationship.getObservation().getAuthors().isEmpty()
					&& entryRelationship.getObservation().getAuthors().get(0) != null && !entryRelationship.getObservation().getAuthors().get(0).isSetNullFlavor()
					&& entryRelationship.getObservation().getAuthors().get(0).getTime() != null && !entryRelationship.getObservation().getAuthors().get(0).getTime().isSetNullFlavor();
			
			if( nullCheckForDateRecorded ){
				DateDt dateRecorded = dtt.TS2Date(entryRelationship.getObservation().getAuthors().get(0).getTime());
				condition.setDateRecorded( dateRecorded );
			}
			
			
			// code <-> value
			if( entryRelationship.getObservation() != null  && entryRelationship.getObservation().isSetNullFlavor()
					&& entryRelationship.getObservation().getValues() != null && !entryRelationship.getObservation().getValues().isEmpty()){
				if( entryRelationship.getObservation().getValues().get(0) != null && !entryRelationship.getObservation().getValues().get(0).isSetNullFlavor() ) {
					if( entryRelationship.getObservation().getValues().get(0) instanceof CD ){
						condition.setCode( dtt.CD2CodeableConcept( (CD) entryRelationship.getObservation().getValues().get(0) ) );
					}
				}
			}
			
			
			// category
			if( entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() ){
				if( entryRelationship.getObservation().getCode() != null && !entryRelationship.getObservation().getCode().isSetNullFlavor() ){
					CodeableConceptDt categoryCoding = dtt.CD2CodeableConcept( entryRelationship.getObservation().getCode() );
					
					// TODO: Necip
					// See the coding example below
					// How to map this coding to  ConditionCategoryCodesEnum ?
					// Coding example:
					/*	<code code="64572001" displayName="Condition" codeSystemName="SNOMED-CT" codeSystem="2.16.840.1.113883.6.96">
					<!-- a.	This code SHALL contain at least one [1..*] translation, which SHOULD be selected from ValueSet Problem Type (LOINC)
					<translation code="75323-6" codeSystem="2.16.840.1.113883.6.1" codeSystemName="LOINC" displayName="Condition"/>
					-->
					<translation nullFlavor="NI"/>
				</code> */
					
					
				}
			}
			
			// Following lines are the former mapping for category
			// However, seems unappropriate
//			CodingDt codingForCategory = new CodingDt();
//			BoundCodeableConceptDt boundCodeableConceptDt = new BoundCodeableConceptDt();
//			if( entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() 
//					&& entryRelationship.getObservation().getCode() != null && !entryRelationship.getObservation().getCode().isSetNullFlavor()){
//
//				if( entryRelationship.getObservation().getCode().getCode() != null ){
//					codingForCategory.setCode( entryRelationship.getObservation().getCode().getCode() );
//				}
//				if( entryRelationship.getObservation().getCode().getDisplayName() != null ){
//					codingForCategory.setDisplay(  entryRelationship.getObservation().getCode().getDisplayName() );
//				}
//				if( entryRelationship.getObservation().getCode().getCodeSystem() != null ){
//					codingForCategory.setSystem( entryRelationship.getObservation().getCode().getCodeSystem() );
//				}
//				
//				boundCodeableConceptDt.addCoding( codingForCategory );
//				condition.setCategory( boundCodeableConceptDt );
//				
//			}
			
			/////
			
		
			
			// onset and abatement
			if(entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() 
			&& entryRelationship.getObservation().getEffectiveTime() != null && 
					!entryRelationship.getObservation().getEffectiveTime().isSetNullFlavor())
			{
				
				IVXB_TS low = entryRelationship.getObservation().getEffectiveTime().getLow();
				IVXB_TS high = entryRelationship.getObservation().getEffectiveTime().getHigh();
				
				// low <-> onset
				if( low != null && !low.isSetNullFlavor() ){
					condition.setOnset( dtt.TS2DateTime(low ) );
				}
				
				// high <-> abatement
				if( high != null && !high.isSetNullFlavor() ){
					condition.setAbatement( dtt.TS2DateTime(high) );
				}

			}
	        
			// bodysite
			if( entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() 
					&&entryRelationship.getObservation().getValues() != null && !entryRelationship.getObservation().getValues().isEmpty()){
				for( ANY value : entryRelationship.getObservation().getValues() ){
					if( value == null || value.isSetNullFlavor() ) continue;
					else{
						if( value instanceof CD ){
							condition.addBodySite( dtt.CD2CodeableConcept( (CD) value ) );
						}
					}
				}
			}
		}
		return conditionBundle;
	}

	// needs testing
	public Bundle ManufacturedProduct2Medication(ManufacturedProduct manPro) {
		
		if( manPro == null || manPro.isSetNullFlavor() ) return null;
		
		Medication medication = new Medication();
		
		Bundle medicationBundle = new Bundle();
		medicationBundle.addEntry( new Bundle.Entry().setResource(medication) );
		
		// id
		IdDt resourceId = new IdDt( "Medication" , getUniqueId());
		medication.setId( resourceId );
		
		// code <-> manufacturedMaterial.code
		if( manPro.getManufacturedMaterial() != null && !manPro.getManufacturedMaterial().isSetNullFlavor() ){
			if( manPro.getManufacturedMaterial().getCode() != null && !manPro.getManufacturedMaterial().isSetNullFlavor() ){
				medication.setCode( dtt.CD2CodeableConcept( manPro.getManufacturedMaterial().getCode() ) );
			}
		}
		
		
		// is_brand and manufacturer
		ResourceReferenceDt resourceReferenceManu = new ResourceReferenceDt();
		
		if( manPro.getManufacturerOrganization() != null && !manPro.getManufacturerOrganization().isSetNullFlavor() ){
			// is_brand
			medication.setIsBrand(true);
			
			// manufacturer
			Bundle orgBundle = Organization2Organization( manPro.getManufacturerOrganization() );
			Organization org = null;
			for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : orgBundle.getEntry()){
				if( entry.getResource() instanceof Organization )
						org = (Organization) entry.getResource();
			}
			resourceReferenceManu.setReference( org.getId() );
				if( manPro.getManufacturerOrganization().getNames() != null){
				if( manPro.getManufacturerOrganization().getNames().size() != 0 )
					resourceReferenceManu.setDisplay( manPro.getManufacturerOrganization().getNames().get(0).getText() );
			}
			medication.setManufacturer( resourceReferenceManu );
			medicationBundle.addEntry( new Bundle.Entry().setResource(org) );
		} else {
			medication.setIsBrand(false);
		}
		
		return medicationBundle;
	}
	
	// needs testing
	public Bundle MedicationActivity2MedicationStatement(MedicationActivity cdaMedAct) {
		
		if( cdaMedAct == null || cdaMedAct.isSetNullFlavor() ) return null;
		// TODO: Necip:
		// Do we need to check moodCode here?
//		if (subAd.getMoodCode() != x_DocumentSubstanceMood.EVN ) return null;
		
		MedicationStatement fhirMedSt = new MedicationStatement();
		
		Bundle medStatementBundle = new Bundle();
		medStatementBundle.addEntry( new Bundle.Entry().setResource(fhirMedSt) );

		// id
		IdDt resourceId = new IdDt( "MedicationActivity", getUniqueId()  );
		fhirMedSt.setId( resourceId );
		
		
		// identifier
		if( cdaMedAct.getIds() != null && !cdaMedAct.getIds().isEmpty() ){
			for(II ii : cdaMedAct.getIds()){
				fhirMedSt.addIdentifier( dtt.II2Identifier(ii) );
			}
		}
		
		
		// status
		if( cdaMedAct.getStatusCode() != null && !cdaMedAct.getStatusCode().isSetNullFlavor()){
			if( cdaMedAct.getStatusCode().getCode() != null && !cdaMedAct.getStatusCode().getCode().isEmpty() ){
				MedicationStatementStatusEnum statusCode = vst.StatusCode2MedicationStatementStatusEnum( cdaMedAct.getStatusCode().getCode());
				if( statusCode != null  ){
					fhirMedSt.setStatus( statusCode );
				}
			}
		}
		
		// patient
		fhirMedSt.setPatient(new ResourceReferenceDt( patientId ));
		
		// medication <-> cdaMedAct.consumable.manufacturedProduct
		if( cdaMedAct.getConsumable() != null && !cdaMedAct.getConsumable().isSetNullFlavor() ){
			if( cdaMedAct.getConsumable().getManufacturedProduct() != null && !cdaMedAct.getConsumable().getManufacturedProduct().isSetNullFlavor() ){

				Medication fhirMedication = null;
				Bundle fhirMedicationBundle = ManufacturedProduct2Medication(cdaMedAct.getConsumable().getManufacturedProduct() );
				
				for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry() ){
					if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication){
						fhirMedication = (ca.uhn.fhir.model.dstu2.resource.Medication) entry.getResource();
					}
				}
				
				medStatementBundle.addEntry( new Bundle.Entry().setResource( fhirMedication ) );
				fhirMedSt.setMedication( new ResourceReferenceDt( fhirMedication.getId() ) );
			}
		}
		
		// effectiveTime
		if( cdaMedAct.getEffectiveTimes() != null && !cdaMedAct.getEffectiveTimes().isEmpty() ){
			for( org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedAct.getEffectiveTimes() ){
				if( ts != null && !ts.isSetNullFlavor() ){
					if( ts instanceof IVL_TS ){
							fhirMedSt.setEffective( dtt.IVL_TS2Period( (IVL_TS) ts ) );
					}
				}
			}
		}
		
		// dosage
		MedicationStatement.Dosage fhirDosage = fhirMedSt.addDosage();
		
		// dosage.route
		if( cdaMedAct.getRouteCode() != null && !cdaMedAct.getRouteCode().isSetNullFlavor() ){
			fhirDosage.setRoute( dtt.CD2CodeableConcept( cdaMedAct.getRouteCode() ) );
		}
		
		// dosage.quantity
		if( cdaMedAct.getDoseQuantity() != null && !cdaMedAct.getDoseQuantity().isSetNullFlavor() ){
			fhirDosage.setQuantity( dtt.PQ2SimpleQuantityDt( cdaMedAct.getDoseQuantity() ) );
		}
		
		// dosage.rate
		if( cdaMedAct.getRateQuantity() != null && !cdaMedAct.getRateQuantity().isSetNullFlavor() ){
			fhirDosage.setRate( dtt.IVL_PQ2Range( cdaMedAct.getRateQuantity() ) );
		}
		
		// dosage.maxDosePerPeriod
		if( cdaMedAct.getMaxDoseQuantity() != null && !cdaMedAct.getMaxDoseQuantity().isSetNullFlavor() ){
			// cdaDataType.RTO does nothing but extends cdaDataType.RTO_PQ_PQ
			fhirDosage.setMaxDosePerPeriod( dtt.RTO2Ratio( (RTO) cdaMedAct.getMaxDoseQuantity() ) );
		}
		
		
		
		// wasNotTaken
		if( cdaMedAct.getNegationInd() != null ){
			fhirMedSt.setWasNotTaken( cdaMedAct.getNegationInd() );
		}
		
		// reason
		for( EntryRelationship ers : cdaMedAct.getEntryRelationships() ){
			if( ers.getTypeCode() == x_ActRelationshipEntryRelationship.RSON ){
				
				
				if( ers.getObservation() != null && !ers.isSetNullFlavor() ){
					
					// to set reasonForUse, we need to set wasNotTaken to false
					fhirMedSt.setWasNotTaken(false);
					
					// reasonForUse
					ca.uhn.fhir.model.dstu2.resource.Observation fhirObservation = null;
					Bundle fhirObservationBundle = Observation2Observation( ers.getObservation() );
					
					for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirObservationBundle.getEntry() ){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Observation ){
							fhirObservation = (ca.uhn.fhir.model.dstu2.resource.Observation) entry.getResource();
						}
					}
					
					medStatementBundle.addEntry( new Bundle.Entry().setResource( fhirObservation ) );
					fhirMedSt.setReasonForUse( new ResourceReferenceDt( fhirObservation.getId() ) );
					
				}

				// reasonNotTaken
				if( cdaMedAct.getNegationInd() != null && cdaMedAct.getNegationInd() ){
					// TODO: Necip:
					// Do we need to fill reasonNotTaken?
				}
			}
		}
		return medStatementBundle;	
	}
	
	// needs testing
	public Bundle MedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMediDisp) {
		
		if( cdaMediDisp == null || cdaMediDisp.isSetNullFlavor() ) return null;
		
		if( cdaMediDisp.getMoodCode().getLiteral() == "EVN" ){
			
			MedicationDispense fhirMediDisp = new MedicationDispense();

			Bundle fhirMediDispBundle = new Bundle();
			fhirMediDispBundle.addEntry( new Bundle.Entry().setResource(fhirMediDisp));
			
			// patient
			fhirMediDisp.setPatient( new ResourceReferenceDt( patientId ) );
			
			// id
			IdDt resourceId = new IdDt( "MedicationDispense", getUniqueId() );
			fhirMediDisp.setId( resourceId );
			
			// identifier
			if( cdaMediDisp.getIds() != null &  !cdaMediDisp.getIds().isEmpty() ){
				for( II ii : cdaMediDisp.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						// Asserting at most one identifier exists
						fhirMediDisp.setIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// status
			if( cdaMediDisp.getStatusCode() != null && !cdaMediDisp.getStatusCode().isSetNullFlavor()){
				if( cdaMediDisp.getStatusCode().getCode() != null && !cdaMediDisp.getStatusCode().getCode().isEmpty() ){
					MedicationDispenseStatusEnum mediDispStatEnum = vst.StatusCode2MedicationDispenseStatusEnum( cdaMediDisp.getStatusCode().getCode() );
					if( mediDispStatEnum != null ){
						fhirMediDisp.setStatus( mediDispStatEnum );
					}
				}
			}
			
			// type <-> code
			if( cdaMediDisp.getCode() != null && !cdaMediDisp.getCode().isSetNullFlavor() ){
				fhirMediDisp.setType( dtt.CD2CodeableConcept( cdaMediDisp.getCode() ) );
			}
			
			// medication <-> product.manufacturedProduct
			if( cdaMediDisp.getProduct() != null && !cdaMediDisp.getProduct().isSetNullFlavor() ){
				if( cdaMediDisp.getProduct().getManufacturedProduct() != null && !cdaMediDisp.getProduct().getManufacturedProduct().isSetNullFlavor() ){

					ca.uhn.fhir.model.dstu2.resource.Medication fhirMedication = null;
					Bundle fhirMedicationBundle = ManufacturedProduct2Medication( cdaMediDisp.getProduct().getManufacturedProduct() );
					
					for(  ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry()  ){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication){
							fhirMedication = (ca.uhn.fhir.model.dstu2.resource.Medication) entry.getResource();
						}
					}
					
					fhirMediDispBundle.addEntry( new Bundle.Entry().setResource( fhirMedication ) );
					fhirMediDisp.setMedication( new ResourceReferenceDt( fhirMedication.getId() ) );
				}
			}
			
			// dispenser
			if( cdaMediDisp.getPerformers() != null && !cdaMediDisp.getPerformers().isEmpty()){
				for( org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaMediDisp.getPerformers() ){
					if( cdaPerformer != null && !cdaPerformer.isSetNullFlavor() ){
						// Asserting that at most one performer exists
						ca.uhn.fhir.model.dstu2.resource.Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = Performer22Practitioner( cdaPerformer );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry() ){
							if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Practitioner ){
								fhirPractitioner = (ca.uhn.fhir.model.dstu2.resource.Practitioner) entry.getResource();
							}
						}
						
						fhirMediDispBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner));
						fhirMediDisp.setDispenser( new ResourceReferenceDt( fhirPractitioner.getId() ) );
					}
				}
			}
			
			// quantity
			if( cdaMediDisp.getQuantity() != null && !cdaMediDisp.getQuantity().isSetNullFlavor() ){
				fhirMediDisp.setQuantity( dtt.PQ2SimpleQuantityDt( cdaMediDisp.getQuantity() ) );
			}
			
			// whenPrepared and whenHandedOver
			int effectiveTimeCount = 0;
			if( cdaMediDisp.getEffectiveTimes() != null && !cdaMediDisp.getEffectiveTimes().isEmpty() ){
				for( SXCM_TS ts : cdaMediDisp.getEffectiveTimes() ){
					if( effectiveTimeCount == 0 ){
						// whenPrepared: 1st effectiveTime
						if( ts != null && !ts.isSetNullFlavor() ){
							fhirMediDisp.setWhenPrepared( dtt.TS2DateTime(ts) );
						}
						effectiveTimeCount++;
					} else if( effectiveTimeCount == 1 ){
						// whenHandedOver: 2nd effectiveTime
						if( ts != null && !ts.isSetNullFlavor() ){
							fhirMediDisp.setWhenHandedOver( dtt.TS2DateTime(ts) );
						}
						effectiveTimeCount++;
					}
				}
			}
			
			// dosageInstruction
			MedicationDispense.DosageInstruction fhirDosageInstruction = fhirMediDisp.addDosageInstruction();
			
			// TODO: Necip:
			// The information used for dosageInstruction is used for different fields, too.
			// Determine which field the information should fit
			// Some other question: where to put cdaMediDisp.repeatNumber
			
			// dosageInstruction.timing <-> effectiveTimes
			if( cdaMediDisp.getEffectiveTimes() != null && !cdaMediDisp.getEffectiveTimes().isEmpty() ){
				TimingDt fhirTiming = new TimingDt();
				
				// adding effectiveTimes to fhirTiming
				for( org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMediDisp.getEffectiveTimes() ){
					if( ts != null && !ts.isSetNullFlavor() ){
						fhirTiming.addEvent( dtt.TS2DateTime(ts) );
					}
				}
				
				// setting fhirTiming for dosageInstruction if it is not empty
				if( !fhirTiming.isEmpty() ){
						fhirDosageInstruction.setTiming(fhirTiming);
				}
			}
			
			// dosageInstruction.dose
			if( cdaMediDisp.getQuantity() != null && !cdaMediDisp.getQuantity().isSetNullFlavor() ){
				fhirDosageInstruction.setDose( dtt.PQ2SimpleQuantityDt( cdaMediDisp.getQuantity() ) );
			}
			return fhirMediDispBundle;
		}
		
		return null;
	}

	public Bundle ParticipantRole2Location(ParticipantRole patRole) {
	
		if( patRole == null || patRole.isSetNullFlavor() ) return null;
		else{
			Location fhirLocation = new Location();
			
			Bundle fhirLocationBundle = new Bundle();
			fhirLocationBundle.addEntry( new Bundle.Entry().setResource(fhirLocation));
			
			// id
			IdDt resourceId = new IdDt( "Location", getUniqueId() );
			fhirLocation.setId(resourceId);
			
			// identifier
			if( patRole.getIds() != null && !patRole.getIds().isEmpty() ){
				for( II ii : patRole.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirLocation.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}

			// name
			if( patRole.getPlayingEntity() != null && !patRole.getPlayingEntity().isSetNullFlavor() ){
				if( patRole.getPlayingEntity().getNames() != null && !patRole.getPlayingEntity().getNames().isEmpty() ){
					for( PN pn : patRole.getPlayingEntity().getNames() ){
						// Asserting that at most one name exists
						if( pn != null && !pn.isSetNullFlavor() ){
							fhirLocation.setName( pn.getText() );
						}
					}
				}
			}
			
			// telecom
			if( patRole.getTelecoms() != null && !patRole.getTelecoms().isEmpty() ){
				for( TEL tel : patRole.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirLocation.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}

			// address
			if( patRole.getAddrs() != null && !patRole.getAddrs().isEmpty() ){
				for( AD ad : patRole.getAddrs() ){
					// Asserting that at most one address exists
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirLocation.setAddress( dtt.AD2Address(ad) );
					}
				}
			}			
			
//			//NAME and TYPE
//			if( patRole.getCode() != null  ){
//				if( !patRole.getCode().isSetNullFlavor()){
//					if(patRole.getCode().getDisplayName() != null)
//						fhirLocation.setName( patRole.getCode().getDisplayName() );
//				}
//			}

			return fhirLocationBundle;
		}
	}
	

// ismail end

	
// tahsin start
	
	// needs testing
	public Bundle VitalSignObservation2Observation(VitalSignObservation vsObs) {
		// Had a detailed look and seen that Observation2Observation satisfies the necessary mapping for this method
		return Observation2Observation(vsObs);
	}

	// needs testing
	public Bundle ResultObservation2Observation(ResultObservation resObs) {
		// Had a detailed look and seen that Observation2Observation satisfies the necessary mapping for this method
		return Observation2Observation(resObs);
	}
	
	
	public Bundle AllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct)
	{
		if(cdaAllergyProbAct==null || cdaAllergyProbAct.isSetNullFlavor()) return null;
		else
		{
			AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
			
			Bundle allergyIntoleranceBundle = new Bundle();
			allergyIntoleranceBundle.addEntry( new Bundle.Entry().setResource(allergyIntolerance));
			
			// id
			IdDt resourceId = new IdDt("AllergyIntolerance", getUniqueId());
			allergyIntolerance.setId(resourceId);
			
			// identifier
			if( cdaAllergyProbAct.getIds() != null && !cdaAllergyProbAct.getIds().isEmpty() ){
				for( II ii : cdaAllergyProbAct.getIds() ){
					if( ii != null && ii.isSetNullFlavor() ){
						allergyIntolerance.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// patient
			allergyIntolerance.setPatient( new ResourceReferenceDt( patientId ) );
			
			// recorder
			if( cdaAllergyProbAct.getAuthors() != null && !cdaAllergyProbAct.getAuthors().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Author author : cdaAllergyProbAct.getAuthors() ){
					// Asserting that at most one author exists
					if( author != null && !author.isSetNullFlavor() ){
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = AssignedAuthor2Practitioner( author.getAssignedAuthor() );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry() ){
							if( entry.getResource() instanceof Practitioner ){
								
								fhirPractitioner = (Practitioner) entry.getResource();
							}
						}
					allergyIntolerance.setRecorder( new ResourceReferenceDt( fhirPractitioner.getId() ) );
					allergyIntoleranceBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner) );	
					}
				}
			}
			
			// status
			if( cdaAllergyProbAct.getStatusCode() != null && !cdaAllergyProbAct.getStatusCode().isSetNullFlavor() ){
				if( cdaAllergyProbAct.getStatusCode().getCode() != null && !cdaAllergyProbAct.getStatusCode().getCode().isEmpty() ){
					AllergyIntoleranceStatusEnum allergyIntoleranceStatusEnum = vst.StatusCode2AllergyIntoleranceStatusEnum( cdaAllergyProbAct.getStatusCode().getCode() );
					if( allergyIntoleranceStatusEnum != null ){
						allergyIntolerance.setStatus( allergyIntoleranceStatusEnum );
					}
				}
			}
			
			// onset <-> effectiveTime.low
			if( cdaAllergyProbAct.getEffectiveTime() != null && !cdaAllergyProbAct.getEffectiveTime().isSetNullFlavor() ){
				if( cdaAllergyProbAct.getEffectiveTime().getLow() != null && !cdaAllergyProbAct.getEffectiveTime().getLow().isSetNullFlavor() ){
					allergyIntolerance.setOnset( dtt.TS2DateTime( cdaAllergyProbAct.getEffectiveTime().getLow() ) );
				}
			}

			// TODO: Necip
			// following lines of code of this method is unreadable
			// check or write from scratch
			// allergyIntolerance.setRecorder <-> author, assigned author vs
			// Allergy Severity <-> AllergyIntolerance.reaction.severity
			// Allergy Intolerance Reaction Duration <-> AllergyIntolerance.reaction.duration
			
//			// getting allergyObservation
//			if( cdaAllergyProbAct.getAllergyObservations() != null && !cdaAllergyProbAct.getAllergyObservations().isEmpty() ){
//				for( AllergyObservation allergyObs : cdaAllergyProbAct.getAllergyObservations() ){
//					
//				}
//			}
			
			for (EntryRelationship entryRelationship : cdaAllergyProbAct.getEntryRelationships())
			{
                // check for alert observation
                if (entryRelationship.getObservation() instanceof AllergyObservation) 
                {
                    AllergyObservation allergyObservation = (AllergyObservation) entryRelationship.getObservation();
                    if(allergyObservation.getIds()!=null && !allergyObservation.getIds().isEmpty())
                    {
                    	for(II ii : allergyObservation.getIds())
                    	{
                    		if(ii!=null && !ii.isSetNullFlavor())
                    			allergyIntolerance.addIdentifier(dtt.II2Identifier(ii));
                    	}
                    }
                    
                    	if(allergyObservation.getValues()!=null && !allergyObservation.getValues().isEmpty())
                    	{
                    		for(ANY any : allergyObservation.getValues())
                    		{
                    			if(any instanceof CD)
                    			{
                    				CD cd = (CD) any;
                    				if(cd.getCode()!=null)
                    				{
                    					switch(cd.getCode())
                    					{
                    						case "419199007":
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.ALLERGY);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.ENVIRONMENT);
                    							break;
                    						case "59037007":
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.INTOLERANCE);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.MEDICATION);
                    							break;
                    						case "420134006":
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.OTHER);
                    							break;
                    						case "418038007":
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.ENVIRONMENT);
                    							break;
                    						case "419511003":
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.MEDICATION);
                    							break;
                    						case "418471000":
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.FOOD);
                    							break;
                    						case "416098002":
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.ALLERGY);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.MEDICATION);
                    							break;
                    						case "414285001":
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.ALLERGY);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.FOOD);
                    							break;
                    						case "235719002":
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.INTOLERANCE);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.FOOD);
                    							break;
                    						default:
                    							allergyIntolerance.setType(AllergyIntoleranceTypeEnum.ALLERGY);
                    							allergyIntolerance.setCategory(AllergyIntoleranceCategoryEnum.ENVIRONMENT);
                    					}
                    				}
                    			}
                    		}
                    		if(allergyObservation.getParticipants()!=null && !allergyObservation.getParticipants().isEmpty())
                    		{
                    			for(Participant2 participant : allergyObservation.getParticipants())
                    			{
                    				if(participant.getParticipantRole()!=null && !participant.getParticipantRole().isSetNullFlavor())
                    				{
                    					ParticipantRole participantRole = participant.getParticipantRole();
                    					if(participantRole.getPlayingEntity()!=null && !participantRole.getPlayingEntity().isSetNullFlavor())
                    					{
                    						if(participantRole.getPlayingEntity().getCode()!=null && !participantRole.getPlayingEntity().getCode().isSetNullFlavor())
                    						{
                    							CD cd =(CD) participantRole.getPlayingEntity().getCode();
                    							allergyIntolerance.setSubstance(dtt.CD2CodeableConcept(cd));
                    						}//end if	
                    						if(participantRole.getPlayingEntity().getNames()!=null && !participantRole.getPlayingEntity().getNames().isEmpty())
                    						{
                    							for(PN pn : participantRole.getPlayingEntity().getNames())
                    							{
                    								/*TODO: Name attribute will be filled.*/
                    							}
                    						}
                    					}
                    				}
                    			}
                    		}
                    	}
                    	if(allergyObservation.getEntryRelationships()!=null && !allergyObservation.getEntryRelationships().isEmpty())
                    	{
                    		for (EntryRelationship entryRelationship2 : allergyObservation.getEntryRelationships())
                			{
                    			if(entryRelationship2.getObservation()!=null)
                    			{
                    				if(entryRelationship2.getObservation().getTemplateIds()!=null && !entryRelationship2.getObservation().getTemplateIds().isEmpty())
                    				{
                    					for(II ii : entryRelationship2.getObservation().getTemplateIds() )
                    					{
                    						if(ii.getRoot().equals("2.16.840.1.113883.10.20.22.4.9"))
                    						{
                    							//We have a allergyReactionObservation now.
                    		                    if(entryRelationship2.getObservation().getValues()!=null && !entryRelationship2.getObservation().getValues().isEmpty())
                    		                    {	
                    		                    	ArrayList <Reaction> reactions= new ArrayList <Reaction>();
                    		                    	for(ANY any : entryRelationship2.getObservation().getValues())
                    		                    	{
                    		                    		if(any instanceof CD)
                    		                    		{
                    		                    			CD cd = (CD) any;
                    		                    			Reaction reaction = new Reaction();
                    		                    			reaction.addManifestation(dtt.CD2CodeableConcept(cd));
                    		                    			if(allergyObservation.getEffectiveTime()!=null && !allergyObservation.getEffectiveTime().isSetNullFlavor())
                    		                    			{
                    		                    				IVL_TS ivl_ts=allergyObservation.getEffectiveTime();
                    		                    				if(ivl_ts.getLow()!=null && !ivl_ts.getLow().isSetNullFlavor())
                    		                    				{
                    		                    					reaction.setOnset(dtt.TS2DateTime(ivl_ts.getLow()));
                    		                    				}
                    		                    			}
                    		                    			reactions.add(reaction);
                    		                    		}
                    		                    	}
                    		                    	allergyIntolerance.setReaction(reactions);
                    		                    }
                    						}
                    					}
                    				}
                    			}
                			}
                    	}
                	}
                	
				}
				
				return allergyIntoleranceBundle;
			}
	}
	
	// needs testing
	public Bundle SubstanceAdministration2Immunization(SubstanceAdministration subAd){
		if(subAd==null || subAd.isSetNullFlavor()) return null;
		else
		{
			Immunization fhirImmunization = new Immunization ();
			
			Bundle fhirImmunizationBundle = new Bundle();
			fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirImmunization));
			
			// id
			IdDt resourceId = new IdDt("Immunization", getUniqueId());
			fhirImmunization.setId(resourceId);
			
			// patient
			fhirImmunization.setPatient(new ResourceReferenceDt( patientId ));
			
			// identifier
			if(subAd.getIds()!=null && !subAd.getIds().isEmpty()){
				for( II ii : subAd.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirImmunization.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			
			// effective time
			if(subAd.getEffectiveTimes()!=null && !subAd.getEffectiveTimes().isEmpty()) {
				for(SXCM_TS effectiveTime : subAd.getEffectiveTimes()) {
					if( effectiveTime != null && !effectiveTime.isSetNullFlavor() ){
						// Asserting that at most one effective time exists
						fhirImmunization.setDate(dtt.TS2DateTime(effectiveTime));
					}
						
				}
			}
			
			// lotNumber, vaccineCode, organization
			if(subAd.getConsumable()!=null && !subAd.getConsumable().isSetNullFlavor())
			{
				if(subAd.getConsumable().getManufacturedProduct()!=null && !subAd.getConsumable().getManufacturedProduct().isSetNullFlavor())
				{
					ManufacturedProduct manufacturedProduct=subAd.getConsumable().getManufacturedProduct();
					
					if(manufacturedProduct.getManufacturedMaterial()!=null && !manufacturedProduct.getManufacturedMaterial().isSetNullFlavor())
					{
						Material manufacturedMaterial=manufacturedProduct.getManufacturedMaterial();
						
						// vaccineCode
						if( manufacturedProduct.getManufacturedMaterial().getCode() != null && !manufacturedProduct.getManufacturedMaterial().getCode().isSetNullFlavor() ){
							fhirImmunization.setVaccineCode(dtt.CD2CodeableConcept( manufacturedMaterial.getCode() ) );
						}
						
						// lotNumber
						if(manufacturedMaterial.getLotNumberText()!=null && !manufacturedMaterial.getLotNumberText().isSetNullFlavor()){
							fhirImmunization.setLotNumber(dtt.ST2String(manufacturedMaterial.getLotNumberText()));
						}
					}
					
					// organization
					if(manufacturedProduct.getManufacturerOrganization()!=null && !manufacturedProduct.getManufacturerOrganization().isSetNullFlavor())
					{
						
						ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = null;
						Bundle fhirOrganizationBundle = Organization2Organization( manufacturedProduct.getManufacturerOrganization() );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirOrganizationBundle.getEntry() ){
							if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Organization ){
								fhirOrganization = (ca.uhn.fhir.model.dstu2.resource.Organization) entry.getResource();
							}
						}
						fhirImmunization.setManufacturer( new ResourceReferenceDt(fhirOrganization.getId()) );
						fhirImmunizationBundle.addEntry( new Bundle.Entry().setResource(fhirOrganization) );
					}
				}
			}
			
			// performer
			if(subAd.getPerformers()!=null && !subAd.getPerformers().isEmpty())
			{
				for(Performer2 performer : subAd.getPerformers())
				{
					if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor())
					{
						Bundle practitioner=Performer22Practitioner(performer);
						fhirImmunization.setPerformer(  new ResourceReferenceDt( practitioner.getId() )  );
						fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(practitioner));
					}
				}
			}
			
			// site
			if(subAd.getApproachSiteCodes()!=null && !subAd.getApproachSiteCodes().isEmpty()){
				for(CD cd : subAd.getApproachSiteCodes()){
					// Asserting that at most one site exists
					fhirImmunization.setSite(dtt.CD2CodeableConcept(cd));
				}
			}
			
			// route
			if(subAd.getRouteCode()!=null && !subAd.getRouteCode().isSetNullFlavor()){
				fhirImmunization.setRoute( dtt.CD2CodeableConcept( subAd.getRouteCode()) );
			}
			
			// dose quantity
			if(subAd.getDoseQuantity()!=null && !subAd.getDoseQuantity().isSetNullFlavor()){
				fhirImmunization.setDoseQuantity( dtt.PQ2SimpleQuantityDt( subAd.getDoseQuantity() ) );
			}
			
			// status
			if(subAd.getStatusCode()!=null && !subAd.getStatusCode().isSetNullFlavor()){
				fhirImmunization.setStatus(subAd.getStatusCode().getCode());
			}
			
			
			return fhirImmunizationBundle;
		}
	}
	
	
	// tahsin end
	
	// tested
	public Composition.Section section2Section(Section cdaSec) {
		if(cdaSec == null || cdaSec.isSetNullFlavor()){
			return null;
		} else{
			Composition.Section fhirSec = new Composition.Section();
			fhirSec.setTitle(cdaSec.getTitle().getText());
			fhirSec.setCode(dtt.CD2CodeableConcept(cdaSec.getCode()));
			fhirSec.setText(dtt.StrucDocText2Narrative(cdaSec.getText()));
			
			return fhirSec;
		}
	}


}