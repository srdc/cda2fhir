package tr.com.srdc.cda2fhir.impl;

import java.util.UUID;

import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Immunization.Explanation;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import ca.uhn.fhir.model.dstu2.valueset.*;

import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.*;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.Patient.Communication;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.IdDt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.cda2fhir.CDATransformer;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;
import tr.com.srdc.cda2fhir.util.Constants;

public class ResourceTransformerImpl implements tr.com.srdc.cda2fhir.ResourceTransformer{

	private DataTypesTransformer dtt;
	private ValueSetsTransformer vst;
	private CDATransformer cdat;
	private ResourceReferenceDt defaultPatientRef;

	private final Logger logger = LoggerFactory.getLogger(ResourceTransformerImpl.class);

	public ResourceTransformerImpl() {
		dtt = new DataTypesTransformerImpl();
		vst = new ValueSetsTransformerImpl();
		cdat = null;
		// This is a default patient reference to be used when ResourceTransformer is not initiated with a CDATransformer
		defaultPatientRef = new ResourceReferenceDt(new IdDt("Patient", "0"));
	}

	public ResourceTransformerImpl(CDATransformer cdaTransformer) {
		this();
		cdat = cdaTransformer;
	}

	protected String getUniqueId() {
		if(cdat != null)
			return cdat.getUniqueId();
		else
			return UUID.randomUUID().toString();
	}

	protected ResourceReferenceDt getPatientRef() {
		if(cdat != null)
			return cdat.getPatientRef();
		else
			return defaultPatientRef;
	}

