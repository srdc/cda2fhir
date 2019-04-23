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

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.hl7.fhir.dstu3.model.Age;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCriticality;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.Base64BinaryType;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttestationMode;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttesterComponent;
import org.hl7.fhir.dstu3.model.Composition.CompositionEventComponent;
import org.hl7.fhir.dstu3.model.Composition.DocumentConfidentiality;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Composition.SectionMode;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.Device.FHIRDeviceStatus;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory.FamilyMemberHistoryConditionComponent;
import org.hl7.fhir.dstu3.model.Group;
import org.hl7.fhir.dstu3.model.Group.GroupType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationPractitionerComponent;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationReactionComponent;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationDispense.MedicationDispenseStatus;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestRequesterComponent;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus;
import org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementTaken;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Patient.ContactComponent;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceEntityRole;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.Substance;
import org.hl7.fhir.dstu3.model.Timing;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentType;
import org.hl7.fhir.exceptions.FHIRException;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.DocumentationOf;
import org.openhealthtools.mdht.uml.cda.Entity;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Performer1;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.Product;
import org.openhealthtools.mdht.uml.cda.RecordTarget;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.ServiceEvent;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.AllergyStatusObservation;
import org.openhealthtools.mdht.uml.cda.consol.CommentActivity;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.Instructions;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationDispense;
import org.openhealthtools.mdht.uml.cda.consol.MedicationInformation;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.cda.consol.NonMedicinalSupplyActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProductInstance;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ServiceDeliveryLocation;
import org.openhealthtools.mdht.uml.cda.consol.SeverityObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.entry.impl.DeferredProcedureEncounterReference;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntityResult;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.ReferenceInfo;
import tr.com.srdc.cda2fhir.util.Constants;

