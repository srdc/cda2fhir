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

import org.hl7.fhir.dstu3.model.Age;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.Group;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Substance;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Entity;
import org.openhealthtools.mdht.uml.cda.LanguageCommunication;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationInformation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ServiceDeliveryLocation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public interface IResourceTransformer {

	/**
	 * Transforms a CDA AgeObservation instance to a FHIR Age composite datatype
	 * instance
	 *
	 * @param cdaAgeObservation A CDA AgeObservation instance
	 * @return A FHIR Age composite datatype instance
	 */
	Age tAgeObservation2Age(org.openhealthtools.mdht.uml.cda.consol.AgeObservation cdaAgeObservation);

	/**
	 * Transforms a CDA AllergyProblemAct instance to a FHIR AllergyIntolerance
	 * resource.
	 *
	 * @param cdaAllergyProblemAct A CDA AllergyProblemAct instance
	 * @return A FHIR Bundle that contains the AllergyIntolerance as the first
	 *         entry, which can also include other referenced resources such as
	 *         Practitioner
	 */
	IEntryResult tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProblemAct, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA AssignedAuthor instance to a FHIR Practitioner resource.
	 *
	 * @param cdaAssignedAuthor A CDA AssignedAuthor instance
	 * @return A result object that might include FHIR Bundle and/or a reference to
	 *         a Practitioner
	 */
	IEntityResult tAssignedAuthor2Practitioner(AssignedAuthor cdaAssignedAuthor, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA AssignedEntity instance to a FHIR Practitioner resource.
	 *
	 * @param cdaAssignedEntity A CDA AssignedEntity instance
	 * @return A result object that might include FHIR Bundle and/or a reference to
	 *         a Practitioner
	 */
	IEntityResult tAssignedEntity2Practitioner(AssignedEntity cdaAssignedEntity, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Author instance to a FHIR Practitioner resource.
	 *
	 * @param cdaAuthor A CDA Author instance
	 * @return A result object that might include FHIR Bundle and/or a reference to
	 *         a Practitioner
	 */
	IEntityResult tAuthor2Practitioner(org.openhealthtools.mdht.uml.cda.Author cdaAuthor, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA CD instance to a FHIR Substance resource.
	 *
	 * @param cdaSubstanceCode A CDA CD instance
	 * @return A FHIR Substance resource
	 */
	Substance tCD2Substance(CD cdaSubstanceCode);

	/**
	 * Transforms a CDA ClicinalDocument instance to a FHIR Composition resource.
	 *
	 * @param cdaClinicalDocument A CDA ClicinalDocument instance
	 * @return A FHIR Bundle that contains the Composition as the first entry, which
	 *         can also include other referenced resources such as Patient,
	 *         Practitioner, Organization
	 */
	IEntryResult tClinicalDocument2Composition(ClinicalDocument cdaClinicalDocument);

	/**
	 * Transforms a CDA ClicinalDocument instance to a FHIR Bundle resource.
	 *
	 * @param cdaClinicalDocument A CDA ClicinalDocument instance
	 * @param includeComposition  Include composition resource or not
	 * @return A FHIR Bundle
	 */

	IEntryResult tClinicalDocument2Bundle(ClinicalDocument cdaClinicalDocument, boolean includeComposition);

	/**
	 * Transforms a CDA CustodianOrganization instance to a FHIR Organization
	 * resource.
	 *
	 * @param cdaCustodianOrganization A CDA CustodianOrganization instance
	 * @return A FHIR Organization resource
	 */
	org.hl7.fhir.dstu3.model.Organization tCustodianOrganization2Organization(
			org.openhealthtools.mdht.uml.cda.CustodianOrganization cdaCustodianOrganization);

	/**
	 * Transforms a CDA EncounterActivity instance to a FHIR Encounter resource.
	 *
	 * @param cdaEncounterActivity A CDA EncounterActivity instance
	 * @return A FHIR Bundle that contains the Encounter as the first entry, which
	 *         can also include other referenced resources such as Practitioner,
	 *         Location
	 */
	IEntryResult tEncounterActivity2Encounter(
			org.openhealthtools.mdht.uml.cda.consol.EncounterActivities cdaEncounterActivity, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Entity instance to a FHIR Group resource.
	 *
	 * @param cdaEntity A CDA Entity instance
	 * @return A FHIR Group resource
	 */
	Group tEntity2Group(Entity cdaEntity);

	/**
	 * Transforms a CDA FamilyHistoryOrganizer instance to a FHIR
	 * FamilyMemberHistory resource.
	 *
	 * @param cdaFamilyHistoryOrganizer A CDA FamilyHistoryOrganizer instance
	 * @return A FHIR FamilyMemberHistory resource
	 */
	FamilyMemberHistory tFamilyHistoryOrganizer2FamilyMemberHistory(FamilyHistoryOrganizer cdaFamilyHistoryOrganizer);

	/**
	 * Transforms a CDA Observation instance that is included in Functional Status
	 * Section to a FHIR Observation resource.
	 *
	 * @param cdaObservation A CDA Observation instance that is included in
	 *                       Functional Status Section
	 * @return A FHIR Bundle that contains the Observation as the first entry, which
	 *         can also include other referenced resources such as Practitioner
	 */
	IEntryResult tFunctionalStatus2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation,
			IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Guardian instance to a FHIR Patient.Contact resource.
	 *
	 * @param cdaGuardian A CDA Guardian instance
	 * @return A FHIR Patient.Contact resource
	 */
	org.hl7.fhir.dstu3.model.Patient.ContactComponent tGuardian2Contact(
			org.openhealthtools.mdht.uml.cda.Guardian cdaGuardian);

	/**
	 * Transforms a CDA ImmunizationActivity instance to a FHIR Immunization
	 * resource.
	 *
	 * @param cdaImmunizationActivity A CDA ImmunizationActivity instance
	 * @return A FHIR Bundle that contains the Immunization as the first entry,
	 *         which can also include other referenced resources such as
	 *         Organization, Practitioner
	 */
	IEntryResult tImmunizationActivity2Immunization(ImmunizationActivity cdaImmunizationActivity,
			IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Indication instance to a FHIR Condition resource.
	 *
	 * @param cdaIndication A CDA Indication instance
	 * @return A FHIR Condition resource
	 */
	Condition tIndication2Condition(Indication cdaIndication);

	/**
	 * Transforms a CDA LanguageCommunication instance to a FHIR Communication
	 * resource.
	 *
	 * @param cdaLanguageCommunication A CDA LanguageCommunication instance
	 * @return A FHIR Communication resource
	 */
	PatientCommunicationComponent tLanguageCommunication2Communication(LanguageCommunication cdaLanguageCommunication);

	/**
	 * Transforms a CDA ManufacturedProduct instance to a FHIR Medication resource.
	 *
	 * @param cdaManufacturedProduct A CDA ManufacturedProduct instance
	 * @return A FHIR Bundle that contains the Medication as the first entry, which
	 *         can also include other referenced resources such as Substance,
	 *         Organization
	 */
	Bundle tManufacturedProduct2Medication(ManufacturedProduct cdaManufacturedProduct);

	/**
	 * Transforms a CDA MedicationActivity instance to a FHIR MedicationStatement
	 * resource.
	 *
	 * @param cdaMedicationActivity A CDA MedicationActivity instance
	 * @return A FHIR Bundle that contains the MedicationStatement as the first
	 *         entry, which can also include other referenced resources such as
	 *         Practitioner, Medication, Condition
	 */
	IEntryResult tMedicationActivity2MedicationStatement(MedicationActivity cdaMedicationActivity,
			IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA MedicationDispense instance to a FHIR MedicationDispense
	 * resource.
	 *
	 * @param cdaMedicationDispense A CDA MedicationDispense instance
	 * @return A FHIR Bundle that contains the MedicationDispense as the first
	 *         entry, which can also include other referenced resources such as
	 *         Medication, Practitioner
	 */
	IEntryResult tMedicationDispense2MedicationDispense(
			org.openhealthtools.mdht.uml.cda.consol.MedicationDispense cdaMedicationDispense, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA MedicationInformation instance to a FHIR Medication
	 * resource.
	 *
	 * @param cdaMedicationInformation A CDA MedicationInformation instance
	 * @return A FHIR Bundle that contains the Medication as the first entry, which
	 *         can also include other referenced resources such as Substance,
	 *         Organization
	 */
	Bundle tMedicationInformation2Medication(MedicationInformation cdaMedicationInformation);

	/**
	 * Transforms a CDA Observation instance to a FHIR Observation resource.
	 *
	 * @param cdaObservation A CDA Observation instance
	 * @return A FHIR Bundle that contains the Observation as the first entry, which
	 *         can also include other referenced resources such as Encounter,
	 *         Practitioner
	 */
	IEntryResult tObservation2Observation(org.openhealthtools.mdht.uml.cda.Observation cdaObservation,
			IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Organization instance to a FHIR Organization resource.
	 *
	 * @param cdaOrganization A CDA Organization instance
	 * @return A FHIR Organization resource
	 */
	org.hl7.fhir.dstu3.model.Organization tOrganization2Organization(
			org.openhealthtools.mdht.uml.cda.Organization cdaOrganization);

	/**
	 * Transforms a CDA ParticipantRole instance to a FHIR Location resource.
	 *
	 * @param cdaParticipantRole A CDA ParticipantRole instance
	 * @return A FHIR Location Resource
	 */
	org.hl7.fhir.dstu3.model.Location tParticipantRole2Location(ParticipantRole cdaParticipantRole);

	/**
	 * Transforms a CDA PatientRole instance to a FHIR Patient resource.
	 *
	 * @param cdaPatientRole A CDA PatientRole instance
	 * @return A FHIR Bundle that contains the PatientRole as the first entry, which
	 *         can also include other referenced resources such as Organization
	 */
	Bundle tPatientRole2Patient(PatientRole cdaPatientRole);

	/**
	 * Transforms a CDA Performer2 instance to a FHIR Practitioner resource.
	 *
	 * @param cdaPerformer2 A CDA Performer2 instance
	 * @return A FHIR Bundle that contains the Practitioner as the first entry,
	 *         which can also include other referenced resources such as
	 *         Organization
	 */
	IEntityResult tPerformer22Practitioner(Performer2 cdaPerformer2, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA ProblemConcernAct instance to FHIR Condition resource(s). A
	 * ProblemConcernAct might include several Problem Observations, and each
	 * Problem Observation corresponds to a FHIR Condition. Therefore, the returning
	 * Bundle can contain several FHIR Conditions.
	 *
	 * @param cdaProblemConcernAct A CDA ProblemConcernAct instance
	 * @return A FHIR Bundle that contains the corresponding Condition(s), and
	 *         further referenced resources such as Encounter, Practitioner
	 */
	IEntryResult tProblemConcernAct2Condition(ProblemConcernAct cdaProblemConcernAct, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA ProblemObservation instance to FHIR Condition resource.
	 *
	 * @param cdaProbObs A CDA ProblemObservation instance
	 * @return A FHIR Bundle that contains the Condition as the first entry, which
	 *         can also include other referenced resources such as Encounter,
	 *         Practitioner
	 */
	IEntryResult tProblemObservation2Condition(ProblemObservation cdaProbObs, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Procedure instance to a FHIR Procedure and supporting
	 * resources.
	 *
	 * @param cdaProcedure    A CDA Procedure instance
	 * @param idedAnnotations Annotations that can be referenced
	 * @return A result object that contains a FHIR Bundle and supporting
	 *         information
	 */
	IEntryResult tProcedure2Procedure(org.openhealthtools.mdht.uml.cda.Procedure cdaProcedure, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA ReactionObservation instance to a FHIR Observation resource.
	 *
	 * @param cdaReactionObservation A CDA ReactionObservation instance
	 * @return A FHIR Bundle that contains the Observation as the first entry, which
	 *         can also include other referenced resources such as Encounter,
	 *         Practitioner
	 */
	IEntryResult tReactionObservation2Observation(ReactionObservation cdaReactionObservation, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA ReferenceRange instance to a FHIR
	 * ObservationReferenceRangeComponent resource.
	 *
	 * @param cdaReferenceRange A CDA ReferenceRange instance
	 * @return A FHIR ObservationReferenceRangeComponent resource
	 */
	ObservationReferenceRangeComponent tReferenceRange2ReferenceRange(
			org.openhealthtools.mdht.uml.cda.ReferenceRange cdaReferenceRange);

	/**
	 * Transforms a CDA ResultObservation instance to a FHIR Observation resource.
	 *
	 * @param cdaResultObservation A CDA ResultObservation instance
	 * @return A FHIR Bundle that contains the Observation as the first entry, which
	 *         can also include other referenced resources such as Encounter,
	 *         Practitioner
	 */
	IEntryResult tResultObservation2Observation(ResultObservation cdaResultObservation, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA ResultOrganizer instance to a FHIR DiagnosticReport
	 * resource.
	 *
	 * @param cdaResultOrganizer A CDA ResultOrganizer instance
	 * @return A FHIR Bundle that contains the DiagnosticReport as the first entry,
	 *         which can also include other referenced resources such as
	 *         Practitioner, Observation
	 */
	IEntryResult tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer, IBundleInfo bundleInfo);

	/**
	 * Transforms a CDA Section instance to a FHIR SectionComponent resource.
	 *
	 * @param cdaSection A CDA Section instance
	 * @return A FHIR SectionComponent resource
	 */
	SectionComponent tSection2Section(Section cdaSection);

	/**
	 * Transforms a CDA ServiceDeliveryLocation instance to a FHIR Location
	 * resource.
	 *
	 * @param cdaSDLOC A CDA ServiceDeliveryLocation instance
	 * @return A FHIR Location resource
	 */
	org.hl7.fhir.dstu3.model.Location tServiceDeliveryLocation2Location(ServiceDeliveryLocation cdaSDLOC);

	/**
	 * Transforms a CDA Supply instance to a FHIR Device resource.
	 *
	 * @param cdaSupply A CDA Supply instance
	 * @return A FHIR Device resource
	 */
	org.hl7.fhir.dstu3.model.Device tSupply2Device(org.openhealthtools.mdht.uml.cda.Supply cdaSupply);

	/**
	 * Transforms a CDA VitalSignObservation to a FHIR Observation resource.
	 *
	 * @param cdaVitalSignObservation A CDA VitalSignObservation instance
	 * @return An EntryResult object containing a FHIR Bundle with the Observation
	 *         as the first entry, which can also include other referenced resources
	 *         such as Encounter, Practitioner
	 */
	IEntryResult tVitalSignObservation2Observation(VitalSignObservation cdaVitalSignObservation,
			IBundleInfo bundleInfo);

	/**
	 *
	 * @param resource A fhir resource for which a reference is needed
	 * @return a reference to the fhir resource parameter
	 */
	Reference getReference(Resource resource);

	/**
	 *
	 * @param bundle
	 * @param encodedBody
	 * @param assemblerDevice
	 * @return
	 */
	Bundle tProvenance(Bundle bundle, String encodedBody, Identifier assemblerDevice);

}
