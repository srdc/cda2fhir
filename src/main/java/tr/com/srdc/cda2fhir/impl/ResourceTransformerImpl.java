package tr.com.srdc.cda2fhir.impl;

import java.util.UUID;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.ccd.FunctionalStatusObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.NonMedicinalSupplyActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProductInstance;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.SeverityObservation;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistoryObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.AgeDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.TimingDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.AllergyIntoleranceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ConditionCategoryCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.GroupTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationDispenseStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ProcedureStatusEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.IdDt;
import tr.com.srdc.cda2fhir.CCDATransformer;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ResourceTransformerImpl implements tr.com.srdc.cda2fhir.ResourceTransformer{

	private DataTypesTransformer dtt;
	private ValueSetsTransformer vst;
	private CCDATransformer cct;
	private ResourceReferenceDt defaultPatientRef;

	public ResourceTransformerImpl() {
		dtt = new DataTypesTransformerImpl();
		vst = new ValueSetsTransformerImpl();
		cct = null;
		// This is a default patient reference to be used when ResourceTransformer is not initiated with a CCDATransformer
		defaultPatientRef = new ResourceReferenceDt(new IdDt("Patient", "0"));
	}

	public ResourceTransformerImpl(CCDATransformer ccdaTransformer) {
		this();
		cct = ccdaTransformer;
	}

	protected String getUniqueId() {
		if(cct != null)
			return cct.getUniqueId();
		else
			return UUID.randomUUID().toString();
	}

	protected ResourceReferenceDt getPatientRef() {
		if(cct != null)
			return cct.getPatientRef();
		else
			return defaultPatientRef;
	}

	public Bundle tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct) {
		if(cdaAllergyProbAct==null || cdaAllergyProbAct.isSetNullFlavor()) 
			return null;

		AllergyIntolerance fhirAllergyIntolerance = new AllergyIntolerance();
		
		Bundle allergyIntoleranceBundle = new Bundle();
		allergyIntoleranceBundle.addEntry(new Bundle.Entry().setResource(fhirAllergyIntolerance));
		
		// id
		IdDt resourceId = new IdDt("AllergyIntolerance", getUniqueId());
		fhirAllergyIntolerance.setId(resourceId);
		
		// identifier
		if(cdaAllergyProbAct.getIds() != null && !cdaAllergyProbAct.getIds().isEmpty()) {
			for(II ii : cdaAllergyProbAct.getIds()){
				if(ii != null && ii.isSetNullFlavor()){
					fhirAllergyIntolerance.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// patient
		fhirAllergyIntolerance.setPatient(getPatientRef());
	
		// recorder
		if(cdaAllergyProbAct.getAuthors() != null && !cdaAllergyProbAct.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaAllergyProbAct.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						allergyIntoleranceBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof Practitioner){
							fhirPractitioner = (Practitioner)entry.getResource();
						}
					}
					fhirAllergyIntolerance.setRecorder(new ResourceReferenceDt(fhirPractitioner.getId()));
				}
			}
		}
		
		// status
		if(cdaAllergyProbAct.getStatusCode() != null && !cdaAllergyProbAct.getStatusCode().isSetNullFlavor()) {
			if(cdaAllergyProbAct.getStatusCode().getCode() != null && !cdaAllergyProbAct.getStatusCode().getCode().isEmpty()) {
				AllergyIntoleranceStatusEnum allergyIntoleranceStatusEnum = vst.tStatusCode2AllergyIntoleranceStatusEnum(cdaAllergyProbAct.getStatusCode().getCode());
				if(allergyIntoleranceStatusEnum != null) {
					fhirAllergyIntolerance.setStatus(allergyIntoleranceStatusEnum);
				}
			}
		}
		
		// onset <-> effectiveTime.low(or value)
		if(cdaAllergyProbAct.getEffectiveTime() != null && !cdaAllergyProbAct.getEffectiveTime().isSetNullFlavor()) {

			// low(if not exists, value) -> onset
			if(cdaAllergyProbAct.getEffectiveTime().getLow() != null && !cdaAllergyProbAct.getEffectiveTime().getLow().isSetNullFlavor()) {
				fhirAllergyIntolerance.setOnset(dtt.tTS2DateTime(cdaAllergyProbAct.getEffectiveTime().getLow()));
			} else if(cdaAllergyProbAct.getEffectiveTime().getValue() != null && !cdaAllergyProbAct.getEffectiveTime().getValue().isEmpty()) {
				fhirAllergyIntolerance.setOnset(dtt.tString2DateTime(cdaAllergyProbAct.getEffectiveTime().getValue()));
			}
		}
		
		// getting allergyObservation
		if(cdaAllergyProbAct.getAllergyObservations() != null && !cdaAllergyProbAct.getAllergyObservations().isEmpty()) {
			for(AllergyObservation cdaAllergyObs : cdaAllergyProbAct.getAllergyObservations()) {
				if(cdaAllergyObs != null && !cdaAllergyObs.isSetNullFlavor()) {
					
					// substance <-> cdaAllergyProbAct.allergyObservation.participant.participantRole.playingEntity.code
					if(cdaAllergyObs.getParticipants() != null && !cdaAllergyObs.getParticipants().isEmpty()) {
						for(Participant2 participant : cdaAllergyObs.getParticipants()) {
							if(participant != null && !participant.isSetNullFlavor()) {
								if(participant.getParticipantRole() != null && !participant.getParticipantRole().isSetNullFlavor()) {
									if(participant.getParticipantRole().getPlayingEntity() != null && !participant.getParticipantRole().getPlayingEntity().isSetNullFlavor()){
										if(participant.getParticipantRole().getPlayingEntity().getCode() != null && !participant.getParticipantRole().getPlayingEntity().getCode().isSetNullFlavor()) {
											fhirAllergyIntolerance.setSubstance(dtt.tCD2CodeableConcept(participant.getParticipantRole().getPlayingEntity().getCode()));
										}
									}
								}
							}
						}
					}
					
					// category
					if(cdaAllergyObs.getValues() != null && !cdaAllergyObs.getValues().isEmpty()) {
						for(ANY value : cdaAllergyObs.getValues()) {
							if(value != null && !value.isSetNullFlavor()) {
								if(value instanceof CD) {
									if(vst.tAllergyCategoryCode2AllergyIntoleranceCategoryEnum(((CD)value).getCode()) != null) {
										fhirAllergyIntolerance.setCategory(vst.tAllergyCategoryCode2AllergyIntoleranceCategoryEnum(((CD)value).getCode()));
									}
								}
							}
						}
					}
					
					
					// searching for reaction observation
					// NOTE: fhirAllergyIntolerance.reaction.duration doesn't exists although daf wants it mapped
					if(cdaAllergyObs.getEntryRelationships() != null && !cdaAllergyObs.getEntryRelationships().isEmpty()) {
						for(org.openhealthtools.mdht.uml.cda.EntryRelationship entryRelShip : cdaAllergyObs.getEntryRelationships()) {
							if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
								if(entryRelShip.getObservation() != null && !entryRelShip.isSetNullFlavor()) {
									if(entryRelShip.getObservation() instanceof ReactionObservation) {
										
										ReactionObservation cdaReactionObs = (ReactionObservation) entryRelShip.getObservation();
										Reaction fhirReaction = fhirAllergyIntolerance.addReaction();
										
										// reaction.manifestation <-> reactionObs.value(CD)
										if(cdaReactionObs.getValues() != null && !cdaReactionObs.getValues().isEmpty()) {
											for(ANY value : cdaReactionObs.getValues()) {
												if(value != null && !value.isSetNullFlavor()) {
													if(value instanceof CD) {
														fhirReaction.addManifestation(dtt.tCD2CodeableConcept((CD)value));
													}
												}
											}
										}

										// severity <-> reactionObservation.severityObservation.value(CD).code
										if(cdaReactionObs.getSeverityObservation() != null && !cdaReactionObs.getSeverityObservation().isSetNullFlavor()) {
											SeverityObservation cdaSeverityObs = cdaReactionObs.getSeverityObservation();
											if(cdaSeverityObs.getValues() != null && !cdaSeverityObs.getValues().isEmpty()) {
												for(ANY value : cdaSeverityObs.getValues()) {
													if(value != null && !value.isSetNullFlavor()) {
														if(value instanceof CD) {
															if(vst.tSeverityCode2AllergyIntoleranceSeverityEnum(((CD)value).getCode()) != null) {
																fhirReaction.setSeverity(vst.tSeverityCode2AllergyIntoleranceSeverityEnum(((CD)value).getCode()));
															}
														}
													}
												}
											}
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

	public Bundle tAssignedAuthor2Practitioner(AssignedAuthor cdaAssignedAuthor) {
		if(cdaAssignedAuthor == null || cdaAssignedAuthor.isSetNullFlavor())
			return null;
		
		Practitioner fhirPractitioner = new Practitioner();
		
		// bundle
		Bundle fhirPractitionerBundle = new Bundle();
		fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirPractitioner));
		
		// id
		IdDt resourceId = new IdDt("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);
		
		// identifier
		if(cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty()) {
			for(II ii : cdaAssignedAuthor.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirPractitioner.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name <-> assignedAuthor.assignedPerson.name
		if(cdaAssignedAuthor.getAssignedPerson() != null && !cdaAssignedAuthor.getAssignedPerson().isSetNullFlavor()) {
			if(cdaAssignedAuthor.getAssignedPerson().getNames() != null && !cdaAssignedAuthor.getAssignedPerson().getNames().isEmpty()) {
				for(PN pn : cdaAssignedAuthor.getAssignedPerson().getNames()) {
					if(pn != null && !pn.isSetNullFlavor()) {
						// Asserting that at most one name exists
						fhirPractitioner.setName(dtt.tEN2HumanName(pn));
					}
				}
			}
		}
		
		// address
		if(cdaAssignedAuthor.getAddrs() != null && !cdaAssignedAuthor.getAddrs().isEmpty()) {
			for(AD ad : cdaAssignedAuthor.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		// telecom
		if(cdaAssignedAuthor.getTelecoms() != null && !cdaAssignedAuthor.getTelecoms().isEmpty()) {
			for(TEL tel : cdaAssignedAuthor.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// Adding a practitionerRole
		Practitioner.PractitionerRole fhirPractitionerRole = fhirPractitioner.addPractitionerRole();
		
		// practitionerRole.role <-> assignedAuthor.code
		if(cdaAssignedAuthor.getCode() != null && !cdaAssignedAuthor.isSetNullFlavor()) {
			fhirPractitionerRole.setRole(dtt.tCD2CodeableConcept(cdaAssignedAuthor.getCode()));
		}
		
		// practitionerRole.organization <-> organization
		if(cdaAssignedAuthor.getRepresentedOrganization() != null && !cdaAssignedAuthor.getRepresentedOrganization().isSetNullFlavor()) {
			Organization fhirOrganization = tOrganization2Organization(cdaAssignedAuthor.getRepresentedOrganization());
			fhirPractitionerRole.setManagingOrganization(new ResourceReferenceDt(fhirOrganization.getId()));
			fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
		}
		
		return fhirPractitionerBundle;
	}

	public Bundle tAssignedEntity2Practitioner(AssignedEntity cdaAssignedEntity) {
		if(cdaAssignedEntity == null || cdaAssignedEntity.isSetNullFlavor())
			return null;
		
		Practitioner fhirPractitioner = new Practitioner();
		Bundle fhirPractitionerBundle = new Bundle();
		fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirPractitioner));
			
		// id
		IdDt resourceId = new IdDt("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);
		
		// identifier
		if(cdaAssignedEntity.getIds() != null && !cdaAssignedEntity.getIds().isEmpty()) {
			for(II id : cdaAssignedEntity.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirPractitioner.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}
		
		// name
		if(cdaAssignedEntity.getAssignedPerson() != null && !cdaAssignedEntity.getAssignedPerson().isSetNullFlavor()) {
			for(PN pn : cdaAssignedEntity.getAssignedPerson().getNames()) {
				if(pn != null && !pn.isSetNullFlavor()) {
					// asserting that at most one name exists
					fhirPractitioner.setName(dtt.tEN2HumanName( pn ));
				}
			}
		}
		
		// address
		if(cdaAssignedEntity.getAddrs() != null && !cdaAssignedEntity.getAddrs().isEmpty()) {
			for(AD ad : cdaAssignedEntity.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		// telecom
		if(cdaAssignedEntity.getTelecoms() != null && ! cdaAssignedEntity.getTelecoms().isEmpty()) {
			for(TEL tel : cdaAssignedEntity.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint( tel ));
				}
			}
		}
		
		// practitionerRole.organization
		if(cdaAssignedEntity.getRepresentedOrganizations() != null && !cdaAssignedEntity.getRepresentedOrganizations().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Organization cdaOrganization : cdaAssignedEntity.getRepresentedOrganizations()) {
				
				if(cdaOrganization != null && !cdaOrganization.isSetNullFlavor()) {
					// Notice that for every organization we add, we create a new practitioner role
					ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(cdaOrganization);
					
					ResourceReferenceDt organizationReference = new ResourceReferenceDt(fhirOrganization.getId());
					fhirPractitioner.addPractitionerRole().setManagingOrganization(organizationReference);	
					fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
				}
			}	
		}
		return fhirPractitionerBundle;
	}
	
	public Bundle tEncounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter) {
		if(cdaEncounter == null || cdaEncounter.isSetNullFlavor())
			return null;
		else if(cdaEncounter.getMoodCode() != org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentEncounterMood.EVN)
			return null;

		ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = new ca.uhn.fhir.model.dstu2.resource.Encounter();

		Bundle fhirEncounterBundle = new Bundle();
		fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirEncounter));
		
		// NOTE: hospitalization.period not found. However, daf requires it being mapped

		// patient
		fhirEncounter.setPatient(getPatientRef());

		// id
		IdDt resourceId = new IdDt("Encounter",getUniqueId());
		fhirEncounter.setId(resourceId);

		// identifier <-> id
		if(cdaEncounter.getIds() != null && !cdaEncounter.getIds().isEmpty()) {
			for(II id : cdaEncounter.getIds()){
				if(id != null && !id.isSetNullFlavor()){
					fhirEncounter.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// status <-> statusCode
		if(cdaEncounter.getStatusCode() != null && !cdaEncounter.getStatusCode().isSetNullFlavor()) {
			if(vst.tStatusCode2EncounterStatusEnum(cdaEncounter.getStatusCode().getCode()) != null) {
				fhirEncounter.setStatus(vst.tStatusCode2EncounterStatusEnum(cdaEncounter.getStatusCode().getCode()));
			}
		}

		// type <-> code
		if(cdaEncounter.getCode() != null && !cdaEncounter.getCode().isSetNullFlavor()) {
			fhirEncounter.addType(dtt.tCD2CodeableConcept(cdaEncounter.getCode()));
		}
		
		// class <-> code.translation
		if(cdaEncounter.getCode() != null && !cdaEncounter.getCode().isSetNullFlavor()) {
			if(cdaEncounter.getCode().getTranslations() != null && !cdaEncounter.getCode().getTranslations().isEmpty()) {
				for(CD cd : cdaEncounter.getCode().getTranslations()) {
					if(cd != null && !cd.isSetNullFlavor()) {
						EncounterClassEnum encounterClass = vst.tEncounterCode2EncounterClassEnum(cd.getCode());
						if(encounterClass != null){
							fhirEncounter.setClassElement(encounterClass);
						}
					}
				}
			}
		}

		// priority <-> priorityCode
		if(cdaEncounter.getPriorityCode() != null && !cdaEncounter.getPriorityCode().isSetNullFlavor()) {
			fhirEncounter.setPriority(dtt.tCD2CodeableConcept(cdaEncounter.getPriorityCode()));
		}

		// performer
		if(cdaEncounter.getPerformers() != null && !cdaEncounter.getPerformers().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaEncounter.getPerformers()) {
				if(cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					ca.uhn.fhir.model.dstu2.resource.Encounter.Participant fhirParticipant = new ca.uhn.fhir.model.dstu2.resource.Encounter.Participant();

					fhirParticipant.addType().addCoding(vst.tParticipationType2ParticipationTypeCode(ParticipationType.PRF));

					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tPerformer22Practitioner(cdaPerformer);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirPractitionerBundle.getEntry()) {
						if(entity.getResource() instanceof Practitioner) {
							fhirPractitioner = (Practitioner)entity.getResource();
						}
					}

					if(fhirPractitioner != null) {
						ResourceReferenceDt practitionerReference = new ResourceReferenceDt();
						practitionerReference.setReference(fhirPractitioner.getId());
						fhirParticipant.setIndividual(practitionerReference);
						fhirEncounterBundle.addEntry(new Bundle().addEntry().setResource(fhirPractitioner));

					}
					fhirEncounter.addParticipant(fhirParticipant);
				}
			}
		}

		// period <-> .effectiveTime (low & high)
		if(cdaEncounter.getEffectiveTime() != null && !cdaEncounter.getEffectiveTime().isSetNullFlavor()) {
			fhirEncounter.setPeriod(dtt.tIVL_TS2Period(cdaEncounter.getEffectiveTime()));
		}

		// location <-> .participant[typeCode=LOC]
		if(cdaEncounter.getParticipants() != null && !cdaEncounter.getParticipants().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Participant2 cdaParticipant : cdaEncounter.getParticipants()) {
				if(cdaParticipant != null && !cdaParticipant.isSetNullFlavor()) {

					// checking if the participant is location
					if(cdaParticipant.getTypeCode() == ParticipationType.LOC) {
						if(cdaParticipant.getParticipantRole() != null && !cdaParticipant.getParticipantRole().isSetNullFlavor()) {
							// We first make the mapping to a resource.location
							// then, we create a resource.encounter.location
							// then, we add the resource.location to resource.encounter.location
							
							// usage of ParticipantRole2Location
							ca.uhn.fhir.model.dstu2.resource.Location fhirLocation = tParticipantRole2Location(cdaParticipant.getParticipantRole());
							
							fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirLocation));
							fhirEncounter.addLocation().setLocation(new ResourceReferenceDt(fhirLocation.getId()));
						}
					}
				}
			}
		}

		// Getting information from entryRelShip.observation(indication)
		if(cdaEncounter.getEntryRelationships() != null && !cdaEncounter.getEntryRelationships().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.EntryRelationship entryRelShip : cdaEncounter.getEntryRelationships()) {
				if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
					if(entryRelShip.getObservation() != null && !entryRelShip.isSetNullFlavor()) {
						if(entryRelShip.getObservation() instanceof Indication) {
							Indication cdaIndication = (Indication)entryRelShip.getObservation();

							// reason <-> indication.value(CD)
							if(cdaIndication.getValues() != null && !cdaIndication.getValues().isEmpty()) {
								for(ANY value : cdaIndication.getValues()) {
									if(value != null && !value.isSetNullFlavor()) {
										if(value instanceof CD) {
											fhirEncounter.addReason(dtt.tCD2CodeableConcept((CD)value));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return fhirEncounterBundle;
	}

	// never used
	public Group tEntity2Group(Entity cdaEntity) {
		if( cdaEntity == null || cdaEntity.isSetNullFlavor() )
			return null;
		else if(cdaEntity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND)
			return null;
		
		Group fhirGroup = new Group();
		
		// identifier <-> id
		if(cdaEntity.getIds() != null && !cdaEntity.getIds().isEmpty()) {
			for(II id : cdaEntity.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					if(id.getDisplayable()) {
						// unique
						fhirGroup.addIdentifier(dtt.tII2Identifier(id));
					}
				}
			}
		}
		
		// type
		if(cdaEntity.getClassCode() != null) {
			GroupTypeEnum groupTypeEnum = vst.tEntityClassRoot2GroupTypeEnum(cdaEntity.getClassCode());
			if(groupTypeEnum != null) {
				fhirGroup.setType(groupTypeEnum);
			}
			
		}
		
		// actual
		if(cdaEntity.isSetDeterminerCode() && cdaEntity.getDeterminerCode() != null) {
			if(cdaEntity.getDeterminerCode() == EntityDeterminer.KIND) {
				fhirGroup.setActual(false);
			} else{
				fhirGroup.setActual(true);
			}
		}
		
		// code
		if(cdaEntity.getCode() != null && !cdaEntity.getCode().isSetNullFlavor()) {
			fhirGroup.setCode(dtt.tCD2CodeableConcept(cdaEntity.getCode()));
		}
		
		return fhirGroup;
	}

	public FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO){
		if(cdaFHO == null || cdaFHO.isSetNullFlavor())
			return null;

		FamilyMemberHistory fhirFMH = new FamilyMemberHistory();
		
		// id
		IdDt resourceId = new IdDt("FamilyMemberHistory", getUniqueId());
		fhirFMH.setId(resourceId);
		
		// identifier
		if(cdaFHO.getIds() != null && !cdaFHO.getIds().isEmpty()) {
			for(II id : cdaFHO.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirFMH.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}
		
		// patient
		fhirFMH.setPatient(getPatientRef());
		
		// statusCode
		if(cdaFHO.getStatusCode() != null && !cdaFHO.getStatusCode().isSetNullFlavor()) {
			fhirFMH.setStatus(vst.tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatusEnum(cdaFHO.getStatusCode().getCode()));
		}
		
		// condition <-> familyHistoryObservation
		// also, deceased value is set by looking at familyHistoryObservation.familyHistoryDeathObservation
		if(cdaFHO.getFamilyHistoryObservations() != null && !cdaFHO.getFamilyHistoryObservations().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryObservation familyHistoryObs : cdaFHO.getFamilyHistoryObservations()) {
				if(familyHistoryObs != null && !familyHistoryObs.isSetNullFlavor()) {
					
					// adding a new condition to fhirFMH
					FamilyMemberHistory.Condition condition = fhirFMH.addCondition();
					
					// code <-> familHistoryObs.value(CD)
					if(familyHistoryObs.getValues() != null && !familyHistoryObs.getValues().isEmpty()) {
						for(ANY value : familyHistoryObs.getValues()) {
							if(value != null && !value.isSetNullFlavor()) {
								if(value instanceof CD){
									condition.setCode(dtt.tCD2CodeableConcept((CD)value));
								}
							}
						}
					}
					
					// deceased
					if(familyHistoryObs.getFamilyHistoryDeathObservation() != null && !familyHistoryObs.getFamilyHistoryDeathObservation().isSetNullFlavor()) {
						// deceased <- true
						fhirFMH.setDeceased(new BooleanDt(true));
						
						// if dead, set outcome as dead
						if(familyHistoryObs.getFamilyHistoryDeathObservation().getValues() != null && !familyHistoryObs.getFamilyHistoryDeathObservation().getValues().isEmpty()) {
							for(ANY value : familyHistoryObs.getFamilyHistoryDeathObservation().getValues()) {
								if(value != null && !value.isSetNullFlavor()) {
									if(value instanceof CD) {
										condition.setOutcome(dtt.tCD2CodeableConcept((CD)value));
									}
								}
							}
						}
					}
					
					// onset <-> familyHistoryObs.ageObservation
					if(familyHistoryObs.getAgeObservation() != null && !familyHistoryObs.getAgeObservation().isSetNullFlavor()) {
						// onset
						AgeDt onset = tAgeObservation2AgeDt(familyHistoryObs.getAgeObservation());
						if(onset != null) {
							condition.setOnset(onset);
						}
					}
				}
			}
		}
			
		// getting information from cda->subject->relatedSubject
		if(cdaFHO.getSubject() != null && !cdaFHO.isSetNullFlavor() && cdaFHO.getSubject().getRelatedSubject() != null && !cdaFHO.getSubject().getRelatedSubject().isSetNullFlavor()) {
			org.openhealthtools.mdht.uml.cda.RelatedSubject cdaRelatedSubject = cdaFHO.getSubject().getRelatedSubject();
			
			// relationship: mother, father etc.
			if(cdaRelatedSubject.getCode() != null && !cdaRelatedSubject.getCode().isSetNullFlavor()) {
				fhirFMH.setRelationship(dtt.tCD2CodeableConcept(cdaRelatedSubject.getCode()));
			}
			
			// subject person
			if(cdaRelatedSubject.getSubject() != null && !cdaRelatedSubject.getSubject().isSetNullFlavor()) {
				org.openhealthtools.mdht.uml.cda.SubjectPerson subjectPerson = cdaRelatedSubject.getSubject();
				
				// name
				if(subjectPerson.getNames() != null && !subjectPerson.getNames().isEmpty()) {
					for(EN en : subjectPerson.getNames()) {
						if(en != null && !en.isSetNullFlavor()) {
							if(en.getText() != null) {
								fhirFMH.setName(en.getText());
							}
						}
					}
				}
				
				// gender
				if(subjectPerson.getAdministrativeGenderCode() != null && !subjectPerson.getAdministrativeGenderCode().isSetNullFlavor() &&
						subjectPerson.getAdministrativeGenderCode().getCode() != null) {
					fhirFMH.setGender(vst.tAdministrativeGenderCode2AdministrativeGenderEnum(subjectPerson.getAdministrativeGenderCode().getCode()));
				}

				// birthTime -> born
				if(subjectPerson.getBirthTime() != null && !subjectPerson.getBirthTime().isSetNullFlavor()) {
					fhirFMH.setBorn(dtt.tTS2Date(subjectPerson.getBirthTime()));
				}
			}
		}
		return fhirFMH;
	}

	public Condition tIndication2Condition(Indication indication) {
		if(indication == null || indication.isSetNullFlavor())
			return null;

		Condition fhirCond = new Condition();

		// id
		IdDt resourceId = new IdDt("Condition", getUniqueId());
		fhirCond.setId(resourceId);

		// identifier
		if(indication.getIds() != null && !indication.getIds().isEmpty()) {
			for(II ii : indication.getIds()) {
				fhirCond.addIdentifier(dtt.tII2Identifier(ii));
			}
		}

		// code -> category
		if(indication.getCode() != null && !indication.getCode().isSetNullFlavor()) {
			if(indication.getCode().getCode() != null) {
				ConditionCategoryCodesEnum conditionCategory = vst.tProblemType2ConditionCategoryCodesEnum(indication.getCode().getCode());
				if(conditionCategory != null) {
					fhirCond.setCategory(conditionCategory);
				}
			}
		}

		// effectiveTime -> onset & abatement
		if(indication.getEffectiveTime() != null && !indication.getEffectiveTime().isSetNullFlavor()) {

			IVXB_TS low = indication.getEffectiveTime().getLow();
			IVXB_TS high = indication.getEffectiveTime().getHigh();
			String value = indication.getEffectiveTime().getValue();

			// low and high are both empty, and only the @value exists -> onset
			if(low == null && high == null && value != null && !value.equals("")) {
				fhirCond.setOnset(dtt.tString2DateTime(value));
			}
			else {
				// low -> onset
				if (low != null && !low.isSetNullFlavor()) {
					fhirCond.setOnset(dtt.tTS2DateTime(low));
				}
				// high -> abatement
				if (high != null && !high.isSetNullFlavor()) {
					fhirCond.setAbatement(dtt.tTS2DateTime(high));
				}
			}
		}

		// value -> code
		if(indication.getValues() != null && !indication.getValues().isEmpty()) {
			// There is only 1 value, but anyway...
			for(ANY value : indication.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof CD)
						fhirCond.setCode(dtt.tCD2CodeableConcept((CD)value));
				}
			}
		}


		return fhirCond;
	}

	public Bundle tManufacturedProduct2Medication(ManufacturedProduct cdaManuProd) {
		if(cdaManuProd == null || cdaManuProd.isSetNullFlavor())
			return null;
		
		Medication fhirMedication = new Medication();
		
		Bundle fhirMedicationBundle = new Bundle();
		fhirMedicationBundle.addEntry(new Bundle.Entry().setResource(fhirMedication));
		
		// id
		IdDt resourceId = new IdDt("Medication", getUniqueId());
		fhirMedication.setId(resourceId);
		
		// TODO: Medication.name?
		// Medication.product.ingredient.amount?
		// Medication.product.form?
		// All of them couldn't found in CDA
		// (cdaManuProd.code gives a hint about all of them)
		
		// code <-> manufacturedMaterial.code
		if(cdaManuProd.getManufacturedMaterial() != null && !cdaManuProd.getManufacturedMaterial().isSetNullFlavor()) {
			if(cdaManuProd.getManufacturedMaterial().getCode() != null && !cdaManuProd.getManufacturedMaterial().isSetNullFlavor()) {
				fhirMedication.setCode(dtt.tCD2CodeableConcept(cdaManuProd.getManufacturedMaterial().getCode()));
			}
		}
		
		// TODO: is_brand & manufacturer are mapped although it is not mandatory by daf
		// Should we?
		
		// is_brand and manufacturer
		ResourceReferenceDt resourceReferenceManu = new ResourceReferenceDt();
		
		if(cdaManuProd.getManufacturerOrganization() != null && !cdaManuProd.getManufacturerOrganization().isSetNullFlavor()) {
			// is_brand
			fhirMedication.setIsBrand(true);
			
			// manufacturer
			Organization org = tOrganization2Organization(cdaManuProd.getManufacturerOrganization());
			
			resourceReferenceManu.setReference(org.getId());
			fhirMedication.setManufacturer(resourceReferenceManu);
			fhirMedicationBundle.addEntry(new Bundle.Entry().setResource(org));
		} else {
			fhirMedication.setIsBrand(false);
		}
		
		return fhirMedicationBundle;
	}

	public Bundle tMedicationActivity2MedicationStatement(MedicationActivity cdaMedAct) {
		if(cdaMedAct == null || cdaMedAct.isSetNullFlavor())
			return null;
		
		MedicationStatement fhirMedSt = new MedicationStatement();
		MedicationStatement.Dosage fhirDosage = fhirMedSt.addDosage();

		Bundle medStatementBundle = new Bundle();
		medStatementBundle.addEntry(new Bundle.Entry().setResource(fhirMedSt));
	
		// id
		IdDt resourceId = new IdDt("MedicationActivity", getUniqueId());
		fhirMedSt.setId(resourceId);

		// identifier
		if(cdaMedAct.getIds() != null && !cdaMedAct.getIds().isEmpty()) {
			for(II ii : cdaMedAct.getIds()) {
				fhirMedSt.addIdentifier(dtt.tII2Identifier(ii));
			}
		}

		// status
		if(cdaMedAct.getStatusCode() != null && !cdaMedAct.getStatusCode().isSetNullFlavor()) {
			if(cdaMedAct.getStatusCode().getCode() != null && !cdaMedAct.getStatusCode().getCode().isEmpty()) {
				MedicationStatementStatusEnum statusCode = vst.tStatusCode2MedicationStatementStatusEnum(cdaMedAct.getStatusCode().getCode());
				if(statusCode != null) {
					fhirMedSt.setStatus(statusCode);
				}
			}
		}
		
		// patient
		fhirMedSt.setPatient(getPatientRef());
		
		// medication <-> cdaMedAct.consumable.manufacturedProduct
		if(cdaMedAct.getConsumable() != null && !cdaMedAct.getConsumable().isSetNullFlavor()) {
			if(cdaMedAct.getConsumable().getManufacturedProduct() != null && !cdaMedAct.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
	
				Medication fhirMedication = null;
				Bundle fhirMedicationBundle = tManufacturedProduct2Medication(cdaMedAct.getConsumable().getManufacturedProduct());
				
				for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry()){
					medStatementBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication) {
						fhirMedication = (ca.uhn.fhir.model.dstu2.resource.Medication)entry.getResource();
					}
				}
				
				
				fhirMedSt.setMedication(new ResourceReferenceDt(fhirMedication.getId()));
			}
		}
		
		// effectiveTime
		if(cdaMedAct.getEffectiveTimes() != null && !cdaMedAct.getEffectiveTimes().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedAct.getEffectiveTimes()) {
				if(ts != null && !ts.isSetNullFlavor()) {
					// IVL_TS -> effectivePeriod
					if(ts instanceof IVL_TS) {
						fhirMedSt.setEffective(dtt.tIVL_TS2Period((IVL_TS)ts));
					}
					// PIVL_TS -> dosage.timing
					if(ts instanceof PIVL_TS) {
						fhirDosage.setTiming(dtt.tPIVL_TS2Timing((PIVL_TS)ts));
					}
				}
			}
		}
		
		// dosage.route
		if(cdaMedAct.getRouteCode() != null && !cdaMedAct.getRouteCode().isSetNullFlavor()) {
			fhirDosage.setRoute(dtt.tCD2CodeableConcept(cdaMedAct.getRouteCode()));
		}
		
		// dosage.quantity
		if(cdaMedAct.getDoseQuantity() != null && !cdaMedAct.getDoseQuantity().isSetNullFlavor()) {
			fhirDosage.setQuantity(dtt.tPQ2SimpleQuantityDt(cdaMedAct.getDoseQuantity()));
		}
		
		// dosage.rate
		if(cdaMedAct.getRateQuantity() != null && !cdaMedAct.getRateQuantity().isSetNullFlavor()) {
			fhirDosage.setRate(dtt.tIVL_PQ2Range(cdaMedAct.getRateQuantity()));
		}
		
		// dosage.maxDosePerPeriod
		if(cdaMedAct.getMaxDoseQuantity() != null && !cdaMedAct.getMaxDoseQuantity().isSetNullFlavor()) {
			// cdaDataType.RTO does nothing but extends cdaDataType.RTO_PQ_PQ
			fhirDosage.setMaxDosePerPeriod(dtt.tRTO2Ratio( (RTO) cdaMedAct.getMaxDoseQuantity()));
		}

		// wasNotTaken
		if(cdaMedAct.getNegationInd() != null) {
			fhirMedSt.setWasNotTaken(cdaMedAct.getNegationInd());
		}

		// indication -> reason
		for(Indication indication : cdaMedAct.getIndications()) {
			// First, to set reasonForUse, we need to set wasNotTaken to false
			fhirMedSt.setWasNotTaken(false);

			Condition cond = tIndication2Condition(indication);
			medStatementBundle.addEntry(new Bundle.Entry().setResource(cond));
			fhirMedSt.setReasonForUse(new ResourceReferenceDt(cond.getId()));
		}

		return medStatementBundle;	
	}

	public Bundle tMedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMediDisp) {
		if(cdaMediDisp == null || cdaMediDisp.isSetNullFlavor())
			return null;
		else if(cdaMediDisp.getMoodCode() != x_DocumentSubstanceMood.EVN)
			return null;
		
		// TODO: Following mapping doesn't really suit the mapping proposed by daf
		// Example file and the pdf doesn't explain much
		
		MedicationDispense fhirMediDisp = new MedicationDispense();
		Bundle fhirMediDispBundle = new Bundle();
		fhirMediDispBundle.addEntry(new Bundle.Entry().setResource(fhirMediDisp));
	
		// patient
		fhirMediDisp.setPatient(getPatientRef());
		
		// id
		IdDt resourceId = new IdDt("MedicationDispense", getUniqueId());
		fhirMediDisp.setId(resourceId);
		
		// identifier
		if(cdaMediDisp.getIds() != null &  !cdaMediDisp.getIds().isEmpty()) {
			for(II ii : cdaMediDisp.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					// Asserting at most one identifier exists
					fhirMediDisp.setIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// status
		if(cdaMediDisp.getStatusCode() != null && !cdaMediDisp.getStatusCode().isSetNullFlavor()) {
			if(cdaMediDisp.getStatusCode().getCode() != null && !cdaMediDisp.getStatusCode().getCode().isEmpty()) {
				MedicationDispenseStatusEnum mediDispStatEnum = vst.tStatusCode2MedicationDispenseStatusEnum(cdaMediDisp.getStatusCode().getCode());
				if(mediDispStatEnum != null){
					fhirMediDisp.setStatus(mediDispStatEnum);
				}
			}
		}
		
		// type <-> code
		if(cdaMediDisp.getCode() != null && !cdaMediDisp.getCode().isSetNullFlavor()){
			fhirMediDisp.setType(dtt.tCD2CodeableConcept(cdaMediDisp.getCode()));
		}
		
		// medication <-> product.manufacturedProduct
		if(cdaMediDisp.getProduct() != null && !cdaMediDisp.getProduct().isSetNullFlavor()) {
			if(cdaMediDisp.getProduct().getManufacturedProduct() != null && !cdaMediDisp.getProduct().getManufacturedProduct().isSetNullFlavor()) {
				Medication fhirMedication = null;
				Bundle fhirMedicationBundle = tManufacturedProduct2Medication(cdaMediDisp.getProduct().getManufacturedProduct());
				
				for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry()) {
					fhirMediDispBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication){
						fhirMedication = (ca.uhn.fhir.model.dstu2.resource.Medication)entry.getResource();
					}
				}
				
				fhirMediDisp.setMedication(new ResourceReferenceDt(fhirMedication.getId()));
			}
		}
		
		// dispenser
		if(cdaMediDisp.getPerformers() != null && !cdaMediDisp.getPerformers().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaMediDisp.getPerformers()) {
				if(cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					// Asserting that at most one performer exists
					ca.uhn.fhir.model.dstu2.resource.Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tPerformer22Practitioner(cdaPerformer);
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						fhirMediDispBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Practitioner) {
							fhirPractitioner = (ca.uhn.fhir.model.dstu2.resource.Practitioner)entry.getResource();
						}
					}
					
					fhirMediDisp.setDispenser(new ResourceReferenceDt(fhirPractitioner.getId()));
				}
			}
		}
		
		// quantity
		if(cdaMediDisp.getQuantity() != null && !cdaMediDisp.getQuantity().isSetNullFlavor()) {
			fhirMediDisp.setQuantity(dtt.tPQ2SimpleQuantityDt( cdaMediDisp.getQuantity()));
		}
		
		// whenPrepared and whenHandedOver
		int effectiveTimeCount = 0;
		if(cdaMediDisp.getEffectiveTimes() != null && !cdaMediDisp.getEffectiveTimes().isEmpty()) {
			for(SXCM_TS ts : cdaMediDisp.getEffectiveTimes()) {
				if(effectiveTimeCount == 0) {
					// whenPrepared: 1st effectiveTime
					if(ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenPrepared(dtt.tTS2DateTime(ts));
					}
					effectiveTimeCount++;
				} else if( effectiveTimeCount == 1) {
					// whenHandedOver: 2nd effectiveTime
					if(ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenHandedOver(dtt.tTS2DateTime(ts));
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
		
		// dosageInstruction.timing <-> effectiveTimes
		if(cdaMediDisp.getEffectiveTimes() != null && !cdaMediDisp.getEffectiveTimes().isEmpty()) {
			TimingDt fhirTiming = new TimingDt();
			
			// adding effectiveTimes to fhirTiming
			for(org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMediDisp.getEffectiveTimes()) {
				if(ts != null && !ts.isSetNullFlavor()) {
					fhirTiming.addEvent(dtt.tTS2DateTime(ts));
				} else if(ts.getValue() != null && !ts.getValue().isEmpty()) {
					fhirTiming.addEvent(dtt.tString2DateTime(ts.getValue()));
				}
			}
			
			// setting fhirTiming for dosageInstruction if it is not empty
			if(!fhirTiming.isEmpty()) {
				fhirDosageInstruction.setTiming(fhirTiming);
			}
		}
		
		// dosageInstruction.dose
		if(cdaMediDisp.getQuantity() != null && !cdaMediDisp.getQuantity().isSetNullFlavor()) {
			fhirDosageInstruction.setDose(dtt.tPQ2SimpleQuantityDt(cdaMediDisp.getQuantity()));
		}
		return fhirMediDispBundle;
	}

	public Bundle tObservation2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObs) {
		if(cdaObs == null || cdaObs.isSetNullFlavor())
			return null;

		Observation fhirObs = new Observation();

		// bundle
		Bundle fhirObsBundle = new Bundle();
		fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirObs));

		// id
		IdDt resourceId = new IdDt("Observation", getUniqueId());
		fhirObs.setId(resourceId);

		// subject
		fhirObs.setSubject(getPatientRef());

		// identifier
		if(cdaObs.getIds() != null && !cdaObs.getIds().isEmpty()) {
			for(II ii : cdaObs.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code
		if(cdaObs.getCode() != null && !cdaObs.getCode().isSetNullFlavor()) {
			fhirObs.setCode(dtt.tCD2CodeableConcept(cdaObs.getCode()));
		}

		// status
		if(cdaObs.getStatusCode() != null && !cdaObs.getStatusCode().isSetNullFlavor()) {
			if(cdaObs.getStatusCode().getCode() != null) {
				fhirObs.setStatus(vst.tObservationStatusCode2ObservationStatusEnum(cdaObs.getStatusCode().getCode()));
			}
		}

		// effective
		if(cdaObs.getEffectiveTime() != null && !cdaObs.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObs.getEffectiveTime()));
		}

		// targetSiteCode <-> bodySite
		if(cdaObs.getTargetSiteCodes() != null && !cdaObs.getTargetSiteCodes().isEmpty()) {
			for(CD cd : cdaObs.getTargetSiteCodes())
			{
				if(cd != null && !cd.isSetNullFlavor()) {
					fhirObs.setBodySite(dtt.tCD2CodeableConcept(cd));
				}
			}
		}

		// value or dataAbsentReason
		if(cdaObs.getValues() != null && !cdaObs.getValues().isEmpty()) {
			// We traverse the values in cdaObs
			for(ANY value : cdaObs.getValues()) {
				if(value == null) continue; // If the value is null, continue
				else if(value.isSetNullFlavor()) {
					// If a null flavor exists, then we set dataAbsentReason by looking at the null-flavor value
					CodingDt DataAbsentReasonCode = vst.tNullFlavor2DataAbsentReasonCode(value.getNullFlavor());
					if(DataAbsentReasonCode != null) {
						if(fhirObs.getDataAbsentReason() == null || fhirObs.getDataAbsentReason().isEmpty()) {
							// If DataAbsentReason was not set, create a new CodeableConcept and add our code into it
							fhirObs.setDataAbsentReason(new CodeableConceptDt().addCoding(DataAbsentReasonCode));
						} else {
							// If DataAbsentReason was set, just get the CodeableConcept and add our code into it
							fhirObs.getDataAbsentReason().addCoding( DataAbsentReasonCode);
						}
					}
				} else{
					// If a non-null value which has no null-flavor exists, then we can get the value
					// Checking the type of value
					if(value instanceof CD) {
						fhirObs.setValue(dtt.tCD2CodeableConcept((CD)value));
					} else if(value instanceof PQ) {
						fhirObs.setValue(dtt.tPQ2Quantity((PQ)value));
					} else if(value instanceof ST) {
						fhirObs.setValue(dtt.tST2String((ST)value));
					} else if(value instanceof IVL_PQ) {
						fhirObs.setValue(dtt.tIVL_PQ2Range((IVL_PQ)value));
					} else if(value instanceof RTO) {
						fhirObs.setValue(dtt.tRTO2Ratio((RTO)value));
					} else if(value instanceof ED) {
						fhirObs.setValue(dtt.tED2Attachment((ED)value));
					}
					else if(value instanceof TS) {
						fhirObs.setValue(dtt.tTS2DateTime((TS)value));
					}
				}
			}
		}

		// encounter
		if(cdaObs.getEncounters() != null && !cdaObs.getEncounters().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter : cdaObs.getEncounters()) {
				if(cdaEncounter != null && !cdaEncounter.isSetNullFlavor()) {
					// Asserting at most one encounter exists
					ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = null;
					Bundle fhirEncounterBundle = tEncounter2Encounter(cdaEncounter);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entity : fhirEncounterBundle.getEntry()) {
						if(entity.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter) {
							fhirEncounter = (ca.uhn.fhir.model.dstu2.resource.Encounter)entity.getResource();
						}
					}

					if(fhirEncounter != null) {
						ResourceReferenceDt fhirEncounterReference = new ResourceReferenceDt();
						fhirEncounterReference.setReference(fhirEncounter.getId());
						fhirObs.setEncounter(fhirEncounterReference);
						fhirObsBundle.addEntry(new Bundle().addEntry().setResource(fhirEncounter));
					}
				}
			}
		}

		// performer
		if(cdaObs.getAuthors() != null && !cdaObs.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaObs.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());

					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						fhirObsBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof Practitioner) {

							fhirPractitioner = (Practitioner)entry.getResource();
						}
					}

					fhirObs.addPerformer().setReference(fhirPractitioner.getId());
				}
			}
		}

		// method
		if(cdaObs.getMethodCodes() != null && !cdaObs.getMethodCodes().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.CE method : cdaObs.getMethodCodes()) {
				if(method != null && !method.isSetNullFlavor()) {
					// Asserting that only one method exists
					fhirObs.setMethod(dtt.tCD2CodeableConcept(method));
				}
			}
		}

		// issued
		if(cdaObs.getAuthors() != null && !cdaObs.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaObs.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					// get time from author
					if(author.getTime() != null && !author.getTime().isSetNullFlavor()) {
						fhirObs.setIssued(dtt.tTS2Instant(author.getTime()));
					}
				}
			}
		}

		// interpretation
		if(cdaObs.getInterpretationCodes() != null && !cdaObs.getInterpretationCodes().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.CE cdaInterprCode : cdaObs.getInterpretationCodes()) {
				if(cdaInterprCode != null && !cdaInterprCode.isSetNullFlavor()) {
				// Asserting that only one interpretation code exists
					fhirObs.setInterpretation(dtt.tCD2CodeableConcept(cdaInterprCode));
				}
			}
		}

		// reference range
		if(cdaObs.getReferenceRanges() != null && !cdaObs.getReferenceRanges().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange : cdaObs.getReferenceRanges()) {
				if(cdaReferenceRange != null && !cdaReferenceRange.isSetNullFlavor()) {
					fhirObs.addReferenceRange(tReferenceRange2ReferenceRange(cdaReferenceRange));
				}
			}
		}
		return fhirObsBundle;
	}

	public Organization tOrganization2Organization (org.openhealthtools.mdht.uml.cda.Organization cdaOrganization){
		if(cdaOrganization == null || cdaOrganization.isSetNullFlavor())
			return null;
		
		Organization fhirOrganization = new Organization();
		
		// id
		IdDt resourceId = new IdDt("Organization",getUniqueId());
		fhirOrganization.setId(resourceId);
		
		// identifier
		if(cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for(II ii : cdaOrganization.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name
		if(cdaOrganization.getNames() != null && !cdaOrganization.isSetNullFlavor()) {
			for(ON name:cdaOrganization.getNames()) {
				if(name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty()) {
					fhirOrganization.setName(name.getText());
				}
			}
		}
		
		// contact <-> telecom
		if(cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty()) {
			for(TEL tel : cdaOrganization.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirOrganization.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// address
		if(cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for(AD ad : cdaOrganization.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		return fhirOrganization;
	}

	public Location tParticipantRole2Location(ParticipantRole cdaParticipantRole) {
		if(cdaParticipantRole == null || cdaParticipantRole.isSetNullFlavor())
			return null;

		Location fhirLocation = new Location();
		
		// id
		IdDt resourceId = new IdDt("Location", getUniqueId());
		fhirLocation.setId(resourceId);
		
		// identifier
		if(cdaParticipantRole.getIds() != null && !cdaParticipantRole.getIds().isEmpty()) {
			for(II ii : cdaParticipantRole.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirLocation.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name
		if(cdaParticipantRole.getPlayingEntity() != null && !cdaParticipantRole.getPlayingEntity().isSetNullFlavor()) {
			if(cdaParticipantRole.getPlayingEntity().getNames() != null && !cdaParticipantRole.getPlayingEntity().getNames().isEmpty()) {
				for(PN pn : cdaParticipantRole.getPlayingEntity().getNames()) {
					// Asserting that at most one name exists
					if(pn != null && !pn.isSetNullFlavor()) {
						fhirLocation.setName(pn.getText());
					}
				}
			}
		}
		
		// telecom
		if(cdaParticipantRole.getTelecoms() != null && !cdaParticipantRole.getTelecoms().isEmpty()) {
			for(TEL tel : cdaParticipantRole.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirLocation.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// address
		if(cdaParticipantRole.getAddrs() != null && !cdaParticipantRole.getAddrs().isEmpty()) {
			for(AD ad : cdaParticipantRole.getAddrs()) {
				// Asserting that at most one address exists
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirLocation.setAddress(dtt.AD2Address(ad));
				}
			}
		}			

		return fhirLocation;
	}
	
	public Bundle tPatientRole2Patient(PatientRole cdaPatientRole) {
		if(cdaPatientRole == null || cdaPatientRole.isSetNullFlavor())
			return null;

		Patient fhirPatient = new Patient();

		Bundle fhirPatientBundle  = new Bundle();
		fhirPatientBundle.addEntry(new Bundle.Entry().setResource(fhirPatient));

		// resource id
		IdDt resourceId = new IdDt("Patient", getUniqueId());
		fhirPatient.setId(resourceId);

		// identifier <-> id
		if(cdaPatientRole.getIds() != null && !cdaPatientRole.getIds().isEmpty()) {
			for(II id : cdaPatientRole.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirPatient.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// name <-> patient.name
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() &&
				cdaPatientRole.getPatient().getNames() != null && !cdaPatientRole.getPatient().getNames().isEmpty()) {
			for(PN pn : cdaPatientRole.getPatient().getNames()) {
				if(pn != null && !pn.isSetNullFlavor()) {
					fhirPatient.addName(dtt.tEN2HumanName(pn));
				}
			}
		}

		// telecom <-> telecom
		if(cdaPatientRole.getTelecoms() != null && !cdaPatientRole.getTelecoms().isEmpty()) {
			for(TEL tel : cdaPatientRole.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirPatient.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// gender <-> patient.administrativeGenderCode
		boolean administrativeGenderCodeNullCheck = cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor()
				&& cdaPatientRole.getPatient().getAdministrativeGenderCode() != null 
				&& !cdaPatientRole.getPatient().getAdministrativeGenderCode().isSetNullFlavor() 
				&& cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode() != null
				&& !cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode().isEmpty();
		if(administrativeGenderCodeNullCheck) {
			AdministrativeGenderEnum administrativeGender = vst.tAdministrativeGenderCode2AdministrativeGenderEnum(cdaPatientRole.getPatient().getAdministrativeGenderCode().getCode());
			fhirPatient.setGender(administrativeGender);
		}

		// birthDate <-> patient.birthTime
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() 
				&& cdaPatientRole.getPatient().getBirthTime() != null && !cdaPatientRole.getPatient().getBirthTime().isSetNullFlavor()) {
			fhirPatient.setBirthDate( dtt.tTS2Date(cdaPatientRole.getPatient().getBirthTime()));
		}

		// address <-> addr
		if(cdaPatientRole.getAddrs() != null && !cdaPatientRole.getAddrs().isEmpty()) {
			for(AD ad : cdaPatientRole.getAddrs()){
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirPatient.addAddress(dtt.AD2Address(ad));
				}
			}
		}

		// maritalStatus <-> patient.maritalStatusCode
		if(cdaPatientRole.getPatient().getMaritalStatusCode() != null
				&& !cdaPatientRole.getPatient().getMaritalStatusCode().isSetNullFlavor()) {
			if(cdaPatientRole.getPatient().getMaritalStatusCode().getCode() != null && !cdaPatientRole.getPatient().getMaritalStatusCode().getCode().isEmpty()) {
				fhirPatient.setMaritalStatus(vst.tMaritalStatusCode2MaritalStatusCodesEnum(cdaPatientRole.getPatient().getMaritalStatusCode().getCode()));
			}
		}

		// communication <-> patient.languageCommunication
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() &&
				cdaPatientRole.getPatient().getLanguageCommunications() != null &&
				!cdaPatientRole.getPatient().getLanguageCommunications().isEmpty()) {

			for(LanguageCommunication LC : cdaPatientRole.getPatient().getLanguageCommunications()) {
				if(LC != null && !LC.isSetNullFlavor()) {
					fhirPatient.addCommunication(tLanguageCommunication2Communication(LC));
				}
			}
		}
		// managingOrganization <-> providerOrganization
		if(cdaPatientRole.getProviderOrganization() != null && !cdaPatientRole.getProviderOrganization().isSetNullFlavor()) {
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(cdaPatientRole.getProviderOrganization());
			fhirPatientBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
			ResourceReferenceDt organizationReference = new ResourceReferenceDt(fhirOrganization.getId());
			fhirPatient.setManagingOrganization(organizationReference);
		}

		// guardian <-> patient.guardians
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() &&
				cdaPatientRole.getPatient().getGuardians() != null && !cdaPatientRole.getPatient().getGuardians().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Guardian guardian : cdaPatientRole.getPatient().getGuardians()) {
				fhirPatient.addContact(tGuardian2Contact(guardian));
			}
		}


		// extensions

		// extRace <-> patient.raceCode
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getRaceCode() != null && !cdaPatientRole.getPatient().getRaceCode().isSetNullFlavor()) {
			ExtensionDt extRace = new ExtensionDt();
			extRace.setModifier(false);
			extRace.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
			CD raceCode = cdaPatientRole.getPatient().getRaceCode();
			extRace.setValue( dtt.tCD2CodeableConcept(raceCode));
			fhirPatient.addUndeclaredExtension(extRace);
		}

		// extEthnicity <-> patient.ethnicGroupCode
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getEthnicGroupCode() != null && !cdaPatientRole.getPatient().getEthnicGroupCode().isSetNullFlavor()) {
			ExtensionDt extEthnicity = new ExtensionDt();
			extEthnicity.setModifier(false);
			extEthnicity.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
			CD ethnicGroupCode = cdaPatientRole.getPatient().getEthnicGroupCode();
			extEthnicity.setValue( dtt.tCD2CodeableConcept(ethnicGroupCode));
			fhirPatient.addUndeclaredExtension(extEthnicity);
		}

		// extReligion
		if(cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor() && cdaPatientRole.getPatient().getReligiousAffiliationCode() != null && !cdaPatientRole.getPatient().getReligiousAffiliationCode().isSetNullFlavor()) {
			ExtensionDt extReligion = new ExtensionDt();
			extReligion.setModifier(false);
			extReligion.setUrl("http://hl7.org/fhir/StructureDefinition/us-core-religion");
			CD religiousAffiliationCode = cdaPatientRole.getPatient().getReligiousAffiliationCode();
			extReligion.setValue( dtt.tCD2CodeableConcept(religiousAffiliationCode));
			fhirPatient.addUndeclaredExtension(extReligion);
		}

		// extBirthPlace
		boolean birthplaceExtensionNullCheck = cdaPatientRole.getPatient() != null && !cdaPatientRole.getPatient().isSetNullFlavor()
				&& cdaPatientRole.getPatient().getBirthplace() != null && !cdaPatientRole.getPatient().getBirthplace().isSetNullFlavor()
				&& cdaPatientRole.getPatient().getBirthplace().getPlace() != null && !cdaPatientRole.getPatient().getBirthplace().getPlace().isSetNullFlavor()
				&& cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr() != null && !cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr().isSetNullFlavor();
		if(birthplaceExtensionNullCheck) {
			ExtensionDt extBirthPlace = new ExtensionDt();
			extBirthPlace.setModifier(false);
			extBirthPlace.setUrl("http://hl7.org/fhir/StructureDefinition/birthPlace");
			extBirthPlace.setValue(dtt.AD2Address(cdaPatientRole.getPatient().getBirthplace().getPlace().getAddr()));
			fhirPatient.addUndeclaredExtension(extBirthPlace);
		}
			
		return fhirPatientBundle;
	}
	
	public Bundle tPerformer22Practitioner(Performer2 cdaPerformer) {
		if(cdaPerformer == null || cdaPerformer.isSetNullFlavor()) 
			return null;
		else
			return tAssignedEntity2Practitioner(cdaPerformer.getAssignedEntity());
		
	}

	public Bundle tProblemConcernAct2Condition(ProblemConcernAct cdaProbConcAct) {
		if(cdaProbConcAct == null || cdaProbConcAct.isSetNullFlavor())
			return null;
		
		Bundle fhirConditionBundle = new Bundle();

		for(EntryRelationship entryRelationship : cdaProbConcAct.getEntryRelationships()) {
			
			Condition fhirCondition = new Condition();
			
			fhirConditionBundle.addEntry(new Bundle.Entry().setResource(fhirCondition));
			
			// id
			IdDt resourceId = new IdDt("Condition", getUniqueId());
			fhirCondition.setId(resourceId);
			
			// id -> identifier
			if(cdaProbConcAct.getIds() != null && !cdaProbConcAct.getIds().isEmpty()) {
				for(II ii : cdaProbConcAct.getIds()) {
					if(ii != null && !ii.isSetNullFlavor()) {
						fhirCondition.addIdentifier(dtt.tII2Identifier(ii));
					}
				}
			}
			
			// patient
			fhirCondition.setPatient(getPatientRef());

			
			// TODO: Severity
			// Couldn't found in the CDA example

			// encounter -> encounter
			if(cdaProbConcAct.getEncounters() != null && !cdaProbConcAct.getEncounters().isEmpty()) {
				if(cdaProbConcAct.getEncounters().get(0) != null && cdaProbConcAct.getEncounters().get(0).isSetNullFlavor()) {
					ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = null;
					Bundle fhirEncounterBundle = tEncounter2Encounter(cdaProbConcAct.getEncounters().get(0));
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirEncounterBundle.getEntry()) {
						fhirConditionBundle.addEntry( new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter){
							fhirEncounter = (ca.uhn.fhir.model.dstu2.resource.Encounter) entry.getResource();
						}
					}
				
					fhirCondition.setEncounter(new ResourceReferenceDt(fhirEncounter.getId()));
				}
			}
		
			// author -> asserter
			if(cdaProbConcAct.getAuthors() != null && !cdaProbConcAct.getAuthors().isEmpty()) {
				for(org.openhealthtools.mdht.uml.cda.Author author : cdaProbConcAct.getAuthors()) {
					if(author != null && !author.isSetNullFlavor()) {
						Practitioner fhirPractitioner = null;
						Bundle fhirPractitionerBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());
						
						for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
							fhirConditionBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));	
							if(entry.getResource() instanceof Practitioner) {
								fhirPractitioner = (Practitioner) entry.getResource();
							}
						}
						fhirCondition.setAsserter(new ResourceReferenceDt(fhirPractitioner.getId()));
					}
				}
			}
						
			// getting information from problem observation
			if(entryRelationship.getObservation() != null && !entryRelationship.getObservation().isSetNullFlavor()) {
				if(entryRelationship.getObservation() instanceof ProblemObservation){
					ProblemObservation cdaProbObs = (ProblemObservation)entryRelationship.getObservation();
					
					// code -> category
					if(cdaProbObs.getCode() != null && !cdaProbObs.getCode().isSetNullFlavor()) {
						if(cdaProbObs.getCode().getCode() != null) {
							ConditionCategoryCodesEnum conditionCategory = vst.tProblemType2ConditionCategoryCodesEnum(cdaProbObs.getCode().getCode());
							if(conditionCategory != null) {
								fhirCondition.setCategory(conditionCategory);
							}
						}
					}

					// value -> code
					if(cdaProbObs.getValues() != null && !cdaProbObs.getValues().isEmpty()) {
						for(ANY value : cdaProbObs.getValues()) {
							if(value != null && !value.isSetNullFlavor()) {
								if(value instanceof CD) {
									fhirCondition.setCode(dtt.tCD2CodeableConcept((CD)value));
								}
							}
						}
					}

					// onset and abatement
					if(cdaProbObs.getEffectiveTime() != null &&  !cdaProbObs.getEffectiveTime().isSetNullFlavor()) {

						IVXB_TS low = cdaProbObs.getEffectiveTime().getLow();
						IVXB_TS high = cdaProbObs.getEffectiveTime().getHigh();

						// low -> onset
						if(low != null && !low.isSetNullFlavor()) {
							fhirCondition.setOnset(dtt.tTS2DateTime(low));
						} else if(cdaProbObs.getEffectiveTime().getValue() != null && !cdaProbObs.getEffectiveTime().getValue().isEmpty()) {
							fhirCondition.setOnset(dtt.tString2DateTime(cdaProbObs.getEffectiveTime().getValue()));
						}

						// high -> abatement
						if(high != null && !high.isSetNullFlavor()) {
							fhirCondition.setAbatement(dtt.tTS2DateTime(high));
						}
					}


					// author.time -> dateRecorded
					if(cdaProbObs.getAuthors() != null && !cdaProbObs.getAuthors().isEmpty()) {
						for(Author author : cdaProbObs.getAuthors()) {
							if(author != null && !author.isSetNullFlavor()) {
								if(author.getTime() != null && !author.getTime().isSetNullFlavor()) {
									fhirCondition.setDateRecorded(dtt.tTS2Date(author.getTime()));
								}
							}
						}
					}
				}
			}
		}
		return fhirConditionBundle;
	}

	public Bundle tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaPr){
		if(cdaPr == null || cdaPr.isSetNullFlavor())
			return null;

		ca.uhn.fhir.model.dstu2.resource.Procedure fhirPr = new ca.uhn.fhir.model.dstu2.resource.Procedure();
		Bundle fhirPrBundle = new Bundle();
		fhirPrBundle.addEntry(new Bundle.Entry().setResource(fhirPr));

		// subject
		fhirPr.setSubject(getPatientRef());

		// id
		IdDt resourceId = new IdDt("Procedure", getUniqueId());
		fhirPr.setId(resourceId);

		// identifier
		if(cdaPr.getIds() != null && !cdaPr.getIds().isEmpty()) {
			for(II id : cdaPr.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirPr.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// performed
		if(cdaPr.getEffectiveTime() != null && !cdaPr.getEffectiveTime().isSetNullFlavor()){
			fhirPr.setPerformed(dtt.tIVL_TS2Period(cdaPr.getEffectiveTime()));
		}

		// bodySite
		if(cdaPr.getTargetSiteCodes() != null && !cdaPr.getTargetSiteCodes().isEmpty()) {
			for(CD cd : cdaPr.getTargetSiteCodes()) {
				if(cd != null && !cd.isSetNullFlavor()){
					fhirPr.addBodySite(dtt.tCD2CodeableConcept(cd));
				}
			}
		}

		// performer
		if(cdaPr.getPerformers() != null && !cdaPr.getPerformers().isEmpty()) {
			for(Performer2 performer : cdaPr.getPerformers()) {
				if(performer.getAssignedEntity()!= null && !performer.getAssignedEntity().isSetNullFlavor()) {
					Bundle practBundle = tPerformer22Practitioner(performer);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
						// Add all the resources returned from the bundle to the main bundle
						fhirPrBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						// Add a reference to performer attribute only for Practitioner resource. Further resources can include Organization.
						if(entry.getResource() instanceof Practitioner) {
							Performer fhirPerformer = new Performer();
							fhirPerformer.setActor(new ResourceReferenceDt(entry.getResource().getId()));
							fhirPr.addPerformer(fhirPerformer);
						}
					}
				}
			}
		}

		// status
		if(cdaPr.getStatusCode() != null && !cdaPr.getStatusCode().isSetNullFlavor() && cdaPr.getStatusCode().getCode() != null) {
			ProcedureStatusEnum status = vst.tStatusCode2ProcedureStatusEnum(cdaPr.getStatusCode().getCode());
			if(status != null) {
				fhirPr.setStatus(status);
			}
		}

		// code
		if(cdaPr.getCode() != null && !cdaPr.getCode().isSetNullFlavor()) {
			fhirPr.setCode(dtt.tCD2CodeableConcept(cdaPr.getCode()));
		}

		// used <-> device
		// no example found in example cda.xml file
		// however, it exists in transformed version

		return fhirPrBundle;

	}

	public Bundle tResultObservation2Observation(ResultObservation cdaResultObs) {
		return tObservation2Observation(cdaResultObs);
	}

	public Bundle tSubstanceAdministration2Immunization(SubstanceAdministration cdaSubAdm) {
		if(cdaSubAdm == null || cdaSubAdm.isSetNullFlavor())
			return null;
		
		Immunization fhirImmunization = new Immunization();
		
		Bundle fhirImmunizationBundle = new Bundle();
		fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirImmunization));
		
		// id
		IdDt resourceId = new IdDt("Immunization", getUniqueId());
		fhirImmunization.setId(resourceId);
		
		// patient
		fhirImmunization.setPatient(getPatientRef());
		
		// identifier
		if(cdaSubAdm.getIds()!=null && !cdaSubAdm.getIds().isEmpty()) {
			for(II ii : cdaSubAdm.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirImmunization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// effective time
		if(cdaSubAdm.getEffectiveTimes()!=null && !cdaSubAdm.getEffectiveTimes().isEmpty()) {
			for(SXCM_TS effectiveTime : cdaSubAdm.getEffectiveTimes()) {
				if(effectiveTime != null && !effectiveTime.isSetNullFlavor()) {
					// Asserting that at most one effective time exists
					fhirImmunization.setDate(dtt.tTS2DateTime(effectiveTime));
				}
			}
		}
		
		// lotNumber, vaccineCode, organization
		if(cdaSubAdm.getConsumable()!=null && !cdaSubAdm.getConsumable().isSetNullFlavor()) {
			if(cdaSubAdm.getConsumable().getManufacturedProduct()!=null && !cdaSubAdm.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
				ManufacturedProduct manufacturedProduct=cdaSubAdm.getConsumable().getManufacturedProduct();
				
				if(manufacturedProduct.getManufacturedMaterial()!=null && !manufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
					Material manufacturedMaterial=manufacturedProduct.getManufacturedMaterial();
					
					// vaccineCode
					if(manufacturedProduct.getManufacturedMaterial().getCode() != null && !manufacturedProduct.getManufacturedMaterial().getCode().isSetNullFlavor()) {
						fhirImmunization.setVaccineCode(dtt.tCD2CodeableConcept(manufacturedMaterial.getCode()));
					}
					
					// lotNumber
					if(manufacturedMaterial.getLotNumberText()!=null && !manufacturedMaterial.getLotNumberText().isSetNullFlavor()) {
						fhirImmunization.setLotNumber(dtt.tST2String(manufacturedMaterial.getLotNumberText()));
					}
				}
				
				// organization
				if(manufacturedProduct.getManufacturerOrganization()!=null && !manufacturedProduct.getManufacturerOrganization().isSetNullFlavor()) {
					
					ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(manufacturedProduct.getManufacturerOrganization());
					
					fhirImmunization.setManufacturer(new ResourceReferenceDt(fhirOrganization.getId()));
					fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
				}
			}
		}
		
		// performer
		if(cdaSubAdm.getPerformers() != null && !cdaSubAdm.getPerformers().isEmpty()) {
			for(Performer2 performer : cdaSubAdm.getPerformers()) {
				if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor()) {
					Bundle practBundle = tPerformer22Practitioner(performer);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
						// Add all the resources returned from the bundle to the main bundle
						fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						// Add a reference to performer attribute only for Practitioner resource. Further resources can include Organization.
						if(entry.getResource() instanceof Practitioner) {
							fhirImmunization.setPerformer(new ResourceReferenceDt(entry.getResource().getId()));
						}
					}
				}
			}
		}
						
		// site
		if(cdaSubAdm.getApproachSiteCodes()!=null && !cdaSubAdm.getApproachSiteCodes().isEmpty()) {
			for(CD cd : cdaSubAdm.getApproachSiteCodes()) {
				// Asserting that at most one site exists
				fhirImmunization.setSite(dtt.tCD2CodeableConcept(cd));
			}
		}
		
		// route
		if(cdaSubAdm.getRouteCode()!=null && !cdaSubAdm.getRouteCode().isSetNullFlavor()) {
			fhirImmunization.setRoute(dtt.tCD2CodeableConcept(cdaSubAdm.getRouteCode()));
		}
		
		// dose quantity
		if(cdaSubAdm.getDoseQuantity()!=null && !cdaSubAdm.getDoseQuantity().isSetNullFlavor()) {
			fhirImmunization.setDoseQuantity(dtt.tPQ2SimpleQuantityDt( cdaSubAdm.getDoseQuantity()));
		}
		
		// status
		if(cdaSubAdm.getStatusCode()!=null && !cdaSubAdm.getStatusCode().isSetNullFlavor()) {
			fhirImmunization.setStatus(cdaSubAdm.getStatusCode().getCode());
		}

		return fhirImmunizationBundle;
		
	}	
	
	public Bundle tVitalSignObservation2Observation(VitalSignObservation cdaVSO) {
		return tObservation2Observation(cdaVSO);
	}

	public AgeDt tAgeObservation2AgeDt(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation) {
		if(cdaAgeObservation == null || cdaAgeObservation.isSetNullFlavor())
			return null;

		AgeDt fhirAge = new AgeDt();

		// age <-> value
		if(cdaAgeObservation != null && !cdaAgeObservation.getValues().isEmpty()) {
			for(ANY value : cdaAgeObservation.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof PQ) {
						if(((PQ)value).getValue() != null) {
							fhirAge.setValue(((PQ)value).getValue());
							fhirAge.setUnit(((PQ)value).getUnit());
							fhirAge.setSystem("http://unitsofmeasure.org");
						}
					}
				}
			}
		}

		return fhirAge;
	}
	
	public ca.uhn.fhir.model.dstu2.resource.Patient.Contact tGuardian2Contact(Guardian cdaGuardian) {
		if(cdaGuardian == null || cdaGuardian.isSetNullFlavor())
			return null;
	
		ca.uhn.fhir.model.dstu2.resource.Patient.Contact fhirContact = new ca.uhn.fhir.model.dstu2.resource.Patient.Contact();
		
		// addr
		if(cdaGuardian.getAddrs() != null && !cdaGuardian.getAddrs().isEmpty()) {
			fhirContact.setAddress(dtt.AD2Address(cdaGuardian.getAddrs().get(0)));
		} 
		
		// tel
		if(cdaGuardian.getTelecoms() != null && !cdaGuardian.getTelecoms().isEmpty()) {
			for(TEL tel : cdaGuardian.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirContact.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// relationship
		if(cdaGuardian.getCode() != null && !cdaGuardian.getCode().isSetNullFlavor()) {
			fhirContact.addRelationship(dtt.tCD2CodeableConcept(cdaGuardian.getCode()));
		}
		return fhirContact;
	}

	public Communication tLanguageCommunication2Communication( LanguageCommunication cdaLanguageCommunication ){
		if(cdaLanguageCommunication == null || cdaLanguageCommunication.isSetNullFlavor())
			return null;
		
		Communication fhirCommunication = new Communication();
		
		// language
		if(cdaLanguageCommunication.getLanguageCode() != null && !cdaLanguageCommunication.getLanguageCode().isSetNullFlavor()) {
			fhirCommunication.setLanguage(dtt.tCD2CodeableConcept(cdaLanguageCommunication.getLanguageCode()));
		}
		
		// preferred
		if(cdaLanguageCommunication.getPreferenceInd() != null && !cdaLanguageCommunication.getPreferenceInd().isSetNullFlavor()) {
			fhirCommunication.setPreferred(dtt.tBL2Boolean(cdaLanguageCommunication.getPreferenceInd()));
		}
		return fhirCommunication;
		
	}
	
	public Observation.ReferenceRange tReferenceRange2ReferenceRange(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaRefRange) {
		if(cdaRefRange == null || cdaRefRange.isSetNullFlavor())
			return null;
	
		ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange fhirRefRange = new ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange();
		
		// Notice that we get all the desired information from cdaRefRange.ObservationRange
		// We may think of transforming ObservationRange instead of ReferenceRange
		if(cdaRefRange.getObservationRange() != null && !cdaRefRange.isSetNullFlavor()) {
		
			// low - high
			if(cdaRefRange.getObservationRange().getValue() != null && !cdaRefRange.getObservationRange().getValue().isSetNullFlavor()) {
				if(cdaRefRange.getObservationRange().getValue() instanceof IVL_PQ) {
					IVL_PQ cdaRefRangeValue = ( (IVL_PQ) cdaRefRange.getObservationRange().getValue());
					// low
					if(cdaRefRangeValue.getLow() != null && !cdaRefRangeValue.getLow().isSetNullFlavor()) {
						fhirRefRange.setLow( dtt.tPQ2SimpleQuantityDt(cdaRefRangeValue.getLow()));
					}
					// high
					if(cdaRefRangeValue.getHigh() != null && !cdaRefRangeValue.getHigh().isSetNullFlavor()) {
						fhirRefRange.setHigh(dtt.tPQ2SimpleQuantityDt(cdaRefRangeValue.getHigh()));
					}
				}
			}
			
			// meaning
			if(cdaRefRange.getObservationRange().getInterpretationCode() != null && !cdaRefRange.getObservationRange().getInterpretationCode().isSetNullFlavor()) {
				fhirRefRange.setMeaning(dtt.tCD2CodeableConcept(cdaRefRange.getObservationRange().getInterpretationCode()));
			}
			
			// text
			if(cdaRefRange.getObservationRange().getText() != null && !cdaRefRange.getObservationRange().getText().isSetNullFlavor()) {
				if(cdaRefRange.getObservationRange().getText().getText() != null && !cdaRefRange.getObservationRange().getText().getText().isEmpty()) {
					fhirRefRange.setText(cdaRefRange.getObservationRange().getText().getText());
				}
			}
		}
		return fhirRefRange;
	}
	
	public Composition.Section tSection2Section(Section cdaSec) {
		if(cdaSec == null || cdaSec.isSetNullFlavor()){
			return null;
		} else {
			Composition.Section fhirSec = new Composition.Section();
			
			// title -> title.text
			if(cdaSec.getTitle() != null && !cdaSec.getTitle().isSetNullFlavor()) {
				if(cdaSec.getTitle().getText() != null && !cdaSec.getTitle().getText().isEmpty()) {
					fhirSec.setTitle(cdaSec.getTitle().getText());
				}
			}
			
			// code -> code
			if(cdaSec.getCode() != null && !cdaSec.getCode().isSetNullFlavor()) {
				fhirSec.setCode(dtt.tCD2CodeableConcept(cdaSec.getCode()));
			}
			
			// text -> text
			if(cdaSec.getText() != null) {
				fhirSec.setText(dtt.tStrucDocText2Narrative(cdaSec.getText()));
			}
			
			return fhirSec;
		}
	}

	public Bundle tClinicalDocument2Composition(ClinicalDocument cda) {
		if(cda == null || cda.isSetNullFlavor())
			return null;

		// create and init the global bundle and the composition resources
		Bundle fhirCompBundle = new Bundle();
		Composition fhirComp = new Composition();
		fhirComp.setId(new IdDt("Composition", getUniqueId()));
		fhirCompBundle.addEntry(new Bundle.Entry().setResource(fhirComp));
		
		// id -> identifier
		if(cda.getId() != null && !cda.getId().isSetNullFlavor()) {
			fhirComp.setIdentifier(dtt.tII2Identifier(cda.getId()));
		}
		
		// effectiveTime -> date
		if(cda.getEffectiveTime() != null && !cda.getEffectiveTime().isSetNullFlavor()) {
			fhirComp.setDate(dtt.tTS2DateTime(cda.getEffectiveTime()));
		}
		
		// code -> type
		if(cda.getCode() != null && !cda.getCode().isSetNullFlavor()) {
			fhirComp.setType(dtt.tCD2CodeableConcept(cda.getCode()));
		}

		// title.text -> title
		if(cda.getTitle() != null && !cda.getTitle().isSetNullFlavor()) {
			if(cda.getTitle().getText() != null && !cda.getTitle().getText().isEmpty()) {
				fhirComp.setTitle(cda.getTitle().getText());
			}
		}
		
		// confidentialityCode -> confidentiality
		if(cda.getConfidentialityCode() != null && !cda.getConfidentialityCode().isSetNullFlavor()) {
			if(cda.getConfidentialityCode().getCode() != null && !cda.getConfidentialityCode().getCode().isEmpty()) {
				fhirComp.setConfidentiality(cda.getConfidentialityCode().getCode());
			}
		}

		// transform the patient data and assign it to Composition.subject.
		// patient might refer to additional resources such as organization; hence the method returns a bundle.
		Bundle subjectBundle = tPatientRole2Patient(cda.getRecordTargets().get(0).getPatientRole());
		for(Bundle.Entry entry : subjectBundle.getEntry()){
			fhirCompBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
			if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Patient){
				fhirComp.setSubject(new ResourceReferenceDt(entry.getResource().getId()));
			}
		}
		
		// author.assignedAuthor -> author
		if(cda.getAuthors() != null && !cda.getAuthors().isEmpty()) {
			for(Author author : cda.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					if(author.getAssignedAuthor() != null && !author.getAssignedAuthor().isSetNullFlavor()) {
						Bundle practBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());
						for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
							// Add all the resources returned from the bundle to the main bundle
							fhirCompBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
							if(entry.getResource() instanceof Practitioner) {
								fhirComp.addAuthor().setReference((entry.getResource()).getId());
							}
						}	
					}
				}
			}
		}
		
		// legalAuthenticator -> attester
		if(cda.getLegalAuthenticator() != null && !cda.getLegalAuthenticator().isSetNullFlavor()) {
			// Bundle Authenticator2Attester(org.openhealthtools.mdht.uml.cda.Authenticator authen);
			// Attester: ca.uhn.fhir.model.dstu2.resource.Composition.Attester
			// Since we need to return Bundle, we can't implement Authenticator2Attester
		}
		
		// custodian -> custodian.assignedCustodian.representedCustodianOrganization
		if(cda.getCustodian() != null && !cda.getCustodian().isSetNullFlavor()) {
			if(cda.getCustodian().getAssignedCustodian() != null && !cda.getCustodian().getAssignedCustodian().isSetNullFlavor()) {
				if(cda.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization() != null && !cda.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization().isSetNullFlavor()) {
					Organization fhirOrganization = tCustodianOrganization2Organization(cda.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization());
					fhirComp.setCustodian(new ResourceReferenceDt(fhirOrganization.getId()));
					fhirCompBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
				}
			}
		}

		return fhirCompBundle;
	}
	
	public Organization tCustodianOrganization2Organization(org.openhealthtools.mdht.uml.cda.CustodianOrganization cdaOrganization) {
		if(cdaOrganization == null || cdaOrganization.isSetNullFlavor())
			return null;
		
		Organization fhirOrganization = new Organization();
		
		// id
		IdDt resourceId = new IdDt("Organization", getUniqueId());
		fhirOrganization.setId(resourceId);
		
		// identifier
		if(cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for(II ii : cdaOrganization.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name
		if(cdaOrganization.getName() != null && !cdaOrganization.getName().isSetNullFlavor()) {
			fhirOrganization.setName(cdaOrganization.getName().getText());
		}
		
		// telecom -> telecom
		if(cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty()) {
			for(TEL tel : cdaOrganization.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirOrganization.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// address
		if(cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for(AD ad : cdaOrganization.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		return fhirOrganization;
	}

	/*
	 * Functional Status Section contains:
	 *   1- Functional Status Observation
	 *   2- Self-Care Activites
	 * Both of them have single Observation which needs mapping.
	 * Therefore, the parameter for the following method(tFunctionalStatus2Observation) chosen to be generic(Observation)
	 * .. to cover the content of the section.
	 */
	public Bundle tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObs) {
		if(cdaObs == null || cdaObs.isSetNullFlavor()) 
			return null;
		
		Observation fhirObs = new Observation();
		
		Bundle fhirObsBundle = new Bundle();
		fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirObs));
		
		// id
		IdDt resourceId = new IdDt("FunctionalStatusObservation", getUniqueId());
		fhirObs.setId(resourceId);
		
		// subject
		fhirObs.setSubject(getPatientRef());
		
		// identifier
		if(cdaObs.getIds() != null && !cdaObs.getIds().isEmpty()) {
			for(II ii : cdaObs.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// category -> code
		if(cdaObs.getCode() != null && !cdaObs.isSetNullFlavor()) {
			fhirObs.setCategory(dtt.tCD2CodeableConcept(cdaObs.getCode()));
		}
		
		// code -> value(CD)
		if(cdaObs.getValues() != null && !cdaObs.getValues().isEmpty()) {
			for(ANY value : cdaObs.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof CD) {
						fhirObs.setCode(dtt.tCD2CodeableConcept((CD)value));
					}
				}
			}
		}
		
		// performer -> author
		if(cdaObs.getAuthors() != null && !cdaObs.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaObs.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());
						
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						fhirObsBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						
						if(entry.getResource() instanceof Practitioner) {
								fhirPractitioner = (Practitioner)entry.getResource();
						}
					}
					fhirObs.addPerformer().setReference(fhirPractitioner.getId());
				}
			}
		}
	
		// effective -> effectiveTime
		if(cdaObs.getEffectiveTime() != null && !cdaObs.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObs.getEffectiveTime()));
		}
		
		// device -> non-medicinal supply activity
		if(cdaObs.getEntryRelationships() != null && !cdaObs.getEntryRelationships().isEmpty()) {
			for(EntryRelationship entryRelShip : cdaObs.getEntryRelationships()) {
				if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
					// supply
					org.openhealthtools.mdht.uml.cda.Supply cdaSupply = entryRelShip.getSupply();
					if(cdaSupply != null && !cdaSupply.isSetNullFlavor()) {
						Device fhirDev = tSupply2Device(cdaSupply);
						fhirObs.setDevice(new ResourceReferenceDt(fhirDev.getId()));
						fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirDev));						
					}
				}
			}
		}

		return fhirObsBundle;
	}
	
	public ca.uhn.fhir.model.dstu2.resource.Device tSupply2Device(org.openhealthtools.mdht.uml.cda.Supply cdaSupply) {
		if(cdaSupply == null || cdaSupply.isSetNullFlavor())
			return null;
		
		Device fhirDev = new Device();
		
		// id
		IdDt resourceId = new IdDt("Device", getUniqueId());
		fhirDev.setId(resourceId);
		
		// identifier
		if(cdaSupply.getIds() != null && !cdaSupply.getIds().isEmpty()) {
			for(II ii : cdaSupply.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirDev.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		ProductInstance productInstance = null;
		// getting productInstance from cdaSupply.participant.participantRole
		if(cdaSupply.getParticipants() != null && !cdaSupply.getParticipants().isEmpty()) {
			for(Participant2 participant : cdaSupply.getParticipants()) {
				if(participant != null && !participant.isSetNullFlavor()) {
					if(participant.getParticipantRole() != null && !participant.getParticipantRole().isSetNullFlavor()) {
						if(participant.getParticipantRole() instanceof ProductInstance) {
							productInstance = (ProductInstance)participant.getParticipantRole();
						}
					}
				}
			}
		}
		
		// type -> productInstance.playingDevice.code
		if(productInstance != null) {
			if(productInstance.getPlayingDevice() != null && !productInstance.getPlayingDevice().isSetNullFlavor()) {
				if(productInstance.getPlayingDevice().getCode() != null && !productInstance.getPlayingDevice().getCode().isSetNullFlavor()) {
					fhirDev.setType(dtt.tCD2CodeableConcept(productInstance.getPlayingDevice().getCode()));
				}
			}
		}
		
		// TODO: Check if OK manufacturedDate <-> effectiveTime
		// manufacturedDate -> effectiveTime(high)
		if(cdaSupply.getEffectiveTimes() != null && !cdaSupply.getEffectiveTimes().isEmpty()) {
			for(SXCM_TS sxcmts : cdaSupply.getEffectiveTimes()) {
				if(sxcmts != null && !sxcmts.isSetNullFlavor()) {
					if(sxcmts instanceof IVL_TS) {
						IVXB_TS highTime = ((IVL_TS)sxcmts).getHigh();
						if(highTime != null && !highTime.isSetNullFlavor()) {
							if(highTime.getValue() != null && !highTime.getValue().isEmpty()) {
								fhirDev.setManufactureDate(dtt.tString2DateTime(highTime.getValue()));
							}
						}
					}
				}
			}
		}
		
		// quantity?
		return fhirDev;
	}

	public Bundle tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer) {
		if(cdaResultOrganizer == null || cdaResultOrganizer.isSetNullFlavor())
			return null;
		
		DiagnosticReport fhirDiagReport = new DiagnosticReport();
		
		// bundle
		Bundle fhirDiagReportBundle = new Bundle();
		fhirDiagReportBundle.addEntry(new Bundle.Entry().setResource(fhirDiagReport));
		
		// id
		IdDt resourceId = new IdDt("DiagnosticReport", getUniqueId());
		fhirDiagReport.setId(resourceId);
		
		// subject
		fhirDiagReport.setSubject(getPatientRef());
		
		// Although DiagnosticReport.request(DiagnosticOrder) is needed by daf, no information exists in CDA side to fill that field.
		
		// id -> identifier 
		if(cdaResultOrganizer.getIds() != null && !cdaResultOrganizer.getIds().isEmpty()) {
			for(II ii : cdaResultOrganizer.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirDiagReport.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// code -> code
		if(cdaResultOrganizer.getCode() != null && !cdaResultOrganizer.getCode().isSetNullFlavor()) {
			fhirDiagReport.setCode(dtt.tCD2CodeableConcept(cdaResultOrganizer.getCode()));
		}
		
		// effectiveTime -> effective
		if(cdaResultOrganizer.getEffectiveTime() != null && !cdaResultOrganizer.getEffectiveTime().isSetNullFlavor()) {
			fhirDiagReport.setEffective(dtt.tIVL_TS2Period(cdaResultOrganizer.getEffectiveTime()));
		}
		
		// author.time -> issued
		if(cdaResultOrganizer.getAuthors() != null && !cdaResultOrganizer.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaResultOrganizer.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					if(author.getTime() != null && !author.getTime().isSetNullFlavor()) {
						fhirDiagReport.setIssued(dtt.tTS2Instant(author.getTime()));
					}
				}
			}
		}
		
		// author -> performer
		if(cdaResultOrganizer.getAuthors() != null && !cdaResultOrganizer.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaResultOrganizer.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAssignedAuthor2Practitioner(author.getAssignedAuthor());

					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						fhirDiagReportBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof Practitioner) {
							fhirPractitioner = (Practitioner)entry.getResource();
						}
					}
					if(fhirPractitioner != null) {
						fhirDiagReport.setPerformer(new ResourceReferenceDt(fhirPractitioner.getId()));
					}
				}
			}
		}
		
		// observation(s) -> result
		if(cdaResultOrganizer.getObservations() != null && !cdaResultOrganizer.getObservations().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Observation cdaObs : cdaResultOrganizer.getObservations()) {
				if(cdaObs != null && !cdaObs.isSetNullFlavor()) {
					Observation fhirObs = null;
					Bundle fhirObsBundle = tObservation2Observation(cdaObs);
					for(Bundle.Entry entry : fhirObsBundle.getEntry()) {
						fhirDiagReportBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof Observation) {
							fhirObs = (Observation)entry.getResource();
						}
					}
					if(fhirObs != null) {
						fhirDiagReport.addResult().setReference(fhirObs.getId());
					}
				}
			}
		}
		
 		return fhirDiagReportBundle;
	}
}