public class ResourceTransformerImpl implements IResourceTransformer, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private IDataTypesTransformer dtt;
	private IValueSetsTransformer vst;
	private ICDATransformer cdat;
	private Reference defaultPatientRef;

	private final Logger logger = LoggerFactory.getLogger(ResourceTransformerImpl.class);

	public ResourceTransformerImpl() {
		dtt = new DataTypesTransformerImpl();
		vst = new ValueSetsTransformerImpl();
		cdat = null;
		// This is a default patient reference to be used when IResourceTransformer is
		// not initiated with a ICDATransformer
		defaultPatientRef = new Reference(new IdType("Patient", "0"));
	}

	public ResourceTransformerImpl(ICDATransformer cdaTransformer) {
		this();
		cdat = cdaTransformer;
	}

	protected String getUniqueId() {
		if (cdat != null)
			return cdat.getUniqueId();
		else
			return UUID.randomUUID().toString();
	}

	protected Reference getPatientRef() {
		if (cdat != null)
			return cdat.getPatientRef();
		else
			return defaultPatientRef;
	}

	private List<Identifier> tIIs2Identifiers(EList<II> iis) {
		if (!iis.isEmpty()) {
			List<Identifier> result = new ArrayList<Identifier>();
			for (II ii : iis) {
				if (ii != null && !ii.isSetNullFlavor()) {
					Identifier identifier = dtt.tII2Identifier(ii);
					result.add(identifier);
				}
			}
			return result;
		}
		return null;
	}

	@Override
	public Reference getReference(Resource resource) {
		Reference reference = new Reference(resource.getId());
		String referenceString = ReferenceInfo.getDisplay(resource);
		if (referenceString != null) {
			reference.setDisplay(referenceString);
		}
		reference.setResource(resource);
		return reference;
	}

	@Override
	public Age tAgeObservation2Age(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation) {
		if (cdaAgeObservation == null || cdaAgeObservation.isSetNullFlavor())
			return null;

		Age fhirAge = new Age();

		// value-> age
		if (cdaAgeObservation != null && !cdaAgeObservation.getValues().isEmpty()) {
			for (ANY value : cdaAgeObservation.getValues()) {
				if (value != null && !value.isSetNullFlavor()) {
					if (value instanceof PQ) {
						if (((PQ) value).getValue() != null) {
							// value.value -> value
							fhirAge.setValue(((PQ) value).getValue());
							// value.unit -> unit
							fhirAge.setUnit(((PQ) value).getUnit());
							fhirAge.setSystem("http://unitsofmeasure.org");
						}
					}
				}
			}
		}

		return fhirAge;
	}

	@Override
	public EntryResult tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaAllergyProbAct == null || cdaAllergyProbAct.isSetNullFlavor()) {
			return result;
		}

		AllergyIntolerance fhirAllergyIntolerance = new AllergyIntolerance();
		result.addResource(fhirAllergyIntolerance);

		// resource id
		IdType resourceId = new IdType("AllergyIntolerance", getUniqueId());
		fhirAllergyIntolerance.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirAllergyIntolerance.getMeta().addProfile(Constants.PROFILE_DAF_ALLERGY_INTOLERANCE);

		// id -> identifier
		for (II ii : cdaAllergyProbAct.getIds()) {
			if (!ii.isSetNullFlavor()) {
				fhirAllergyIntolerance.addIdentifier(dtt.tII2Identifier(ii));
			}
		}

		// patient
		fhirAllergyIntolerance.setPatient(getPatientRef());

		// statusCode -> verificationStatus
		if (cdaAllergyProbAct.getStatusCode() != null && !cdaAllergyProbAct.getStatusCode().isSetNullFlavor()) {
			if (cdaAllergyProbAct.getStatusCode().getCode() != null
					&& !cdaAllergyProbAct.getStatusCode().getCode().isEmpty()) {
				AllergyIntoleranceVerificationStatus allergyIntoleranceStatusEnum = vst
						.tStatusCode2AllergyIntoleranceVerificationStatus(cdaAllergyProbAct.getStatusCode().getCode());
				if (allergyIntoleranceStatusEnum != null) {
					fhirAllergyIntolerance.setVerificationStatus(allergyIntoleranceStatusEnum);
				}
			}
		}

		// effectiveTime -> asserted
		if (cdaAllergyProbAct.getEffectiveTime() != null && !cdaAllergyProbAct.getEffectiveTime().isSetNullFlavor()) {

			// low(if not exists, value) -> asserted
			if (cdaAllergyProbAct.getEffectiveTime().getLow() != null
					&& !cdaAllergyProbAct.getEffectiveTime().getLow().isSetNullFlavor()) {
				fhirAllergyIntolerance
						.setAssertedDateElement(dtt.tTS2DateTime(cdaAllergyProbAct.getEffectiveTime().getLow()));
			} else if (cdaAllergyProbAct.getEffectiveTime().getValue() != null
					&& !cdaAllergyProbAct.getEffectiveTime().getValue().isEmpty()) {
				fhirAllergyIntolerance
						.setAssertedDateElement(dtt.tString2DateTime(cdaAllergyProbAct.getEffectiveTime().getValue()));
			}
		}

		// getting allergyObservation
		if (cdaAllergyProbAct.getAllergyObservations() != null
				&& !cdaAllergyProbAct.getAllergyObservations().isEmpty()) {
			for (AllergyObservation cdaAllergyObs : cdaAllergyProbAct.getAllergyObservations()) {
				if (cdaAllergyObs != null && !cdaAllergyObs.isSetNullFlavor()) {

					// allergyObservation.author -> AllergyIntolerance.recorder
					if (cdaAllergyObs.getAuthors() != null && !cdaAllergyObs.getAuthors().isEmpty()) {
						for (Author author : cdaAllergyObs.getAuthors()) {
							if (author != null && !author.isSetNullFlavor()) {
								EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
								result.updateFrom(entityResult);
								if (entityResult.hasPractitioner()) {
									Reference reference = getReference(entityResult.getPractitioner());
									fhirAllergyIntolerance.setRecorder(reference);
								}
							}
						}
					}

					// allergyObservation.participant.participantRole.playingEntity.code -> code
					if (cdaAllergyObs.getParticipants() != null && !cdaAllergyObs.getParticipants().isEmpty()) {
						for (Participant2 participant : cdaAllergyObs.getParticipants()) {
							if (participant != null && !participant.isSetNullFlavor()) {
								if (participant.getParticipantRole() != null
										&& !participant.getParticipantRole().isSetNullFlavor()) {
									if (participant.getParticipantRole().getPlayingEntity() != null
											&& !participant.getParticipantRole().getPlayingEntity().isSetNullFlavor()) {

										if (participant.getParticipantRole().getPlayingEntity().getCode() != null) {
											fhirAllergyIntolerance.setCode(dtt.tCD2CodeableConcept(
													participant.getParticipantRole().getPlayingEntity().getCode()));
										}

										if (participant.getParticipantRole().getPlayingEntity().getNames() != null) {
											List<PN> names = participant.getParticipantRole().getPlayingEntity()
													.getNames();
											if (!names.isEmpty()) {
												if (fhirAllergyIntolerance.getCode() == null) {
													fhirAllergyIntolerance.setCode(new CodeableConcept());
												}
												PN name = names.get(0);
												fhirAllergyIntolerance.getCode().setText(name.getText());
											}
										}

									}
								}
							}
						}
					}

					// allergyObservation.value[@xsi:type='CD'] -> category
					if (cdaAllergyObs.getValues() != null && !cdaAllergyObs.getValues().isEmpty()) {
						for (ANY value : cdaAllergyObs.getValues()) {
							if (value != null && !value.isSetNullFlavor()) {
								if (value instanceof CD) {
									if (vst.tAllergyCategoryCode2AllergyIntoleranceCategory(
											((CD) value).getCode()) != null) {
										fhirAllergyIntolerance
												.addCategory(vst.tAllergyCategoryCode2AllergyIntoleranceCategory(
														((CD) value).getCode()));
									}
								}
							}
						}
					}

					// allergyObservation.value[@xsi:type='CD'] -> type
					if (cdaAllergyObs.getValues() != null && !cdaAllergyObs.getValues().isEmpty()) {
						for (ANY value : cdaAllergyObs.getValues()) {
							if (value != null && !value.isSetNullFlavor()) {
								if (value instanceof CD) {
									if (vst.tAllergyCategoryCode2AllergyIntoleranceType(
											((CD) value).getCode()) != null) {
										fhirAllergyIntolerance.setType(vst
												.tAllergyCategoryCode2AllergyIntoleranceType(((CD) value).getCode()));
									}
								}
							}
						}
					}

					// effectiveTime -> onset
					if (cdaAllergyObs.getEffectiveTime() != null
							&& !cdaAllergyObs.getEffectiveTime().isSetNullFlavor()) {

						// low(if not exists, value) -> onset
						if (cdaAllergyObs.getEffectiveTime().getLow() != null
								&& !cdaAllergyObs.getEffectiveTime().getLow().isSetNullFlavor()) {
							fhirAllergyIntolerance
									.setOnset(dtt.tTS2DateTime(cdaAllergyObs.getEffectiveTime().getLow()));
						} else if (cdaAllergyObs.getEffectiveTime().getValue() != null
								&& !cdaAllergyObs.getEffectiveTime().getValue().isEmpty()) {
							fhirAllergyIntolerance
									.setOnset(dtt.tString2DateTime(cdaAllergyObs.getEffectiveTime().getValue()));
						}
					}

					// searching for reaction observation
					if (cdaAllergyObs.getEntryRelationships() != null
							&& !cdaAllergyObs.getEntryRelationships().isEmpty()) {
						for (EntryRelationship entryRelShip : cdaAllergyObs.getEntryRelationships()) {
							if (entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
								if (entryRelShip.getObservation() != null && !entryRelShip.isSetNullFlavor()) {
									Observation observation = entryRelShip.getObservation();

									// status observation -> clinical status
									if (observation != null && observation instanceof AllergyStatusObservation) {
										observation.getValues().stream().filter(value -> value instanceof CE)
												.map(value -> (CE) value).map(ce -> ce.getCode()).forEach(code -> {
													AllergyIntoleranceClinicalStatus status = vst
															.tProblemStatus2AllergyIntoleranceClinicalStatus(code);
													fhirAllergyIntolerance.setClinicalStatus(status);
												});
									}

									// reaction observation
									if (entryRelShip.getObservation() instanceof ReactionObservation) {

										ReactionObservation cdaReactionObs = (ReactionObservation) entryRelShip
												.getObservation();
										AllergyIntoleranceReactionComponent fhirReaction = fhirAllergyIntolerance
												.addReaction();

										// reactionObservation/value[@xsi:type='CD'] -> reaction.manifestation
										if (cdaReactionObs.getValues() != null
												&& !cdaReactionObs.getValues().isEmpty()) {
											for (ANY value : cdaReactionObs.getValues()) {
												if (value != null && !value.isSetNullFlavor()) {
													if (value instanceof CD) {
														fhirReaction
																.addManifestation(dtt.tCD2CodeableConcept((CD) value));
													}
												}
											}
										}
										// reactionObservation/low -> reaction.onset
										if (cdaReactionObs.getEffectiveTime() != null
												&& !cdaReactionObs.getEffectiveTime().isSetNullFlavor()) {
											if (cdaReactionObs.getEffectiveTime().getLow() != null
													&& !cdaReactionObs.getEffectiveTime().getLow().isSetNullFlavor()) {
												fhirReaction.setOnsetElement(dtt.tString2DateTime(
														cdaReactionObs.getEffectiveTime().getLow().getValue()));
											}
										}

										// severityObservation/value[@xsi:type='CD'].code -> severity
										if (cdaReactionObs.getSeverityObservation() != null
												&& !cdaReactionObs.getSeverityObservation().isSetNullFlavor()) {
											SeverityObservation cdaSeverityObs = cdaReactionObs
													.getSeverityObservation();
											if (cdaSeverityObs.getValues() != null
													&& !cdaSeverityObs.getValues().isEmpty()) {
												for (ANY value : cdaSeverityObs.getValues()) {
													if (value != null && !value.isSetNullFlavor()) {
														if (value instanceof CD) {
															if (vst.tSeverityCode2AllergyIntoleranceSeverity(
																	((CD) value).getCode()) != null) {
																fhirReaction.setSeverity(
																		vst.tSeverityCode2AllergyIntoleranceSeverity(
																				((CD) value).getCode()));
															}
														}
													}
												}
											}
										}
									}

									// criticality observation. found by checking the templateId
									// entryRelationship.observation[templateId/@root='2.16.840.1.113883.10.20.22.4.145'].value[CD].code
									// -> criticality
									if (entryRelShip.getObservation().getTemplateIds() != null
											&& !entryRelShip.getObservation().getTemplateIds().isEmpty()) {
										for (II templateId : entryRelShip.getObservation().getTemplateIds()) {
											if (templateId.getRoot() != null && templateId.getRoot()
													.equals("2.16.840.1.113883.10.20.22.4.145")) {
												org.openhealthtools.mdht.uml.cda.Observation cdaCriticalityObservation = entryRelShip
														.getObservation();
												for (ANY value : cdaCriticalityObservation.getValues()) {
													if (value != null && !value.isSetNullFlavor()) {
														if (value instanceof CD) {
															AllergyIntoleranceCriticality allergyIntoleranceCriticalityEnum = vst
																	.tCriticalityObservationValue2AllergyIntoleranceCriticality(
																			((CD) value).getCode());
															if (allergyIntoleranceCriticalityEnum != null) {
																fhirAllergyIntolerance.setCriticality(
																		allergyIntoleranceCriticalityEnum);
															}
														}
													}
												}
												// since we already found the desired templateId, we may break the
												// searching for templateId to avoid containing duplicate observations
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

		return result;
	}

	@Override
	public EntityResult tAssignedAuthor2Device(AssignedAuthor cdaAssignedAuthor, IBundleInfo bundleInfo) {
		if (cdaAssignedAuthor == null || cdaAssignedAuthor.isSetNullFlavor()) {
			return new EntityResult();
		}

		List<II> ids = null;
		if (cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty()) {
			for (II ii : cdaAssignedAuthor.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					if (ids == null) {
						ids = new ArrayList<II>();
					}
					ids.add(ii);
				}
			}
		}

		if (ids != null) {
			IEntityInfo existingInfo = bundleInfo.findEntityResult(ids);
			if (existingInfo != null) {
				return new EntityResult(existingInfo);
			}
		}

		EntityInfo info = new EntityInfo();
		Device fhirDevice = new Device();

		// id -> identifier
		if (ids != null) {
			for (II id : ids) {
				fhirDevice.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// resource id
		IdType resourceDeviceId = new IdType("Device", getUniqueId());
		fhirDevice.setId(resourceDeviceId);

		// All things with the Device.
		if (cdaAssignedAuthor.getAssignedAuthoringDevice() != null
				&& !cdaAssignedAuthor.getAssignedAuthoringDevice().isSetNullFlavor()) {
			fhirDevice.setManufacturer(
					cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName().getText());
			fhirDevice.setVersion(cdaAssignedAuthor.getAssignedAuthoringDevice().getSoftwareName().getText());
			if (cdaAssignedAuthor.getAssignedAuthoringDevice().getCode() != null) {
				fhirDevice.setType(dtt.tCD2CodeableConcept(cdaAssignedAuthor.getAssignedAuthoringDevice().getCode()));
			} else {
				Coding cd1 = new Coding();
				cd1.setCode(cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName().getCode());
				if (cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName()
						.getDisplayName() == null) {
					cd1.setDisplay(cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName().getText());
				} else {
					cd1.setDisplay(
							cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName().getDisplayName());
				}
				cd1.setSystem(
						cdaAssignedAuthor.getAssignedAuthoringDevice().getManufacturerModelName().getCodeSystem());
				fhirDevice.setType(new CodeableConcept().addCoding(cd1));
			}

		}

		info.setDevice(fhirDevice);

		Organization fhirOrganization;
		// representedOrganization -> EntityInfo.organization
		if (cdaAssignedAuthor.getRepresentedOrganization() != null
				&& !cdaAssignedAuthor.getRepresentedOrganization().isSetNullFlavor()) {
			fhirOrganization = tOrganization2Organization(cdaAssignedAuthor.getRepresentedOrganization());
			info.setOrganization(fhirOrganization);
			fhirDevice.setOwner(new Reference(fhirOrganization.getId()));
		}

		return new EntityResult(info, ids);
	}

	@Override
	public EntityResult tAssignedAuthor2Practitioner(AssignedAuthor cdaAssignedAuthor, IBundleInfo bundleInfo) {
		if (cdaAssignedAuthor == null || cdaAssignedAuthor.isSetNullFlavor()) {
			return new EntityResult();
		}

		List<II> ids = null;
		if (cdaAssignedAuthor.getIds() != null && !cdaAssignedAuthor.getIds().isEmpty()) {
			for (II ii : cdaAssignedAuthor.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					if (ids == null) {
						ids = new ArrayList<II>();
					}
					ids.add(ii);
				}
			}
		}

		if (ids != null) {
			IEntityInfo existingInfo = bundleInfo.findEntityResult(ids);
			if (existingInfo != null) {
				return new EntityResult(existingInfo);
			}
		}

		EntityInfo info = new EntityInfo();

		Practitioner fhirPractitioner = new Practitioner();
		info.setPractitioner(fhirPractitioner);

		// resource id
		IdType resourceId = new IdType("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);

		// id -> identifier
		if (ids != null) {
			for (II id : ids) {
				fhirPractitioner.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirPractitioner.getMeta().addProfile(Constants.PROFILE_DAF_PRACTITIONER);

		// assignedPerson.name -> name
		if (cdaAssignedAuthor.getAssignedPerson() != null && !cdaAssignedAuthor.getAssignedPerson().isSetNullFlavor()) {
			if (cdaAssignedAuthor.getAssignedPerson().getNames() != null
					&& !cdaAssignedAuthor.getAssignedPerson().getNames().isEmpty()) {
				for (PN pn : cdaAssignedAuthor.getAssignedPerson().getNames()) {
					if (pn != null && !pn.isSetNullFlavor()) {
						fhirPractitioner.addName(dtt.tEN2HumanName(pn));
					}
				}
			}
		}

		// addr -> address
		if (cdaAssignedAuthor.getAddrs() != null && !cdaAssignedAuthor.getAddrs().isEmpty()) {
			for (AD ad : cdaAssignedAuthor.getAddrs()) {
				if (ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}

		// telecom -> telecom
		if (cdaAssignedAuthor.getTelecoms() != null && !cdaAssignedAuthor.getTelecoms().isEmpty()) {
			for (TEL tel : cdaAssignedAuthor.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// Adding a practitionerRole
		// Practitioner.PractitionerRole fhirPractitionerRole =
		// fhirPractitioner.addPractitionerRole();
		PractitionerRole fhirPractitionerRole = new PractitionerRole();
		fhirPractitionerRole.setId(new IdType("PractitionerRole", getUniqueId()));

		// code -> practitionerRole.role
		if (cdaAssignedAuthor.getCode() != null && !cdaAssignedAuthor.isSetNullFlavor()) {
			// fhirPractitionerRole.setRole(dtt.tCD2CodeableConcept(cdaAssignedAuthor.getCode()));
			fhirPractitionerRole.addCode(dtt.tCD2CodeableConcept(cdaAssignedAuthor.getCode()));
		}

		// representedOrganization -> practitionerRole.managingOrganization
		if (cdaAssignedAuthor.getRepresentedOrganization() != null
				&& !cdaAssignedAuthor.getRepresentedOrganization().isSetNullFlavor()) {
			org.hl7.fhir.dstu3.model.Organization fhirOrganization = tOrganization2Organization(
					cdaAssignedAuthor.getRepresentedOrganization());
			fhirPractitionerRole.setOrganization(getReference(fhirOrganization));
			fhirPractitionerRole.setPractitioner(getReference(fhirPractitioner));

			info.setPractitionerRole(fhirPractitionerRole);
			info.setOrganization(fhirOrganization);
		}

		return new EntityResult(info, ids);
	}

	@Override
	public EntityResult tAssignedEntity2Practitioner(AssignedEntity cdaAssignedEntity, IBundleInfo bundleInfo) {
		if (cdaAssignedEntity == null || cdaAssignedEntity.isSetNullFlavor()) {
			return new EntityResult();
		}

		List<II> ids = null;
		if (cdaAssignedEntity.getIds() != null && !cdaAssignedEntity.getIds().isEmpty()) {
			for (II ii : cdaAssignedEntity.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					if (ids == null) {
						ids = new ArrayList<II>();
					}
					ids.add(ii);
				}
			}
		}

		if (ids != null) {
			IEntityInfo existingInfo = bundleInfo.findEntityResult(ids);
			if (existingInfo != null) {
				return new EntityResult(existingInfo);
			}
		}

		EntityInfo info = new EntityInfo();

		Practitioner fhirPractitioner = new Practitioner();
		info.setPractitioner(fhirPractitioner);

		// resource id
		IdType resourceId = new IdType("Practitioner", getUniqueId());
		fhirPractitioner.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirPractitioner.getMeta().addProfile(Constants.PROFILE_DAF_PRACTITIONER);

		// id -> identifier
		if (ids != null) {
			for (II id : ids) {
				fhirPractitioner.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// assignedPerson.name -> name
		if (cdaAssignedEntity.getAssignedPerson() != null && !cdaAssignedEntity.getAssignedPerson().isSetNullFlavor()) {
			for (PN pn : cdaAssignedEntity.getAssignedPerson().getNames()) {
				if (pn != null && !pn.isSetNullFlavor()) {
					fhirPractitioner.addName(dtt.tEN2HumanName(pn));
				}
			}
		}

		// addr -> address
		if (cdaAssignedEntity.getAddrs() != null && !cdaAssignedEntity.getAddrs().isEmpty()) {
			for (AD ad : cdaAssignedEntity.getAddrs()) {
				if (ad != null && !ad.isSetNullFlavor()) {
					fhirPractitioner.addAddress(dtt.AD2Address(ad));
				}
			}
		}

		// telecom -> telecom
		if (cdaAssignedEntity.getTelecoms() != null && !cdaAssignedEntity.getTelecoms().isEmpty()) {
			for (TEL tel : cdaAssignedEntity.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirPractitioner.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// Practitioner.PractitionerRole fhirPractitionerRole =
		// fhirPractitioner.addPractitionerRole();
		PractitionerRole fhirPractitionerRole = new PractitionerRole();
		fhirPractitionerRole.setId(new IdType("PractitionerRole", getUniqueId()));

		// code -> practitionerRole.role
		if (cdaAssignedEntity.getCode() != null && !cdaAssignedEntity.isSetNullFlavor()) {
			// fhirPractitionerRole.setRole(dtt.tCD2CodeableConcept(cdaAssignedEntity.getCode()));
			fhirPractitionerRole.addCode(dtt.tCD2CodeableConcept(cdaAssignedEntity.getCode()));
		}

		// representedOrganization -> practitionerRole.organization
		// NOTE: we skipped multiple instances of represented organization; we just omit
		// apart from the first
		if (!cdaAssignedEntity.getRepresentedOrganizations().isEmpty()) {
			if (cdaAssignedEntity.getRepresentedOrganizations().get(0) != null
					&& !cdaAssignedEntity.getRepresentedOrganizations().get(0).isSetNullFlavor()) {
				org.hl7.fhir.dstu3.model.Organization fhirOrganization = tOrganization2Organization(
						cdaAssignedEntity.getRepresentedOrganizations().get(0));

				fhirPractitionerRole.setOrganization(getReference(fhirOrganization));
				fhirPractitionerRole.setPractitioner(getReference(fhirPractitioner));

				info.setPractitionerRole(fhirPractitionerRole);
				info.setOrganization(fhirOrganization);
			}
		}

		return new EntityResult(info, ids);
	}

	@Override
	public EntityResult tAuthor2Practitioner(org.openhealthtools.mdht.uml.cda.Author cdaAuthor,
			IBundleInfo bundleInfo) {
		if (cdaAuthor == null || cdaAuthor.isSetNullFlavor()) {
			return new EntityResult();
		}

		if (cdaAuthor.getAssignedAuthor() == null || cdaAuthor.getAssignedAuthor().isSetNullFlavor()) {
			return new EntityResult();
		}

		return tAssignedAuthor2Practitioner(cdaAuthor.getAssignedAuthor(), bundleInfo);
	}

	@Override
	public Substance tCD2Substance(CD cdaSubstanceCode) {
		if (cdaSubstanceCode == null || cdaSubstanceCode.isSetNullFlavor())
			return null;

		Substance fhirSubstance = new Substance();

		// resource id
		fhirSubstance.setId(new IdType("Substance", getUniqueId()));

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirSubstance.getMeta().addProfile(Constants.PROFILE_DAF_SUBSTANCE);

		// code -> code
		fhirSubstance.setCode(dtt.tCD2CodeableConcept(cdaSubstanceCode));

		return fhirSubstance;
	}

	@Override
	public EntryResult tClinicalDocument2Composition(ClinicalDocument cdaClinicalDocument) {
		return tClinicalDocument2Bundle(cdaClinicalDocument, true);
	}

	@Override
	public EntryResult tClinicalDocument2Bundle(ClinicalDocument cdaClinicalDocument, boolean includeComposition) {
		EntryResult result = new EntryResult();

		if (cdaClinicalDocument == null || cdaClinicalDocument.isSetNullFlavor()) {
			return result;
		}

		// create and init the global bundle and the composition resources
		Composition fhirComp = includeComposition ? new Composition() : null;

		if (fhirComp != null) {
			fhirComp.setId(new IdType("Composition", getUniqueId()));
			result.addResource(fhirComp);

			CodeableConcept classConcept = new CodeableConcept();
			Coding classCoding = new Coding("http://hl7.org/fhir/ValueSet/doc-classcodes", "LP173421-7", "Note");
			classConcept.addCoding(classCoding);
			fhirComp.setClass_(classConcept);

			// id -> identifier
			if (cdaClinicalDocument.getId() != null && !cdaClinicalDocument.getId().isSetNullFlavor()) {
				fhirComp.setIdentifier(dtt.tII2Identifier(cdaClinicalDocument.getId()));
			}

			// status
			fhirComp.setStatus(Config.DEFAULT_COMPOSITION_STATUS);

			// effectiveTime -> date
			if (cdaClinicalDocument.getEffectiveTime() != null
					&& !cdaClinicalDocument.getEffectiveTime().isSetNullFlavor()) {
				fhirComp.setDateElement(dtt.tTS2DateTime(cdaClinicalDocument.getEffectiveTime()));
			}

			// code -> type
			if (cdaClinicalDocument.getCode() != null && !cdaClinicalDocument.getCode().isSetNullFlavor()) {
				fhirComp.setType(dtt.tCD2CodeableConcept(cdaClinicalDocument.getCode()));
			}

			// title.text -> title
			if (cdaClinicalDocument.getTitle() != null && !cdaClinicalDocument.getTitle().isSetNullFlavor()) {
				if (cdaClinicalDocument.getTitle().getText() != null
						&& !cdaClinicalDocument.getTitle().getText().isEmpty()) {
					fhirComp.setTitle(cdaClinicalDocument.getTitle().getText());
				}
			}

			// confidentialityCode -> confidentiality
			if (cdaClinicalDocument.getConfidentialityCode() != null
					&& !cdaClinicalDocument.getConfidentialityCode().isSetNullFlavor()) {
				if (cdaClinicalDocument.getConfidentialityCode().getCode() != null
						&& !cdaClinicalDocument.getConfidentialityCode().getCode().isEmpty()) {
					// fhirComp.setConfidentiality(cdaClinicalDocument.getConfidentialityCode().getCode());
					try {
						fhirComp.setConfidentiality(DocumentConfidentiality
								.fromCode(cdaClinicalDocument.getConfidentialityCode().getCode()));
					} catch (FHIRException e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
		}

		BundleInfo bundleInfo = new BundleInfo(this);

		EList<RecordTarget> recordTargets = cdaClinicalDocument.getRecordTargets();
		if (recordTargets != null && !recordTargets.isEmpty()) { // Support empty for testing purposes. We might need a
																	// flag here not to include patient in the bundle as
																	// well in future
			// transform the patient data and assign it to Composition.subject.
			// patient might refer to additional resources such as organization; hence the
			// method returns a bundle.
			Bundle subjectBundle = tPatientRole2Patient(cdaClinicalDocument.getRecordTargets().get(0).getPatientRole());
			for (BundleEntryComponent entry : subjectBundle.getEntry()) {
				result.addResource(entry.getResource());
				if (fhirComp != null && entry.getResource() instanceof org.hl7.fhir.dstu3.model.Patient) {
					fhirComp.setSubject(getReference(entry.getResource()));
				}
			}
		}

		// author.assignedAuthor -> author
		if (cdaClinicalDocument.getAuthors() != null && !cdaClinicalDocument.getAuthors().isEmpty()) {
			for (Author author : cdaClinicalDocument.getAuthors()) {
				// Asserting that at most one author exists
				if (author != null && !author.isSetNullFlavor()) {
					if (author.getAssignedAuthor() != null && !author.getAssignedAuthor().isSetNullFlavor()) {
						if (author.getAssignedAuthor().getAssignedPerson() != null
								&& !author.getAssignedAuthor().getAssignedPerson().isSetNullFlavor()) {
							EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
							result.updateFrom(entityResult);
							bundleInfo.updateFrom(entityResult);
							if (fhirComp != null && entityResult.hasPractitioner()) {
								fhirComp.addAuthor().setReference(entityResult.getPractitionerId());
								String referenceString = ReferenceInfo.getDisplay(entityResult.getPractitioner());
								if (referenceString != null) {
									fhirComp.getAuthor().get(0).setDisplay(referenceString);
								}
							}
						} else if (author.getAssignedAuthor().getAssignedAuthoringDevice() != null
								&& author.getAssignedAuthor().getRepresentedOrganization() != null
								&& !author.getAssignedAuthor().getRepresentedOrganization().isSetNullFlavor()
								&& !author.getAssignedAuthor().getAssignedAuthoringDevice().isSetNullFlavor()) {
							EntityResult entityResult = tAssignedAuthor2Device(author.getAssignedAuthor(), bundleInfo);
							result.updateFrom(entityResult);
							bundleInfo.updateFrom(entityResult);
							result.addResource(entityResult.getDevice()); // Device added separately because updateFrom
																			// ignores it.
							if (fhirComp != null && entityResult.hasDevice() && entityResult.hasOrganization()) {
								fhirComp.getAuthor().add(new Reference(entityResult.getDeviceId()));
							}
						}
					}
				}
			}
		}

		// legalAuthenticator -> attester[mode = legal]
		if (cdaClinicalDocument.getLegalAuthenticator() != null
				&& !cdaClinicalDocument.getLegalAuthenticator().isSetNullFlavor()) {
			CompositionAttesterComponent attester = fhirComp != null ? fhirComp.addAttester() : null;
			if (attester != null) {
				attester.addMode(CompositionAttestationMode.LEGAL);
				attester.setTimeElement(dtt.tTS2DateTime(cdaClinicalDocument.getLegalAuthenticator().getTime()));
			}
			EntityResult entityResult = tAssignedEntity2Practitioner(
					cdaClinicalDocument.getLegalAuthenticator().getAssignedEntity(), bundleInfo);
			result.updateFrom(entityResult);
			bundleInfo.updateFrom(entityResult);
			Reference reference = getReference(entityResult.getPractitioner());
			if (attester != null && reference != null) {
				attester.setParty(reference);
			}
		}

		// authenticator -> attester[mode = professional]
		for (org.openhealthtools.mdht.uml.cda.Authenticator authenticator : cdaClinicalDocument.getAuthenticators()) {
			if (!authenticator.isSetNullFlavor()) {
				CompositionAttesterComponent attester = fhirComp != null ? fhirComp.addAttester() : null;
				if (attester != null) {
					attester.addMode(CompositionAttestationMode.PROFESSIONAL);
					attester.setTimeElement(dtt.tTS2DateTime(authenticator.getTime()));
				}
				EntityResult entityResult = tAssignedEntity2Practitioner(authenticator.getAssignedEntity(), bundleInfo);
				result.updateFrom(entityResult);
				bundleInfo.updateFrom(entityResult);
				Reference reference = getReference(entityResult.getPractitioner());
				if (attester != null && reference != null) {
					attester.setParty(reference);
				}
			}
		}

		for (DocumentationOf docOf : cdaClinicalDocument.getDocumentationOfs()) {
			if (docOf.getServiceEvent() != null && fhirComp != null) {
				ServiceEvent cdaServiceEvent = docOf.getServiceEvent();
				CompositionEventComponent event = new CompositionEventComponent();

				fhirComp.addEvent(event);
				// documentationOf.serviceEvent.effectiveTime => event.period
				if (cdaServiceEvent.getEffectiveTime() != null) {
					Period period = dtt.tIVL_TS2Period(cdaServiceEvent.getEffectiveTime());
					event.setPeriod(period);
				}

				// documentationOf.serviceEvent.assignedEntity -> composition.event.detail
				if (cdaServiceEvent.getPerformers() != null && !cdaServiceEvent.getPerformers().isEmpty()) {
					for (Performer1 performer : cdaServiceEvent.getPerformers()) {
						if (performer.getAssignedEntity() != null) {
							EntityResult entityResult = tPerformer12Practitioner(performer, bundleInfo);
							if (entityResult.hasPractitioner()) {
								result.updateFrom(entityResult);
								Reference reference = getReference(entityResult.getPractitioner());
								event.addDetail(reference);
							}
						}

					}
				}
			}
		}
		// custodian.assignedCustodian.representedCustodianOrganization -> custodian
		if (cdaClinicalDocument.getCustodian() != null && !cdaClinicalDocument.getCustodian().isSetNullFlavor()) {
			if (cdaClinicalDocument.getCustodian().getAssignedCustodian() != null
					&& !cdaClinicalDocument.getCustodian().getAssignedCustodian().isSetNullFlavor()) {
				if (cdaClinicalDocument.getCustodian().getAssignedCustodian()
						.getRepresentedCustodianOrganization() != null
						&& !cdaClinicalDocument.getCustodian().getAssignedCustodian()
								.getRepresentedCustodianOrganization().isSetNullFlavor()) {
					org.hl7.fhir.dstu3.model.Organization fhirOrganization = tCustodianOrganization2Organization(
							cdaClinicalDocument.getCustodian().getAssignedCustodian()
									.getRepresentedCustodianOrganization());
					if (fhirComp != null) {
						fhirComp.setCustodian(getReference(fhirOrganization));
					}
					result.addResource(fhirOrganization);
					;
				}
			}
		}

		return result;
	}

	@Override
	public org.hl7.fhir.dstu3.model.Organization tCustodianOrganization2Organization(
			org.openhealthtools.mdht.uml.cda.CustodianOrganization cdaOrganization) {
		if (cdaOrganization == null || cdaOrganization.isSetNullFlavor())
			return null;

		org.hl7.fhir.dstu3.model.Organization fhirOrganization = new org.hl7.fhir.dstu3.model.Organization();

		// resource id
		IdType resourceId = new IdType("Organization", getUniqueId());
		fhirOrganization.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirOrganization.getMeta().addProfile(Constants.PROFILE_DAF_ORGANIZATION);

		// id -> identifier
		if (cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for (II ii : cdaOrganization.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// name.text -> name
		if (cdaOrganization.getName() != null && !cdaOrganization.getName().isSetNullFlavor()) {
			fhirOrganization.setName(cdaOrganization.getName().getText());
		}

		// telecom -> telecom
		if (cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty()) {
			for (TEL tel : cdaOrganization.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirOrganization.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// addr -> address
		if (cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for (AD ad : cdaOrganization.getAddrs()) {
				if (ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}

		return fhirOrganization;
	}

	@Override
	public EntryResult tEncounterActivity2Encounter(
			org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaEncounterActivity, IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaEncounterActivity == null || cdaEncounterActivity.isSetNullFlavor()) {
			return result;
		}

		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = new org.hl7.fhir.dstu3.model.Encounter();
		result.addResource(fhirEncounter);

		// NOTE: hospitalization.period not found. However, daf requires it being mapped

		// resource id
		IdType resourceId = new IdType("Encounter", getUniqueId());
		fhirEncounter.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirEncounter.getMeta().addProfile(Constants.PROFILE_DAF_ENCOUNTER);

		// subject
		fhirEncounter.setSubject(getPatientRef());

		// id -> identifier
		if (cdaEncounterActivity.getIds() != null && !cdaEncounterActivity.getIds().isEmpty()) {
			for (II id : cdaEncounterActivity.getIds()) {
				if (id != null && !id.isSetNullFlavor()) {
					fhirEncounter.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// statusCode -> status
		if (cdaEncounterActivity.getStatusCode() != null && !cdaEncounterActivity.getStatusCode().isSetNullFlavor()) {
			if (vst.tStatusCode2EncounterStatusEnum(cdaEncounterActivity.getStatusCode().getCode()) != null) {
				fhirEncounter
						.setStatus(vst.tStatusCode2EncounterStatusEnum(cdaEncounterActivity.getStatusCode().getCode()));
			}
		} else {
			fhirEncounter.setStatus(Config.DEFAULT_ENCOUNTER_STATUS);
		}

		// code -> type
		if (cdaEncounterActivity.getCode() != null) {
			fhirEncounter
					.addType(dtt.tCD2CodeableConcept(cdaEncounterActivity.getCode(), bundleInfo.getIdedAnnotations()));
		}

		// code.translation -> classElement
		if (cdaEncounterActivity.getCode() != null && !cdaEncounterActivity.getCode().isSetNullFlavor()) {
			if (cdaEncounterActivity.getCode().getTranslations() != null
					&& !cdaEncounterActivity.getCode().getTranslations().isEmpty()) {
				for (CD cd : cdaEncounterActivity.getCode().getTranslations()) {
					if (cd != null && !cd.isSetNullFlavor()) {
						Coding encounterClass = vst.tEncounterCode2EncounterCode(cd.getCode());
						if (encounterClass != null) {
							fhirEncounter.setClass_(encounterClass);
						}
					}
				}
			}
		}

		// priorityCode -> priority
		if (cdaEncounterActivity.getPriorityCode() != null
				&& !cdaEncounterActivity.getPriorityCode().isSetNullFlavor()) {
			fhirEncounter.setPriority(dtt.tCD2CodeableConcept(cdaEncounterActivity.getPriorityCode()));
		}

		// performer -> participant.individual
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		if (cdaEncounterActivity.getPerformers() != null && !cdaEncounterActivity.getPerformers().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaEncounterActivity.getPerformers()) {
				if (cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					EntityResult entityResult = tPerformer22Practitioner(cdaPerformer, localBundleInfo);
					result.updateFrom(entityResult);
					localBundleInfo.updateFrom(result);
					if (entityResult.hasPractitioner()) {
						EncounterParticipantComponent fhirParticipant = new EncounterParticipantComponent();
						// default encounter participant type code
						fhirParticipant.addType().addCoding(Config.DEFAULT_ENCOUNTER_PARTICIPANT_TYPE_CODE);
						fhirParticipant.setIndividual(getReference(entityResult.getPractitioner()));
						fhirEncounter.addParticipant(fhirParticipant);
					}
				}
			}
		}

		// effectiveTime -> period
		if (cdaEncounterActivity.getEffectiveTime() != null
				&& !cdaEncounterActivity.getEffectiveTime().isSetNullFlavor()) {
			fhirEncounter.setPeriod(dtt.tIVL_TS2Period(cdaEncounterActivity.getEffectiveTime()));
		}

		// indication -> diagnosis.condition
		for (Indication cdaIndication : cdaEncounterActivity.getIndications()) {
			if (!cdaIndication.isSetNullFlavor()) {
				Condition fhirIndication = tIndication2ConditionEncounter(cdaIndication, bundleInfo);
				result.addResource(fhirIndication);
				// TODO: check if this is correct mapping
				// Reference indicationRef = fhirEncounter.addIndication();
				// indicationRef.setReference(fhirIndication.getId());
				fhirEncounter.addDiagnosis().setCondition(getReference(fhirIndication));
			}
		}

		// serviceDeliveryLocation -> location.location
		// Although encounter contains serviceDeliveryLocation,
		// getServiceDeliveryLocation method returns empty list
		// Therefore, get the location information from
		// participant[@typeCode='LOC'].participantRole
//		if(cdaEncounterActivity.getServiceDeliveryLocations() != null && !cdaEncounterActivity.getServiceDeliveryLocations().isEmpty()) {
//			for(ServiceDeliveryLocation SDLOC : cdaEncounterActivity.getServiceDeliveryLocations()) {
//				if(SDLOC != null && !SDLOC.isSetNullFlavor()) {
//					org.hl7.fhir.dstu3.model.Location fhirLocation = tServiceDeliveryLocation2Location(SDLOC);
//					fhirEncounterBundle.addEntry(new BundleEntryComponent().setResource(fhirLocation));
//					fhirEncounter.addLocation().setLocation(new Reference(fhirLocation.getId()));
//				}
//			}
//		}

		// participant[@typeCode='LOC'].participantRole[SDLOC] -> location
		if (cdaEncounterActivity.getParticipants() != null && !cdaEncounterActivity.getParticipants().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Participant2 cdaParticipant : cdaEncounterActivity
					.getParticipants()) {
				if (cdaParticipant != null && !cdaParticipant.isSetNullFlavor()) {

					// checking if the participant is location
					if (cdaParticipant.getTypeCode() == ParticipationType.LOC) {
						if (cdaParticipant.getParticipantRole() != null
								&& !cdaParticipant.getParticipantRole().isSetNullFlavor()) {
							if (cdaParticipant.getParticipantRole().getClassCode() != null
									&& cdaParticipant.getParticipantRole().getClassCode() == RoleClassRoot.SDLOC) {
								// We first make the mapping to a resource.location
								// then, we create a resource.encounter.location
								// then, we add the resource.location to resource.encounter.location

								// usage of ParticipantRole2Location
								org.hl7.fhir.dstu3.model.Location fhirLocation = tParticipantRole2Location(
										cdaParticipant.getParticipantRole());
								result.addResource(fhirLocation);
								fhirEncounter.addLocation().setLocation(getReference(fhirLocation));
							}
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public Group tEntity2Group(Entity cdaEntity) {
		// never used
		if (cdaEntity == null || cdaEntity.isSetNullFlavor())
			return null;
		else if (cdaEntity.getDeterminerCode() != org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer.KIND)
			return null;

		Group fhirGroup = new Group();

		// id -> identifier
		if (cdaEntity.getIds() != null && !cdaEntity.getIds().isEmpty()) {
			for (II id : cdaEntity.getIds()) {
				if (id != null && !id.isSetNullFlavor()) {
					if (id.getDisplayable()) {
						// unique
						fhirGroup.addIdentifier(dtt.tII2Identifier(id));
					}
				}
			}
		}

		// classCode -> type
		if (cdaEntity.getClassCode() != null) {
			GroupType groupTypeEnum = vst.tEntityClassRoot2GroupType(cdaEntity.getClassCode());
			if (groupTypeEnum != null) {
				fhirGroup.setType(groupTypeEnum);
			}

		}

		// deteminerCode -> actual
		if (cdaEntity.isSetDeterminerCode() && cdaEntity.getDeterminerCode() != null) {
			if (cdaEntity.getDeterminerCode() == EntityDeterminer.KIND) {
				fhirGroup.setActual(false);
			} else {
				fhirGroup.setActual(true);
			}
		}

		// code -> code
		if (cdaEntity.getCode() != null && !cdaEntity.getCode().isSetNullFlavor()) {
			fhirGroup.setCode(dtt.tCD2CodeableConcept(cdaEntity.getCode()));
		}

		return fhirGroup;
	}

	@Override
	public FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFHO) {
		if (cdaFHO == null || cdaFHO.isSetNullFlavor())
			return null;

		FamilyMemberHistory fhirFMH = new FamilyMemberHistory();

		// resource id
		IdType resourceId = new IdType("FamilyMemberHistory", getUniqueId());
		fhirFMH.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirFMH.getMeta().addProfile(Constants.PROFILE_DAF_FAMILY_MEMBER_HISTORY);

		// patient
		fhirFMH.setPatient(getPatientRef());

		// id -> identifier
		for (II id : cdaFHO.getIds()) {
			if (id != null && !id.isSetNullFlavor()) {
				fhirFMH.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// statusCode -> status
		if (cdaFHO.getStatusCode() != null && !cdaFHO.getStatusCode().isSetNullFlavor()) {
			fhirFMH.setStatus(
					vst.tFamilyHistoryOrganizerStatusCode2FamilyHistoryStatus(cdaFHO.getStatusCode().getCode()));
		}

		// condition <-> familyHistoryObservation
		// also, deceased value is set by looking at
		// familyHistoryObservation.familyHistoryDeathObservation
		if (cdaFHO.getFamilyHistoryObservations() != null && !cdaFHO.getFamilyHistoryObservations().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryObservation familyHistoryObs : cdaFHO
					.getFamilyHistoryObservations()) {
				if (familyHistoryObs != null && !familyHistoryObs.isSetNullFlavor()) {

					// adding a new condition to fhirFMH
					FamilyMemberHistoryConditionComponent condition = fhirFMH.addCondition();

					// familyHistoryObservation.value[@xsi:type='CD'] -> code
					for (ANY value : familyHistoryObs.getValues()) {
						if (value != null && !value.isSetNullFlavor()) {
							if (value instanceof CD) {
								condition.setCode(dtt.tCD2CodeableConcept((CD) value));
							}
						}
					}

					// NOTE: An alternative is to use the relatedSubject/subject/sdtc:deceasedInd
					// and relatedSubject/subject/sdtc:deceasedTime values
					// deceased
					if (familyHistoryObs.getFamilyHistoryDeathObservation() != null
							&& !familyHistoryObs.getFamilyHistoryDeathObservation().isSetNullFlavor()) {
						// if deathObservation exists, set fmh.deceased true
						fhirFMH.setDeceased(new BooleanType(true));

						// familyHistoryDeathObservation.value[@xsi:type='CD'] -> condition.outcome
						for (ANY value : familyHistoryObs.getFamilyHistoryDeathObservation().getValues()) {
							if (value != null && !value.isSetNullFlavor()) {
								if (value instanceof CD) {
									condition.setOutcome(dtt.tCD2CodeableConcept((CD) value));
								}
							}
						}
					}

					// familyHistoryObservation.ageObservation -> condition.onset
					if (familyHistoryObs.getAgeObservation() != null
							&& !familyHistoryObs.getAgeObservation().isSetNullFlavor()) {
						Age onset = tAgeObservation2Age(familyHistoryObs.getAgeObservation());
						if (onset != null) {
							condition.setOnset(onset);
						}
					}
				}
			}
		}

		// info from subject.relatedSubject
		if (cdaFHO.getSubject() != null && !cdaFHO.isSetNullFlavor() && cdaFHO.getSubject().getRelatedSubject() != null
				&& !cdaFHO.getSubject().getRelatedSubject().isSetNullFlavor()) {
			org.openhealthtools.mdht.uml.cda.RelatedSubject cdaRelatedSubject = cdaFHO.getSubject().getRelatedSubject();

			// subject.relatedSubject.code -> relationship
			if (cdaRelatedSubject.getCode() != null && !cdaRelatedSubject.getCode().isSetNullFlavor()) {
				fhirFMH.setRelationship(dtt.tCD2CodeableConcept(cdaRelatedSubject.getCode()));
			}

			// info from subject.relatedSubject.subject
			if (cdaRelatedSubject.getSubject() != null && !cdaRelatedSubject.getSubject().isSetNullFlavor()) {
				org.openhealthtools.mdht.uml.cda.SubjectPerson subjectPerson = cdaRelatedSubject.getSubject();

				// subject.relatedSubject.subject.name.text -> name
				for (EN en : subjectPerson.getNames()) {
					if (en != null && !en.isSetNullFlavor()) {
						if (en.getText() != null) {
							fhirFMH.setName(en.getText());
						}
					}
				}

				// subject.relatedSubject.subject.administrativeGenderCode -> gender
				if (subjectPerson.getAdministrativeGenderCode() != null
						&& !subjectPerson.getAdministrativeGenderCode().isSetNullFlavor()
						&& subjectPerson.getAdministrativeGenderCode().getCode() != null) {
					fhirFMH.setGender(vst.tAdministrativeGenderCode2AdministrativeGender(
							subjectPerson.getAdministrativeGenderCode().getCode()));
				}

				// subject.relatedSubject.subject.birthTime -> born
				if (subjectPerson.getBirthTime() != null && !subjectPerson.getBirthTime().isSetNullFlavor()) {
					fhirFMH.setBorn(dtt.tTS2Date(subjectPerson.getBirthTime()));
				}
			}
		}

		return fhirFMH;
	}

	/*
	 * Functional Status Section contains: 1- Functional Status Observation 2-
	 * Self-Care Activites Both of them have single Observation which needs mapping.
	 * Therefore, the parameter for the following
	 * method(tFunctionalStatus2Observation) chosen to be generic(Observation) .. to
	 * cover the content of the section. Also, notice that the transformation of
	 * those Observations are different from the generic Observation transformation
	 */

	@Override
	public EntryResult tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaObservation == null || cdaObservation.isSetNullFlavor()) {
			return result;
		}

		org.hl7.fhir.dstu3.model.Observation fhirObs = new org.hl7.fhir.dstu3.model.Observation();
		result.addResource(fhirObs);

		// resource id
		IdType resourceId = new IdType("Observation", getUniqueId());
		fhirObs.setId(resourceId);

		// subject
		fhirObs.setSubject(getPatientRef());

		// statusCode -> status
		if (cdaObservation.getStatusCode() != null && !cdaObservation.getStatusCode().isSetNullFlavor()) {
			if (cdaObservation.getStatusCode().getCode() != null
					&& !cdaObservation.getStatusCode().getCode().isEmpty()) {
				fhirObs.setStatus(
						vst.tObservationStatusCode2ObservationStatus(cdaObservation.getStatusCode().getCode()));
			}
		}

		// id -> identifier
		if (cdaObservation.getIds() != null && !cdaObservation.getIds().isEmpty()) {
			for (II ii : cdaObservation.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code -> category
		if (cdaObservation.getCode() != null && !cdaObservation.isSetNullFlavor()) {
			fhirObs.addCategory(dtt.tCD2CodeableConcept(cdaObservation.getCode()));
		}

		// value[@xsi:type='CD'] -> code
		if (cdaObservation.getValues() != null && !cdaObservation.getValues().isEmpty()) {
			for (ANY value : cdaObservation.getValues()) {
				if (value != null && !value.isSetNullFlavor()) {
					if (value instanceof CD) {
						fhirObs.setCode(dtt.tCD2CodeableConcept((CD) value));
					}
				}
			}
		}

		// author -> performer
		if (cdaObservation.getAuthors() != null && !cdaObservation.getAuthors().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
				if (author != null && !author.isSetNullFlavor()) {
					EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
					result.updateFrom(entityResult);
					if (entityResult.hasPractitioner()) {
						fhirObs.addPerformer().setReference(entityResult.getPractitionerId());
					}
				}
			}
		}

		// effectiveTime -> effective
		if (cdaObservation.getEffectiveTime() != null && !cdaObservation.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObservation.getEffectiveTime()));
		}

		// non-medicinal supply activity -> device
		if (cdaObservation.getEntryRelationships() != null && !cdaObservation.getEntryRelationships().isEmpty()) {
			for (EntryRelationship entryRelShip : cdaObservation.getEntryRelationships()) {
				if (entryRelShip != null && !entryRelShip.isSetNullFlavor()) {
					// supply
					org.openhealthtools.mdht.uml.cda.Supply cdaSupply = entryRelShip.getSupply();
					if (cdaSupply != null && !cdaSupply.isSetNullFlavor()) {
						if (cdaSupply instanceof NonMedicinalSupplyActivity) {
							// Non-Medicinal Supply Activity
							org.hl7.fhir.dstu3.model.Device fhirDev = tSupply2Device(cdaSupply);
							fhirObs.setDevice(getReference(fhirDev));
							result.addResource(fhirDev);
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public ContactComponent tGuardian2Contact(Guardian cdaGuardian) {
		if (cdaGuardian == null || cdaGuardian.isSetNullFlavor())
			return null;

		ContactComponent fhirContact = new ContactComponent();

		// addr -> address
		if (cdaGuardian.getAddrs() != null && !cdaGuardian.getAddrs().isEmpty()) {
			fhirContact.setAddress(dtt.AD2Address(cdaGuardian.getAddrs().get(0)));
		}

		// telecom -> telecom
		if (cdaGuardian.getTelecoms() != null && !cdaGuardian.getTelecoms().isEmpty()) {
			for (TEL tel : cdaGuardian.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirContact.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// guardianPerson/name -> name
		if (cdaGuardian.getGuardianPerson() != null && !cdaGuardian.getGuardianPerson().isSetNullFlavor()) {
			for (PN pn : cdaGuardian.getGuardianPerson().getNames()) {
				if (!pn.isSetNullFlavor()) {
					fhirContact.setName(dtt.tEN2HumanName(pn));
				}
			}
		}

		// code -> relationship
		if (cdaGuardian.getCode() != null && !cdaGuardian.getCode().isSetNullFlavor()) {
			// try to use IValueSetsTransformer method
			// tRoleCode2PatientContactRelationshipCode
			Coding relationshipCoding = null;
			if (cdaGuardian.getCode().getCode() != null && !cdaGuardian.getCode().getCode().isEmpty()) {
				relationshipCoding = vst.tRoleCode2PatientContactRelationshipCode(cdaGuardian.getCode().getCode());
			}
			// if tRoleCode2PatientContactRelationshipCode returns non-null value, add as
			// coding
			// otherwise, add relationship directly by making code
			// transformation(tCD2CodeableConcept)
			if (relationshipCoding != null)
				fhirContact.addRelationship(new CodeableConcept().addCoding(relationshipCoding));
			else
				fhirContact.addRelationship(dtt.tCD2CodeableConcept(cdaGuardian.getCode()));
		}
		return fhirContact;
	}

	@Override
	public EntryResult tImmunizationActivity2Immunization(ImmunizationActivity cdaImmunizationActivity,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaImmunizationActivity == null || cdaImmunizationActivity.isSetNullFlavor()) {
			return result;
		}

		Immunization fhirImmunization = new Immunization();
		result.addResource(fhirImmunization);

		// resource id
		IdType resourceId = new IdType("Immunization", getUniqueId());
		fhirImmunization.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirImmunization.getMeta().addProfile(Constants.PROFILE_DAF_IMMUNIZATION);

		// patient
		fhirImmunization.setPatient(getPatientRef());

		// id -> identifier
		if (cdaImmunizationActivity.getIds() != null && !cdaImmunizationActivity.getIds().isEmpty()) {
			for (II ii : cdaImmunizationActivity.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirImmunization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// negationInd -> notGiven
		if (cdaImmunizationActivity.getNegationInd() != null) {
			fhirImmunization.setNotGiven(cdaImmunizationActivity.getNegationInd());
		}

		// effectiveTime -> date
		if (cdaImmunizationActivity.getEffectiveTimes() != null
				&& !cdaImmunizationActivity.getEffectiveTimes().isEmpty()) {
			for (SXCM_TS effectiveTime : cdaImmunizationActivity.getEffectiveTimes()) {
				if (effectiveTime != null && !effectiveTime.isSetNullFlavor()) {
					// Asserting that at most one effective time exists
					fhirImmunization.setDateElement(dtt.tTS2DateTime(effectiveTime));
				}
			}
		}

		// lotNumber, vaccineCode, organization

		if (cdaImmunizationActivity.getConsumable() != null
				&& !cdaImmunizationActivity.getConsumable().isSetNullFlavor()) {
			if (cdaImmunizationActivity.getConsumable().getManufacturedProduct() != null
					&& !cdaImmunizationActivity.getConsumable().getManufacturedProduct().isSetNullFlavor()) {
				ManufacturedProduct manufacturedProduct = cdaImmunizationActivity.getConsumable()
						.getManufacturedProduct();

				if (manufacturedProduct.getManufacturedMaterial() != null
						&& !manufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
					Material manufacturedMaterial = manufacturedProduct.getManufacturedMaterial();

					// consumable.manufacturedProduct.manufacturedMaterial.code -> vaccineCode
					if (manufacturedProduct.getManufacturedMaterial().getCode() != null) {
						fhirImmunization.setVaccineCode(dtt.tCD2CodeableConcept(manufacturedMaterial.getCode(),
								bundleInfo.getIdedAnnotations()));
					}

					// consumable.manufacturedProduct.manufacturedMaterial.lotNumberText ->
					// lotNumber
					if (manufacturedMaterial.getLotNumberText() != null
							&& !manufacturedMaterial.getLotNumberText().isSetNullFlavor()) {
						fhirImmunization.setLotNumberElement(dtt.tST2String(manufacturedMaterial.getLotNumberText()));
					}
				}

				// consumable.manufacturedProduct.manufacturerOrganization -> manufacturer
				if (manufacturedProduct.getManufacturerOrganization() != null
						&& !manufacturedProduct.getManufacturerOrganization().isSetNullFlavor()) {

					org.hl7.fhir.dstu3.model.Organization fhirOrganization = tOrganization2Organization(
							manufacturedProduct.getManufacturerOrganization());

					fhirImmunization.setManufacturer(getReference(fhirOrganization));
					result.addResource(fhirOrganization);
				}
			}
		}

		// performer -> practitioner
		if (cdaImmunizationActivity.getPerformers() != null && !cdaImmunizationActivity.getPerformers().isEmpty()) {
			for (Performer2 performer : cdaImmunizationActivity.getPerformers()) {
				if (performer.getAssignedEntity() != null && !performer.getAssignedEntity().isSetNullFlavor()) {
					EntityResult entityResult = tPerformer22Practitioner(performer, bundleInfo);
					result.updateFrom(entityResult);
					if (entityResult.hasPractitioner()) {
						// TODO: verify the STU3 mappings
						// TODO: find defined valueset/codesystem for immunization role
						// fhirImmunization.setPerformer(new Reference(entry.getResource().getId()));
						ImmunizationPractitionerComponent perf = fhirImmunization.addPractitioner();
						perf.getRole().addCoding().setSystem("http://hl7.org/fhir/v2/0443").setCode("AP")
								.setDisplay("Administering Provider");
						perf.setActor(getReference(entityResult.getPractitioner()));
						fhirImmunization.setPrimarySource(true);
					}
				}
			}
		} else {
			// if no practitioner gets set, default to false.
			fhirImmunization.setPrimarySource(false);
		}

		// approachSiteCode -> site
		for (CD cd : cdaImmunizationActivity.getApproachSiteCodes()) {
			fhirImmunization.setSite(dtt.tCD2CodeableConcept(cd));
		}

		// routeCode -> route
		if (cdaImmunizationActivity.getRouteCode() != null
				&& !cdaImmunizationActivity.getRouteCode().isSetNullFlavor()) {
			fhirImmunization.setRoute(dtt.tCD2CodeableConcept(cdaImmunizationActivity.getRouteCode()));
		}

		// doseQuantity -> doseQuantity
		if (cdaImmunizationActivity.getDoseQuantity() != null
				&& !cdaImmunizationActivity.getDoseQuantity().isSetNullFlavor()) {
			SimpleQuantity dose = dtt.tPQ2SimpleQuantity(cdaImmunizationActivity.getDoseQuantity());
			// manually set dose system, source object doesn't support it.
			dose.setSystem(vst.tOid2Url("2.16.840.1.113883.1.11.12839"));

			fhirImmunization.setDoseQuantity(dose);
		}

		// statusCode -> status
		if (cdaImmunizationActivity.getStatusCode() != null
				&& !cdaImmunizationActivity.getStatusCode().isSetNullFlavor()) {
			if (cdaImmunizationActivity.getStatusCode().getCode() != null
					&& !cdaImmunizationActivity.getStatusCode().getCode().isEmpty()) {

				ImmunizationStatus status = vst
						.tStatusCode2ImmunizationStatus(cdaImmunizationActivity.getStatusCode().getCode());
				if (status != null) {
					try {
						fhirImmunization.setStatus(status);
					} catch (FHIRException e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
		}

		// notGiven == true
		if (fhirImmunization.getNotGiven()) {
			// immunizationRefusalReason.code -> explanation.reasonNotGiven
			if (cdaImmunizationActivity.getImmunizationRefusalReason() != null
					&& !cdaImmunizationActivity.getImmunizationRefusalReason().isSetNullFlavor()) {
				if (cdaImmunizationActivity.getImmunizationRefusalReason().getCode() != null
						&& !cdaImmunizationActivity.getImmunizationRefusalReason().getCode().isSetNullFlavor()) {
					// fhirImmunization.setExplanation(new
					// Explanation().addReasonNotGiven(dtt.tCD2CodeableConcept(cdaImmunizationActivity.getImmunizationRefusalReason().getCode())));
					fhirImmunization.getExplanation().addReasonNotGiven(
							dtt.tCD2CodeableConcept(cdaImmunizationActivity.getImmunizationRefusalReason().getCode()));
				}
			}
		}
		// notGiven == false
		else if (!fhirImmunization.getNotGiven()) {
			// indication.value -> explanation.reason
			if (cdaImmunizationActivity.getIndication() != null
					&& !cdaImmunizationActivity.getIndication().isSetNullFlavor()) {
				if (!cdaImmunizationActivity.getIndication().getValues().isEmpty()
						&& cdaImmunizationActivity.getIndication().getValues().get(0) != null
						&& !cdaImmunizationActivity.getIndication().getValues().get(0).isSetNullFlavor()) {
					// fhirImmunization.setExplanation(new
					// Explanation().addReason(dtt.tCD2CodeableConcept((CD)cdaImmunizationActivity.getIndication().getValues().get(0))));
					fhirImmunization.getExplanation().addReason(
							dtt.tCD2CodeableConcept((CD) cdaImmunizationActivity.getIndication().getValues().get(0)));
				}
			}
		}

		// reaction (i.e.
		// entryRelationship/observation[templateId/@root="2.16.840.1.113883.10.20.22.4.9"]
		// -> reaction
		if (cdaImmunizationActivity.getReactionObservation() != null
				&& !cdaImmunizationActivity.getReactionObservation().isSetNullFlavor()) {
			EntryResult er = tReactionObservation2Observation(cdaImmunizationActivity.getReactionObservation(),
					bundleInfo);
			Bundle reactionBundle = er.getBundle();
			org.hl7.fhir.dstu3.model.Observation fhirReactionObservation = null;
			for (BundleEntryComponent entry : reactionBundle.getEntry()) {
				result.addResource(entry.getResource());
				if (entry.getResource() instanceof org.hl7.fhir.dstu3.model.Observation) {
					fhirReactionObservation = (org.hl7.fhir.dstu3.model.Observation) entry.getResource();

					ImmunizationReactionComponent fhirReaction = fhirImmunization.addReaction();
					// reaction -> reaction.detail[ref=Observation]
					fhirReaction.setDetail(getReference(fhirReactionObservation));

					// reaction/effectiveTime/low -> reaction.date
					if (fhirReactionObservation.getEffective() != null) {
						Period reactionDate = (Period) fhirReactionObservation.getEffective();
						if (reactionDate.getStart() != null)
							fhirReaction.setDateElement(reactionDate.getStartElement());
					}

				}

			}
		}

		// TODO: in STU3 this property at this level was removed. Figure out how
		// to map this to STU3
		// fhirImmunization.setReported(Config.DEFAULT_IMMUNIZATION_REPORTED);
		return result;

	}

	@Override
	public Condition tIndication2ConditionEncounter(Indication cdaIndication, IBundleInfo bundleInfo) {
		Condition cond = tIndication2Condition(cdaIndication, bundleInfo);
		Coding conditionCategory = new Coding().setSystem(vst.tOid2Url("2.16.840.1.113883.4.642.3.153"));
		conditionCategory.setCode("encounter-diagnosis");
		conditionCategory.setDisplay("Encounter Diagnosis");
		cond.addCategory().addCoding(conditionCategory);
		return cond;
	}

	@Override
	public Condition tIndication2ConditionProblemListItem(Indication cdaIndication, IBundleInfo bundleInfo) {
		Condition cond = tIndication2Condition(cdaIndication, bundleInfo);
		Coding conditionCategory = new Coding().setSystem(vst.tOid2Url("2.16.840.1.113883.4.642.3.153"));
		conditionCategory.setCode("problem-list-item");
		conditionCategory.setDisplay("Problem List Item");
		cond.addCategory().addCoding(conditionCategory);
		return cond;

	}

	private Condition tIndication2Condition(Indication cdaIndication, IBundleInfo bundleInfo) {
		if (cdaIndication == null || cdaIndication.isSetNullFlavor())
			return null;

		Condition fhirCond = new Condition();

		// resource id
		IdType resourceId = new IdType("Condition", getUniqueId());
		fhirCond.setId(resourceId);

		// patient
		fhirCond.setSubject(getPatientRef());

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirCond.getMeta().addProfile(Constants.PROFILE_DAF_CONDITION);

		// id -> identifier
		if (cdaIndication.getIds() != null && !cdaIndication.getIds().isEmpty()) {
			for (II ii : cdaIndication.getIds()) {
				fhirCond.addIdentifier(dtt.tII2Identifier(ii));
			}
		}

		// effectiveTime -> onset & abatement
		if (cdaIndication.getEffectiveTime() != null && !cdaIndication.getEffectiveTime().isSetNullFlavor()) {

			IVXB_TS low = cdaIndication.getEffectiveTime().getLow();
			IVXB_TS high = cdaIndication.getEffectiveTime().getHigh();
			String value = cdaIndication.getEffectiveTime().getValue();

			// low and high are both empty, and only the @value exists -> onset
			if (low == null && high == null && value != null && !value.equals("")) {
				fhirCond.setOnset(dtt.tString2DateTime(value));
			} else {
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

		// effectiveTime info -> clinicalStatus
		if (cdaIndication.getEffectiveTime() != null && !cdaIndication.getEffectiveTime().isSetNullFlavor()) {
			// high & low is present -> inactive
			if (cdaIndication.getEffectiveTime().getLow() != null
					&& !cdaIndication.getEffectiveTime().getLow().isSetNullFlavor()
					&& cdaIndication.getEffectiveTime().getHigh() != null
					&& !cdaIndication.getEffectiveTime().getHigh().isSetNullFlavor()) {
				fhirCond.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
			} else if (cdaIndication.getEffectiveTime().getLow() != null
					&& !cdaIndication.getEffectiveTime().getLow().isSetNullFlavor()) {
				// low is present, high is not present -> active
				fhirCond.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
			} else if (cdaIndication.getEffectiveTime().getValue() != null) {
				// value is present, low&high is not present -> active
				fhirCond.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
			}
		}

		// value[CD] -> code
		if (cdaIndication.getValues() != null && !cdaIndication.getValues().isEmpty()) {
			// There is only 1 value, but anyway...
			for (ANY value : cdaIndication.getValues()) {
				if (value != null && !value.isSetNullFlavor()) {
					if (value instanceof CD)
						fhirCond.setCode(dtt.tCD2CodeableConcept((CD) value));
				}
			}
		}

		return fhirCond;
	}

	@Override
	public PatientCommunicationComponent tLanguageCommunication2Communication(
			LanguageCommunication cdaLanguageCommunication) {
		if (cdaLanguageCommunication == null || cdaLanguageCommunication.isSetNullFlavor())
			return null;

		PatientCommunicationComponent fhirCommunication = new PatientCommunicationComponent();

		// languageCode -> language
		if (cdaLanguageCommunication.getLanguageCode() != null
				&& !cdaLanguageCommunication.getLanguageCode().isSetNullFlavor()) {
			fhirCommunication.setLanguage(dtt.tCD2CodeableConcept(cdaLanguageCommunication.getLanguageCode()));
			// http://hl7.org/fhir/ValueSet/languages -> language.codeSystem
			fhirCommunication.getLanguage().getCodingFirstRep().setSystem("http://hl7.org/fhir/ValueSet/languages");
		}

		// preferenceInd -> preferred
		if (cdaLanguageCommunication.getPreferenceInd() != null
				&& !cdaLanguageCommunication.getPreferenceInd().isSetNullFlavor()) {
			fhirCommunication.setPreferredElement(dtt.tBL2Boolean(cdaLanguageCommunication.getPreferenceInd()));
		}

		return fhirCommunication;

	}

	@Override
	public EntryResult tManufacturedProduct2Medication(ManufacturedProduct cdaManufacturedProduct,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaManufacturedProduct == null || cdaManufacturedProduct.isSetNullFlavor())
			return result;

		Medication fhirMedication = new Medication();

		if (cdaManufacturedProduct.getManufacturedMaterial() != null
				&& !cdaManufacturedProduct.getManufacturedMaterial().isSetNullFlavor()) {
			CD cd = cdaManufacturedProduct.getManufacturedMaterial().getCode();
			if (cd != null) {

				Medication previousMedication = (Medication) bundleInfo.findResourceResult(cd);
				if (previousMedication != null) {
					// Return Previously created medication
					result.addExistingResource(previousMedication);
					return result;
				} else {
					result.putCDResource(cd, fhirMedication);
				}

				// manufacturedMaterial.code -> code
				fhirMedication.setCode(dtt.tCD2CodeableConcept(
						cdaManufacturedProduct.getManufacturedMaterial().getCode(), bundleInfo.getIdedAnnotations()));
			}
		}

		// add fhir resource after we check for previous value
		result.addResource(fhirMedication);

		// resource id
		IdType resourceId = new IdType("Medication", getUniqueId());
		fhirMedication.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirMedication.getMeta().addProfile(Constants.PROFILE_DAF_MEDICATION);

		// manufacturerOrganization -> manufacturer
		if (cdaManufacturedProduct.getManufacturerOrganization() != null
				&& !cdaManufacturedProduct.getManufacturerOrganization().isSetNullFlavor()) {
			org.hl7.fhir.dstu3.model.Organization org = tOrganization2Organization(
					cdaManufacturedProduct.getManufacturerOrganization());

			fhirMedication.setManufacturer(getReference(org));
			result.addResource(org);

		}

		return result;
	}

	@Override
	public EntryResult tManufacturedProduct2Medication(Product cdaProduct, IBundleInfo bundleInfo) {

		if (cdaProduct == null || cdaProduct.isSetNullFlavor()) {
			return new EntryResult();
		}

		return tManufacturedProduct2Medication(cdaProduct.getManufacturedProduct(), bundleInfo);
	}

	@Override
	public EntryResult tManufacturedProduct2Medication(Consumable cdaConsumable, IBundleInfo bundleInfo) {

		if (cdaConsumable == null || cdaConsumable.isSetNullFlavor()) {
			return new EntryResult();
		}

		return tManufacturedProduct2Medication(cdaConsumable.getManufacturedProduct(), bundleInfo);

	}

	@Override
	public EntryResult tMedicationActivity2MedicationStatement(MedicationActivity cdaMedicationActivity,
			IBundleInfo bundleInfo) {

		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		EntryResult result = new EntryResult();

		if (cdaMedicationActivity == null || cdaMedicationActivity.isSetNullFlavor()) {
			return result;
		}

		MedicationStatement fhirMedSt = new MedicationStatement();
		org.hl7.fhir.dstu3.model.Dosage fhirDosage = fhirMedSt.addDosage();
		result.addResource(fhirMedSt);

		// resource id
		IdType resourceId = new IdType("MedicationStatement", getUniqueId());
		fhirMedSt.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirMedSt.getMeta().addProfile(Constants.PROFILE_DAF_MEDICATION_STATEMENT);

		// patient
		fhirMedSt.setSubject(getPatientRef());

		// id -> identifier
		if (cdaMedicationActivity.getIds() != null && !cdaMedicationActivity.getIds().isEmpty()) {
			for (II ii : cdaMedicationActivity.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirMedSt.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// statusCode -> status
		if (cdaMedicationActivity.getStatusCode() != null && !cdaMedicationActivity.getStatusCode().isSetNullFlavor()) {
			if (cdaMedicationActivity.getStatusCode().getCode() != null
					&& !cdaMedicationActivity.getStatusCode().getCode().isEmpty()) {
				MedicationStatementStatus statusCode = vst
						.tStatusCode2MedicationStatementStatus(cdaMedicationActivity.getStatusCode().getCode());
				if (statusCode != null) {
					fhirMedSt.setStatus(statusCode);
				}
			}
		}

		// author[0] -> informationSource
		if (!cdaMedicationActivity.getAuthors().isEmpty()) {
			if (!cdaMedicationActivity.getAuthors().get(0).isSetNullFlavor()) {
				EntityResult entityResult = tAuthor2Practitioner(cdaMedicationActivity.getAuthors().get(0), bundleInfo);
				result.updateFrom(entityResult);
				if (entityResult.hasPractitioner()) {
					fhirMedSt.setInformationSource(getReference(entityResult.getPractitioner()));
				}
			}
		}

		// consumable.manufacturedProduct -> medication
		if (cdaMedicationActivity.getConsumable() != null && !cdaMedicationActivity.getConsumable().isSetNullFlavor()) {
			EntryResult fhirMedicationResult = tManufacturedProduct2Medication(cdaMedicationActivity.getConsumable(),
					localBundleInfo);
			result.updateFrom(fhirMedicationResult);
			localBundleInfo.updateFrom(fhirMedicationResult);
			for (BundleEntryComponent entry : fhirMedicationResult.getFullBundle().getEntry()) {
				if (entry.getResource() instanceof org.hl7.fhir.dstu3.model.Medication) {
					fhirMedSt.setMedication(getReference(entry.getResource()));
				}

			}
		}

		// getting info from effectiveTimes
		if (cdaMedicationActivity.getEffectiveTimes() != null && !cdaMedicationActivity.getEffectiveTimes().isEmpty()) {
			for (org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedicationActivity.getEffectiveTimes()) {
				if (ts != null && !ts.isSetNullFlavor()) {
					// effectiveTime[@xsi:type='IVL_TS'] -> effective
					if (ts instanceof IVL_TS) {
						fhirMedSt.setEffective(dtt.tIVL_TS2Period((IVL_TS) ts));
					}
					// effectiveTime[@xsi:type='PIVL_TS'] -> dosage.timing
					if (ts instanceof PIVL_TS) {
						fhirDosage.setTiming(dtt.tPIVL_TS2Timing((PIVL_TS) ts));
					}
				}
			}
		}

		// doseQuantity -> dosage.quantity
		if (cdaMedicationActivity.getDoseQuantity() != null
				&& !cdaMedicationActivity.getDoseQuantity().isSetNullFlavor()) {
			SimpleQuantity dose = dtt.tPQ2SimpleQuantity(cdaMedicationActivity.getDoseQuantity());
			// manually set dose system, source object doesn't support it.
			dose.setSystem(vst.tOid2Url("2.16.840.1.113883.1.11.12839"));
			fhirDosage.setDose(dose);
		}

		// routeCode -> dosage.route
		if (cdaMedicationActivity.getRouteCode() != null && !cdaMedicationActivity.getRouteCode().isSetNullFlavor()) {
			fhirDosage.setRoute(dtt.tCD2CodeableConcept(cdaMedicationActivity.getRouteCode()));
		}

		// rateQuantity -> dosage.rate
		if (cdaMedicationActivity.getRateQuantity() != null
				&& !cdaMedicationActivity.getRateQuantity().isSetNullFlavor()) {
			fhirDosage.setRate(dtt.tIVL_PQ2Range(cdaMedicationActivity.getRateQuantity()));
		}

		// maxDoseQuantity -> dosage.maxDosePerPeriod
		if (cdaMedicationActivity.getMaxDoseQuantity() != null
				&& !cdaMedicationActivity.getMaxDoseQuantity().isSetNullFlavor()) {
			// cdaDataType.RTO does nothing but extends cdaDataType.RTO_PQ_PQ
			fhirDosage.setMaxDosePerPeriod(dtt.tRTO2Ratio((RTO) cdaMedicationActivity.getMaxDoseQuantity()));
		}

		if (cdaMedicationActivity.getEntryRelationships() != null
				&& !cdaMedicationActivity.getEntryRelationships().isEmpty()) {

			for (EntryRelationship er : cdaMedicationActivity.getEntryRelationships()) {

				if (er.getTypeCode() != null && er.getInversionInd() != null) {
					// If entry relationship contains frequency observation instruction
					if (er.getTypeCode().equals(x_ActRelationshipEntryRelationship.SUBJ) && er.getInversionInd()) {
						if (er.getObservation() != null && !er.getObservation().isSetNullFlavor()) {
							Observation obs = er.getObservation();
							if (obs.getClassCode() != null && obs.getMoodCode() != null) {
								if (obs.getClassCode().equals(ActClassObservation.OBS)
										&& obs.getMoodCode().equals(x_ActMoodDocumentObservation.EVN)) {
									if (obs.getCode() != null && obs.getCode().getCode().contentEquals("FREQUENCY")) {
										if (!obs.getValues().isEmpty()) {
											ANY valueElement = obs.getValues().get(0);
											// Instruction.Frequency -> Dosage.timing
											if (((ED) valueElement).getText() != null) {
												CodeableConcept timingCoding = new CodeableConcept();
												Timing timing = new Timing();
												fhirDosage.setTiming(timing);
												timingCoding.setText(((ED) valueElement).getText());
												timing.setCode(timingCoding);
											}
										}
									}
								}
							}
						}
					}
				} else if (er.getTypeCode() != null) {
					// if entry relationship contains Medication Free Text Signature
					if (er.getTypeCode().equals(x_ActRelationshipEntryRelationship.COMP)) {
						if (er.getSubstanceAdministration() != null
								&& !er.getSubstanceAdministration().isSetNullFlavor()) {
							SubstanceAdministration sa = er.getSubstanceAdministration();
							if (sa.getClassCode() != null && sa.getMoodCode() != null) {
								// substance administration is a Medication Free Text Sig
								if (sa.getClassCode().equals(ActClass.SBADM)
										&& (sa.getMoodCode().equals(x_DocumentSubstanceMood.EVN)
												|| sa.getMoodCode().equals(x_DocumentSubstanceMood.INT))) {

									String freeTextInstruction = dtt.tED2Annotation(sa.getText(),
											bundleInfo.getIdedAnnotations());

									if (freeTextInstruction != null) {
										// Medication Free Text Sig -> Dosage.text/Dosage.PatientInstructions
										fhirDosage.setText(freeTextInstruction);
										fhirDosage.setPatientInstruction(freeTextInstruction);
									}
								}
							}
						}
					}
				}
			}
		}

		// taken -> UNK
		fhirMedSt.setTaken(MedicationStatementTaken.UNK);

		// indication -> reason
		for (Indication indication : cdaMedicationActivity.getIndications()) {
			Condition cond = tIndication2ConditionProblemListItem(indication, bundleInfo);

			result.addResource(cond);
			fhirMedSt.addReasonReference(getReference(cond));
		}

		if (cdaMedicationActivity.getMedicationSupplyOrder() != null) {
			IEntryResult medRequestResult = medicationSupplyOrder2MedicationRequest(
					cdaMedicationActivity.getMedicationSupplyOrder(), localBundleInfo);
			localBundleInfo.updateFrom(medRequestResult);
			result.updateFrom(medRequestResult);
		}

		EList<MedicationDispense> dispenses = cdaMedicationActivity.getMedicationDispenses();
		if (dispenses != null && !dispenses.isEmpty()) {
			MedicationDispense dispense = dispenses.get(0); // Cardinality is 1 in spec
			IEntryResult medDispenseResult = tMedicationDispense2MedicationDispense(dispense, localBundleInfo);
			localBundleInfo.updateFrom(medDispenseResult);
			result.updateFrom(medDispenseResult);
		}

		return result;
	}

	@Override
	public EntryResult tMedicationDispense2MedicationDispense(
			org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMedicationDispense, IBundleInfo bundleInfo) {

		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		EntryResult result = new EntryResult();

		if (cdaMedicationDispense == null || cdaMedicationDispense.isSetNullFlavor()) {
			return result;
		}

		// NOTE: Following mapping doesn't really suit the mapping proposed by daf

		org.hl7.fhir.dstu3.model.MedicationDispense fhirMediDisp = new org.hl7.fhir.dstu3.model.MedicationDispense();
		result.addResource(fhirMediDisp);

		// patient
		fhirMediDisp.setSubject(getPatientRef());

		// resource id
		IdType resourceId = new IdType("MedicationDispense", getUniqueId());
		fhirMediDisp.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirMediDisp.getMeta().addProfile(Constants.PROFILE_DAF_MEDICATION_DISPENSE);

		// id -> identifier
		if (cdaMedicationDispense.getIds() != null & !cdaMedicationDispense.getIds().isEmpty()) {
			for (II ii : cdaMedicationDispense.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					// Asserting at most one identifier exists
					fhirMediDisp.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// statusCode -> status
		if (cdaMedicationDispense.getStatusCode() != null && !cdaMedicationDispense.getStatusCode().isSetNullFlavor()) {
			if (cdaMedicationDispense.getStatusCode().getCode() != null
					&& !cdaMedicationDispense.getStatusCode().getCode().isEmpty()) {
				MedicationDispenseStatus mediDispStatEnum = vst
						.tStatusCode2MedicationDispenseStatus(cdaMedicationDispense.getStatusCode().getCode());
				if (mediDispStatEnum != null) {
					fhirMediDisp.setStatus(mediDispStatEnum);
				}
			}
		}

		// product -> medication (reference)
		if (cdaMedicationDispense.getProduct() != null && !cdaMedicationDispense.getProduct().isSetNullFlavor()) {
			EntryResult fhirMedicationResult = tManufacturedProduct2Medication(cdaMedicationDispense.getProduct(),
					localBundleInfo);
			result.updateFrom(fhirMedicationResult);
			localBundleInfo.updateFrom(fhirMedicationResult);
			for (BundleEntryComponent entry : fhirMedicationResult.getFullBundle().getEntry()) {
				if (entry.getResource() instanceof org.hl7.fhir.dstu3.model.Medication) {
					Medication medicationResource = (Medication) entry.getResource();
					fhirMediDisp.setMedication(getReference(medicationResource));

				}
			}
		}

		// performer -> performer
		if (cdaMedicationDispense.getPerformers() != null && !cdaMedicationDispense.getPerformers().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Performer2 cdaPerformer : cdaMedicationDispense.getPerformers()) {
				if (cdaPerformer != null && !cdaPerformer.isSetNullFlavor()) {
					EntityResult entityResult = tPerformer22Practitioner(cdaPerformer, localBundleInfo);
					localBundleInfo.updateFrom(entityResult);
					result.updateFrom(entityResult);
					if (entityResult.hasPractitioner()) {
						fhirMediDisp.addPerformer().setActor(getReference(entityResult.getPractitioner()));
					}
				}
			}
		}

		// quantity -> quantity
		if (cdaMedicationDispense.getQuantity() != null && !cdaMedicationDispense.getQuantity().isSetNullFlavor()) {
			fhirMediDisp.setQuantity(dtt.tPQ2SimpleQuantity(cdaMedicationDispense.getQuantity()));
		}

		// whenPrepared and whenHandedOver
		// effectiveTime[0] -> whenPrepared, effectiveTime[1] -> whenHandedOver
		int effectiveTimeCount = 0;
		if (cdaMedicationDispense.getEffectiveTimes() != null && !cdaMedicationDispense.getEffectiveTimes().isEmpty()) {
			for (SXCM_TS ts : cdaMedicationDispense.getEffectiveTimes()) {
				if (effectiveTimeCount == 0) {
					// effectiveTime[0] -> whenPrepared
					if (ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenPreparedElement(dtt.tTS2DateTime(ts));
					}
					effectiveTimeCount++;
				} else if (effectiveTimeCount == 1) {
					// effectiveTime[1] -> whenHandedOver
					if (ts != null && !ts.isSetNullFlavor()) {
						fhirMediDisp.setWhenHandedOverElement(dtt.tTS2DateTime(ts));
					}
					effectiveTimeCount++;
				}
			}
		}

		// Adding dosageInstruction
		org.hl7.fhir.dstu3.model.Dosage fhirDosageInstruction = fhirMediDisp.addDosageInstruction();

		// TODO: The information used for dosageInstruction is used for different
		// fields, too.
		// Determine which field the information should fit

		// effectiveTimes -> dosageInstruction.timing.event
		if (cdaMedicationDispense.getEffectiveTimes() != null && !cdaMedicationDispense.getEffectiveTimes().isEmpty()) {
			Timing fhirTiming = new Timing();

			// adding effectiveTimes to fhirTiming
			for (org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS ts : cdaMedicationDispense.getEffectiveTimes()) {
				if (ts != null && !ts.isSetNullFlavor()) {
					fhirTiming.getEvent().add(dtt.tTS2DateTime(ts));
				} else if (ts.getValue() != null && !ts.getValue().isEmpty()) {
					fhirTiming.getEvent().add(dtt.tString2DateTime(ts.getValue()));
				}
			}

			// setting fhirTiming for dosageInstruction if it is not empty
			if (!fhirTiming.isEmpty()) {
				fhirDosageInstruction.setTiming(fhirTiming);
			}
		}

		// quantity -> dosageInstruction.dose
		if (cdaMedicationDispense.getQuantity() != null && !cdaMedicationDispense.getQuantity().isSetNullFlavor()) {
			fhirDosageInstruction.setDose(dtt.tPQ2SimpleQuantity(cdaMedicationDispense.getQuantity()));
		}

		return result;
	}

	@Override
	public EntryResult medicationSupplyOrder2MedicationRequest(MedicationSupplyOrder cdaSupplyOrder,
			IBundleInfo bundleInfo) {

		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		EntryResult result = new EntryResult();

		if (cdaSupplyOrder == null || cdaSupplyOrder.isSetNullFlavor())
			return result;

		MedicationRequest medRequest = new MedicationRequest();
		result.addResource(medRequest);

		// patient
		medRequest.setSubject(getPatientRef());

		// resource id
		IdType resourceId = new IdType("MedicationRequest", getUniqueId());
		medRequest.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			medRequest.getMeta().addProfile(Constants.PROFILE_DAF_MEDICATION_REQUEST);

		// id -> identifier
		if (cdaSupplyOrder.getIds() != null && !cdaSupplyOrder.getIds().isEmpty()) {
			for (II ii : cdaSupplyOrder.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					medRequest.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// statusCode -> status
		if (cdaSupplyOrder.getStatusCode() != null && !cdaSupplyOrder.getStatusCode().isSetNullFlavor()) {
			medRequest.setStatus(vst.tActStatus2MedicationRequestStatus(cdaSupplyOrder.getStatusCode().getCode()));
		}

		// hardcoded to "instance-order"
		medRequest.setIntent(MedicationRequestIntent.INSTANCEORDER);

		// product.manufacturedProduct(MedicationInformation ||
		// ImmunizationMedicationInformation) -> medication
		if (cdaSupplyOrder.getProduct() != null && !cdaSupplyOrder.getProduct().isSetNullFlavor()) {
			EntryResult fhirMedicationResult = tManufacturedProduct2Medication(cdaSupplyOrder.getProduct(),
					localBundleInfo);
			result.updateFrom(fhirMedicationResult);
			localBundleInfo.updateFrom(fhirMedicationResult);
			for (BundleEntryComponent resultEntry : fhirMedicationResult.getFullBundle().getEntry()) {
				if (resultEntry.getResource() instanceof Medication) {
					Medication medicationResult = (Medication) resultEntry.getResource();
					// We can only add either a reference here or a codeableconcept. Opting for
					// Reference.
					medRequest.setMedication(getReference(medicationResult));

				}
			}
		}

		if (cdaSupplyOrder.getQuantity() != null || cdaSupplyOrder.getRepeatNumber() != null
				|| cdaSupplyOrder.getEffectiveTimes() != null) {
			MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
			medRequest.setDispenseRequest(dispenseRequest);
			// quantity -> dosageRequest.quantity
			if (cdaSupplyOrder.getQuantity() != null && cdaSupplyOrder.getQuantity().getValue() != null
					&& !cdaSupplyOrder.getQuantity().isSetNullFlavor()) {
				SimpleQuantity sq = new SimpleQuantity();
				sq.setValue(cdaSupplyOrder.getQuantity().getValue());
				sq.setUnit(cdaSupplyOrder.getQuantity().getUnit());
				dispenseRequest.setQuantity(sq);
			}
			// repeatNumber -> dosageRequest.numberOfRepeatsAllowed
			if (cdaSupplyOrder.getRepeatNumber() != null && !cdaSupplyOrder.isSetNullFlavor()) {
				dispenseRequest.setNumberOfRepeatsAllowed(cdaSupplyOrder.getRepeatNumber().getValue().intValue());
			}

			// effectiveTime -> dispenseRequest.validityPeriod
			if (cdaSupplyOrder.getEffectiveTimes() != null && !cdaSupplyOrder.getEffectiveTimes().isEmpty()) {
				for (SXCM_TS ts : cdaSupplyOrder.getEffectiveTimes()) {
					if (ts instanceof IVL_TS) {
						Period period = dtt.tIVL_TS2Period((IVL_TS) ts);
						dispenseRequest.setValidityPeriod(period);
					}
				}
			}
		}

		// instructions -> notes
		if (cdaSupplyOrder.getInstructions() != null && !cdaSupplyOrder.getInstructions().isSetNullFlavor()) {
			Instructions instructions = cdaSupplyOrder.getInstructions();
			List<Annotation> annotations = new ArrayList<Annotation>();
			for (Act act : instructions.getActs()) {
				Annotation annotation = new Annotation();
				if (act.getText() != null) {
					annotation.setText(act.getText().getText());
					annotations.add(annotation);
				}
			}
			medRequest.setNote(annotations);
		}

		// author -> requester
		if (!cdaSupplyOrder.getAuthors().isEmpty()) {
			Author author = cdaSupplyOrder.getAuthors().get(0);
			EntityResult entityResult = tAuthor2Practitioner(author, localBundleInfo);
			localBundleInfo.updateFrom(entityResult);
			result.updateFrom(entityResult);
			if (entityResult.hasPractitioner()) {
				MedicationRequestRequesterComponent requester = new MedicationRequestRequesterComponent();
				requester.setAgent(getReference(entityResult.getPractitioner()));
				medRequest.setRequester(requester);
			}
		}

		return result;
	}

	@Override
	public EntryResult tMedicationInformation2Medication(MedicationInformation cdaMedicationInformation,
			IBundleInfo bundleInfo) {
		/*
		 * Since MedicationInformation is a ManufacturedProduct instance with a specific
		 * templateId, tManufacturedProduct2Medication should satisfy the required
		 * mapping for MedicationInformation
		 */
		return tManufacturedProduct2Medication(cdaMedicationInformation, bundleInfo);
	}

	@Override
	public EntryResult tObservation2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaObservation == null || cdaObservation.isSetNullFlavor()) {
			return result;
		}

		org.hl7.fhir.dstu3.model.Observation fhirObs = new org.hl7.fhir.dstu3.model.Observation();
		result.addResource(fhirObs);

		// resource id
		IdType resourceId = new IdType("Observation", getUniqueId());
		fhirObs.setId(resourceId);

		// subject
		fhirObs.setSubject(getPatientRef());

		// id -> identifier
		if (cdaObservation.getIds() != null && !cdaObservation.getIds().isEmpty()) {
			for (II ii : cdaObservation.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirObs.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code -> code
		if (cdaObservation.getCode() != null) {
			fhirObs.setCode(dtt.tCD2CodeableConcept(cdaObservation.getCode(), bundleInfo.getIdedAnnotations()));
		}

		// statusCode -> status
		if (cdaObservation.getStatusCode() != null && !cdaObservation.getStatusCode().isSetNullFlavor()) {
			if (cdaObservation.getStatusCode().getCode() != null) {
				fhirObs.setStatus(
						vst.tObservationStatusCode2ObservationStatus(cdaObservation.getStatusCode().getCode()));
			}
		}

		// effectiveTime -> effective
		if (cdaObservation.getEffectiveTime() != null && !cdaObservation.getEffectiveTime().isSetNullFlavor()) {
			fhirObs.setEffective(dtt.tIVL_TS2Period(cdaObservation.getEffectiveTime()));
		}

		// targetSiteCode -> bodySite
		for (CD cd : cdaObservation.getTargetSiteCodes()) {
			if (cd != null && !cd.isSetNullFlavor()) {
				fhirObs.setBodySite(dtt.tCD2CodeableConcept(cd));
			}
		}

		// value or dataAbsentReason
		if (cdaObservation.getValues() != null && !cdaObservation.getValues().isEmpty()) {
			// We traverse the values in cdaObs
			for (ANY value : cdaObservation.getValues()) {
				if (value == null)
					continue; // If the value is null, continue
				else if (value.isSetNullFlavor()) {
					// If a null flavor exists, then we set dataAbsentReason by looking at the
					// null-flavor value
					Coding DataAbsentReasonCode = vst.tNullFlavor2DataAbsentReasonCode(value.getNullFlavor());
					if (DataAbsentReasonCode != null) {
						if (fhirObs.getDataAbsentReason() == null || fhirObs.getDataAbsentReason().isEmpty()) {
							// If DataAbsentReason was not set, create a new CodeableConcept and add our
							// code into it
							fhirObs.getDataAbsentReason().addCoding(DataAbsentReasonCode);
						} else {
							// If DataAbsentReason was set, just get the CodeableConcept and add our code
							// into it
							fhirObs.getDataAbsentReason().addCoding(DataAbsentReasonCode);
						}
					}
				} else {
					// If a non-null value which has no null-flavor exists, then we can get the
					// value
					// Checking the type of value
					if (value instanceof CD) {
						fhirObs.setValue(dtt.tCD2CodeableConcept((CD) value));
					} else if (value instanceof IVL_PQ) {
						fhirObs.setValue(dtt.tIVL_PQ2Range((IVL_PQ) value));
					} else if (value instanceof PQ) {
						fhirObs.setValue(dtt.tPQ2Quantity((PQ) value));
					} else if (value instanceof ST) {
						fhirObs.setValue(dtt.tST2String((ST) value));
					} else if (value instanceof RTO) {
						fhirObs.setValue(dtt.tRTO2Ratio((RTO) value));
					} else if (value instanceof ED) {
						fhirObs.setValue(dtt.tED2Attachment((ED) value));
					} else if (value instanceof TS) {
						fhirObs.setValue(dtt.tTS2DateTime((TS) value));
					} else if (value instanceof BL) {
						fhirObs.setValue(dtt.tBL2Boolean((BL) value));
					} else if (value instanceof REAL) {

						fhirObs.setValue(dtt.tREAL2Quantity((REAL) value));

						// Epic specific: attempt to get units from custom observation.
						String SNOMED_OID = "2.16.840.1.113883.6.96";
						String SNOMED_VAL = "246514001";

						for (EntryRelationship er : cdaObservation.getEntryRelationships()) {
							Observation obs = er.getObservation();
							if (obs != null) {
								if (obs.getCode() != null) {
									if (obs.getCode().getCodeSystem() != null && obs.getCode().getCode() != null) {
										// Look for SNOMED unit encoding.
										if (obs.getCode().getCodeSystem().equals(SNOMED_OID)
												&& obs.getCode().getCode().equals(SNOMED_VAL)) {
											for (ANY val : obs.getValues()) {
												if (val instanceof ST) {
													ST stVal = (ST) val;
													String units = stVal.getText();
													Quantity fhirVal = (Quantity) fhirObs.getValue();
													fhirVal.setUnit(units);
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
		}

		// author -> performer
		for (org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
			if (author != null && !author.isSetNullFlavor()) {
				EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
				result.updateFrom(entityResult);
				if (entityResult.hasPractitioner()) {
					fhirObs.addPerformer().setReference(entityResult.getPractitionerId());
				}
			}
		}

		// methodCode -> method
		for (CE method : cdaObservation.getMethodCodes()) {
			if (method != null && !method.isSetNullFlavor()) {
				// Asserting that only one method exists
				fhirObs.setMethod(dtt.tCD2CodeableConcept(method));
			}
		}

		// author.time -> issued
		if (cdaObservation.getAuthors() != null && !cdaObservation.getAuthors().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Author author : cdaObservation.getAuthors()) {
				if (author != null && !author.isSetNullFlavor()) {
					// get time from author
					if (author.getTime() != null && !author.getTime().isSetNullFlavor()) {
						fhirObs.setIssuedElement(dtt.tTS2Instant(author.getTime()));
					}
				}
			}
		}

		// interpretationCode -> interpretation
		if (cdaObservation.getInterpretationCodes() != null && !cdaObservation.getInterpretationCodes().isEmpty()) {
			for (org.openhealthtools.mdht.uml.hl7.datatypes.CE cdaInterprCode : cdaObservation
					.getInterpretationCodes()) {
				if (cdaInterprCode != null && !cdaInterprCode.isSetNullFlavor()) {
					// Asserting that only one interpretation code exists
					fhirObs.setInterpretation(
							vst.tObservationInterpretationCode2ObservationInterpretationCode(cdaInterprCode));
				}
			}
		}

		// referenceRange -> referenceRange
		if (cdaObservation.getReferenceRanges() != null && !cdaObservation.getReferenceRanges().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange : cdaObservation
					.getReferenceRanges()) {
				if (cdaReferenceRange != null && !cdaReferenceRange.isSetNullFlavor()) {
					fhirObs.addReferenceRange(tReferenceRange2ReferenceRange(cdaReferenceRange));
				}
			}
		}

		return result;
	}

	@Override
	public org.hl7.fhir.dstu3.model.Organization tOrganization2Organization(
			org.openhealthtools.mdht.uml.cda.Organization cdaOrganization) {
		if (cdaOrganization == null || cdaOrganization.isSetNullFlavor())
			return null;

		org.hl7.fhir.dstu3.model.Organization fhirOrganization = new org.hl7.fhir.dstu3.model.Organization();

		// resource id
		IdType resourceId = new IdType("Organization", getUniqueId());
		fhirOrganization.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirOrganization.getMeta().addProfile(Constants.PROFILE_DAF_ORGANIZATION);

		// id -> identifier
		if (cdaOrganization.getIds() != null && !cdaOrganization.getIds().isEmpty()) {
			for (II ii : cdaOrganization.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirOrganization.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// name -> name
		if (cdaOrganization.getNames() != null && !cdaOrganization.isSetNullFlavor()) {

			int namesLength = cdaOrganization.getNames().size();

			for (int iter = 0; iter < namesLength; ++iter) {
				ON name = cdaOrganization.getNames().get(iter);
				if (name != null && !name.isSetNullFlavor() && name.getText() != null && !name.getText().isEmpty()) {
					if (iter == 0) {
						fhirOrganization.setName(name.getText());
					} else {
						fhirOrganization.addAlias(name.getText());
					}
				}
			}

		}

		// telecom -> telecom
		if (cdaOrganization.getTelecoms() != null && !cdaOrganization.getTelecoms().isEmpty()) {
			for (TEL tel : cdaOrganization.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirOrganization.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// addr -> address
		if (cdaOrganization.getAddrs() != null && !cdaOrganization.getAddrs().isEmpty()) {
			for (AD ad : cdaOrganization.getAddrs()) {
				if (ad != null && !ad.isSetNullFlavor()) {
					fhirOrganization.addAddress(dtt.AD2Address(ad));
				}
			}
		}

		return fhirOrganization;
	}

	@Override
	public org.hl7.fhir.dstu3.model.Location tParticipantRole2Location(ParticipantRole cdaParticipantRole) {
		if (cdaParticipantRole == null || cdaParticipantRole.isSetNullFlavor())
			return null;

		org.hl7.fhir.dstu3.model.Location fhirLocation = new org.hl7.fhir.dstu3.model.Location();

		// resource id
		IdType resourceId = new IdType("Location", getUniqueId());
		fhirLocation.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirLocation.getMeta().addProfile(Constants.PROFILE_DAF_LOCATION);

		// code -> type
		// TODO: Requires huge mapping work from HL7 HealthcareServiceLocation value set
		// to http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType
		if (cdaParticipantRole.getCode() != null && !cdaParticipantRole.getCode().isSetNullFlavor()) {
			logger.info(
					"Found location.code in the CDA document, which can be mapped to Location.type on the FHIR side. But this is skipped for the moment, as it requires huge mapping work from HL7 HealthcareServiceLocation value set to http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType");
			// fhirLocation.setType();
		}

		// playingEntity.name.text -> name
		if (cdaParticipantRole.getPlayingEntity() != null && !cdaParticipantRole.getPlayingEntity().isSetNullFlavor()) {
			if (cdaParticipantRole.getPlayingEntity().getNames() != null
					&& !cdaParticipantRole.getPlayingEntity().getNames().isEmpty()) {
				for (PN pn : cdaParticipantRole.getPlayingEntity().getNames()) {
					// Asserting that at most one name exists
					if (pn != null && !pn.isSetNullFlavor()) {
						fhirLocation.setName(pn.getText());
					}
				}
			}
		}

		// telecom -> telecom
		if (cdaParticipantRole.getTelecoms() != null && !cdaParticipantRole.getTelecoms().isEmpty()) {
			for (TEL tel : cdaParticipantRole.getTelecoms()) {
				if (tel != null && !tel.isSetNullFlavor()) {
					fhirLocation.addTelecom(dtt.tTEL2ContactPoint(tel));
				}
			}
		}

		// addr -> address
		if (cdaParticipantRole.getAddrs() != null && !cdaParticipantRole.getAddrs().isEmpty()) {
			for (AD ad : cdaParticipantRole.getAddrs()) {
				// Asserting that at most one address exists
				if (ad != null && !ad.isSetNullFlavor()) {
					fhirLocation.setAddress(dtt.AD2Address(ad));
				}
			}
		}

		return fhirLocation;
	}

	@Override
	public Bundle tPatientRole2Patient(PatientRole cdaPatientRole) {
		if (cdaPatientRole == null || cdaPatientRole.isSetNullFlavor())
			return null;

		Patient fhirPatient = new Patient();

		Bundle fhirPatientBundle = new Bundle();
		fhirPatientBundle.addEntry(new BundleEntryComponent().setResource(fhirPatient));

		// resource id
		IdType resourceId = new IdType("Patient", getUniqueId());
		fhirPatient.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirPatient.getMeta().addProfile(Constants.PROFILE_DAF_PATIENT);

		// id -> identifier
		if (cdaPatientRole.getIds() != null && !cdaPatientRole.getIds().isEmpty()) {
			for (II id : cdaPatientRole.getIds()) {
				if (id != null && !id.isSetNullFlavor()) {
					fhirPatient.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// addr -> address
		for (AD ad : cdaPatientRole.getAddrs()) {
			if (ad != null && !ad.isSetNullFlavor()) {
				fhirPatient.addAddress(dtt.AD2Address(ad));
			}
		}

		// telecom -> telecom
		for (TEL tel : cdaPatientRole.getTelecoms()) {
			if (tel != null && !tel.isSetNullFlavor()) {
				fhirPatient.addTelecom(dtt.tTEL2ContactPoint(tel));
			}
		}

		// providerOrganization -> managingOrganization
		if (cdaPatientRole.getProviderOrganization() != null
				&& !cdaPatientRole.getProviderOrganization().isSetNullFlavor()) {
			org.hl7.fhir.dstu3.model.Organization fhirOrganization = tOrganization2Organization(
					cdaPatientRole.getProviderOrganization());
			fhirPatientBundle.addEntry(new BundleEntryComponent().setResource(fhirOrganization));
			Reference organizationReference = getReference(fhirOrganization);
			fhirPatient.setManagingOrganization(organizationReference);
		}

		org.openhealthtools.mdht.uml.cda.Patient cdaPatient = cdaPatientRole.getPatient();

		if (cdaPatient != null && !cdaPatient.isSetNullFlavor()) {
			// patient.name -> name
			for (PN pn : cdaPatient.getNames()) {
				if (pn != null && !pn.isSetNullFlavor()) {
					fhirPatient.addName(dtt.tEN2HumanName(pn));
				}
			}

			// patient.administrativeGenderCode -> gender
			if (cdaPatient.getAdministrativeGenderCode() != null
					&& !cdaPatient.getAdministrativeGenderCode().isSetNullFlavor()
					&& cdaPatient.getAdministrativeGenderCode().getCode() != null
					&& !cdaPatient.getAdministrativeGenderCode().getCode().isEmpty()) {
				AdministrativeGender administrativeGender = vst.tAdministrativeGenderCode2AdministrativeGender(
						cdaPatient.getAdministrativeGenderCode().getCode());
				fhirPatient.setGender(administrativeGender);
			}

			// patient.birthTime -> birthDate
			if (cdaPatient.getBirthTime() != null && !cdaPatient.getBirthTime().isSetNullFlavor()) {
				fhirPatient.setBirthDateElement(dtt.tTS2Date(cdaPatient.getBirthTime()));
			}

			// patient.maritalStatusCode -> maritalStatus
			if (cdaPatient.getMaritalStatusCode() != null && !cdaPatient.getMaritalStatusCode().isSetNullFlavor()) {
				if (cdaPatient.getMaritalStatusCode().getCode() != null
						&& !cdaPatient.getMaritalStatusCode().getCode().isEmpty()) {
					fhirPatient.getMaritalStatus().addCoding(
							vst.tMaritalStatusCode2MaritalStatusCode(cdaPatient.getMaritalStatusCode().getCode()));
				}
			}

			// patient.languageCommunication -> communication
			for (LanguageCommunication LC : cdaPatient.getLanguageCommunications()) {
				if (LC != null && !LC.isSetNullFlavor()) {
					fhirPatient.addCommunication(tLanguageCommunication2Communication(LC));
				}
			}

			// patient.guardian -> contact
			for (org.openhealthtools.mdht.uml.cda.Guardian guardian : cdaPatient.getGuardians()) {
				if (guardian != null && !guardian.isSetNullFlavor()) {
					fhirPatient.addContact(tGuardian2Contact(guardian));
				}
			}

		}

		return fhirPatientBundle;
	}

	@Override
	public EntityResult tPerformer22Practitioner(Performer2 cdaPerformer2, IBundleInfo bundleInfo) {
		if (cdaPerformer2 == null || cdaPerformer2.isSetNullFlavor()) {
			return new EntityResult();
		}

		return tAssignedEntity2Practitioner(cdaPerformer2.getAssignedEntity(), bundleInfo);
	}

	public EntityResult tPerformer12Practitioner(Performer1 cdaPerformer1, IBundleInfo bundleInfo) {
		if (cdaPerformer1 == null || cdaPerformer1.isSetNullFlavor()) {
			return new EntityResult();
		}

		return tAssignedEntity2Practitioner(cdaPerformer1.getAssignedEntity(), bundleInfo);
	}

	@Override
	public EntryResult tProblemConcernAct2Condition(ProblemConcernAct cdaProblemConcernAct, IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaProblemConcernAct == null || cdaProblemConcernAct.isSetNullFlavor()) {
			return result;
		}

		// each problem observation instance -> FHIR Condition instance
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (ProblemObservation cdaProbObs : cdaProblemConcernAct.getProblemObservations()) {
			EntryResult er = tProblemObservation2Condition(cdaProbObs, localBundleInfo);
			localBundleInfo.updateFrom(er);
			result.updateEntitiesFrom(er);
			Bundle fhirProbObsBundle = er.getBundle();
			if (fhirProbObsBundle == null)
				continue;

			for (BundleEntryComponent entry : fhirProbObsBundle.getEntry()) {
				result.addResource(entry.getResource());
				if (entry.getResource() instanceof Condition) {
					Condition fhirCond = (Condition) entry.getResource();

					CS statusCode = cdaProblemConcernAct.getStatusCode();
					String statusCodeValue = statusCode == null || statusCode.isSetNullFlavor() ? null
							: statusCode.getCode();

					// statusCode -> verificationStatus
					fhirCond.setVerificationStatus(vst.tStatusCode2ConditionVerificationStatus(statusCodeValue));
				}
			}
		}

		return result;
	}

	@Override
	public EntryResult tProblemObservation2Condition(ProblemObservation cdaProbObs, IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaProbObs == null || cdaProbObs.isSetNullFlavor()) {
			return result;
		}

		// NOTE: Although DAF requires the mapping for severity, this data is not
		// available on the C-CDA side.
		// NOTE: Problem status template is deprecated in C-CDA Release 2.1; hence
		// status data is not retrieved from this template.

		Condition fhirCondition = new Condition();
		result.addResource(fhirCondition);

		// resource id
		IdType resourceId = new IdType("Condition", getUniqueId());
		fhirCondition.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirCondition.getMeta().addProfile(Constants.PROFILE_DAF_CONDITION);

		// patient
		fhirCondition.setSubject(getPatientRef());

		// id -> identifier
		for (II id : cdaProbObs.getIds()) {
			if (!id.isSetNullFlavor()) {
				fhirCondition.addIdentifier(dtt.tII2Identifier(id));
			}
		}

		// category -> problem-list-item
		Coding conditionCategory = new Coding().setSystem("http://hl7.org/fhir/condition-category");
		conditionCategory.setCode("problem-list-item");
		conditionCategory.setDisplay("Problem List Item");
		fhirCondition.addCategory().addCoding(conditionCategory);

		// value -> code
		if (cdaProbObs.getValues() != null && !cdaProbObs.getValues().isEmpty()) {
			for (ANY value : cdaProbObs.getValues()) {
				if (value != null) {
					if (value instanceof CD) {
						fhirCondition.setCode(dtt.tCD2CodeableConcept((CD) value, bundleInfo.getIdedAnnotations()));
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
			} else if (cdaProbObs.getEffectiveTime().getValue() != null
					&& !cdaProbObs.getEffectiveTime().getValue().isEmpty()) {
				fhirCondition.setOnset(dtt.tString2DateTime(cdaProbObs.getEffectiveTime().getValue()));
			}

			// high -> abatement
			if (high != null && !high.isSetNullFlavor()) {
				fhirCondition.setAbatement(dtt.tTS2DateTime(high));
			}
		}

		// onset and abatement -> clinicalStatus
		if (fhirCondition.getAbatement() != null) {
			fhirCondition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		} else {
			fhirCondition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		}

		// per spec will always have effectiveTime, so no need for verification status
		// adjustment (as in indication section).

		// author[0] -> asserter
		if (!cdaProbObs.getAuthors().isEmpty()) {
			if (cdaProbObs.getAuthors().get(0) != null && !cdaProbObs.getAuthors().get(0).isSetNullFlavor()) {
				Author author = cdaProbObs.getAuthors().get(0);
				EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
				result.updateFrom(entityResult);
				if (entityResult.hasPractitioner()) {
					fhirCondition.setAsserter(getReference(entityResult.getPractitioner()));
				}
				// author.time -> assertedDate
				if (author.getTime() != null && !author.getTime().isSetNullFlavor()) {
					fhirCondition.setAssertedDateElement(dtt.tTS2DateTime(author.getTime()));
				}
			}
		}

		return result;
	}

	@Override
	public EntryResult tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure,
			IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaProcedure == null || cdaProcedure.isSetNullFlavor()) {
			return result;
		}

		org.hl7.fhir.dstu3.model.Procedure fhirProc = new org.hl7.fhir.dstu3.model.Procedure();
		result.addResource(fhirProc);

		// resource id
		IdType resourceId = new IdType("Procedure", getUniqueId());
		fhirProc.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirProc.getMeta().addProfile(Constants.PROFILE_DAF_PROCEDURE);

		// subject
		fhirProc.setSubject(getPatientRef());

		// id -> identifier
		if (cdaProcedure.getIds() != null && !cdaProcedure.getIds().isEmpty()) {
			for (II id : cdaProcedure.getIds()) {
				if (id != null && !id.isSetNullFlavor()) {
					fhirProc.addIdentifier(dtt.tII2Identifier(id));
				}
			}
		}

		// effectiveTime -> performed
		if (cdaProcedure.getEffectiveTime() != null && !cdaProcedure.getEffectiveTime().isSetNullFlavor()) {
			fhirProc.setPerformed(dtt.tIVL_TS2Period(cdaProcedure.getEffectiveTime()));
		}

		// targetSiteCode -> bodySite
		if (cdaProcedure.getTargetSiteCodes() != null && !cdaProcedure.getTargetSiteCodes().isEmpty()) {
			for (CD cd : cdaProcedure.getTargetSiteCodes()) {
				if (cd != null && !cd.isSetNullFlavor()) {
					fhirProc.addBodySite(dtt.tCD2CodeableConcept(cd));
				}
			}
		}

		// performer -> performer
		for (Performer2 performer : cdaProcedure.getPerformers()) {
			if (performer.getAssignedEntity() != null && !performer.getAssignedEntity().isSetNullFlavor()) {
				EntityResult entityResult = tPerformer22Practitioner(performer, bundleInfo);
				result.updateFrom(entityResult);
				if (!entityResult.isEmpty()) {
					ProcedurePerformerComponent fhirPerformer = new ProcedurePerformerComponent();
					fhirProc.addPerformer(fhirPerformer);
					if (entityResult.hasPractitioner()) {
						fhirPerformer.setActor(getReference(entityResult.getPractitioner()));
					}
					if (entityResult.hasOrganization()) {
						fhirPerformer.setOnBehalfOf(entityResult.getOrganizationReference());
					}
					if (entityResult.hasPractitionerRoleCode()) {
						fhirPerformer.setRole(entityResult.getPractitionerRoleCode());
					}
				}
			}
		}

		// statusCode -> status
		if (cdaProcedure.getStatusCode() != null && !cdaProcedure.getStatusCode().isSetNullFlavor()
				&& cdaProcedure.getStatusCode().getCode() != null) {
			ProcedureStatus status = vst.tStatusCode2ProcedureStatus(cdaProcedure.getStatusCode().getCode());
			if (status != null) {
				fhirProc.setStatus(status);
			}
		}

		// code -> code
		CD code = cdaProcedure.getCode();
		if (code != null) {
			CodeableConcept cc = dtt.tCD2CodeableConcept(code, bundleInfo.getIdedAnnotations());
			if (cc != null) {
				fhirProc.setCode(cc);
			}
		}

		// encounter[0] -> context (per spec leave it to encounter section to create the
		// encounter resource)
		EList<org.openhealthtools.mdht.uml.cda.Encounter> encounters = cdaProcedure.getEncounters();
		if (!encounters.isEmpty()) {
			if (encounters.size() > 1) {
				logger.warn("Procudures cannot have multiple encounter. Only using first.");
			}
			org.openhealthtools.mdht.uml.cda.Encounter cdaEncounter = encounters.get(0);
			List<Identifier> identifiers = tIIs2Identifiers(cdaEncounter.getIds());
			if (!identifiers.isEmpty()) {
				if (identifiers.size() > 1) {
					logger.warn("Procudures encounter cannot have multiple ids. Only using first.");
				}
				IDeferredReference deferredReference = new DeferredProcedureEncounterReference(fhirProc,
						identifiers.get(0));
				result.addDeferredReference(deferredReference);
			}
		}

		List<EntryRelationship> relationships = cdaProcedure.getEntryRelationships();
		if (relationships != null) {
			for (EntryRelationship relationship : relationships) {
				Observation observation = relationship.getObservation();
				if (observation != null && observation instanceof Indication) {
					CodeableConcept cc = dtt.tCD2CodeableConcept(observation.getCode());
					if (cc != null) {
						fhirProc.addReasonCode(cc);
					}
					continue;
				}

				Act act = relationship.getAct();
				if (act != null && act instanceof CommentActivity) {
					String annotation = dtt.tED2Annotation(act.getText(), bundleInfo.getIdedAnnotations());
					if (annotation != null) {
						Annotation fhirAnnotation = new Annotation();
						fhirAnnotation.setText(annotation);
						fhirProc.addNote(fhirAnnotation);
					}
				}
			}
		}

		return result;
	}

	@Override
	public EntryResult tReactionObservation2Observation(ReactionObservation cdaReactionObservation,
			IBundleInfo bundleInfo) {
		return tObservation2Observation(cdaReactionObservation, bundleInfo);
	}

	@Override
	public ObservationReferenceRangeComponent tReferenceRange2ReferenceRange(
			org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange) {
		if (cdaReferenceRange == null || cdaReferenceRange.isSetNullFlavor())
			return null;

		ObservationReferenceRangeComponent fhirRefRange = new ObservationReferenceRangeComponent();

		// Notice that we get all the desired information from
		// cdaRefRange.ObservationRange
		// We may think of transforming ObservationRange instead of ReferenceRange
		if (cdaReferenceRange.getObservationRange() != null && !cdaReferenceRange.isSetNullFlavor()) {

			// low - high
			if (cdaReferenceRange.getObservationRange().getValue() != null
					&& !cdaReferenceRange.getObservationRange().getValue().isSetNullFlavor()) {
				if (cdaReferenceRange.getObservationRange().getValue() instanceof IVL_PQ) {
					IVL_PQ cdaRefRangeValue = ((IVL_PQ) cdaReferenceRange.getObservationRange().getValue());
					// low
					if (cdaRefRangeValue.getLow() != null && !cdaRefRangeValue.getLow().isSetNullFlavor()) {
						fhirRefRange.setLow(dtt.tPQ2SimpleQuantity(cdaRefRangeValue.getLow()));
					}
					// high
					if (cdaRefRangeValue.getHigh() != null && !cdaRefRangeValue.getHigh().isSetNullFlavor()) {
						fhirRefRange.setHigh(dtt.tPQ2SimpleQuantity(cdaRefRangeValue.getHigh()));
					}
				}
			}

			// observationRange.interpretationCode -> type
			if (cdaReferenceRange.getObservationRange().getInterpretationCode() != null
					&& !cdaReferenceRange.getObservationRange().getInterpretationCode().isSetNullFlavor()) {
				fhirRefRange.setType(
						dtt.tCD2CodeableConcept(cdaReferenceRange.getObservationRange().getInterpretationCode()));
			}

			// text.text -> text
			if (cdaReferenceRange.getObservationRange().getText() != null
					&& !cdaReferenceRange.getObservationRange().getText().isSetNullFlavor()) {
				if (cdaReferenceRange.getObservationRange().getText().getText() != null
						&& !cdaReferenceRange.getObservationRange().getText().getText().isEmpty()) {
					fhirRefRange.setText(cdaReferenceRange.getObservationRange().getText().getText());
				}
			}
		}

		return fhirRefRange;
	}

	@Override
	public EntryResult tResultObservation2Observation(ResultObservation cdaResultObservation, IBundleInfo bundleInfo) {
		EntryResult result = tObservation2Observation(cdaResultObservation, bundleInfo);
		Bundle fhirObservationBundle = result.getBundle();
		if (fhirObservationBundle == null) {
			return result;
		}

		// finding the observation resource and setting its meta.profile to result
		// observation's profile url
		if (Config.isGenerateDafProfileMetadata()) {
			for (BundleEntryComponent entry : fhirObservationBundle.getEntry()) {
				if (entry.getResource() instanceof Observation) {
					(entry.getResource()).getMeta().addProfile(Constants.PROFILE_DAF_RESULT_OBS);
				}
			}
		}

		return result;
	}

	@Override
	public EntryResult tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer, IBundleInfo bundleInfo) {
		EntryResult result = new EntryResult();

		if (cdaResultOrganizer == null || cdaResultOrganizer.isSetNullFlavor()) {
			return result;
		}

		DiagnosticReport fhirDiagReport = new DiagnosticReport();
		result.addResource(fhirDiagReport);

		// resource id
		IdType resourceId = new IdType("DiagnosticReport", getUniqueId());
		fhirDiagReport.setId(resourceId);

		// meta.profile
		if (Config.isGenerateDafProfileMetadata())
			fhirDiagReport.getMeta().addProfile(Constants.PROFILE_DAF_DIAGNOSTIC_REPORT);

		// subject
		fhirDiagReport.setSubject(getPatientRef());

		// Although DiagnosticReport.request(DiagnosticOrder) is needed by daf, no
		// information exists in CDA side to fill that field.

		// id -> identifier
		if (cdaResultOrganizer.getIds() != null && !cdaResultOrganizer.getIds().isEmpty()) {
			for (II ii : cdaResultOrganizer.getIds()) {
				if (ii != null && !ii.isSetNullFlavor()) {
					fhirDiagReport.addIdentifier(dtt.tII2Identifier(ii));
				}
			}
		}

		// code -> code
		if (cdaResultOrganizer.getCode() != null) {
			fhirDiagReport
					.setCode(dtt.tCD2CodeableConcept(cdaResultOrganizer.getCode(), bundleInfo.getIdedAnnotations()));
		}

		// statusCode -> status
		if (cdaResultOrganizer.getStatusCode() != null && !cdaResultOrganizer.isSetNullFlavor()) {
			fhirDiagReport.setStatus(vst
					.tResultOrganizerStatusCode2DiagnosticReportStatus(cdaResultOrganizer.getStatusCode().getCode()));
		}

		// effectiveTime -> effective
		if (cdaResultOrganizer.getEffectiveTime() != null && !cdaResultOrganizer.getEffectiveTime().isSetNullFlavor()) {
			fhirDiagReport.setEffective(dtt.tIVL_TS2Period(cdaResultOrganizer.getEffectiveTime()));
		}

		// author -> performer
		if (cdaResultOrganizer.getAuthors() != null && !cdaResultOrganizer.getAuthors().isEmpty()) {
			for (org.openhealthtools.mdht.uml.cda.Author author : cdaResultOrganizer.getAuthors()) {
				// Asserting that at most one author exists
				if (author != null && !author.isSetNullFlavor()) {
					EntityResult entityResult = tAuthor2Practitioner(author, bundleInfo);
					result.updateFrom(entityResult);
					// TODO: what about role?
					if (entityResult.hasPractitioner()) {
						fhirDiagReport.addPerformer().setActor(getReference(entityResult.getPractitioner()));
					}
				}
			}

		}

		// ResultObservation -> result
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (ResultObservation cdaResultObs : cdaResultOrganizer.getResultObservations()) {
			if (!cdaResultObs.isSetNullFlavor()) {
				EntryResult er = tResultObservation2Observation(cdaResultObs, localBundleInfo);
				localBundleInfo.updateFrom(er);
				result.updateEntitiesFrom(er);
				Bundle fhirObsBundle = er.getBundle();
				if (fhirObsBundle != null) {
					for (BundleEntryComponent entry : fhirObsBundle.getEntry()) {
						result.addResource(entry.getResource());
						if (entry.getResource() instanceof org.hl7.fhir.dstu3.model.Observation) {

							Reference resultRef = new Reference();
							resultRef.setReference(entry.getResource().getId());
							String referenceString = ReferenceInfo.getDisplay(entry.getResource());
							if (referenceString != null) {
								resultRef.setDisplay(referenceString);
							}
							fhirDiagReport.addResult(resultRef);

						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public SectionComponent tSection2Section(Section cdaSection) {
		if (cdaSection == null || cdaSection.isSetNullFlavor()) {
			return null;
		}

		SectionComponent fhirSec = new SectionComponent();

		// title -> title.text
		if (cdaSection.getTitle() != null && !cdaSection.getTitle().isSetNullFlavor()) {
			if (cdaSection.getTitle().getText() != null && !cdaSection.getTitle().getText().isEmpty()) {
				fhirSec.setTitle(cdaSection.getTitle().getText());
			}
		}

		// code -> code
		if (cdaSection.getCode() != null && !cdaSection.getCode().isSetNullFlavor()) {
			fhirSec.setCode(dtt.tCD2CodeableConcept(cdaSection.getCode()));
		}

		// text -> text
		if (cdaSection.getText() != null) {
			Narrative fhirText = dtt.tStrucDocText2Narrative(cdaSection.getText());
			if (fhirText != null && Config.getGenerateNarrative())
				fhirSec.setText(fhirText);
		}

		fhirSec.setMode(SectionMode.SNAPSHOT);

		return fhirSec;
	}

	@Override
	public org.hl7.fhir.dstu3.model.Location tServiceDeliveryLocation2Location(ServiceDeliveryLocation cdaSDLOC) {
		/*
		 * ServiceDeliveryLocation is a ParticipantRole instance with a specific
		 * templateId Therefore, tParticipantRole2Location should satisfy the necessary
		 * mapping
		 */
		return tParticipantRole2Location(cdaSDLOC);
	}

	@Override
	public org.hl7.fhir.dstu3.model.Device tSupply2Device(org.openhealthtools.mdht.uml.cda.Supply cdaSupply) {
		if (cdaSupply == null || cdaSupply.isSetNullFlavor())
			return null;

		ProductInstance productInstance = null;
		// getting productInstance from cdaSupply.participant.participantRole
		if (cdaSupply.getParticipants() != null && !cdaSupply.getParticipants().isEmpty()) {
			for (Participant2 participant : cdaSupply.getParticipants()) {
				if (participant != null && !participant.isSetNullFlavor()) {
					if (participant.getParticipantRole() != null
							&& !participant.getParticipantRole().isSetNullFlavor()) {
						if (participant.getParticipantRole() instanceof ProductInstance) {
							productInstance = (ProductInstance) participant.getParticipantRole();
						}
					}
				}
			}
		}

		if (productInstance == null)
			return null;

		org.hl7.fhir.dstu3.model.Device fhirDev = new org.hl7.fhir.dstu3.model.Device();

		// resource id
		IdType resourceId = new IdType("Device", getUniqueId());
		fhirDev.setId(resourceId);

		// patient
		fhirDev.setPatient(getPatientRef());

		// productInstance.id -> identifier
		for (II id : productInstance.getIds()) {
			if (!id.isSetNullFlavor())
				fhirDev.addIdentifier(dtt.tII2Identifier(id));
		}

		// productInstance.playingDevice.code -> type
		if (productInstance.getPlayingDevice() != null && !productInstance.getPlayingDevice().isSetNullFlavor()) {
			if (productInstance.getPlayingDevice().getCode() != null
					&& !productInstance.getPlayingDevice().getCode().isSetNullFlavor()) {
				fhirDev.setType(dtt.tCD2CodeableConcept(productInstance.getPlayingDevice().getCode()));
			}
		}

		return fhirDev;
	}

	@Override
	public EntryResult tVitalSignObservation2Observation(VitalSignObservation cdaVitalSignObservation,
			IBundleInfo bundleInfo) {
		EntryResult result = tObservation2Observation(cdaVitalSignObservation, bundleInfo);
		Bundle fhirObservationBundle = result.getBundle();
		if (fhirObservationBundle == null) {
			return result;
		}

		// finding the observation resource and setting its meta.profile to result
		// observation's profile url
		if (Config.isGenerateDafProfileMetadata()) {
			for (BundleEntryComponent entry : fhirObservationBundle.getEntry()) {
				if (entry.getResource() instanceof Observation) {
					(entry.getResource()).getMeta().addProfile(Constants.PROFILE_DAF_VITAL_SIGNS);
				}
			}
		}

		return result;
	}

	public DocumentReference tDocumentReference(String documentBody) {

		DocumentReference docReference = new DocumentReference();

		// set id
		docReference.setId(new IdType("DocumentReference", getUniqueId()));

		// status -> current
		docReference.setStatus(DocumentReferenceStatus.CURRENT);

		// type -> 34133-9 (hard-coded from specification)
		CodeableConcept docType = new CodeableConcept();
		Coding docTypeCoding = new Coding();
		docTypeCoding.setCode("34133-9");
		docTypeCoding.setSystem("2.16.840.1.113883.6.1");
		docTypeCoding.setDisplay("Summarization of Episode Note");
		docType.addCoding(docTypeCoding);
		docReference.setType(docType);

		// attachment
		Attachment docAttachment = new Attachment();
		docAttachment.setContentType("text/plain");

		// attachment doc
		Base64BinaryType doc64 = new Base64BinaryType(Base64.encode(documentBody.getBytes()));
		docAttachment.setDataElement(doc64);

		// attachment hash
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			byte[] encodedhash = digest.digest(documentBody.getBytes());
			docAttachment.setHashElement(new Base64BinaryType(Base64.encode(encodedhash)));
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.toString());
		}

		// set attachment
		DocumentReferenceContentComponent docContent = new DocumentReferenceContentComponent();
		docContent.setAttachment(docAttachment);
		docReference.addContent(docContent);

		return docReference;

	}

	public Device tDevice(Identifier assemblerDevice) {
		Device device = new Device();
		device.setStatus(FHIRDeviceStatus.ACTIVE);
		device.addIdentifier(assemblerDevice);

		Narrative deviceNarrative = new Narrative().setStatus(NarrativeStatus.GENERATED);
		deviceNarrative.setDivAsString(assemblerDevice.getValue());
		device.setText(deviceNarrative);

		device.setId(new IdType("Device", getUniqueId()));
		return device;
	}

	@Override
	public Bundle tProvenance(Bundle bundle, String documentBody, Identifier assemblerDevice) {
		Provenance provenance = new Provenance();
		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		provenance.setId(new IdType("Provenance", getUniqueId()));

		DocumentReference documentReference = tDocumentReference(documentBody);
		bundle.addEntry(new BundleEntryComponent().setResource(documentReference));
		ProvenanceEntityComponent pec = new ProvenanceEntityComponent();
		pec.setRole(ProvenanceEntityRole.SOURCE);
		pec.setId(documentReference.getId());
		provenance.addEntity(pec);

		Device device = tDevice(assemblerDevice);
		bundle.addEntry(new BundleEntryComponent().setResource(device));

		Coding agentTypeCoding = new Coding(ProvenanceAgentType.DEVICE.getSystem(), ProvenanceAgentType.DEVICE.toCode(),
				ProvenanceAgentType.DEVICE.getDisplay());
		agentTypeCoding.setId(device.getId());
		pac.setRelatedAgentType(new CodeableConcept().addCoding(agentTypeCoding));

		Coding agentRoleCoding = new Coding(ProvenanceAgentRole.ASSEMBLER.getSystem(),
				ProvenanceAgentRole.ASSEMBLER.toCode(), ProvenanceAgentRole.ASSEMBLER.getDisplay());
		agentRoleCoding.setId(device.getId());
		pac.addRole(new CodeableConcept().addCoding(agentRoleCoding));

		pac.setWho(getReference(device));
		provenance.addAgent(pac);

		for (BundleEntryComponent bec : bundle.getEntry()) {
			provenance.addTarget(getReference(bec.getResource()));
		}

		bundle.addEntry(new BundleEntryComponent().setResource(provenance));
		return bundle;
	}

}