	public Bundle tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct) {
		if(cdaAllergyProbAct==null || cdaAllergyProbAct.isSetNullFlavor()) 
			return null;

		AllergyIntolerance fhirAllergyIntolerance = new AllergyIntolerance();

		Bundle allergyIntoleranceBundle = new Bundle();
		allergyIntoleranceBundle.addEntry(new Bundle.Entry().setResource(fhirAllergyIntolerance));
		
		// resource id
		IdDt resourceId = new IdDt("AllergyIntolerance", getUniqueId());
		fhirAllergyIntolerance.setId(resourceId);
		
		// id -> identifier
		for(II ii : cdaAllergyProbAct.getIds()) {
			if(!ii.isSetNullFlavor()) {
				fhirAllergyIntolerance.addIdentifier(dtt.tII2Identifier(ii));
			}
		}
		
		// patient
		fhirAllergyIntolerance.setPatient(getPatientRef());
	
		// author -> recorder
		if(cdaAllergyProbAct.getAuthors() != null && !cdaAllergyProbAct.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaAllergyProbAct.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAuthor2Practitioner(author);
					
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
		
		// statusCode -> status
		if(cdaAllergyProbAct.getStatusCode() != null && !cdaAllergyProbAct.getStatusCode().isSetNullFlavor()) {
			if(cdaAllergyProbAct.getStatusCode().getCode() != null && !cdaAllergyProbAct.getStatusCode().getCode().isEmpty()) {
				AllergyIntoleranceStatusEnum allergyIntoleranceStatusEnum = vst.tStatusCode2AllergyIntoleranceStatusEnum(cdaAllergyProbAct.getStatusCode().getCode());
				if(allergyIntoleranceStatusEnum != null) {
					fhirAllergyIntolerance.setStatus(allergyIntoleranceStatusEnum);
				}
			}
		}
		
		// effectiveTime -> onset
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
					
					// allergyObservation.participant.participantRole.playingEntity.code -> substance
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
					
					// allergyObservation.value[@xsi:type='CD'] -> category
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
					if(cdaAllergyObs.getEntryRelationships() != null && !cdaAllergyObs.getEntryRelationships().isEmpty()) {
						for(EntryRelationship entryRelShip : cdaAllergyObs.getEntryRelationships()) {
							if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
								if(entryRelShip.getObservation() != null && !entryRelShip.isSetNullFlavor()) {

									// reaction observation
									if(entryRelShip.getObservation() instanceof ReactionObservation) {
										
										ReactionObservation cdaReactionObs = (ReactionObservation) entryRelShip.getObservation();
										Reaction fhirReaction = fhirAllergyIntolerance.addReaction();
										
										// reactionObservation/value[@xsi:type='CD'] -> reaction.manifestation
										if(cdaReactionObs.getValues() != null && !cdaReactionObs.getValues().isEmpty()) {
											for(ANY value : cdaReactionObs.getValues()) {
												if(value != null && !value.isSetNullFlavor()) {
													if(value instanceof CD) {
														fhirReaction.addManifestation(dtt.tCD2CodeableConcept((CD)value));
													}
												}
											}
										}

										// reactionObservation/low -> reaction.onset
										if(cdaReactionObs.getEffectiveTime() != null && !cdaReactionObs.getEffectiveTime().isSetNullFlavor()) {
											if(cdaReactionObs.getEffectiveTime().getLow() != null && !cdaReactionObs.getEffectiveTime().getLow().isSetNullFlavor()) {
												fhirReaction.setOnset(dtt.tString2DateTime(cdaReactionObs.getEffectiveTime().getLow().getValue()));
											}
										}

										// severityObservation/value[@xsi:type='CD'].code -> severity
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

									// criticality observation. found by checking the templateId
									// entryRelationship.observation[templateId/@root='2.16.840.1.113883.10.20.22.4.145'].value[CD].code -> criticality
									if(entryRelShip.getObservation().getTemplateIds() != null && !entryRelShip.getObservation().getTemplateIds().isEmpty()) {
										for(II templateId : entryRelShip.getObservation().getTemplateIds()) {
											if(templateId.getRoot() != null && templateId.getRoot().equals("2.16.840.1.113883.10.20.22.4.145")) {
												org.openhealthtools.mdht.uml.cda.Observation cdaCriticalityObservation = entryRelShip.getObservation();
												for(ANY value : cdaCriticalityObservation.getValues()) {
													if(value != null && !value.isSetNullFlavor()) {
														if(value instanceof CD) {
															AllergyIntoleranceCriticalityEnum allergyIntoleranceCriticalityEnum = vst.tCriticalityObservationValue2AllergyIntoleranceCriticalityEnum(((CD)value).getCode());
															if(allergyIntoleranceCriticalityEnum != null) {
																fhirAllergyIntolerance.setCriticality(allergyIntoleranceCriticalityEnum);
															}
														}
													}
												}
												// since we already found the desired templateId, we may break the searching for templateId to avoid containing duplicate observations
												break;
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
		
		// resource id
		IdDt resourceId = new IdDt("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);
		
		// id -> identifier
		if(cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty()) {
			for(II ii : cdaAssignedAuthor.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirPractitioner.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// assignedPerson.name -> name
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
		
		// addr -> address
		if(cdaAssignedAuthor.getAddrs() != null && !cdaAssignedAuthor.getAddrs().isEmpty()) {
			for(AD ad : cdaAssignedAuthor.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		// telecom -> telecom
		if(cdaAssignedAuthor.getTelecoms() != null && !cdaAssignedAuthor.getTelecoms().isEmpty()) {
			for(TEL tel : cdaAssignedAuthor.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// Adding a practitionerRole
		Practitioner.PractitionerRole fhirPractitionerRole = fhirPractitioner.addPractitionerRole();
		
		// code -> practitionerRole.role
		if(cdaAssignedAuthor.getCode() != null && !cdaAssignedAuthor.isSetNullFlavor()) {
			fhirPractitionerRole.setRole(dtt.tCD2CodeableConcept(cdaAssignedAuthor.getCode()));
		}
		
		// representedOrganization -> practitionerRole.managingOrganization
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

		// bundle
		Bundle fhirPractitionerBundle = new Bundle();
		fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirPractitioner));
			
		// resource id
		IdDt resourceId = new IdDt("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);
		
		// id -> identifier
		if(cdaAssignedEntity.getIds() != null && !cdaAssignedEntity.getIds().isEmpty()) {
			for(II id : cdaAssignedEntity.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirPractitioner.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}
		
		// assignedPerson.name -> name
		if(cdaAssignedEntity.getAssignedPerson() != null && !cdaAssignedEntity.getAssignedPerson().isSetNullFlavor()) {
			for(PN pn : cdaAssignedEntity.getAssignedPerson().getNames()) {
				if(pn != null && !pn.isSetNullFlavor()) {
					// asserting that at most one name exists
					fhirPractitioner.setName(dtt.tEN2HumanName(pn));
				}
			}
		}
		
		// addr -> address
		if(cdaAssignedEntity.getAddrs() != null && !cdaAssignedEntity.getAddrs().isEmpty()) {
			for(AD ad : cdaAssignedEntity.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		// telecom -> telecom
		if(cdaAssignedEntity.getTelecoms() != null && ! cdaAssignedEntity.getTelecoms().isEmpty()) {
			for(TEL tel : cdaAssignedEntity.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint( tel ));
				}
			}
		}

		Practitioner.PractitionerRole fhirPractitionerRole = fhirPractitioner.addPractitionerRole();

		// code -> practitionerRole.role
		if(cdaAssignedEntity.getCode() != null && !cdaAssignedEntity.isSetNullFlavor()) {
			fhirPractitionerRole.setRole(dtt.tCD2CodeableConcept(cdaAssignedEntity.getCode()));
		}
		
		// representedOrganization -> practitionerRole.organization
		// NOTE: we skipped multiple instances of representated organization; we just omit apart from the first
		if(!cdaAssignedEntity.getRepresentedOrganizations().isEmpty()) {
			if(cdaAssignedEntity.getRepresentedOrganizations().get(0) != null && !cdaAssignedEntity.getRepresentedOrganizations().get(0).isSetNullFlavor()) {
				ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(cdaAssignedEntity.getRepresentedOrganizations().get(0));
				fhirPractitionerRole.setManagingOrganization(new ResourceReferenceDt(fhirOrganization.getId()));
				fhirPractitionerBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
			}
		}

		return fhirPractitionerBundle;
	}

	public Bundle tAuthor2Practitioner(org.openhealthtools.mdht.uml.cda.Author cdaAuthor) {
		if(cdaAuthor == null || cdaAuthor.isSetNullFlavor()) {
			return null;
		} else if(cdaAuthor.getAssignedAuthor() == null || cdaAuthor.getAssignedAuthor().isSetNullFlavor()) {
			return null;
		} else {
			return tAssignedAuthor2Practitioner(cdaAuthor.getAssignedAuthor());
		}
	}
	
	public Substance tCD2Substance(CD cdaSubstanceCode) {
		if(cdaSubstanceCode == null || cdaSubstanceCode.isSetNullFlavor())
			return null;

		Substance fhirSubstance = new Substance();

		// resource id
		fhirSubstance.setId(new IdDt("Substance", getUniqueId()));

		// code -> code
		fhirSubstance.setCode(dtt.tCD2CodeableConcept(cdaSubstanceCode));

		return fhirSubstance;
	}

	public Bundle tEncounter2Encounter(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter) {
		if(cdaEncounter == null || cdaEncounter.isSetNullFlavor())
			return null;

		ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = new ca.uhn.fhir.model.dstu2.resource.Encounter();

		Bundle fhirEncounterBundle = new Bundle();
		fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirEncounter));
		
		// NOTE: hospitalization.period not found. However, daf requires it being mapped

		// resource id
		IdDt resourceId = new IdDt("Encounter", getUniqueId());
		fhirEncounter.setId(resourceId);

		// patient
		fhirEncounter.setPatient(getPatientRef());

		// id -> identifier
		if(cdaEncounter.getIds() != null && !cdaEncounter.getIds().isEmpty()) {
			for(II id : cdaEncounter.getIds()){
				if(id != null && !id.isSetNullFlavor()){
					fhirEncounter.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// statusCode -> status
		if(cdaEncounter.getStatusCode() != null && !cdaEncounter.getStatusCode().isSetNullFlavor()) {
			if(vst.tStatusCode2EncounterStatusEnum(cdaEncounter.getStatusCode().getCode()) != null) {
				fhirEncounter.setStatus(vst.tStatusCode2EncounterStatusEnum(cdaEncounter.getStatusCode().getCode()));
			}
		} else {
			fhirEncounter.setStatus(Constants.DEFAULT_ENCOUNTER_STATUS);
		}

		// code -> type
		if(cdaEncounter.getCode() != null && !cdaEncounter.getCode().isSetNullFlavor()) {
			fhirEncounter.addType(dtt.tCD2CodeableConcept(cdaEncounter.getCode()));
		}
		
		// code.translation -> classElement
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

		// priorityCode -> priority
		if(cdaEncounter.getPriorityCode() != null && !cdaEncounter.getPriorityCode().isSetNullFlavor()) {
			fhirEncounter.setPriority(dtt.tCD2CodeableConcept(cdaEncounter.getPriorityCode()));
		}

		// performer -> participant.individual
		if(cdaEncounter.getPerformers() != null && !cdaEncounter.getPerformers().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaEncounter.getPerformers()) {
				if(cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					ca.uhn.fhir.model.dstu2.resource.Encounter.Participant fhirParticipant = new ca.uhn.fhir.model.dstu2.resource.Encounter.Participant();

					// default encunter participant type code
					fhirParticipant.addType().addCoding(Constants.DEFAULT_ENCOUNTER_PARTICIPANT_TYPE_CODE);

					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tPerformer22Practitioner(cdaPerformer);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						if(entry.getResource() instanceof Practitioner) {
							fhirPractitioner = (Practitioner)entry.getResource();
							fhirEncounterBundle.addEntry(new Bundle().addEntry().setResource(entry.getResource()));
						}
					}

					fhirParticipant.setIndividual(new ResourceReferenceDt(fhirPractitioner.getId()));
					fhirEncounter.addParticipant(fhirParticipant);
				}
			}
		}

		// effectiveTime -> period
		if(cdaEncounter.getEffectiveTime() != null && !cdaEncounter.getEffectiveTime().isSetNullFlavor()) {
			fhirEncounter.setPeriod(dtt.tIVL_TS2Period(cdaEncounter.getEffectiveTime()));
		}

		// participant[@typeCode='LOC'].participantRole[SDLOC] -> location.location
		if(cdaEncounter.getParticipants() != null && !cdaEncounter.getParticipants().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Participant2 cdaParticipant : cdaEncounter.getParticipants()) {
				if(cdaParticipant != null && !cdaParticipant.isSetNullFlavor()) {
					// checking if the participant is location
					if(cdaParticipant.getTypeCode() == ParticipationType.LOC) {
						if(cdaParticipant.getParticipantRole() != null && !cdaParticipant.getParticipantRole().isSetNullFlavor()) {
							if(cdaParticipant.getParticipantRole().getClassCode() != null && cdaParticipant.getParticipantRole().getClassCode() == RoleClassRoot.SDLOC) {
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
		}
		
		// entryRelationship[@typeCode='RSON'].observation[Indication] -> indication
		if(cdaEncounter.getEntryRelationships() != null && !cdaEncounter.getEntryRelationships().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.EntryRelationship entryRelShip : cdaEncounter.getEntryRelationships()) {
				if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
					if(entryRelShip.getObservation() != null && !entryRelShip.isSetNullFlavor()) {
						if(entryRelShip.getObservation() instanceof Indication) {
							Indication cdaIndication = (Indication)entryRelShip.getObservation();
							Condition fhirIndication = tIndication2Condition(cdaIndication);
							fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirIndication));
							ResourceReferenceDt indicationRef = fhirEncounter.addIndication();
							indicationRef.setReference(fhirIndication.getId());
						}
					}
				}
			}
		}
		return fhirEncounterBundle;
	}

	public Bundle tEncounterActivity2Encounter(org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaEncounterActivity) {
		/*
		 * EncounterActivity2Encounter and Encounter2Encounter are nearly the same methods.
		 * Since EncounterActivity class has neater methods, we may think of using EncounterActivity2Encounter instead of Encounter2Encounter in later times
		 * Also, notice that some of those methods are not working properly, yet.
		 * Therefore, those of methods that are not working properly but seems to be neater hasn't used in this implementation.
		 */
		if(cdaEncounterActivity == null || cdaEncounterActivity.isSetNullFlavor())
			return null;

		ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = new ca.uhn.fhir.model.dstu2.resource.Encounter();

		Bundle fhirEncounterBundle = new Bundle();
		fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirEncounter));

		// NOTE: hospitalization.period not found. However, daf requires it being mapped

		// resource id
		IdDt resourceId = new IdDt("Encounter",getUniqueId());
		fhirEncounter.setId(resourceId);

		// patient
		fhirEncounter.setPatient(getPatientRef());

		// id -> identifier
		if(cdaEncounterActivity.getIds() != null && !cdaEncounterActivity.getIds().isEmpty()) {
			for(II id : cdaEncounterActivity.getIds()){
				if(id != null && !id.isSetNullFlavor()){
					fhirEncounter.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// statusCode -> status
		if(cdaEncounterActivity.getStatusCode() != null && !cdaEncounterActivity.getStatusCode().isSetNullFlavor()) {
			if(vst.tStatusCode2EncounterStatusEnum(cdaEncounterActivity.getStatusCode().getCode()) != null) {
				fhirEncounter.setStatus(vst.tStatusCode2EncounterStatusEnum(cdaEncounterActivity.getStatusCode().getCode()));
			}
		} else {
			fhirEncounter.setStatus(Constants.DEFAULT_ENCOUNTER_STATUS);
		}

		// code -> type
		if(cdaEncounterActivity.getCode() != null && !cdaEncounterActivity.getCode().isSetNullFlavor()) {
			fhirEncounter.addType(dtt.tCD2CodeableConcept(cdaEncounterActivity.getCode()));
		}

		// code.translation -> classElement
		if(cdaEncounterActivity.getCode() != null && !cdaEncounterActivity.getCode().isSetNullFlavor()) {
			if(cdaEncounterActivity.getCode().getTranslations() != null && !cdaEncounterActivity.getCode().getTranslations().isEmpty()) {
				for(CD cd : cdaEncounterActivity.getCode().getTranslations()) {
					if(cd != null && !cd.isSetNullFlavor()) {
						EncounterClassEnum encounterClass = vst.tEncounterCode2EncounterClassEnum(cd.getCode());
						if(encounterClass != null){
							fhirEncounter.setClassElement(encounterClass);
						}
					}
				}
			}
		}

		// priorityCode -> priority
		if(cdaEncounterActivity.getPriorityCode() != null && !cdaEncounterActivity.getPriorityCode().isSetNullFlavor()) {
			fhirEncounter.setPriority(dtt.tCD2CodeableConcept(cdaEncounterActivity.getPriorityCode()));
		}

		// performer -> participant.individual
		if(cdaEncounterActivity.getPerformers() != null && !cdaEncounterActivity.getPerformers().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaEncounterActivity.getPerformers()) {
				if(cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					ca.uhn.fhir.model.dstu2.resource.Encounter.Participant fhirParticipant = new ca.uhn.fhir.model.dstu2.resource.Encounter.Participant();

					// default encounter participant type code
					fhirParticipant.addType().addCoding(Constants.DEFAULT_ENCOUNTER_PARTICIPANT_TYPE_CODE);

					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tPerformer22Practitioner(cdaPerformer);
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
						if(entry.getResource() instanceof Practitioner) {
							fhirPractitioner = (Practitioner)entry.getResource();
							fhirEncounterBundle.addEntry(new Bundle().addEntry().setResource(entry.getResource()));
						}
					}
					fhirParticipant.setIndividual(new ResourceReferenceDt(fhirPractitioner.getId()));
					fhirEncounter.addParticipant(fhirParticipant);
				}
			}
		}

		// effectiveTime -> period
		if(cdaEncounterActivity.getEffectiveTime() != null && !cdaEncounterActivity.getEffectiveTime().isSetNullFlavor()) {
			fhirEncounter.setPeriod(dtt.tIVL_TS2Period(cdaEncounterActivity.getEffectiveTime()));
		}

		// indication -> indication
		for(Indication cdaIndication : cdaEncounterActivity.getIndications()) {
			if(!cdaIndication.isSetNullFlavor()) {
				Condition fhirIndication = tIndication2Condition(cdaIndication);
				fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirIndication));
				ResourceReferenceDt indicationRef = fhirEncounter.addIndication();
				indicationRef.setReference(fhirIndication.getId());
			}
		}

		// serviceDeliveryLocation -> location.location
		// Although encounter contains serviceDeliveryLocation, getServiceDeliveryLocation method returns empty list
		// Therefore, get the location information from participant[@typeCode='LOC'].participantRole
//		if(cdaEncounterActivity.getServiceDeliveryLocations() != null && !cdaEncounterActivity.getServiceDeliveryLocations().isEmpty()) {
//			for(ServiceDeliveryLocation SDLOC : cdaEncounterActivity.getServiceDeliveryLocations()) {
//				if(SDLOC != null && !SDLOC.isSetNullFlavor()) {
//					ca.uhn.fhir.model.dstu2.resource.Location fhirLocation = tServiceDeliveryLocation2Location(SDLOC);
//					fhirEncounterBundle.addEntry(new Bundle.Entry().setResource(fhirLocation));
//					fhirEncounter.addLocation().setLocation(new ResourceReferenceDt(fhirLocation.getId()));
//				}
//			}
//		}

		// participant[@typeCode='LOC'].participantRole[SDLOC] -> location
		if(cdaEncounterActivity.getParticipants() != null && !cdaEncounterActivity.getParticipants().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Participant2 cdaParticipant : cdaEncounterActivity.getParticipants()) {
				if(cdaParticipant != null && !cdaParticipant.isSetNullFlavor()) {

					// checking if the participant is location
					if(cdaParticipant.getTypeCode() == ParticipationType.LOC) {
						if(cdaParticipant.getParticipantRole() != null && !cdaParticipant.getParticipantRole().isSetNullFlavor()) {
							if(cdaParticipant.getParticipantRole().getClassCode() != null && cdaParticipant.getParticipantRole().getClassCode() == RoleClassRoot.SDLOC) {
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
		}

		return fhirEncounterBundle;
	}

	public Group tEntity2Group(Entity cdaEntity) {
		// never used
		if( cdaEntity == null || cdaEntity.isSetNullFlavor() )
			return null;
		else if(cdaEntity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND)
			return null;
		
		Group fhirGroup = new Group();
		
		// id -> identifier
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
		
		// classCode -> type
		if(cdaEntity.getClassCode() != null) {
			GroupTypeEnum groupTypeEnum = vst.tEntityClassRoot2GroupTypeEnum(cdaEntity.getClassCode());
			if(groupTypeEnum != null) {
				fhirGroup.setType(groupTypeEnum);
			}
			
		}
		
		// deteminerCode -> actual
		if(cdaEntity.isSetDeterminerCode() && cdaEntity.getDeterminerCode() != null) {
			if(cdaEntity.getDeterminerCode() == EntityDeterminer.KIND) {
				fhirGroup.setActual(false);
			} else{
				fhirGroup.setActual(true);
			}
		}
		
		// code -> code
		if(cdaEntity.getCode() != null && !cdaEntity.getCode().isSetNullFlavor()) {
			fhirGroup.setCode(dtt.tCD2CodeableConcept(cdaEntity.getCode()));
		}
		
		return fhirGroup;
	}

	public FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO){
		if(cdaFHO == null || cdaFHO.isSetNullFlavor())
			return null;

		FamilyMemberHistory fhirFMH = new FamilyMemberHistory();
		
		// resource id
		IdDt resourceId = new IdDt("FamilyMemberHistory", getUniqueId());
		fhirFMH.setId(resourceId);
		
		// patient
		fhirFMH.setPatient(getPatientRef());
		
		// id -> identifier
		for(II id : cdaFHO.getIds()) {
			if(id != null && !id.isSetNullFlavor()) {
				fhirFMH.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// statusCode -> status
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
					
					// familyHistoryObservation.value[@xsi:type='CD'] -> code
					for(ANY value : familyHistoryObs.getValues()) {
						if(value != null && !value.isSetNullFlavor()) {
							if(value instanceof CD){
								condition.setCode(dtt.tCD2CodeableConcept((CD)value));
							}
						}
					}

					// NOTE: An alternative is to use the relatedSubject/subject/sdtc:deceasedInd and relatedSubject/subject/sdtc:deceasedTime values
					// deceased
					if(familyHistoryObs.getFamilyHistoryDeathObservation() != null && !familyHistoryObs.getFamilyHistoryDeathObservation().isSetNullFlavor()) {
						// if deathObservation exists, set fmh.deceased true
						fhirFMH.setDeceased(new BooleanDt(true));
						
						// familyHistoryDeathObservation.value[@xsi:type='CD'] -> condition.outcome
						for(ANY value : familyHistoryObs.getFamilyHistoryDeathObservation().getValues()) {
							if(value != null && !value.isSetNullFlavor()) {
								if(value instanceof CD) {
									condition.setOutcome(dtt.tCD2CodeableConcept((CD)value));
								}
							}
						}
					}
					
					// familyHistoryObservation.ageObservation -> condition.onset 
					if(familyHistoryObs.getAgeObservation() != null && !familyHistoryObs.getAgeObservation().isSetNullFlavor()) {
						AgeDt onset = tAgeObservation2AgeDt(familyHistoryObs.getAgeObservation());
						if(onset != null) {
							condition.setOnset(onset);
						}
					}
				}
			}
		}
			
		// info from subject.relatedSubject
		if(cdaFHO.getSubject() != null && !cdaFHO.isSetNullFlavor() && cdaFHO.getSubject().getRelatedSubject() != null && !cdaFHO.getSubject().getRelatedSubject().isSetNullFlavor()) {
			org.openhealthtools.mdht.uml.cda.RelatedSubject cdaRelatedSubject = cdaFHO.getSubject().getRelatedSubject();
			
			// subject.relatedSubject.code -> relationship
			if(cdaRelatedSubject.getCode() != null && !cdaRelatedSubject.getCode().isSetNullFlavor()) {
				fhirFMH.setRelationship(dtt.tCD2CodeableConcept(cdaRelatedSubject.getCode()));
			}
			
			// info from subject.relatedSubject.subject
			if(cdaRelatedSubject.getSubject() != null && !cdaRelatedSubject.getSubject().isSetNullFlavor()) {
				org.openhealthtools.mdht.uml.cda.SubjectPerson subjectPerson = cdaRelatedSubject.getSubject();
				
				// subject.relatedSubject.subject.name.text -> name
				for(EN en : subjectPerson.getNames()) {
					if(en != null && !en.isSetNullFlavor()) {
						if(en.getText() != null) {
							fhirFMH.setName(en.getText());
						}
					}
				}
				
				// subject.relatedSubject.subject.administrativeGenderCode -> gender
				if(subjectPerson.getAdministrativeGenderCode() != null && !subjectPerson.getAdministrativeGenderCode().isSetNullFlavor() &&
						subjectPerson.getAdministrativeGenderCode().getCode() != null) {
					fhirFMH.setGender(vst.tAdministrativeGenderCode2AdministrativeGenderEnum(subjectPerson.getAdministrativeGenderCode().getCode()));
				}

				// subject.relatedSubject.subject.birthTime -> born
				if(subjectPerson.getBirthTime() != null && !subjectPerson.getBirthTime().isSetNullFlavor()) {
					fhirFMH.setBorn(dtt.tTS2Date(subjectPerson.getBirthTime()));
				}
			}
		}
		return fhirFMH;
	}

	/*
	 * Functional Status Section contains:
	 *   1- Functional Status Observation
	 *   2- Self-Care Activites
	 * Both of them have single Observation which needs mapping.
	 * Therefore, the parameter for the following method(tFunctionalStatus2Observation) chosen to be generic(Observation)
	 * .. to cover the content of the section.
	 * Also, notice that the transformation of those Observations are different from the generic Observation transformation
	 */
	public Bundle tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation) {
		if(cdaObservation == null || cdaObservation.isSetNullFlavor()) 
			return null;
		
		Observation fhirObs = new Observation();
		
		Bundle fhirObsBundle = new Bundle();
		fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirObs));
		
		// resource id
		IdDt resourceId = new IdDt("Observation", getUniqueId());
		fhirObs.setId(resourceId);
		
		// subject
		fhirObs.setSubject(getPatientRef());
		
		// statusCode -> status
		if(cdaObservation.getStatusCode() != null && !cdaObservation.getStatusCode().isSetNullFlavor()) {
			if(cdaObservation.getStatusCode().getCode() != null && !cdaObservation.getStatusCode().getCode().isEmpty()) {
				fhirObs.setStatus(vst.tObservationStatusCode2ObservationStatusEnum(cdaObservation.getStatusCode().getCode()));
			}
		}
		
		// id -> identifier
		if(cdaObservation.getIds() != null && !cdaObservation.getIds().isEmpty()) {
			for(II ii : cdaObservation.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// code -> category
		if(cdaObservation.getCode() != null && !cdaObservation.isSetNullFlavor()) {
			fhirObs.setCategory(dtt.tCD2CodeableConcept(cdaObservation.getCode()));
		}
		
		// value[@xsi:type='CD'] -> code
		if(cdaObservation.getValues() != null && !cdaObservation.getValues().isEmpty()) {
			for(ANY value : cdaObservation.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof CD) {
						fhirObs.setCode(dtt.tCD2CodeableConcept((CD)value));
					}
				}
			}
		}
		
		// author -> performer
		if(cdaObservation.getAuthors() != null && !cdaObservation.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAuthor2Practitioner(author);
						
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
	
		// effectiveTime -> effective
		if(cdaObservation.getEffectiveTime() != null && !cdaObservation.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObservation.getEffectiveTime()));
		}
		
		// non-medicinal supply activity -> device
		if(cdaObservation.getEntryRelationships() != null && !cdaObservation.getEntryRelationships().isEmpty()) {
			for(EntryRelationship entryRelShip : cdaObservation.getEntryRelationships()) {
				if(entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
					// supply
					org.openhealthtools.mdht.uml.cda.Supply cdaSupply = entryRelShip.getSupply();
					if(cdaSupply != null && !cdaSupply.isSetNullFlavor()) {
						if(cdaSupply instanceof NonMedicinalSupplyActivity) {
							// Non-Medicinal Supply Activity
							Device fhirDev = tSupply2Device(cdaSupply);
							fhirObs.setDevice(new ResourceReferenceDt(fhirDev.getId()));
							fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirDev));
						}
					}
				}
			}
		}

		return fhirObsBundle;
	}
	
	public Condition tIndication2Condition(Indication cdaIndication) {
		if(cdaIndication == null || cdaIndication.isSetNullFlavor())
			return null;

		Condition fhirCond = new Condition();

		// resource id
		IdDt resourceId = new IdDt("Condition", getUniqueId());
		fhirCond.setId(resourceId);

		// patient
		fhirCond.setPatient(getPatientRef());
		
		// id -> identifier
		if(cdaIndication.getIds() != null && !cdaIndication.getIds().isEmpty()) {
			for(II ii : cdaIndication.getIds()) {
				fhirCond.addIdentifier(dtt.tII2Identifier(ii));
			}
		}
		
		// code -> category
		if(cdaIndication.getCode() != null && !cdaIndication.getCode().isSetNullFlavor()) {
			if(cdaIndication.getCode().getCode() != null) {
				ConditionCategoryCodesEnum conditionCategory = vst.tProblemType2ConditionCategoryCodesEnum(cdaIndication.getCode().getCode());
				if(conditionCategory != null) {
					fhirCond.setCategory(conditionCategory);
				}
			}
		}

		// effectiveTime -> onset & abatement
		if(cdaIndication.getEffectiveTime() != null && !cdaIndication.getEffectiveTime().isSetNullFlavor()) {

			IVXB_TS low = cdaIndication.getEffectiveTime().getLow();
			IVXB_TS high = cdaIndication.getEffectiveTime().getHigh();
			String value = cdaIndication.getEffectiveTime().getValue();

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

		// value[CD] -> code
		if(cdaIndication.getValues() != null && !cdaIndication.getValues().isEmpty()) {
			// There is only 1 value, but anyway...
			for(ANY value : cdaIndication.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof CD)
						fhirCond.setCode(dtt.tCD2CodeableConcept((CD)value));
				}
			}
		}

		// NOTE: A default value is assigned to verificationStatus attribute, as it is mandatory but cannot be mapped from the CDA side
		fhirCond.setVerificationStatus(Constants.DEFAULT_CONDITION_VERIFICATION_STATUS);

		return fhirCond;
	}
	
	public Bundle tManufacturedProduct2Medication(ManufacturedProduct cdaManufacturedProduct) {
		if(cdaManufacturedProduct == null || cdaManufacturedProduct.isSetNullFlavor())
			return null;
		
		Medication fhirMedication = new Medication();
		
		Bundle fhirMedicationBundle = new Bundle();
		fhirMedicationBundle.addEntry(new Bundle.Entry().setResource(fhirMedication));
		
		// resource id
		IdDt resourceId = new IdDt("Medication", getUniqueId());
		fhirMedication.setId(resourceId);

		// init Medication.product
		Medication.Product fhirProduct = new Medication.Product();
		fhirMedication.setProduct(fhirProduct);

		// manufacturedMaterial -> code and ingredient
		if(cdaManufacturedProduct.getManufacturedMaterial() != null && !cdaManufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
			if(cdaManufacturedProduct.getManufacturedMaterial().getCode() != null && !cdaManufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
				// manufacturedMaterial.code -> code
				fhirMedication.setCode(dtt.tCD2CodeableConceptExcludingTranslations(cdaManufacturedProduct.getManufacturedMaterial().getCode()));
				// translation -> ingredient
				for(CD translation : cdaManufacturedProduct.getManufacturedMaterial().getCode().getTranslations()) {
					if(!translation.isSetNullFlavor()) {
						Medication.ProductIngredient fhirIngredient = fhirProduct.addIngredient();
						Substance fhirSubstance = tCD2Substance(translation);
						fhirIngredient.setItem(new ResourceReferenceDt(fhirSubstance.getId()));
						fhirMedicationBundle.addEntry(new Bundle.Entry().setResource(fhirSubstance));
					}
				}
			}
		}
		
		// manufacturerOrganization -> manufacturer
		if(cdaManufacturedProduct.getManufacturerOrganization() != null && !cdaManufacturedProduct.getManufacturerOrganization().isSetNullFlavor()) {
			Organization org = tOrganization2Organization(cdaManufacturedProduct.getManufacturerOrganization());
			fhirMedication.setManufacturer(new ResourceReferenceDt(org.getId()));
			fhirMedicationBundle.addEntry(new Bundle.Entry().setResource(org));
		}
		
		return fhirMedicationBundle;
	}

	public Bundle tMedicationActivity2MedicationStatement(MedicationActivity cdaMedicationActivity) {
		if(cdaMedicationActivity == null || cdaMedicationActivity.isSetNullFlavor())
			return null;
		
		MedicationStatement fhirMedSt = new MedicationStatement();
		MedicationStatement.Dosage fhirDosage = fhirMedSt.addDosage();

		// bundle
		Bundle medStatementBundle = new Bundle();
		medStatementBundle.addEntry(new Bundle.Entry().setResource(fhirMedSt));
	
		// resource id
		IdDt resourceId = new IdDt("MedicationStatement", getUniqueId());
		fhirMedSt.setId(resourceId);
		
		// patient
		fhirMedSt.setPatient(getPatientRef());

		// id -> identifier
		if(cdaMedicationActivity.getIds() != null && !cdaMedicationActivity.getIds().isEmpty()) {
			for(II ii : cdaMedicationActivity.getIds()) {
				fhirMedSt.addIdentifier(dtt.tII2Identifier(ii));
			}
		}

		// statusCode -> status
		if(cdaMedicationActivity.getStatusCode() != null && !cdaMedicationActivity.getStatusCode().isSetNullFlavor()) {
			if(cdaMedicationActivity.getStatusCode().getCode() != null && !cdaMedicationActivity.getStatusCode().getCode().isEmpty()) {
				MedicationStatementStatusEnum statusCode = vst.tStatusCode2MedicationStatementStatusEnum(cdaMedicationActivity.getStatusCode().getCode());
				if(statusCode != null) {
					fhirMedSt.setStatus(statusCode);
				}
			}
		}
		
		// author[0] -> informationSource
		if(!cdaMedicationActivity.getAuthors().isEmpty()) {
			if(!cdaMedicationActivity.getAuthors().get(0).isSetNullFlavor()) {
				Bundle practBundle = tAuthor2Practitioner(cdaMedicationActivity.getAuthors().get(0));
				for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
					// Add all the resources returned from the bundle to the main bundle
					medStatementBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					// Add a reference to informationSource attribute only for Practitioner resource. Further resources can include Organization.
					if(entry.getResource() instanceof Practitioner) {
						fhirMedSt.setInformationSource(new ResourceReferenceDt(entry.getResource().getId()));
					}
				}
			}
		}

		// consumable.manufacturedProduct -> medication
		if(cdaMedicationActivity.getConsumable() != null && !cdaMedicationActivity.getConsumable().isSetNullFlavor()) {
			if(cdaMedicationActivity.getConsumable().getManufacturedProduct() != null && !cdaMedicationActivity.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
				Bundle fhirMedicationBundle = tManufacturedProduct2Medication(cdaMedicationActivity.getConsumable().getManufacturedProduct());
				for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry()){
					medStatementBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication) {
						fhirMedSt.setMedication(new ResourceReferenceDt(entry.getResource().getId()));
					}
				}
			}
		}
		
		// getting info from effectiveTimes
		if(cdaMedicationActivity.getEffectiveTimes() != null && !cdaMedicationActivity.getEffectiveTimes().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedicationActivity.getEffectiveTimes()) {
				if(ts != null && !ts.isSetNullFlavor()) {
					// effectiveTime[@xsi:type='IVL_TS'] -> effective
					if(ts instanceof IVL_TS) {
						fhirMedSt.setEffective(dtt.tIVL_TS2Period((IVL_TS)ts));
					}
					// effectiveTime[@xsi:type='PIVL_TS'] -> dosage.timing
					if(ts instanceof PIVL_TS) {
						fhirDosage.setTiming(dtt.tPIVL_TS2Timing((PIVL_TS)ts));
					}
				}
			}
		}

		// doseQuantity -> dosage.quantity
		if(cdaMedicationActivity.getDoseQuantity() != null && !cdaMedicationActivity.getDoseQuantity().isSetNullFlavor()) {
			fhirDosage.setQuantity(dtt.tPQ2SimpleQuantityDt(cdaMedicationActivity.getDoseQuantity()));
		}
		
		// routeCode -> dosage.route
		if(cdaMedicationActivity.getRouteCode() != null && !cdaMedicationActivity.getRouteCode().isSetNullFlavor()) {
			fhirDosage.setRoute(dtt.tCD2CodeableConcept(cdaMedicationActivity.getRouteCode()));
		}
		
		// rateQuantity -> dosage.rate
		if(cdaMedicationActivity.getRateQuantity() != null && !cdaMedicationActivity.getRateQuantity().isSetNullFlavor()) {
			fhirDosage.setRate(dtt.tIVL_PQ2Range(cdaMedicationActivity.getRateQuantity()));
		}
		
		// maxDoseQuantity -> dosage.maxDosePerPeriod
		if(cdaMedicationActivity.getMaxDoseQuantity() != null && !cdaMedicationActivity.getMaxDoseQuantity().isSetNullFlavor()) {
			// cdaDataType.RTO does nothing but extends cdaDataType.RTO_PQ_PQ
			fhirDosage.setMaxDosePerPeriod(dtt.tRTO2Ratio((RTO)cdaMedicationActivity.getMaxDoseQuantity()));
		}

		// negationInd -> wasNotTaken
		if(cdaMedicationActivity.getNegationInd() != null) {
			fhirMedSt.setWasNotTaken(cdaMedicationActivity.getNegationInd());
		}

		// indication -> reason
		for(Indication indication : cdaMedicationActivity.getIndications()) {
			// First, to set reasonForUse, we need to set wasNotTaken to false
			fhirMedSt.setWasNotTaken(false);

			Condition cond = tIndication2Condition(indication);
			medStatementBundle.addEntry(new Bundle.Entry().setResource(cond));
			fhirMedSt.setReasonForUse(new ResourceReferenceDt(cond.getId()));
		}

		return medStatementBundle;	
	}

	public Bundle tMedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMedicationDispense) {
		if(cdaMedicationDispense == null || cdaMedicationDispense.isSetNullFlavor())
			return null;
		
		// NOTE: Following mapping doesn't really suit the mapping proposed by daf
		
		MedicationDispense fhirMediDisp = new MedicationDispense();
		Bundle fhirMediDispBundle = new Bundle();
		fhirMediDispBundle.addEntry(new Bundle.Entry().setResource(fhirMediDisp));
	
		// patient
		fhirMediDisp.setPatient(getPatientRef());
		
		// resource id
		IdDt resourceId = new IdDt("MedicationDispense", getUniqueId());
		fhirMediDisp.setId(resourceId);
		
		// id -> identifier
		if(cdaMedicationDispense.getIds() != null &  !cdaMedicationDispense.getIds().isEmpty()) {
			for(II ii : cdaMedicationDispense.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					// Asserting at most one identifier exists
					fhirMediDisp.setIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// statusCode -> status
		if(cdaMedicationDispense.getStatusCode() != null && !cdaMedicationDispense.getStatusCode().isSetNullFlavor()) {
			if(cdaMedicationDispense.getStatusCode().getCode() != null && !cdaMedicationDispense.getStatusCode().getCode().isEmpty()) {
				MedicationDispenseStatusEnum mediDispStatEnum = vst.tStatusCode2MedicationDispenseStatusEnum(cdaMedicationDispense.getStatusCode().getCode());
				if(mediDispStatEnum != null){
					fhirMediDisp.setStatus(mediDispStatEnum);
				}
			}
		}
		
		// code -> type
		if(cdaMedicationDispense.getCode() != null && !cdaMedicationDispense.getCode().isSetNullFlavor()){
			fhirMediDisp.setType(dtt.tCD2CodeableConcept(cdaMedicationDispense.getCode()));
		}
		
		// product.manufacturedProduct(MedicationInformation) -> medication
		if(cdaMedicationDispense.getProduct() != null && !cdaMedicationDispense.getProduct().isSetNullFlavor()) {
			if(cdaMedicationDispense.getProduct().getManufacturedProduct() != null && !cdaMedicationDispense.getProduct().getManufacturedProduct().isSetNullFlavor()) {
				if(cdaMedicationDispense.getProduct().getManufacturedProduct() instanceof MedicationInformation) {
					MedicationInformation cdaMedicationInformation = (MedicationInformation) cdaMedicationDispense.getProduct().getManufacturedProduct();
					Medication fhirMedication = null;
					Bundle fhirMedicationBundle = tMedicationInformation2Medication(cdaMedicationInformation);
					
					for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : fhirMedicationBundle.getEntry()) {
						fhirMediDispBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
						if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Medication){
							fhirMedication = (ca.uhn.fhir.model.dstu2.resource.Medication)entry.getResource();
						}
					}
					fhirMediDisp.setMedication(new ResourceReferenceDt(fhirMedication.getId()));
				}
			}
		}
		
		// performer -> dispenser
		if(cdaMedicationDispense.getPerformers() != null && !cdaMedicationDispense.getPerformers().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaMedicationDispense.getPerformers()) {
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
		
		// quantity -> quantity
		if(cdaMedicationDispense.getQuantity() != null && !cdaMedicationDispense.getQuantity().isSetNullFlavor()) {
			fhirMediDisp.setQuantity(dtt.tPQ2SimpleQuantityDt( cdaMedicationDispense.getQuantity()));
		}
		
		// whenPrepared and whenHandedOver
		// effectiveTime[0] -> whenPrepared, effectiveTime[1] -> whenHandedOver
		int effectiveTimeCount = 0;
		if(cdaMedicationDispense.getEffectiveTimes() != null && !cdaMedicationDispense.getEffectiveTimes().isEmpty()) {
			for(SXCM_TS ts : cdaMedicationDispense.getEffectiveTimes()) {
				if(effectiveTimeCount == 0) {
					// effectiveTime[0] -> whenPrepared
					if(ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenPrepared(dtt.tTS2DateTime(ts));
					}
					effectiveTimeCount++;
				} else if(effectiveTimeCount == 1) {
					// effectiveTime[1] -> whenHandedOver
					if(ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenHandedOver(dtt.tTS2DateTime(ts));
					}
					effectiveTimeCount++;
				}
			}
		}
		
		// Adding dosageInstruction
		MedicationDispense.DosageInstruction fhirDosageInstruction = fhirMediDisp.addDosageInstruction();

		// TODO: The information used for dosageInstruction is used for different fields, too.
		// Determine which field the information should fit
		
		// effectiveTimes -> dosageInstruction.timing.event
		if(cdaMedicationDispense.getEffectiveTimes() != null && !cdaMedicationDispense.getEffectiveTimes().isEmpty()) {
			TimingDt fhirTiming = new TimingDt();
			
			// adding effectiveTimes to fhirTiming
			for(org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedicationDispense.getEffectiveTimes()) {
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
		
		// quantity -> dosageInstruction.dose
		if(cdaMedicationDispense.getQuantity() != null && !cdaMedicationDispense.getQuantity().isSetNullFlavor()) {
			fhirDosageInstruction.setDose(dtt.tPQ2SimpleQuantityDt(cdaMedicationDispense.getQuantity()));
		}
		return fhirMediDispBundle;
	}

	public Bundle tMedicationInformation2Medication(MedicationInformation cdaMedicationInformation) {
		/*
		 * Since MedicationInformation is a ManufacturedProduct instance with a specific templateId,
		 * tManufacturedProduct2Medication should satisfy the required mapping for MedicationInformation
		 */
		return tManufacturedProduct2Medication(cdaMedicationInformation);
	}
	
	public Bundle tObservation2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation) {
		if(cdaObservation == null || cdaObservation.isSetNullFlavor())
			return null;

		Observation fhirObs = new Observation();

		// bundle
		Bundle fhirObsBundle = new Bundle();
		fhirObsBundle.addEntry(new Bundle.Entry().setResource(fhirObs));

		// resource id
		IdDt resourceId = new IdDt("Observation", getUniqueId());
		fhirObs.setId(resourceId);

		// subject
		fhirObs.setSubject(getPatientRef());

		// id -> identifier
		if(cdaObservation.getIds() != null && !cdaObservation.getIds().isEmpty()) {
			for(II ii : cdaObservation.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code -> code
		if(cdaObservation.getCode() != null && !cdaObservation.getCode().isSetNullFlavor()) {
			fhirObs.setCode(dtt.tCD2CodeableConcept(cdaObservation.getCode()));
		}

		// statusCode -> status
		if(cdaObservation.getStatusCode() != null && !cdaObservation.getStatusCode().isSetNullFlavor()) {
			if(cdaObservation.getStatusCode().getCode() != null) {
				fhirObs.setStatus(vst.tObservationStatusCode2ObservationStatusEnum(cdaObservation.getStatusCode().getCode()));
			}
		}

		// effectiveTime -> effective
		if(cdaObservation.getEffectiveTime() != null && !cdaObservation.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObservation.getEffectiveTime()));
		}

		// targetSiteCode -> bodySite
		for(CD cd : cdaObservation.getTargetSiteCodes()) {
			if(cd != null && !cd.isSetNullFlavor()) {
				fhirObs.setBodySite(dtt.tCD2CodeableConcept(cd));
			}
		}

		// value or dataAbsentReason
		if(cdaObservation.getValues() != null && !cdaObservation.getValues().isEmpty()) {
			// We traverse the values in cdaObs
			for(ANY value : cdaObservation.getValues()) {
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
							fhirObs.getDataAbsentReason().addCoding(DataAbsentReasonCode);
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
					} else if(value instanceof TS) {
						fhirObs.setValue(dtt.tTS2DateTime((TS)value));
					}
				}
			}
		}

		// encounter -> encounter
		if(cdaObservation.getEncounters() != null && !cdaObservation.getEncounters().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter : cdaObservation.getEncounters()) {
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

		// author -> performer
		for(org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
			if(author != null && !author.isSetNullFlavor()) {
				Bundle fhirPractitionerBundle = tAuthor2Practitioner(author);
				for(Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
					fhirObsBundle.addEntry(entry);
					if(entry.getResource() instanceof Practitioner) {
						fhirObs.addPerformer().setReference(entry.getResource().getId());
					}
				}
			}
		}

		// methodCode -> method
		for(CE method : cdaObservation.getMethodCodes()) {
			if(method != null && !method.isSetNullFlavor()) {
				// Asserting that only one method exists
				fhirObs.setMethod(dtt.tCD2CodeableConcept(method));
			}
		}

		// author.time -> issued
		if(cdaObservation.getAuthors() != null && !cdaObservation.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
				if(author != null && !author.isSetNullFlavor()) {
					// get time from author
					if(author.getTime() != null && !author.getTime().isSetNullFlavor()) {
						fhirObs.setIssued(dtt.tTS2Instant(author.getTime()));
					}
				}
			}
		}

		// interpretationCode -> interpretation
		if(cdaObservation.getInterpretationCodes() != null && !cdaObservation.getInterpretationCodes().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.CE cdaInterprCode : cdaObservation.getInterpretationCodes()) {
				if(cdaInterprCode != null && !cdaInterprCode.isSetNullFlavor()) {
					// Asserting that only one interpretation code exists
					fhirObs.setInterpretation(vst.ObservationInterpretationCode2ObservationInterpretationCode(cdaInterprCode));
				}
			}
		}

		// referenceRange -> referenceRange
		if(cdaObservation.getReferenceRanges() != null && !cdaObservation.getReferenceRanges().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange : cdaObservation.getReferenceRanges()) {
				if(cdaReferenceRange != null && !cdaReferenceRange.isSetNullFlavor()) {
					fhirObs.addReferenceRange(tReferenceRange2ReferenceRange(cdaReferenceRange));
				}
			}
		}

		return fhirObsBundle;
	}

	public Organization tOrganization2Organization(org.openhealthtools.mdht.uml.cda.Organization cdaOrganization){
		if(cdaOrganization == null || cdaOrganization.isSetNullFlavor())
			return null;
		
		Organization fhirOrganization = new Organization();
		
		// resource id id
		IdDt resourceId = new IdDt("Organization",getUniqueId());
		fhirOrganization.setId(resourceId);
		
		// id -> identifier
		if(cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for(II ii : cdaOrganization.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name -> name
		if(cdaOrganization.getNames() != null && !cdaOrganization.isSetNullFlavor()) {
			for(ON name:cdaOrganization.getNames()) {
				if(name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty()) {
					fhirOrganization.setName(name.getText());
				}
			}
		}
		
		// telecom -> telecom
		if(cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty()) {
			for(TEL tel : cdaOrganization.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirOrganization.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// addr -> address
		if(cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for(AD ad : cdaOrganization.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		return fhirOrganization;
	}

	public Location tServiceDeliveryLocation2Location(ServiceDeliveryLocation cdaSDLOC) {
		/*
		 * ServiceDeliveryLocation is a ParticipantRole instance with a specific templateId
		 * Therefore, tParticipantRole2Location should satisfy the necessary mapping
		 */
		return tParticipantRole2Location(cdaSDLOC);
	}

	public Location tParticipantRole2Location(ParticipantRole cdaParticipantRole) {
		if(cdaParticipantRole == null || cdaParticipantRole.isSetNullFlavor())
			return null;

		Location fhirLocation = new Location();
		
		// resource id
		IdDt resourceId = new IdDt("Location", getUniqueId());
		fhirLocation.setId(resourceId);
		
		// id -> identifier
		if(cdaParticipantRole.getIds() != null && !cdaParticipantRole.getIds().isEmpty()) {
			for(II ii : cdaParticipantRole.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirLocation.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code -> type
		// TODO: Requires huge mapping work from HL7 HealthcareServiceLocation value set to http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType
		if(cdaParticipantRole.getCode() != null && !cdaParticipantRole.getCode().isSetNullFlavor()) {
			logger.info("Found location.code in the CDA document, which can be mapped to Location.type on the FHIR side. But this is skipped for the moment, as it requires huge mapping work from HL7 HealthcareServiceLocation value set to http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType");
			//fhirLocation.setType();
		}
		
		// playingEntity.name.text -> name
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
		
		// telecom -> telecom
		if(cdaParticipantRole.getTelecoms() != null && !cdaParticipantRole.getTelecoms().isEmpty()) {
			for(TEL tel : cdaParticipantRole.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirLocation.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}
		
		// addr -> address
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

		// id -> identifier
		if(cdaPatientRole.getIds() != null && !cdaPatientRole.getIds().isEmpty()) {
			for(II id : cdaPatientRole.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirPatient.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}
		
		// addr -> address
		for(AD ad : cdaPatientRole.getAddrs()){
			if(ad != null && !ad.isSetNullFlavor()) {
				fhirPatient.addAddress(dtt.AD2Address(ad));
			}
		}
		
		// telecom -> telecom
		for(TEL tel : cdaPatientRole.getTelecoms()) {
			if(tel != null && !tel.isSetNullFlavor()) {
				fhirPatient.addTelecom(dtt.tTEL2ContactPoint(tel));
			}
		}

		// providerOrganization -> managingOrganization
		if(cdaPatientRole.getProviderOrganization() != null && !cdaPatientRole.getProviderOrganization().isSetNullFlavor()) {
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(cdaPatientRole.getProviderOrganization());
			fhirPatientBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
			ResourceReferenceDt organizationReference = new ResourceReferenceDt(fhirOrganization.getId());
			fhirPatient.setManagingOrganization(organizationReference);
		}

		org.openhealthtools.mdht.uml.cda.Patient cdaPatient = cdaPatientRole.getPatient();
		
		if(cdaPatient != null && !cdaPatient.isSetNullFlavor()) {
			// patient.name -> name
			for(PN pn : cdaPatient.getNames()) {
				if(pn != null && !pn.isSetNullFlavor()) {
					fhirPatient.addName(dtt.tEN2HumanName(pn));
				}
			}

			// patient.administrativeGenderCode -> gender
			if(cdaPatient.getAdministrativeGenderCode() != null && !cdaPatient.getAdministrativeGenderCode().isSetNullFlavor()
					&& cdaPatient.getAdministrativeGenderCode().getCode() != null && !cdaPatient.getAdministrativeGenderCode().getCode().isEmpty()) {
				AdministrativeGenderEnum administrativeGender = vst.tAdministrativeGenderCode2AdministrativeGenderEnum(cdaPatient.getAdministrativeGenderCode().getCode());
				fhirPatient.setGender(administrativeGender);
			}

			// patient.birthTime -> birthDate
			if(cdaPatient.getBirthTime() != null && !cdaPatient.getBirthTime().isSetNullFlavor()) {
				fhirPatient.setBirthDate(dtt.tTS2Date(cdaPatient.getBirthTime()));
			}

			// patient.maritalStatusCode -> maritalStatus 
			if(cdaPatient.getMaritalStatusCode() != null && !cdaPatient.getMaritalStatusCode().isSetNullFlavor()) {
				if(cdaPatient.getMaritalStatusCode().getCode() != null && !cdaPatient.getMaritalStatusCode().getCode().isEmpty()) {
					fhirPatient.setMaritalStatus(vst.tMaritalStatusCode2MaritalStatusCodesEnum(cdaPatient.getMaritalStatusCode().getCode()));
				}
			}

			// patient.languageCommunication -> communication
			for(LanguageCommunication LC : cdaPatient.getLanguageCommunications()) {
				if(LC != null && !LC.isSetNullFlavor()) {
					fhirPatient.addCommunication(tLanguageCommunication2Communication(LC));
				}
			}

			// patient.guardian -> contact 
			for(org.openhealthtools.mdht.uml.cda.Guardian guardian : cdaPatient.getGuardians()) {
				if(guardian != null && !guardian.isSetNullFlavor()) {
					fhirPatient.addContact(tGuardian2Contact(guardian));
				}
			}

			// extensions

			// patient.raceCode -> extRace
			if (cdaPatient.getRaceCode() != null && !cdaPatient.getRaceCode().isSetNullFlavor()) {
				ExtensionDt extRace = new ExtensionDt();
				extRace.setModifier(false);
				extRace.setUrl(Constants.URL_EXTENSION_RACE);
				CD raceCode = cdaPatient.getRaceCode();
				extRace.setValue(dtt.tCD2CodeableConcept(raceCode));
				fhirPatient.addUndeclaredExtension(extRace);
			}

			// patient.ethnicGroupCode -> extEthnicity
			if (cdaPatient.getEthnicGroupCode() != null && !cdaPatient.getEthnicGroupCode().isSetNullFlavor()) {
				ExtensionDt extEthnicity = new ExtensionDt();
				extEthnicity.setModifier(false);
				extEthnicity.setUrl(Constants.URL_EXTENSION_ETHNICITY);
				CD ethnicGroupCode = cdaPatient.getEthnicGroupCode();
				extEthnicity.setValue(dtt.tCD2CodeableConcept(ethnicGroupCode));
				fhirPatient.addUndeclaredExtension(extEthnicity);
			}

			// patient.religiousAffiliationCode -> extReligion
			if (cdaPatient.getReligiousAffiliationCode() != null && !cdaPatient.getReligiousAffiliationCode().isSetNullFlavor()) {
				ExtensionDt extReligion = new ExtensionDt();
				extReligion.setModifier(false);
				extReligion.setUrl(Constants.URL_EXTENSION_RELIGION);
				CD religiousAffiliationCode = cdaPatient.getReligiousAffiliationCode();
				extReligion.setValue(dtt.tCD2CodeableConcept(religiousAffiliationCode));
				fhirPatient.addUndeclaredExtension(extReligion);
			}

			// patient.birthplace.place.addr -> extBirthPlace
			if (cdaPatient.getBirthplace() != null && !cdaPatient.getBirthplace().isSetNullFlavor()
					&& cdaPatient.getBirthplace().getPlace() != null && !cdaPatient.getBirthplace().getPlace().isSetNullFlavor()
					&& cdaPatient.getBirthplace().getPlace().getAddr() != null && !cdaPatient.getBirthplace().getPlace().getAddr().isSetNullFlavor()) {
				ExtensionDt extBirthPlace = new ExtensionDt();
				extBirthPlace.setModifier(false);
				extBirthPlace.setUrl(Constants.URL_EXTENSION_BIRTHPLACE);
				extBirthPlace.setValue(dtt.AD2Address(cdaPatient.getBirthplace().getPlace().getAddr()));
				fhirPatient.addUndeclaredExtension(extBirthPlace);
			}
		}
			
		return fhirPatientBundle;
	}
	
	public Bundle tPerformer22Practitioner(Performer2 cdaPerformer2) {
		if(cdaPerformer2 == null || cdaPerformer2.isSetNullFlavor()) 
			return null;
		else
			return tAssignedEntity2Practitioner(cdaPerformer2.getAssignedEntity());
		
	}

	public Bundle tProblemConcernAct2Condition(ProblemConcernAct cdaProblemConcernAct) {
		if(cdaProblemConcernAct == null || cdaProblemConcernAct.isSetNullFlavor())
			return null;
		
		Bundle fhirConditionBundle = new Bundle();

		// each problem observation instance -> FHIR Condition instance
		for(ProblemObservation cdaProbObs : cdaProblemConcernAct.getProblemObservations()) {
			Bundle fhirProbObsBundle = tProblemObservation2Condition(cdaProbObs);
			if(fhirProbObsBundle == null)
				continue;

			for(Bundle.Entry entry : fhirProbObsBundle.getEntry()) {
				fhirConditionBundle.addEntry(entry);
				if(entry.getResource() instanceof Condition) {
					Condition fhirCond = (Condition) entry.getResource();

					// act/statusCode -> Condition.clinicalStatus
					// NOTE: Problem status template is deprecated in C-CDA Release 2.1; hence status data is not retrieved from this template.
					if(cdaProblemConcernAct.getStatusCode() != null && !cdaProblemConcernAct.getStatusCode().isSetNullFlavor()) {
						fhirCond.setClinicalStatus(vst.tStatusCode2ConditionClinicalStatusCodesEnum(cdaProblemConcernAct.getStatusCode().getCode()));
					}
				}
			}
		}

		return fhirConditionBundle;
	}

	public Bundle tProblemObservation2Condition(ProblemObservation cdaProbObs) {
		if(cdaProbObs == null || cdaProbObs.isSetNullFlavor())
			return null;

		// NOTE: Although DAF requires the mapping for severity, this data is not available on the C-CDA side.
		// NOTE: Problem status template is deprecated in C-CDA Release 2.1; hence status data is not retrieved from this template.

		Bundle fhirConditionBundle = new Bundle();

		Condition fhirCondition = new Condition();
		fhirConditionBundle.addEntry(new Bundle.Entry().setResource(fhirCondition));

		// resource id
		IdDt resourceId = new IdDt("Condition", getUniqueId());
		fhirCondition.setId(resourceId);

		// patient
		fhirCondition.setPatient(getPatientRef());

		// id -> identifier
		for(II id : cdaProbObs.getIds()) {
			if(!id.isSetNullFlavor()) {
				fhirCondition.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// code -> category
		if (cdaProbObs.getCode() != null && !cdaProbObs.getCode().isSetNullFlavor()) {
			if (cdaProbObs.getCode().getCode() != null) {
				ConditionCategoryCodesEnum conditionCategory = vst.tProblemType2ConditionCategoryCodesEnum(cdaProbObs.getCode().getCode());
				if (conditionCategory != null) {
					fhirCondition.setCategory(conditionCategory);
				}
			}
		}

		// value -> code
		if (cdaProbObs.getValues() != null && !cdaProbObs.getValues().isEmpty()) {
			for (ANY value : cdaProbObs.getValues()) {
				if (value != null && !value.isSetNullFlavor()) {
					if (value instanceof CD) {
						fhirCondition.setCode(dtt.tCD2CodeableConcept((CD) value));
					}
				}
			}
		}

		// onset and abatement
		if (cdaProbObs.getEffectiveTime() != null && !cdaProbObs.getEffectiveTime().isSetNullFlavor()) {

			IVXB_TS low = cdaProbObs.getEffectiveTime().getLow();
			IVXB_TS high = cdaProbObs.getEffectiveTime().getHigh();

			// low -> onset (if doesn't exist, checking value)
			if (low != null && !low.isSetNullFlavor()) {
				fhirCondition.setOnset(dtt.tTS2DateTime(low));
			} else if (cdaProbObs.getEffectiveTime().getValue() != null && !cdaProbObs.getEffectiveTime().getValue().isEmpty()) {
				fhirCondition.setOnset(dtt.tString2DateTime(cdaProbObs.getEffectiveTime().getValue()));
			}

			// high -> abatement
			if (high != null && !high.isSetNullFlavor()) {
				fhirCondition.setAbatement(dtt.tTS2DateTime(high));
			}
		}

		// author[0] -> asserter
		if(!cdaProbObs.getAuthors().isEmpty()) {
			if (cdaProbObs.getAuthors().get(0) != null && !cdaProbObs.getAuthors().get(0).isSetNullFlavor()) {
				Author author = cdaProbObs.getAuthors().get(0);
				Bundle fhirPractitionerBundle = tAuthor2Practitioner(author);
				for (Bundle.Entry entry : fhirPractitionerBundle.getEntry()) {
					fhirConditionBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if (entry.getResource() instanceof Practitioner) {
						fhirCondition.setAsserter(new ResourceReferenceDt(entry.getResource().getId()));
					}
				}

				// author.time -> dateRecorded
				if (author.getTime() != null && !author.getTime().isSetNullFlavor()) {
					fhirCondition.setDateRecorded(dtt.tTS2Date(author.getTime()));
				}
			}
		}

		// encounter -> encounter
		if (cdaProbObs.getEncounters() != null && !cdaProbObs.getEncounters().isEmpty()) {
			if (cdaProbObs.getEncounters().get(0) != null && cdaProbObs.getEncounters().get(0).isSetNullFlavor()) {
				Bundle fhirEncounterBundle = tEncounter2Encounter(cdaProbObs.getEncounters().get(0));
				for (Bundle.Entry entry : fhirEncounterBundle.getEntry()) {
					fhirConditionBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if (entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Encounter) {
						fhirCondition.setEncounter(new ResourceReferenceDt(entry.getResource().getId()));
					}
				}
			}
		}

		// NOTE: A default value is assigned to verificationStatus attribute, as it is mandatory but cannot be mapped from the CDA side
		fhirCondition.setVerificationStatus(Constants.DEFAULT_CONDITION_VERIFICATION_STATUS);

		return fhirConditionBundle;
	}

	public Bundle tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure){
		if(cdaProcedure == null || cdaProcedure.isSetNullFlavor())
			return null;

		ca.uhn.fhir.model.dstu2.resource.Procedure fhirProc = new ca.uhn.fhir.model.dstu2.resource.Procedure();
		Bundle fhirProcBundle = new Bundle();
		fhirProcBundle.addEntry(new Bundle.Entry().setResource(fhirProc));

		// subject
		fhirProc.setSubject(getPatientRef());

		// resource id
		IdDt resourceId = new IdDt("Procedure", getUniqueId());
		fhirProc.setId(resourceId);

		// id -> identifier
		if(cdaProcedure.getIds() != null && !cdaProcedure.getIds().isEmpty()) {
			for(II id : cdaProcedure.getIds()) {
				if(id != null && !id.isSetNullFlavor()) {
					fhirProc.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// effectiveTime -> performed
		if(cdaProcedure.getEffectiveTime() != null && !cdaProcedure.getEffectiveTime().isSetNullFlavor()){
			fhirProc.setPerformed(dtt.tIVL_TS2Period(cdaProcedure.getEffectiveTime()));
		}

		// targetSiteCode -> bodySite
		if(cdaProcedure.getTargetSiteCodes() != null && !cdaProcedure.getTargetSiteCodes().isEmpty()) {
			for(CD cd : cdaProcedure.getTargetSiteCodes()) {
				if(cd != null && !cd.isSetNullFlavor()){
					fhirProc.addBodySite(dtt.tCD2CodeableConcept(cd));
				}
			}
		}

		// performer -> performer
		for(Performer2 performer : cdaProcedure.getPerformers()) {
			if(performer.getAssignedEntity()!= null && !performer.getAssignedEntity().isSetNullFlavor()) {
				Bundle practBundle = tPerformer22Practitioner(performer);
				for(Bundle.Entry entry : practBundle.getEntry()) {
					// Add all the resources returned from the bundle to the main bundle
					fhirProcBundle.addEntry(entry);
					// Add a reference to performer attribute only for Practitioner resource. Further resources can include Organization.
					if(entry.getResource() instanceof Practitioner) {
						Performer fhirPerformer = new Performer();
						fhirPerformer.setActor(new ResourceReferenceDt(entry.getResource().getId()));
						fhirProc.addPerformer(fhirPerformer);
					}
				}
			}
		}

		// statusCode -> status
		if(cdaProcedure.getStatusCode() != null && !cdaProcedure.getStatusCode().isSetNullFlavor() && cdaProcedure.getStatusCode().getCode() != null) {
			ProcedureStatusEnum status = vst.tStatusCode2ProcedureStatusEnum(cdaProcedure.getStatusCode().getCode());
			if(status != null) {
				fhirProc.setStatus(status);
			}
		}

		// code -> code
		if(cdaProcedure.getCode() != null && !cdaProcedure.getCode().isSetNullFlavor()) {
			fhirProc.setCode(dtt.tCD2CodeableConcept(cdaProcedure.getCode()));
		}

		// encounter[0] -> encounter
		if(!cdaProcedure.getEncounters().isEmpty()) {
			org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter = cdaProcedure.getEncounters().get(0);
			if(cdaEncounter != null && !cdaEncounter.isSetNullFlavor()) {
				Bundle encBundle = tEncounter2Encounter(cdaEncounter);
				for(Bundle.Entry entry : encBundle.getEntry()) {
					fhirProcBundle.addEntry(entry);
					if(entry.getResource() instanceof Encounter) {
						fhirProc.setEncounter(new ResourceReferenceDt(entry.getResource().getId()));
					}
				}
			}
		}

		return fhirProcBundle;
	}

	public Bundle tResultObservation2Observation(ResultObservation cdaResultObservation) {
		return tObservation2Observation(cdaResultObservation);
	}

	public Bundle tImmunizationActivity2Immunization(ImmunizationActivity cdaImmunizationActivity) {
		if(cdaImmunizationActivity == null || cdaImmunizationActivity.isSetNullFlavor())
			return null;
		
		Immunization fhirImmunization = new Immunization();
		
		Bundle fhirImmunizationBundle = new Bundle();
		fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirImmunization));
		
		// resource id
		IdDt resourceId = new IdDt("Immunization", getUniqueId());
		fhirImmunization.setId(resourceId);
		
		// patient
		fhirImmunization.setPatient(getPatientRef());
		
		// id -> identifier
		if(cdaImmunizationActivity.getIds()!=null && !cdaImmunizationActivity.getIds().isEmpty()) {
			for(II ii : cdaImmunizationActivity.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirImmunization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// negationInd -> wasNotTaken
		if(cdaImmunizationActivity.getNegationInd() != null) {
			fhirImmunization.setWasNotGiven(cdaImmunizationActivity.getNegationInd());
		}

		// effectiveTime -> date
		if(cdaImmunizationActivity.getEffectiveTimes() != null && !cdaImmunizationActivity.getEffectiveTimes().isEmpty()) {
			for(SXCM_TS effectiveTime : cdaImmunizationActivity.getEffectiveTimes()) {
				if(effectiveTime != null && !effectiveTime.isSetNullFlavor()) {
					// Asserting that at most one effective time exists
					fhirImmunization.setDate(dtt.tTS2DateTime(effectiveTime));
				}
			}
		}
		
		// lotNumber, vaccineCode, organization
		if(cdaImmunizationActivity.getConsumable()!=null && !cdaImmunizationActivity.getConsumable().isSetNullFlavor()) {
			if(cdaImmunizationActivity.getConsumable().getManufacturedProduct()!=null && !cdaImmunizationActivity.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
				ManufacturedProduct manufacturedProduct=cdaImmunizationActivity.getConsumable().getManufacturedProduct();
				
				if(manufacturedProduct.getManufacturedMaterial()!=null && !manufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
					Material manufacturedMaterial=manufacturedProduct.getManufacturedMaterial();
					
					// consumable.manufacturedProduct.manufacturedMaterial.code -> vaccineCode
					if(manufacturedProduct.getManufacturedMaterial().getCode() != null && !manufacturedProduct.getManufacturedMaterial().getCode().isSetNullFlavor()) {
						fhirImmunization.setVaccineCode(dtt.tCD2CodeableConcept(manufacturedMaterial.getCode()));
					}
					
					// consumable.manufacturedProduct.manufacturedMaterial.lotNumberText -> lotNumber
					if(manufacturedMaterial.getLotNumberText()!=null && !manufacturedMaterial.getLotNumberText().isSetNullFlavor()) {
						fhirImmunization.setLotNumber(dtt.tST2String(manufacturedMaterial.getLotNumberText()));
					}
				}
				
				// consumable.manufacturedProduct.manufacturerOrganization -> manufacturer
				if(manufacturedProduct.getManufacturerOrganization()!=null && !manufacturedProduct.getManufacturerOrganization().isSetNullFlavor()) {
					
					ca.uhn.fhir.model.dstu2.resource.Organization fhirOrganization = tOrganization2Organization(manufacturedProduct.getManufacturerOrganization());
					
					fhirImmunization.setManufacturer(new ResourceReferenceDt(fhirOrganization.getId()));
					fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(fhirOrganization));
				}
			}
		}
		
		// performer -> performer
		if(cdaImmunizationActivity.getPerformers() != null && !cdaImmunizationActivity.getPerformers().isEmpty()) {
			for(Performer2 performer : cdaImmunizationActivity.getPerformers()) {
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
						
		// approachSiteCode -> site
		for(CD cd : cdaImmunizationActivity.getApproachSiteCodes()) {
			fhirImmunization.setSite(dtt.tCD2CodeableConcept(cd));
		}
		
		// routeCode -> route
		if(cdaImmunizationActivity.getRouteCode()!=null && !cdaImmunizationActivity.getRouteCode().isSetNullFlavor()) {
			fhirImmunization.setRoute(dtt.tCD2CodeableConcept(cdaImmunizationActivity.getRouteCode()));
		}
		
		// doseQuantity -> doseQuantity
		if(cdaImmunizationActivity.getDoseQuantity()!=null && !cdaImmunizationActivity.getDoseQuantity().isSetNullFlavor()) {
			fhirImmunization.setDoseQuantity(dtt.tPQ2SimpleQuantityDt(cdaImmunizationActivity.getDoseQuantity()));
		}
		
		// statusCode -> status
		if(cdaImmunizationActivity.getStatusCode()!=null && !cdaImmunizationActivity.getStatusCode().isSetNullFlavor()) {
			if(cdaImmunizationActivity.getStatusCode().getCode() != null && !cdaImmunizationActivity.getStatusCode().getCode().isEmpty()) {
				fhirImmunization.setStatus(cdaImmunizationActivity.getStatusCode().getCode());
			}
		}

		// wasNotGiven == true
		if(fhirImmunization.getWasNotGiven()) {
			// immunizationRefusalReason.code -> explanation.reasonNotGiven
			if (cdaImmunizationActivity.getImmunizationRefusalReason() != null && !cdaImmunizationActivity.getImmunizationRefusalReason().isSetNullFlavor()) {
				if (cdaImmunizationActivity.getImmunizationRefusalReason().getCode() != null && !cdaImmunizationActivity.getImmunizationRefusalReason().getCode().isSetNullFlavor()) {
					fhirImmunization.setExplanation(new Explanation().addReasonNotGiven(dtt.tCD2CodeableConcept(cdaImmunizationActivity.getImmunizationRefusalReason().getCode())));
				}
			}
		}
		// wasNotGiven == false
		else if(!fhirImmunization.getWasNotGiven()) {
			// indication.value -> explanation.reason
			if(cdaImmunizationActivity.getIndication() != null && !cdaImmunizationActivity.getIndication().isSetNullFlavor()) {
				if(!cdaImmunizationActivity.getIndication().getValues().isEmpty() && cdaImmunizationActivity.getIndication().getValues().get(0) != null && !cdaImmunizationActivity.getIndication().getValues().get(0).isSetNullFlavor()) {
					fhirImmunization.setExplanation(new Explanation().addReason(dtt.tCD2CodeableConcept((CD)cdaImmunizationActivity.getIndication().getValues().get(0))));
				}
			}
		}

		// reaction (i.e. entryRelationship/observation[templateId/@root="2.16.840.1.113883.10.20.22.4.9"] -> reaction
		if(cdaImmunizationActivity.getReactionObservation() != null && !cdaImmunizationActivity.getReactionObservation().isSetNullFlavor()) {
			Bundle reactionBundle = tReactionObservation2Observation(cdaImmunizationActivity.getReactionObservation());
			Observation fhirReactionObservation = null;
			for(Bundle.Entry entry : reactionBundle.getEntry()) {
				fhirImmunizationBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
				if(entry.getResource() instanceof Observation) {
					fhirReactionObservation = (Observation) entry.getResource();
				}
				Immunization.Reaction fhirReaction = fhirImmunization.addReaction();
				// reaction -> reaction.detail[ref=Observation]
				fhirReaction.setDetail(new ResourceReferenceDt(fhirReactionObservation.getId()));

				// reaction/effectiveTime/low -> reaction.date
				if(fhirReactionObservation.getEffective() != null) {
					PeriodDt reactionDate = (PeriodDt) fhirReactionObservation.getEffective();
					if(reactionDate.getStart() != null)
						fhirReaction.setDate(reactionDate.getStartElement());
				}
			}
		}
		
		// reported
		fhirImmunization.setReported(Constants.DEFAULT_IMMUNIZATION_REPORTED);

		return fhirImmunizationBundle;
		
	}	
	
	public Bundle tVitalSignObservation2Observation(VitalSignObservation cdaVitalSignObservation) {
		return tObservation2Observation(cdaVitalSignObservation);
	}

	public AgeDt tAgeObservation2AgeDt(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation) {
		if(cdaAgeObservation == null || cdaAgeObservation.isSetNullFlavor())
			return null;

		AgeDt fhirAge = new AgeDt();

		// value-> age
		if(cdaAgeObservation != null && !cdaAgeObservation.getValues().isEmpty()) {
			for(ANY value : cdaAgeObservation.getValues()) {
				if(value != null && !value.isSetNullFlavor()) {
					if(value instanceof PQ) {
						if(((PQ)value).getValue() != null) {
							// value.value -> value
							fhirAge.setValue(((PQ)value).getValue());
							// value.unit -> unit
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
		
		// addr -> address
		if(cdaGuardian.getAddrs() != null && !cdaGuardian.getAddrs().isEmpty()) {
			fhirContact.setAddress(dtt.AD2Address(cdaGuardian.getAddrs().get(0)));
		} 
		
		// telecom -> telecom
		if(cdaGuardian.getTelecoms() != null && !cdaGuardian.getTelecoms().isEmpty()) {
			for(TEL tel : cdaGuardian.getTelecoms()) {
				if(tel != null && !tel.isSetNullFlavor()) {
					fhirContact.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// guardianPerson/name -> name
		if(cdaGuardian.getGuardianPerson() != null && !cdaGuardian.getGuardianPerson().isSetNullFlavor()) {
			for(PN pn : cdaGuardian.getGuardianPerson().getNames()) {
				if(!pn.isSetNullFlavor()) {
					fhirContact.setName(dtt.tEN2HumanName(pn));
				}
			}
		}
		
		// code -> relationship
		if(cdaGuardian.getCode() != null && !cdaGuardian.getCode().isSetNullFlavor()) {
			// try to use ValueSetsTransformer method tRoleCode2PatientContactRelationshipCode
			CodingDt relationshipCoding = null;
			if(cdaGuardian.getCode().getCode() != null && !cdaGuardian.getCode().getCode().isEmpty()) {
				relationshipCoding = vst.tRoleCode2PatientContactRelationshipCode(cdaGuardian.getCode().getCode());
			}
			// if tRoleCode2PatientContactRelationshipCode returns non-null value, add as coding
			// otherwise, add relationship directly by making code transformation(tCD2CodeableConcept)
			if(relationshipCoding != null)
				fhirContact.addRelationship(new CodeableConceptDt().addCoding(relationshipCoding));
			else
				fhirContact.addRelationship(dtt.tCD2CodeableConcept(cdaGuardian.getCode()));

		}
		return fhirContact;
	}

	public Communication tLanguageCommunication2Communication(LanguageCommunication cdaLanguageCommunication) {
		if(cdaLanguageCommunication == null || cdaLanguageCommunication.isSetNullFlavor())
			return null;
		
		Communication fhirCommunication = new Communication();
		
		// languageCode -> language
		if(cdaLanguageCommunication.getLanguageCode() != null && !cdaLanguageCommunication.getLanguageCode().isSetNullFlavor()) {
			fhirCommunication.setLanguage(dtt.tCD2CodeableConcept(cdaLanguageCommunication.getLanguageCode()));
			// urn:ietf:bcp:47 -> language.codeSystem
			fhirCommunication.getLanguage().getCodingFirstRep().setSystem(Constants.DEFAULT_COMMUNICATION_LANGUAGE_CODE_SYSTEM);
		}
		
		// preferenceInd -> preferred
		if(cdaLanguageCommunication.getPreferenceInd() != null && !cdaLanguageCommunication.getPreferenceInd().isSetNullFlavor()) {
			fhirCommunication.setPreferred(dtt.tBL2Boolean(cdaLanguageCommunication.getPreferenceInd()));
		}

		return fhirCommunication;
	}
	
	public Observation.ReferenceRange tReferenceRange2ReferenceRange(org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange) {
		if(cdaReferenceRange == null || cdaReferenceRange.isSetNullFlavor())
			return null;
	
		ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange fhirRefRange = new ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange();
		
		// Notice that we get all the desired information from cdaRefRange.ObservationRange
		// We may think of transforming ObservationRange instead of ReferenceRange
		if(cdaReferenceRange.getObservationRange() != null && !cdaReferenceRange.isSetNullFlavor()) {
		
			// low - high
			if(cdaReferenceRange.getObservationRange().getValue() != null && !cdaReferenceRange.getObservationRange().getValue().isSetNullFlavor()) {
				if(cdaReferenceRange.getObservationRange().getValue() instanceof IVL_PQ) {
					IVL_PQ cdaRefRangeValue = ( (IVL_PQ) cdaReferenceRange.getObservationRange().getValue());
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
			
			// observationRange.interpretationCode -> meaning
			if(cdaReferenceRange.getObservationRange().getInterpretationCode() != null && !cdaReferenceRange.getObservationRange().getInterpretationCode().isSetNullFlavor()) {
				fhirRefRange.setMeaning(dtt.tCD2CodeableConcept(cdaReferenceRange.getObservationRange().getInterpretationCode()));
			}
			
			// text.text -> text
			if(cdaReferenceRange.getObservationRange().getText() != null && !cdaReferenceRange.getObservationRange().getText().isSetNullFlavor()) {
				if(cdaReferenceRange.getObservationRange().getText().getText() != null && !cdaReferenceRange.getObservationRange().getText().getText().isEmpty()) {
					fhirRefRange.setText(cdaReferenceRange.getObservationRange().getText().getText());
				}
			}
		}
		return fhirRefRange;
	}
	
	public Composition.Section tSection2Section(Section cdaSection) {
		if(cdaSection == null || cdaSection.isSetNullFlavor()){
			return null;
		} else {
			Composition.Section fhirSec = new Composition.Section();
			
			// title -> title.text
			if(cdaSection.getTitle() != null && !cdaSection.getTitle().isSetNullFlavor()) {
				if(cdaSection.getTitle().getText() != null && !cdaSection.getTitle().getText().isEmpty()) {
					fhirSec.setTitle(cdaSection.getTitle().getText());
				}
			}
			
			// code -> code
			if(cdaSection.getCode() != null && !cdaSection.getCode().isSetNullFlavor()) {
				fhirSec.setCode(dtt.tCD2CodeableConcept(cdaSection.getCode()));
			}
			
			// text -> text
			if(cdaSection.getText() != null) {
				fhirSec.setText(dtt.tStrucDocText2Narrative(cdaSection.getText()));
			}
			
			return fhirSec;
		}
	}

	public Bundle tClinicalDocument2Composition(ClinicalDocument cdaClinicalDocument) {
		if(cdaClinicalDocument == null || cdaClinicalDocument.isSetNullFlavor())
			return null;

		// create and init the global bundle and the composition resources
		Bundle fhirCompBundle = new Bundle();
		Composition fhirComp = new Composition();
		fhirComp.setId(new IdDt("Composition", getUniqueId()));
		fhirCompBundle.addEntry(new Bundle.Entry().setResource(fhirComp));
		
		// id -> identifier
		if(cdaClinicalDocument.getId() != null && !cdaClinicalDocument.getId().isSetNullFlavor()) {
			fhirComp.setIdentifier(dtt.tII2Identifier(cdaClinicalDocument.getId()));
		}

		// status
		fhirComp.setStatus(Constants.DEFAULT_COMPOSITION_STATUS);
		
		// effectiveTime -> date
		if(cdaClinicalDocument.getEffectiveTime() != null && !cdaClinicalDocument.getEffectiveTime().isSetNullFlavor()) {
			fhirComp.setDate(dtt.tTS2DateTime(cdaClinicalDocument.getEffectiveTime()));
		}
		
		// code -> type
		if(cdaClinicalDocument.getCode() != null && !cdaClinicalDocument.getCode().isSetNullFlavor()) {
			fhirComp.setType(dtt.tCD2CodeableConcept(cdaClinicalDocument.getCode()));
		}

		// title.text -> title
		if(cdaClinicalDocument.getTitle() != null && !cdaClinicalDocument.getTitle().isSetNullFlavor()) {
			if(cdaClinicalDocument.getTitle().getText() != null && !cdaClinicalDocument.getTitle().getText().isEmpty()) {
				fhirComp.setTitle(cdaClinicalDocument.getTitle().getText());
			}
		}
		
		// confidentialityCode -> confidentiality
		if(cdaClinicalDocument.getConfidentialityCode() != null && !cdaClinicalDocument.getConfidentialityCode().isSetNullFlavor()) {
			if(cdaClinicalDocument.getConfidentialityCode().getCode() != null && !cdaClinicalDocument.getConfidentialityCode().getCode().isEmpty()) {
				fhirComp.setConfidentiality(cdaClinicalDocument.getConfidentialityCode().getCode());
			}
		}

		// transform the patient data and assign it to Composition.subject.
		// patient might refer to additional resources such as organization; hence the method returns a bundle.
		Bundle subjectBundle = tPatientRole2Patient(cdaClinicalDocument.getRecordTargets().get(0).getPatientRole());
		for(Bundle.Entry entry : subjectBundle.getEntry()){
			fhirCompBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
			if(entry.getResource() instanceof ca.uhn.fhir.model.dstu2.resource.Patient){
				fhirComp.setSubject(new ResourceReferenceDt(entry.getResource().getId()));
			}
		}
		
		// author.assignedAuthor -> author
		if(cdaClinicalDocument.getAuthors() != null && !cdaClinicalDocument.getAuthors().isEmpty()) {
			for(Author author : cdaClinicalDocument.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					if(author.getAssignedAuthor() != null && !author.getAssignedAuthor().isSetNullFlavor()) {
						Bundle practBundle = tAuthor2Practitioner(author);
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
		
		// legalAuthenticator -> attester[mode = legal]
		if(cdaClinicalDocument.getLegalAuthenticator() != null && !cdaClinicalDocument.getLegalAuthenticator().isSetNullFlavor()) {
			Composition.Attester attester = fhirComp.addAttester();
			attester.setMode(CompositionAttestationModeEnum.LEGAL);
			attester.setTime(dtt.tTS2DateTime(cdaClinicalDocument.getLegalAuthenticator().getTime()));
			Bundle practBundle = tAssignedEntity2Practitioner(cdaClinicalDocument.getLegalAuthenticator().getAssignedEntity());
			for(ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
				// Add all the resources returned from the bundle to the main bundle
				fhirCompBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
				if(entry.getResource() instanceof Practitioner) {
					attester.setParty(new ResourceReferenceDt((entry.getResource()).getId()));
				}
			}
		}

		// authenticator -> attester[mode = professional]
		for(org.openhealthtools.mdht.uml.cda.Authenticator authenticator : cdaClinicalDocument.getAuthenticators()) {
			if(!authenticator.isSetNullFlavor()) {
				Composition.Attester attester = fhirComp.addAttester();
				attester.setMode(CompositionAttestationModeEnum.PROFESSIONAL);
				attester.setTime(dtt.tTS2DateTime(authenticator.getTime()));
				Bundle practBundle = tAssignedEntity2Practitioner(authenticator.getAssignedEntity());
				for (ca.uhn.fhir.model.dstu2.resource.Bundle.Entry entry : practBundle.getEntry()) {
					// Add all the resources returned from the bundle to the main bundle
					fhirCompBundle.addEntry(new Bundle.Entry().setResource(entry.getResource()));
					if (entry.getResource() instanceof Practitioner) {
						attester.setParty(new ResourceReferenceDt((entry.getResource()).getId()));
					}
				}
			}
		}
		
		// custodian.assignedCustodian.representedCustodianOrganization -> custodian
		if(cdaClinicalDocument.getCustodian() != null && !cdaClinicalDocument.getCustodian().isSetNullFlavor()) {
			if(cdaClinicalDocument.getCustodian().getAssignedCustodian() != null && !cdaClinicalDocument.getCustodian().getAssignedCustodian().isSetNullFlavor()) {
				if(cdaClinicalDocument.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization() != null && !cdaClinicalDocument.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization().isSetNullFlavor()) {
					Organization fhirOrganization = tCustodianOrganization2Organization(cdaClinicalDocument.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization());
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
		
		// resource id
		IdDt resourceId = new IdDt("Organization", getUniqueId());
		fhirOrganization.setId(resourceId);
		
		// id -> identifier
		if(cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for(II ii : cdaOrganization.getIds()) {
				if(ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}
		
		// name.text -> name
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
		
		// addr -> address
		if(cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for(AD ad : cdaOrganization.getAddrs()) {
				if(ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}
		
		return fhirOrganization;
	}
	
	public ca.uhn.fhir.model.dstu2.resource.Device tSupply2Device(org.openhealthtools.mdht.uml.cda.Supply cdaSupply) {
		if(cdaSupply == null || cdaSupply.isSetNullFlavor())
			return null;
		
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

		if(productInstance == null)
			return null;

		Device fhirDev = new Device();

		// resource id
		IdDt resourceId = new IdDt("Device", getUniqueId());
		fhirDev.setId(resourceId);
		
		// patient
		fhirDev.setPatient(getPatientRef());

		// productInstance.id -> identifier
		for(II id : productInstance.getIds()) {
			if(!id.isSetNullFlavor())
				fhirDev.addIdentifier(dtt.tII2Identifier(id));
		}

		// productInstance.playingDevice.code -> type
		if(productInstance.getPlayingDevice() != null && !productInstance.getPlayingDevice().isSetNullFlavor()) {
			if(productInstance.getPlayingDevice().getCode() != null && !productInstance.getPlayingDevice().getCode().isSetNullFlavor()) {
				fhirDev.setType(dtt.tCD2CodeableConcept(productInstance.getPlayingDevice().getCode()));
			}
		}
		
		return fhirDev;
	}

	public Bundle tReactionObservation2Observation(ReactionObservation cdaReactionObservation) {
		return tObservation2Observation(cdaReactionObservation);
	}

	public Bundle tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer) {
		if(cdaResultOrganizer == null || cdaResultOrganizer.isSetNullFlavor())
			return null;
		
		DiagnosticReport fhirDiagReport = new DiagnosticReport();
		
		// bundle
		Bundle fhirDiagReportBundle = new Bundle();
		fhirDiagReportBundle.addEntry(new Bundle.Entry().setResource(fhirDiagReport));
		
		// resource id
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
		
		// statusCode -> status
		if(cdaResultOrganizer.getStatusCode() != null && !cdaResultOrganizer.isSetNullFlavor()) {
			fhirDiagReport.setStatus(vst.tResultOrganizerStatusCode2DiagnosticReportStatusEnum(cdaResultOrganizer.getStatusCode().getCode()));
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
		
		// if DiagnosticReport.issued is not set, set the highest value of the effectiveTime to DiagnosticReport.issued
		// effectiveTime.high, low or value -> issued
		if(fhirDiagReport.getIssued() == null) {
			if(cdaResultOrganizer.getEffectiveTime() != null && !cdaResultOrganizer.getEffectiveTime().isSetNullFlavor()) {
				if(cdaResultOrganizer.getEffectiveTime().getHigh() != null && !cdaResultOrganizer.getEffectiveTime().getHigh().isSetNullFlavor()) {
					// effectiveTime.high -> issued
					fhirDiagReport.setIssued(dtt.tTS2Instant(cdaResultOrganizer.getEffectiveTime().getHigh()));
				} else if(cdaResultOrganizer.getEffectiveTime().getLow() != null && !cdaResultOrganizer.getEffectiveTime().getLow().isSetNullFlavor()) {
					// effectiveTime.low -> issued
					fhirDiagReport.setIssued(dtt.tTS2Instant(cdaResultOrganizer.getEffectiveTime().getLow()));
				} else if(cdaResultOrganizer.getEffectiveTime().getValue() != null) {
					// effectiveTime.value -> issued
					TS ts = DatatypesFactory.eINSTANCE.createTS();
					ts.setValue(cdaResultOrganizer.getEffectiveTime().getValue());
					fhirDiagReport.setIssued(dtt.tTS2Instant(ts));
				} 	
			}
		}
		
		// author -> performer
		if(cdaResultOrganizer.getAuthors() != null && !cdaResultOrganizer.getAuthors().isEmpty()) {
			for(org.openhealthtools.mdht.uml.cda.Author author : cdaResultOrganizer.getAuthors()) {
				// Asserting that at most one author exists
				if(author != null && !author.isSetNullFlavor()) {
					Practitioner fhirPractitioner = null;
					Bundle fhirPractitionerBundle = tAuthor2Practitioner(author);

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
		} else {
			// if there is no information about the performer in CDA side, assign an empty Practitioner resource
			// which has data absent reason: unknown
			Practitioner fhirPerformerDataAbsent = new Practitioner();
			fhirPerformerDataAbsent.setId(new IdDt("Practitioner",getUniqueId()));
			ExtensionDt extDataAbsentReason = new ExtensionDt();
			
			// setting dataAbsentReason extension
			extDataAbsentReason.setModifier(false);
			extDataAbsentReason.setUrl(Constants.URL_EXTENSION_DATA_ABSENT_REASON);
			extDataAbsentReason.setValue(Constants.DEFAULT_DIAGNOSTICREPORT_PERFORMER_DATA_ABSENT_REASON_CODE);
			
			// adding dataAbsentReason as undeclaredExtesion to fhirPerformer
			fhirPerformerDataAbsent.addUndeclaredExtension(extDataAbsentReason);
			
			// setting the performer of DiagnosticReport
			fhirDiagReportBundle.addEntry(new Bundle.Entry().setResource(fhirPerformerDataAbsent));
			fhirDiagReport.setPerformer(new ResourceReferenceDt(fhirPerformerDataAbsent.getId()));
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