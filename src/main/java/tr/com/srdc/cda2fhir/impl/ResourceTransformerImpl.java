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
import ca.uhn.fhir.model.dstu2.composite.AgeDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.TimingDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction;
import ca.uhn.fhir.model.dstu2.resource.Organization.Contact;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
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
	

	public Bundle AllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct){
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
                    		// type and category
                    		// TODO: Necip: It doesn't seem appropriate to set type and category this way
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
                    		
                    		// substance
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

	public Bundle AssignedEntity2Practitioner( AssignedEntity cdaAssignedEntity ){
		if( cdaAssignedEntity == null || cdaAssignedEntity.isSetNullFlavor() ) return null;
		else{
			Practitioner fhirPractitioner = new Practitioner();
			Bundle fhirPractitionerBundle = new Bundle();
			fhirPractitionerBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner) );
			
			// id
			IdDt resourceId = new IdDt("Practitioner", getUniqueId());
			fhirPractitioner.setId(resourceId);
			
			// identifier
			if( cdaAssignedEntity.getIds() != null && !cdaAssignedEntity.getIds().isEmpty() ){
				for( II id : cdaAssignedEntity.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						fhirPractitioner.addIdentifier( dtt.II2Identifier(id) );
					}
				}
			}
			
			// name
			if( cdaAssignedEntity.getAssignedPerson() != null && !cdaAssignedEntity.getAssignedPerson().isSetNullFlavor() ){
				for( PN pn : cdaAssignedEntity.getAssignedPerson().getNames() ){
					if( pn != null && !pn.isSetNullFlavor() ){
						// asserting that at most one name exists
						fhirPractitioner.setName( dtt.EN2HumanName( pn ) );
					}
				}
			}
			
			// address
			if( cdaAssignedEntity.getAddrs() != null && !cdaAssignedEntity.getAddrs().isEmpty() ){
				for( AD ad : cdaAssignedEntity.getAddrs() ){
					if( ad != null && !ad.isSetNullFlavor() ){
						fhirPractitioner.addAddress( dtt.AD2Address(ad) );
					}
				}
			}
			
			// telecom
			if( cdaAssignedEntity.getTelecoms() != null && ! cdaAssignedEntity.getTelecoms().isEmpty() ){
				for( TEL tel : cdaAssignedEntity.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirPractitioner.addTelecom( dtt.TEL2ContactPoint( tel ) );
					}
				}
			}
			
			// practitionerRole.organization
			if( cdaAssignedEntity.getRepresentedOrganizations() != null && !cdaAssignedEntity.getRepresentedOrganizations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Organization cdaOrganization : cdaAssignedEntity.getRepresentedOrganizations() ){
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
							fhirPractitioner.addPractitionerRole().setManagingOrganization( organizationReference );	
							fhirPractitionerBundle.addEntry( new Bundle.Entry().setResource(fhirOrganization) );
							
						}		
					}
				}	
			}
			return fhirPractitionerBundle;
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

	public Bundle Entity2Group( Entity cdaEntity ){
		if( cdaEntity == null || cdaEntity.isSetNullFlavor() ) return null;
		else if( cdaEntity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND ) return null;
		else{
			Group fhirGroup = new Group();
			
			Bundle fhirGroupBundle = new Bundle();
			fhirGroupBundle.addEntry( new Bundle.Entry().setResource(fhirGroup) );
			
			// identifier <-> id
			if( cdaEntity.getIds() != null && !cdaEntity.getIds().isEmpty() ){
				for( II id : cdaEntity.getIds() ){
					if( id != null && !id.isSetNullFlavor() ){
						if( id.getDisplayable() ){
							// unique
							fhirGroup.addIdentifier( dtt.II2Identifier(id) );
						}
					}
				}
			}
			
			// type
			if( cdaEntity.getClassCode() != null ){
				GroupTypeEnum groupTypeEnum = vst.EntityClassRoot2GroupTypeEnum( cdaEntity.getClassCode() );
				if( groupTypeEnum != null ){
					fhirGroup.setType( groupTypeEnum );
				}
				
			}
			
			// actual
			if( cdaEntity.isSetDeterminerCode() && cdaEntity.getDeterminerCode() != null ){
				if( cdaEntity.getDeterminerCode() == EntityDeterminer.KIND ){
					fhirGroup.setActual(false);
				} else{
					fhirGroup.setActual(true);
				}
			}
			
			// code
			if( cdaEntity.getCode() != null && !cdaEntity.getCode().isSetNullFlavor() ){
				fhirGroup.setCode( dtt.CD2CodeableConcept(cdaEntity.getCode()) );
			}
	
			return fhirGroupBundle;
		}
	}

	public Bundle FamilyMemberOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO){
		if( cdaFHO == null || cdaFHO.isSetNullFlavor() ) return null;
		else{
			// TODO: Necip: Couldn't understand what corresponds to outcome when looked to the example CDA file
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
			
			// condition <-> familyHistoryObservation
			// also, deceased value is set by looking at familyHistoryObservation.familyHistoryDeathObservation
			if( cdaFHO.getFamilyHistoryObservations() != null && !cdaFHO.getFamilyHistoryObservations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryObservation familyHistoryObs : cdaFHO.getFamilyHistoryObservations() ){
					if( familyHistoryObs != null && !familyHistoryObs.isSetNullFlavor() ){
						
						// adding a new condition to fhirFMH
						FamilyMemberHistory.Condition condition = fhirFMH.addCondition();
						
						// code familHistoryObs.value(CD)
						if(familyHistoryObs.getValues() != null && !familyHistoryObs.getValues().isEmpty()){
							for(ANY value : familyHistoryObs.getValues()){
								if(value != null && !value.isSetNullFlavor()){
									if(value instanceof CD){
										condition.setCode(dtt.CD2CodeableConcept((CD)value));
									}
								}
							}
						}

						// deceased
						if( familyHistoryObs.getFamilyHistoryDeathObservation() != null && !familyHistoryObs.getFamilyHistoryDeathObservation().isSetNullFlavor() ){
							// deceased <- true
							fhirFMH.setDeceased(new BooleanDt(true));
							
							// effective
							// TODO: Necip: When trying to set the time of death, deceased value disappears from the JSON object
							// You can try by using the following line:
//							fhirFMH.setDeceased( dtt.IVL_TS2Period( familyHistoryObs.getEffectiveTime() ) );
//							 The value of effectiveTime in the example: <effectiveTime value="1967"/>
						}
						
						// onset <-> familyHistoryObs.ageObservation
						if( familyHistoryObs.getAgeObservation() != null && !familyHistoryObs.getAgeObservation().isSetNullFlavor() ){
							// onset
							AgeDt onset = AgeObservation2AgeDt(familyHistoryObs.getAgeObservation());
							if(onset != null){
								condition.setOnset(onset);
							}
						}
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

	public Bundle ManufacturedProduct2Medication(ManufacturedProduct cdaManuProd) {
		if( cdaManuProd == null || cdaManuProd.isSetNullFlavor() ) return null;
		
		Medication fhirMedication = new Medication();
		
		Bundle fhirMedicationBundle = new Bundle();
		fhirMedicationBundle.addEntry( new Bundle.Entry().setResource(fhirMedication) );
		
		// id
		IdDt resourceId = new IdDt( "Medication" , getUniqueId());
		fhirMedication.setId( resourceId );
		
		// code <-> manufacturedMaterial.code
		if( cdaManuProd.getManufacturedMaterial() != null && !cdaManuProd.getManufacturedMaterial().isSetNullFlavor() ){
			if( cdaManuProd.getManufacturedMaterial().getCode() != null && !cdaManuProd.getManufacturedMaterial().isSetNullFlavor() ){
				fhirMedication.setCode( dtt.CD2CodeableConcept( cdaManuProd.getManufacturedMaterial().getCode() ) );
			}
		}
		
		
		// is_brand and manufacturer
		ResourceReferenceDt resourceReferenceManu = new ResourceReferenceDt();
		
		if( cdaManuProd.getManufacturerOrganization() != null && !cdaManuProd.getManufacturerOrganization().isSetNullFlavor() ){
			// is_brand
			fhirMedication.setIsBrand(true);
			
			// manufacturer
			Bundle orgBundle = Organization2Organization( cdaManuProd.getManufacturerOrganization() );
			Organization org = null;
			for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : orgBundle.getEntry()){
				if( entry.getResource() instanceof Organization )
						org = (Organization) entry.getResource();
			}
			resourceReferenceManu.setReference( org.getId() );
				if( cdaManuProd.getManufacturerOrganization().getNames() != null){
				if( cdaManuProd.getManufacturerOrganization().getNames().size() != 0 )
					resourceReferenceManu.setDisplay( cdaManuProd.getManufacturerOrganization().getNames().get(0).getText() );
			}
				fhirMedication.setManufacturer( resourceReferenceManu );
				fhirMedicationBundle.addEntry( new Bundle.Entry().setResource(org) );
		} else {
			fhirMedication.setIsBrand(false);
		}
		
		return fhirMedicationBundle;
	}

	public Bundle MedicationActivity2MedicationStatement(MedicationActivity cdaMedAct) {
		if( cdaMedAct == null || cdaMedAct.isSetNullFlavor() ) return null;
		
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

	public Bundle ParticipantRole2Location(ParticipantRole cdaParticipantRole) {
		if( cdaParticipantRole == null || cdaParticipantRole.isSetNullFlavor() ) return null;
		else{
			Location fhirLocation = new Location();
			
			Bundle fhirLocationBundle = new Bundle();
			fhirLocationBundle.addEntry( new Bundle.Entry().setResource(fhirLocation));
			
			// id
			IdDt resourceId = new IdDt( "Location", getUniqueId() );
			fhirLocation.setId(resourceId);
			
			// identifier
			if( cdaParticipantRole.getIds() != null && !cdaParticipantRole.getIds().isEmpty() ){
				for( II ii : cdaParticipantRole.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirLocation.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}

			// name
			if( cdaParticipantRole.getPlayingEntity() != null && !cdaParticipantRole.getPlayingEntity().isSetNullFlavor() ){
				if( cdaParticipantRole.getPlayingEntity().getNames() != null && !cdaParticipantRole.getPlayingEntity().getNames().isEmpty() ){
					for( PN pn : cdaParticipantRole.getPlayingEntity().getNames() ){
						// Asserting that at most one name exists
						if( pn != null && !pn.isSetNullFlavor() ){
							fhirLocation.setName( pn.getText() );
						}
					}
				}
			}
			
			// telecom
			if( cdaParticipantRole.getTelecoms() != null && !cdaParticipantRole.getTelecoms().isEmpty() ){
				for( TEL tel : cdaParticipantRole.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirLocation.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}

			// address
			if( cdaParticipantRole.getAddrs() != null && !cdaParticipantRole.getAddrs().isEmpty() ){
				for( AD ad : cdaParticipantRole.getAddrs() ){
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
	
	public Bundle PatientRole2Patient(PatientRole cdaPatientRole){
		if( cdaPatientRole == null || cdaPatientRole.isSetNullFlavor() ) return null;
		else{
			Patient fhirPatient = new Patient();
			
			Bundle fhirPatientBundle  = new Bundle();
			fhirPatientBundle.addEntry( new Bundle.Entry().setResource(fhirPatient) );
			
			// id
			fhirPatient.setId(patientId);
//			IdDt resourceId = new IdDt("Patient", getUniqueId());
//			patient.setId(resourceId);
			
			// identifier <-> id
			if( cdaPatientRole.getIds() != null && !cdaPatientRole.getIds().isEmpty() ){
				for( II id : cdaPatientRole.getIds() ){
					if( id == null || id.isSetNullFlavor() ) continue;
						else{
							fhirPatient.addIdentifier(  dtt.II2Identifier(id)  );
					}
				}
			}
			
			// name <-> patient.name
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && 
					cdaPatientRole.getPatient().getNames() != null && !cdaPatientRole.getPatient().getNames().isEmpty() )
			{
				for( PN pn : cdaPatientRole.getPatient().getNames() ){
					if( pn == null || pn.isSetNullFlavor() ) continue;
					else{
						fhirPatient.addName( dtt.EN2HumanName(pn) );
					}
				}
			}
			
			// telecom <-> telecom
			if( cdaPatientRole.getTelecoms() != null && !cdaPatientRole.getTelecoms().isEmpty() )
			{
				for( TEL tel : cdaPatientRole.getTelecoms() ){
					if( tel == null || tel.isSetNullFlavor() ) continue;
					else{
						fhirPatient.addTelecom( dtt.TEL2ContactPoint(tel) );
					}
				}
			}
			
			// gender <-> patient.administrativeGenderCode
			if(     cdaPatientRole.getPatient() != null &&
					!cdaPatientRole.getPatient().isSetNullFlavor() &&
					cdaPatientRole.getPatient().getAdministrativeGenderCode() != null && 
					!cdaPatientRole.getPatient().getAdministrativeGenderCode().isSetNullFlavor()  )
			{
				
				if( cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode() != null && 
						!cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode().isEmpty() )
				{
					fhirPatient.setGender(vst.AdministrativeGenderCode2AdministrativeGenderEnum( cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode() ) );
				}
			}
			
			// birthDate <-> patient.birthTime
			if( cdaPatientRole.getPatient() != null && 
					!cdaPatientRole.getPatient().isSetNullFlavor() &&
					cdaPatientRole.getPatient().getBirthTime() != null &&
					!cdaPatientRole.getPatient().getBirthTime().isSetNullFlavor() )
			{
				fhirPatient.setBirthDate( dtt.TS2Date(cdaPatientRole.getPatient().getBirthTime()) );
			}
			
			// address <-> addr
			if( cdaPatientRole.getAddrs() != null && !cdaPatientRole.getAddrs().isEmpty() ){
				for(AD ad : cdaPatientRole.getAddrs()){
				if( ad == null || ad.isSetNullFlavor() ) continue;
					else{
						fhirPatient.addAddress(dtt.AD2Address(ad));
					}
				}
			}
			
			// maritalStatus <-> patient.maritalStatusCode
			if(cdaPatientRole.getPatient().getMaritalStatusCode() != null 
					&& !cdaPatientRole.getPatient().getMaritalStatusCode().isSetNullFlavor())
			{
				if( cdaPatientRole.getPatient().getMaritalStatusCode().getCode() != null && !cdaPatientRole.getPatient().getMaritalStatusCode().getCode().isEmpty() )
				{
					fhirPatient.setMaritalStatus( vst.MaritalStatusCode2MaritalStatusCodesEnum(cdaPatientRole.getPatient().getMaritalStatusCode().getCode()) );
				}
			}
			
			// communication <-> patient.languageCommunication
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() &&
					cdaPatientRole.getPatient().getLanguageCommunications() != null &&
					!cdaPatientRole.getPatient().getLanguageCommunications().isEmpty() )
			{
				
				for( LanguageCommunication LC : cdaPatientRole.getPatient().getLanguageCommunications() ){
					if(LC == null || LC.isSetNullFlavor() ) continue;
					else{
						Communication communication = LanguageCommunication2Communication(LC);
						fhirPatient.addCommunication(communication);
					}
				}
			}
			
			// managingOrganization <-> providerOrganization
			// According to the DAF profile of Patient, there is no need to map managingOrganization2providerOrganization
//			if( cdaPatientRole.getProviderOrganization() != null && !cdaPatientRole.getProviderOrganization().isSetNullFlavor() ){
//				
//				ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = null;
//				Bundle fhirOrganizationBundle = Organization2Organization( cdaPatientRole.getProviderOrganization() );
//				for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirOrganizationBundle.getEntry() ){
//					if( entity.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Organization ){
//						fhirOrganization = (Organization) entity.getResource();
//					}
//				}
//				
//				if( fhirOrganization != null && !fhirOrganization.isEmpty() ){
//					ResourceReferenceDt organizationReference = new ResourceReferenceDt();
//					organizationReference.setReference(fhirOrganization.getId());
//					if( fhirOrganization.getName() != null ){
//						organizationReference.setDisplay( fhirOrganization.getName() );
//					}
//					fhirPatientBundle.addEntry( new Bundle().addEntry().setResource( fhirOrganization ) );
//					
//					fhirPatient.setManagingOrganization(organizationReference);
//				}
//			}
			
			// guardian <-> patient.guardians
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && 
					cdaPatientRole.getPatient().getGuardians() != null && !cdaPatientRole.getPatient().getGuardians().isEmpty() )
			{
				for( org.openhealthtools.mdht.uml.cda.Guardian guardian : cdaPatientRole.getPatient().getGuardians() ){
					fhirPatient.addContact( Guardian2Contact(guardian) );
				}
			}
			
			
			// extensions
			
			// extRace <-> patient.raceCode
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getRaceCode() != null && !cdaPatientRole.getPatient().getRaceCode().isSetNullFlavor())
			{
				ExtensionDt extRace = new ExtensionDt();
				extRace.setModifier(false);
				extRace.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
				CD raceCode = cdaPatientRole.getPatient().getRaceCode();
				extRace.setValue( dtt.CD2CodeableConcept(raceCode) );
				fhirPatient.addUndeclaredExtension( extRace );
			}
			
			// extEthnicity <-> patient.ethnicGroupCode
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getEthnicGroupCode() != null && !cdaPatientRole.getPatient().getEthnicGroupCode().isSetNullFlavor() )
			{
			ExtensionDt extEthnicity = new ExtensionDt();
				extEthnicity.setModifier(false);
				extEthnicity.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
				CD ethnicGroupCode = cdaPatientRole.getPatient().getEthnicGroupCode();
				extEthnicity.setValue( dtt.CD2CodeableConcept(ethnicGroupCode) );
				fhirPatient.addUndeclaredExtension(extEthnicity);
			}
			
			// extReligion
			if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getReligiousAffiliationCode() != null && !cdaPatientRole.getPatient().getReligiousAffiliationCode().isSetNullFlavor() )
			{
				ExtensionDt extReligion = new ExtensionDt();
				extReligion.setModifier(false);
				extReligion.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-religion");
				CD religiousAffiliationCode = cdaPatientRole.getPatient().getReligiousAffiliationCode();
				extReligion.setValue( dtt.CD2CodeableConcept(religiousAffiliationCode) );
				fhirPatient.addUndeclaredExtension(extReligion);
				}

			// extBirthPlace
				ExtensionDt extBirthPlace = new ExtensionDt();
				extBirthPlace.setModifier(false);
				extBirthPlace.setUrl("http://hl7.org/fhir/StructureDefinition/birthPlace");
					if( cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor()
							&& cdaPatientRole.getPatient().getBirthplace() != null && !cdaPatientRole.getPatient().getBirthplace().isSetNullFlavor() 
							&& cdaPatientRole.getPatient().getBirthplace().getPlace() != null && !cdaPatientRole.getPatient().getBirthplace().getPlace().isSetNullFlavor()
							&& cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr() != null && !cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr().isSetNullFlavor())
				{
					extBirthPlace.setValue( dtt.AD2Address(cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr()) );
					// Birthplace mapping
					// We can get the Birthplace info from ccd
					// However, there is no type to put it
				}
				fhirPatient.addUndeclaredExtension(extBirthPlace);
			
			return fhirPatientBundle;
		}
	}
	
	public Bundle Performer22Practitioner( Performer2 cdaPerformer ){
		if( cdaPerformer == null || cdaPerformer.isSetNullFlavor() ) return null;
		else{
			return AssignedEntity2Practitioner( cdaPerformer.getAssignedEntity() );
		}
	}

	public Bundle ProblemConcernAct2Condition(ProblemConcernAct cdaProbConcAct) {
		if( cdaProbConcAct == null || cdaProbConcAct.isSetNullFlavor()) return null;
		
		Bundle fhirConditionBundle = new Bundle();

		for(EntryRelationship entryRelationship : cdaProbConcAct.getEntryRelationships()){
			
			Condition fhirCondition = new Condition();
			
			fhirConditionBundle.addEntry( new Bundle.Entry().setResource(fhirCondition) );
			
			// id
			IdDt resourceId = new IdDt("Condition", getUniqueId());
			fhirCondition.setId( resourceId );
			
			// identifier
			if( cdaProbConcAct.getIds() != null && !cdaProbConcAct.getIds().isEmpty() ){
				for( II ii : cdaProbConcAct.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirCondition.addIdentifier( dtt.II2Identifier(ii) );
					}
				}
			}
			
			// patient
			fhirCondition.setPatient( new ResourceReferenceDt( patientId ) );

			
			// TODO: Severity
			// See https://www.hl7.org/fhir/daf/condition-daf.html
			// Couldn't found in the CDA example
			
			// encounter
			if( cdaProbConcAct.getEncounters() != null && !cdaProbConcAct.getEncounters().isEmpty() ){
				if(cdaProbConcAct.getEncounters().get(0) != null && cdaProbConcAct.getEncounters().get(0).isSetNullFlavor() ){
					ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = null;
					Bundle fhirEncounterBundle = Encounter2Encounter(cdaProbConcAct.getEncounters().get(0));
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirEncounterBundle.getEntry()){
						if( entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter )
							fhirEncounter = (ca.uhn.fhir.model.dstu2.resource.Encounter) entry.getResource();
					}
				
					fhirCondition.setEncounter( new ResourceReferenceDt(fhirEncounter.getId()) );
					fhirConditionBundle.addEntry( new Bundle.Entry().setResource(fhirEncounter));
				}
			}
		
			// asserter <-> author
			if( cdaProbConcAct.getAuthors() != null && !cdaProbConcAct.getAuthors().isEmpty() ){
				for( org.openhealthtools.mdht.uml.cda.Author author : cdaProbConcAct.getAuthors() ){
					if( author != null && !author.isSetNullFlavor() ){
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = AssignedAuthor2Practitioner( author.getAssignedAuthor() );
						
						for( ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry() ){
							if( entry.getResource() instanceof Practitioner ){
								
								fhirPractitioner = (Practitioner) entry.getResource();
							}
						}			
						fhirCondition.setAsserter( new ResourceReferenceDt( fhirPractitioner.getId() ) );
						fhirConditionBundle.addEntry( new Bundle.Entry().setResource(fhirPractitioner) );	
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
				fhirCondition.setDateRecorded( dateRecorded );
			}
			
		
			// code <-> value
			if( entryRelationship.getObservation() != null  && entryRelationship.getObservation().isSetNullFlavor()
					&& entryRelationship.getObservation().getValues() != null && !entryRelationship.getObservation().getValues().isEmpty()){
				if( entryRelationship.getObservation().getValues().get(0) != null && !entryRelationship.getObservation().getValues().get(0).isSetNullFlavor() ) {
					if( entryRelationship.getObservation().getValues().get(0) instanceof CD ){
						fhirCondition.setCode( dtt.CD2CodeableConcept( (CD) entryRelationship.getObservation().getValues().get(0) ) );
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
			
//			Following lines are the former mapping for category
//			However, seems unappropriate
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

			
			// onset and abatement
			if(entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() 
			&& entryRelationship.getObservation().getEffectiveTime() != null && 
					!entryRelationship.getObservation().getEffectiveTime().isSetNullFlavor())
			{
				
				IVXB_TS low = entryRelationship.getObservation().getEffectiveTime().getLow();
				IVXB_TS high = entryRelationship.getObservation().getEffectiveTime().getHigh();
		
				// low <-> onset
				if( low != null && !low.isSetNullFlavor() ){
					fhirCondition.setOnset( dtt.TS2DateTime(low ) );
				}
				
				// high <-> abatement
				if( high != null && !high.isSetNullFlavor() ){
					fhirCondition.setAbatement( dtt.TS2DateTime(high) );
				}

			}
				        
			// bodysite
			if( entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor() 
					&&entryRelationship.getObservation().getValues() != null && !entryRelationship.getObservation().getValues().isEmpty()){
				for( ANY value : entryRelationship.getObservation().getValues() ){
					if( value == null || value.isSetNullFlavor() ) continue;
					else{
						if( value instanceof CD ){
							fhirCondition.addBodySite( dtt.CD2CodeableConcept( (CD) value ) );
						}
					}
				}
			}
		}
		return fhirConditionBundle;
	}

	public Bundle Procedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaPr){
		// TODO: used <-> device
		if(cdaPr == null || cdaPr.isSetNullFlavor())
			return null;

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

	public Bundle ResultObservation2Observation(ResultObservation cdaResultObs) {
		// Had a detailed look and seen that Observation2Observation satisfies the necessary mapping for this method
		return Observation2Observation(cdaResultObs);
	}

	public Bundle SocialHistoryObservation2Observation( org.openhealthtools.mdht.uml.cda.consol.SocialHistoryObservation cdaSocialHistoryObs ){
		if(cdaSocialHistoryObs == null || cdaSocialHistoryObs.isSetNullFlavor())
			return null;

		Bundle fhirObsBundle = new Bundle();
		for( org.openhealthtools.mdht.uml.cda.Observation cdaObs : cdaSocialHistoryObs.getObservations() ){
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

	public Bundle SubstanceAdministration2Immunization(SubstanceAdministration cdaSubAdm){
		if(cdaSubAdm == null || cdaSubAdm.isSetNullFlavor())
			return null;
		else {
			Immunization fhirImmunization = new Immunization ();
			
			Bundle fhirImmunizationBundle = new Bundle();
			fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirImmunization));
			
			// id
			IdDt resourceId = new IdDt("Immunization", getUniqueId());
			fhirImmunization.setId(resourceId);
			
			// patient
			fhirImmunization.setPatient(new ResourceReferenceDt(patientId));
			
			// identifier
			if(cdaSubAdm.getIds()!=null && !cdaSubAdm.getIds().isEmpty()){
				for( II ii : cdaSubAdm.getIds() ){
					if( ii != null && !ii.isSetNullFlavor() ){
						fhirImmunization.addIdentifier(dtt.II2Identifier(ii));
					}
				}
			}
			
			// effective time
			if(cdaSubAdm.getEffectiveTimes()!=null && !cdaSubAdm.getEffectiveTimes().isEmpty()) {
				for(SXCM_TS effectiveTime : cdaSubAdm.getEffectiveTimes()) {
						if( effectiveTime != null && !effectiveTime.isSetNullFlavor() ){
						// Asserting that at most one effective time exists
						fhirImmunization.setDate(dtt.TS2DateTime(effectiveTime));
					}
				}
			}
			
			// lotNumber, vaccineCode, organization
			if(cdaSubAdm.getConsumable()!=null && !cdaSubAdm.getConsumable().isSetNullFlavor())
			{
				if(cdaSubAdm.getConsumable().getManufacturedProduct()!=null && !cdaSubAdm.getConsumable().getManufacturedProduct().isSetNullFlavor())
				{
					ManufacturedProduct manufacturedProduct=cdaSubAdm.getConsumable().getManufacturedProduct();
					
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
			if(cdaSubAdm.getPerformers() != null && !cdaSubAdm.getPerformers().isEmpty()) {
				for(Performer2 performer : cdaSubAdm.getPerformers()) {
					if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor()) {
						Bundle practBundle = Performer22Practitioner(performer);
						for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
							// Add all the resources returned from the bundle to the main bundle
							fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
							// Add a reference to performer attribute only for Practitioner resource. Further resources can include Organization.
							if(entry.getResource() instanceof Practitioner)
								fhirImmunization.setPerformer(new ResourceReferenceDt(entry.getResource().getId()));
						}
					}
				}
			}
						
			// site
			if(cdaSubAdm.getApproachSiteCodes()!=null && !cdaSubAdm.getApproachSiteCodes().isEmpty()){
				for(CD cd : cdaSubAdm.getApproachSiteCodes()){
					// Asserting that at most one site exists
					fhirImmunization.setSite(dtt.CD2CodeableConcept(cd));
				}
			}
			
			// route
			if(cdaSubAdm.getRouteCode()!=null && !cdaSubAdm.getRouteCode().isSetNullFlavor()){
				fhirImmunization.setRoute(dtt.CD2CodeableConcept(cdaSubAdm.getRouteCode()));
			}
			
			// dose quantity
			if(cdaSubAdm.getDoseQuantity()!=null && !cdaSubAdm.getDoseQuantity().isSetNullFlavor()){
				fhirImmunization.setDoseQuantity( dtt.PQ2SimpleQuantityDt( cdaSubAdm.getDoseQuantity() ) );
			}
			
			// status
			if(cdaSubAdm.getStatusCode()!=null && !cdaSubAdm.getStatusCode().isSetNullFlavor()){
				fhirImmunization.setStatus(cdaSubAdm.getStatusCode().getCode());
			}

			return fhirImmunizationBundle;
		}
	}
	
	public Bundle VitalSignObservation2Observation(VitalSignObservation cdaVSO) {
		// Had a detailed look and seen that Observation2Observation satisfies the necessary mapping for this method
		return Observation2Observation(cdaVSO);
	}

	public AgeDt AgeObservation2AgeDt(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation){
		if(cdaAgeObservation == null || cdaAgeObservation.isSetNullFlavor()) return null;
		else{
			AgeDt fhirAge = new AgeDt();
			
			// coding
			if(cdaAgeObservation.getCode() != null && !cdaAgeObservation.getCode().isSetNullFlavor()){
				CodeableConceptDt codeableConcept = dtt.CD2CodeableConcept(cdaAgeObservation.getCode());
				if(codeableConcept != null){
					for(CodingDt coding : codeableConcept.getCoding()){
						// Asserting that only one coding exists
						if(coding != null && !coding.isEmpty()){
							// code
							if(coding.getCode() != null && !coding.getCode().isEmpty()){
								fhirAge.setCode(coding.getCode());
							}
							
							// system
							if(coding.getSystem() != null && !coding.getSystem().isEmpty()){
								fhirAge.setSystem(coding.getSystem());
							}
						}
						
					}
				}
			}
			
			// age <-> value
			if(cdaAgeObservation != null && !cdaAgeObservation.getValues().isEmpty()){
				for(ANY value : cdaAgeObservation.getValues()){
					if(value != null && !value.isSetNullFlavor()){
						if(value instanceof PQ){
							if(((PQ)value).getValue() != null){
								fhirAge.setValue(((PQ)value).getValue());
								
								// definition requires a human readable unit for fhirAge
								String unit = vst.AgeObservationUnit2AgeUnit(((PQ)value).getUnit());
								if(unit != null){
									fhirAge.setUnit(unit);
								}
							}
						}
					}
				}
			}
			
			
			return fhirAge;
		}
	}
	
	public ca.uhn.fhir.model.dstu2.resource.Patient.Contact Guardian2Contact( Guardian cdaGuardian ){
		if(cdaGuardian == null || cdaGuardian.isSetNullFlavor())
			return null;
		else{
			ca.uhn.fhir.model.dstu2.resource.Patient.Contact fhirContact = new ca.uhn.fhir.model.dstu2.resource.Patient.Contact();
			
			// addr
			if( cdaGuardian.getAddrs() != null && !cdaGuardian.getAddrs().isEmpty() ){
				fhirContact.setAddress( dtt.AD2Address(cdaGuardian.getAddrs().get(0)) );
			} 
			
			// tel
			if( cdaGuardian.getTelecoms() != null && !cdaGuardian.getTelecoms().isEmpty() ){
				for( TEL tel : cdaGuardian.getTelecoms() ){
					if( tel != null && !tel.isSetNullFlavor() ){
						fhirContact.addTelecom( dtt.TEL2ContactPoint( tel ) );
					}
				}
			}
			
			// relationship
			if( cdaGuardian.getCode() != null && !cdaGuardian.getCode().isSetNullFlavor() ){
				fhirContact.addRelationship( dtt.CD2CodeableConcept( cdaGuardian.getCode() ) );
			}
			return fhirContact;
		}
	}

	public Communication LanguageCommunication2Communication( LanguageCommunication cdaLanguageCommunication ){
		if(cdaLanguageCommunication == null || cdaLanguageCommunication.isSetNullFlavor()) return null;
		else{
			Communication fhirCommunication = new Communication();
			
			// language
			if( cdaLanguageCommunication.getLanguageCode() != null && !cdaLanguageCommunication.getLanguageCode().isSetNullFlavor() ){
				fhirCommunication.setLanguage(  dtt.CD2CodeableConcept( cdaLanguageCommunication.getLanguageCode() )  );
			}
			
			// preferred
			if( cdaLanguageCommunication.getPreferenceInd() != null && !cdaLanguageCommunication.getPreferenceInd().isSetNullFlavor() ){
				fhirCommunication.setPreferred(  dtt.BL2Boolean( cdaLanguageCommunication.getPreferenceInd() )  );
			}
			return fhirCommunication;
		}
	}

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