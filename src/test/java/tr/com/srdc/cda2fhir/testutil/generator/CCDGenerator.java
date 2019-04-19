package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttestationMode;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttesterComponent;
import org.hl7.fhir.dstu3.model.Composition.CompositionEventComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Authenticator;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Component2;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.Custodian;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.DocumentationOf;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.LegalAuthenticator;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.RecordTarget;
import org.openhealthtools.mdht.uml.cda.StructuredBody;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CCDGenerator {
	private IDGenerator idGenerator;
	private TSGenerator effectiveTimeGenerator;
	private CEGenerator codeGenerator;
	private STGenerator titleGenerator;
	private CEGenerator confidentialityGenerator;

	private AuthorGenerator authorGenerator;
	private AssignedEntityGenerator legalAuthenticatorGenerator;
	private TSGenerator legalAuthenticatorTimeGenerator;
	private List<AssignedEntityGenerator> authenticatorGenerators = new ArrayList<>();
	private List<TSGenerator> authenticatorTimeGenerators = new ArrayList<>();
	private List<DocumentationOfGenerator> documentOfGenerators = new ArrayList<>();
	private CustodianOrganizationGenerator organizationGenerator;

	private PatientRoleGenerator patientRoleGenerator;

	private List<AllergyConcernActGenerator> allergyGenerators = new ArrayList<>();
	private List<MedicationActivityGenerator> medActivityGenerators = new ArrayList<>();
	private List<ImmunizationActivityGenerator> immActivityGenerators = new ArrayList<>();
	private List<ResultOrganizerGenerator> resultGenerators = new ArrayList<>();
	private List<VitalSignsOrganizerGenerator> vitalsGenerators = new ArrayList<>();
	private List<ProblemConcernActGenerator> problemListGenerators = new ArrayList<>();
	private List<ProcedureActivityProcedureGenerator> procedureGenerators = new ArrayList<>();
	private List<EncounterActivityGenerator> encounterGenerators = new ArrayList<>();

	public ContinuityOfCareDocument generate(CDAFactories factories) {
		ContinuityOfCareDocument ccd = factories.consol.createContinuityOfCareDocument();

		if (idGenerator != null) {
			ccd.setId(idGenerator.generate(factories));
		}

		if (effectiveTimeGenerator != null) {
			ccd.setEffectiveTime(effectiveTimeGenerator.generate(factories));
		}

		if (codeGenerator != null) {
			ccd.setCode(codeGenerator.generate(factories));
		}

		if (titleGenerator != null) {
			ccd.setTitle(titleGenerator.generate(factories));
		}

		if (confidentialityGenerator != null) {
			CE ce = confidentialityGenerator.generate(factories);
			ccd.setConfidentialityCode(ce);
		}

		if (authorGenerator != null) {
			Author author = authorGenerator.generate(factories);
			ccd.getAuthors().add(author);
		}

		if (legalAuthenticatorGenerator != null) {
			LegalAuthenticator legalAuthenticator = factories.base.createLegalAuthenticator();
			AssignedEntity ae = legalAuthenticatorGenerator.generate(factories);
			legalAuthenticator.setAssignedEntity(ae);
			if (legalAuthenticatorTimeGenerator != null) {
				legalAuthenticator.setTime(legalAuthenticatorTimeGenerator.generate(factories));
			}
			ccd.setLegalAuthenticator(legalAuthenticator);
		}

		for (int index = 0; index < authenticatorGenerators.size(); ++index) {
			AssignedEntityGenerator ag = authenticatorGenerators.get(index);
			Authenticator authenticator = factories.base.createAuthenticator();
			AssignedEntity ae = ag.generate(factories);
			authenticator.setAssignedEntity(ae);

			TSGenerator tsg = authenticatorTimeGenerators.get(index);
			if (tsg != null) {
				authenticator.setTime(tsg.generate(factories));
			}

			ccd.getAuthenticators().add(authenticator);
		}

		documentOfGenerators.forEach(dog -> {
			DocumentationOf docOf = dog.generate(factories);
			ccd.getDocumentationOfs().add(docOf);
		});

		if (organizationGenerator != null) {
			Custodian custodian = factories.base.createCustodian();
			AssignedCustodian assignedCustodian = factories.base.createAssignedCustodian();
			CustodianOrganization custodianOrganization = organizationGenerator.generate(factories);
			assignedCustodian.setRepresentedCustodianOrganization(custodianOrganization);
			custodian.setAssignedCustodian(assignedCustodian);
			ccd.setCustodian(custodian);
		}

		if (patientRoleGenerator != null) {
			PatientRole patientRole = patientRoleGenerator.generate(factories);
			RecordTarget recordTarget = factories.base.createRecordTarget();
			recordTarget.setPatientRole(patientRole);
			ccd.getRecordTargets().add(recordTarget);
		}

		if (!allergyGenerators.isEmpty()) {
			AllergiesSection section = factories.consol.createAllergiesSection();
			CE ce = factories.datatype.createCE("48765-2", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.6.1");
			section.getTemplateIds().add(ii);
			allergyGenerators.forEach(ag -> {
				AllergyProblemAct act = ag.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setAct(act);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!medActivityGenerators.isEmpty()) {
			MedicationsSection section = factories.consol.createMedicationsSection();
			CE ce = factories.datatype.createCE("10160-0", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.1.1");
			section.getTemplateIds().add(ii);
			medActivityGenerators.forEach(ma -> {
				SubstanceAdministration sa = ma.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setSubstanceAdministration(sa);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!immActivityGenerators.isEmpty()) {
			ImmunizationsSection section = factories.consol.createImmunizationsSection();
			CE ce = factories.datatype.createCE("11369-6", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.2.1");
			section.getTemplateIds().add(ii);
			immActivityGenerators.forEach(immg -> {
				SubstanceAdministration sa = immg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setSubstanceAdministration(sa);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!resultGenerators.isEmpty()) {
			ResultsSection section = factories.consol.createResultsSection();
			CE ce = factories.datatype.createCE("30954-2", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.3.1");
			section.getTemplateIds().add(ii);
			resultGenerators.forEach(rg -> {
				ResultOrganizer sa = rg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setOrganizer(sa);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!vitalsGenerators.isEmpty()) {
			VitalSignsSection section = factories.consol.createVitalSignsSection();
			CE ce = factories.datatype.createCE("8716-3", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.4.1");
			section.getTemplateIds().add(ii);
			vitalsGenerators.forEach(vg -> {
				VitalSignsOrganizer vso = vg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setOrganizer(vso);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!problemListGenerators.isEmpty()) {
			ProblemSection section = factories.consol.createProblemSection();
			CE ce = factories.datatype.createCE("11450-4", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.5.1");
			section.getTemplateIds().add(ii);
			problemListGenerators.forEach(pg -> {
				ProblemConcernAct pca = pg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setAct(pca);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!procedureGenerators.isEmpty()) {
			ProceduresSection section = factories.consol.createProceduresSection();
			CE ce = factories.datatype.createCE("47519-4", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.7.1");
			section.getTemplateIds().add(ii);
			procedureGenerators.forEach(pg -> {
				ProcedureActivityProcedure pa = pg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setProcedure(pa);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		if (!encounterGenerators.isEmpty()) {
			EncountersSection section = factories.consol.createEncountersSection();
			CE ce = factories.datatype.createCE("46240-8", "2.16.840.1.113883.6.1");
			section.setCode(ce);
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.2.22.1");
			section.getTemplateIds().add(ii);
			encounterGenerators.forEach(eg -> {
				EncounterActivities ee = eg.generate(factories);
				Entry entry = factories.base.createEntry();
				entry.setEncounter(ee);
				section.getEntries().add(entry);
			});
			Component3 component3 = factories.base.createComponent3();
			component3.setSection(section);
			if (ccd.getComponent() == null) {
				Component2 component2 = factories.base.createComponent2();
				StructuredBody structuredBody = factories.base.createStructuredBody();
				component2.setStructuredBody(structuredBody);
				ccd.setComponent(component2);
				structuredBody.getComponents().add(component3);
			} else {
				ccd.getComponent().getStructuredBody().getComponents().add(component3);
			}
		}

		return ccd;
	}

	public static CCDGenerator getDefaultInstance() {
		CCDGenerator generator = new CCDGenerator();

		generator.idGenerator = IDGenerator.getNextInstance();
		generator.effectiveTimeGenerator = TSGenerator.getNextInstance();
		generator.codeGenerator = CEGenerator.getNextInstance();
		generator.titleGenerator = STGenerator.getNextInstance();
		generator.confidentialityGenerator = new CEGenerator("L");
		generator.authorGenerator = AuthorGenerator.getDefaultInstance();
		generator.legalAuthenticatorGenerator = AssignedEntityGenerator.getDefaultInstance();
		generator.legalAuthenticatorTimeGenerator = TSGenerator.getNextInstance();
		generator.authenticatorGenerators.add(AssignedEntityGenerator.getDefaultInstance());
		generator.authenticatorTimeGenerators.add(TSGenerator.getNextInstance());
		generator.documentOfGenerators.add(DocumentationOfGenerator.getDefaultInstance());
		generator.organizationGenerator = CustodianOrganizationGenerator.getDefaultInstance();
		generator.patientRoleGenerator = PatientRoleGenerator.getDefaultInstance();
		generator.allergyGenerators.add(AllergyConcernActGenerator.getDefaultInstance());
		generator.medActivityGenerators.add(MedicationActivityGenerator.getDefaultInstance());
		generator.immActivityGenerators.add(ImmunizationActivityGenerator.getDefaultInstance());
		generator.resultGenerators.add(ResultOrganizerGenerator.getDefaultInstance());
		generator.vitalsGenerators.add(VitalSignsOrganizerGenerator.getDefaultInstance());
		generator.problemListGenerators.add(ProblemConcernActGenerator.getDefaultInstance());
		generator.encounterGenerators.add(EncounterActivityGenerator.getDefaultInstance());
		generator.procedureGenerators.add(ProcedureActivityProcedureGenerator.getDefaultInstance());

		return generator;
	}

	public void verify(Composition composition) {
		Assert.assertEquals("Composition status", "preliminary", composition.getStatus().toCode());

		if (idGenerator == null) {
			Assert.assertTrue("No composition identifier", !composition.hasIdentifier());
		} else {
			Identifier identifier = composition.getIdentifier();
			idGenerator.verify(identifier);
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No composition date", !composition.hasDate());
		} else {
			effectiveTimeGenerator.verify(composition.getDateElement().asStringValue());
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No composition type", !composition.hasType());
		} else {
			codeGenerator.verify(composition.getType());
		}

		if (titleGenerator == null) {
			Assert.assertTrue("No composition title", !composition.hasTitle());
		} else {
			titleGenerator.verify(composition.getTitle());
		}

		if (confidentialityGenerator == null) {
			Assert.assertTrue("No composition confidentiality", !composition.hasConfidentiality());
		} else {
			String actual = composition.getConfidentiality().toCode();
			Assert.assertEquals("Composition confidentiality code", confidentialityGenerator.getCode(), actual);
		}
	}

	private Composition.SectionComponent findCompositionSection(Composition composition, String sectionCode) {
		for (Composition.SectionComponent section : composition.getSection()) {
			if (section.hasCode()) {
				List<Coding> codings = section.getCode().getCoding();
				if (!codings.isEmpty()) {
					Coding coding = codings.get(0);
					String code = coding.getCode();
					if (sectionCode.equals(code)) {
						return section;
					}
				}
			}
		}
		return null;
	}

	public void verify(Bundle bundle) throws Exception {
		Composition composition = BundleUtil.findOneResource(bundle, Composition.class);

		verify(composition);

		BundleUtil bundleUtil = new BundleUtil(bundle);

		if (authorGenerator == null) {
			Assert.assertTrue("No composition author", !composition.hasAuthor());
		} else {
			String practitionerId = composition.getAuthor().get(0).getReference();
			authorGenerator.verifyFromPractionerId(bundle, practitionerId);
		}

		if (legalAuthenticatorGenerator == null && authenticatorGenerators.isEmpty()) {
			Assert.assertTrue("No composition attester", !composition.hasAttester());
		} else {
			int count = authenticatorGenerators.size() + (legalAuthenticatorGenerator == null ? 0 : 1);
			Assert.assertEquals("Attester count", count, composition.getAttester().size());

			List<CompositionAttesterComponent> attesters = composition.getAttester();

			int authenticatorIndex = 0;
			for (int index = 0; index < count; ++index) {
				CompositionAttesterComponent attester = attesters.get(index);
				CompositionAttestationMode mode = attester.getMode().get(0).getValue();
				if (mode == CompositionAttestationMode.LEGAL) {
					Assert.assertNotNull("Legal asserter expected", legalAuthenticatorGenerator);
					String attesterId = attester.getParty().getReference();
					legalAuthenticatorGenerator.verifyFromPractionerId(bundle, attesterId);
					if (legalAuthenticatorTimeGenerator == null) {
						Assert.assertTrue("No legal attester time", !attester.hasTime());
					} else {
						legalAuthenticatorTimeGenerator.verify(attester.getTimeElement().asStringValue());
					}
				} else {
					String attesterId = attester.getParty().getReference();
					Assert.assertTrue("Mode professional", CompositionAttestationMode.PROFESSIONAL == mode);
					authenticatorGenerators.get(authenticatorIndex).verifyFromPractionerId(bundle, attesterId);
					TSGenerator tsg = authenticatorTimeGenerators.get(authenticatorIndex);
					if (tsg == null) {
						Assert.assertTrue("No attester time", !attester.hasTime());
					} else {
						tsg.verify(attester.getTimeElement().asStringValue());
					}

					authenticatorIndex += 1;
				}
			}
		}

		if (documentOfGenerators.isEmpty()) {
			Assert.assertTrue("No event", !composition.hasEvent());
		} else {
			int count = documentOfGenerators.size();
			Assert.assertEquals("Documentation of count", count, composition.getEvent().size());
			List<CompositionEventComponent> events = composition.getEvent();
			for (int index = 0; index < count; ++index) {
				CompositionEventComponent event = events.get(index);
				documentOfGenerators.get(index).verify(bundle, event);
			}
		}

		if (organizationGenerator == null) {
			Assert.assertTrue("No custodian", !composition.hasCustodian());
		} else {
			String organizationId = composition.getCustodian().getReference();
			org.hl7.fhir.dstu3.model.Organization organization = bundleUtil.getResourceFromReference(organizationId,
					org.hl7.fhir.dstu3.model.Organization.class);
			organizationGenerator.verify(organization);
		}

		if (patientRoleGenerator == null) {
			Assert.assertTrue("No subject", !composition.hasSubject());
		} else {
			String patientId = composition.getSubject().getReference();
			Patient patient = bundleUtil.getResourceFromReference(patientId, Patient.class);
			Assert.assertNotNull("Patient exists", patient);
			patientRoleGenerator.verify(bundle);
		}

		Composition.SectionComponent allergiesSection = findCompositionSection(composition, "48765-2");
		if (allergyGenerators.isEmpty()) {
			Assert.assertTrue("No allergies section", allergiesSection == null || !allergiesSection.hasEntry());
		} else {
			Assert.assertNotNull("Allergies section exists", allergiesSection);
			int count = allergyGenerators.size();
			Assert.assertEquals("Allergies entry count", count, allergiesSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = allergiesSection.getEntry().get(index).getReference();
				AllergyIntolerance allergy = bundleUtil.getResourceFromReference(reference, AllergyIntolerance.class);
				allergyGenerators.get(index).verify(bundle, allergy);
			}
		}

		Composition.SectionComponent medicationsSection = findCompositionSection(composition, "10160-0");
		if (medActivityGenerators.isEmpty()) {
			Assert.assertTrue("No medications section", medicationsSection == null || !medicationsSection.hasEntry());
		} else {
			Assert.assertNotNull("MedicationsSection section exists", medicationsSection);
			int count = medActivityGenerators.size();
			Assert.assertEquals("Medication entry count", count, medicationsSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = medicationsSection.getEntry().get(index).getReference();
				MedicationStatement medStatement = bundleUtil.getResourceFromReference(reference,
						MedicationStatement.class);
				medActivityGenerators.get(index).verify(bundle, medStatement);
			}
		}

		Composition.SectionComponent immunizationsSection = findCompositionSection(composition, "11369-6");
		if (immActivityGenerators.isEmpty()) {
			Assert.assertTrue("No immunizations section",
					immunizationsSection == null || !immunizationsSection.hasEntry());
		} else {
			Assert.assertNotNull("ImmunizationsSection section exists", immunizationsSection);
			int count = immActivityGenerators.size();
			Assert.assertEquals("Immunization entry count", count, immunizationsSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = immunizationsSection.getEntry().get(index).getReference();
				Immunization immunization = bundleUtil.getResourceFromReference(reference, Immunization.class);
				immActivityGenerators.get(index).verify(bundle, immunization);
			}
		}

		Composition.SectionComponent resultsSection = findCompositionSection(composition, "30954-2");
		if (resultGenerators.isEmpty()) {
			Assert.assertTrue("No result section", resultsSection == null || !resultsSection.hasEntry());
		} else {
			Assert.assertNotNull("ResultsSection section exists", resultsSection);
			int count = resultGenerators.size();
			Assert.assertEquals("Results entry count", count, resultsSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = resultsSection.getEntry().get(index).getReference();
				DiagnosticReport report = bundleUtil.getResourceFromReference(reference, DiagnosticReport.class);
				resultGenerators.get(index).verify(bundle, report);
			}
		}

		Composition.SectionComponent proceduresSection = findCompositionSection(composition, "47519-4");
		if (procedureGenerators.isEmpty()) {
			Assert.assertTrue("No procedure section", proceduresSection == null || !proceduresSection.hasEntry());
		} else {
			Assert.assertNotNull("Procedures section exists", proceduresSection);
			int count = procedureGenerators.size();
			Assert.assertEquals("Procedures entry count", count, proceduresSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = proceduresSection.getEntry().get(index).getReference();
				Procedure procedure = bundleUtil.getResourceFromReference(reference, Procedure.class);
				procedureGenerators.get(index).verify(bundle, procedure);
			}
		}

		Composition.SectionComponent encountersSection = findCompositionSection(composition, "46240-8");
		if (encounterGenerators.isEmpty()) {
			Assert.assertTrue("No encounter section", encountersSection == null || !encountersSection.hasEntry());
		} else {
			Assert.assertNotNull("Encounters ection sexists", encountersSection);
			int count = encounterGenerators.size();
			Assert.assertEquals("Results entry count", count, encountersSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = encountersSection.getEntry().get(index).getReference();
				Encounter encounter = bundleUtil.getResourceFromReference(reference, Encounter.class);
				encounterGenerators.get(index).verify(bundle, encounter);
			}
		}

		Composition.SectionComponent vitalsSection = findCompositionSection(composition, "8716-3");
		if (vitalsGenerators.isEmpty()) {
			Assert.assertTrue("No vitals section", vitalsSection == null || !vitalsSection.hasEntry());
		} else {
			Assert.assertNotNull("Vitals Section section exists", vitalsSection);
			int count = vitalsGenerators.size();
			Assert.assertEquals("Vitals entry count", count, vitalsSection.getEntry().size());
		}

		Composition.SectionComponent problemListSection = findCompositionSection(composition, "11450-4");
		if (problemListSection.isEmpty()) {
			Assert.assertTrue("No problem section", problemListSection == null || !problemListSection.hasEntry());
		} else {
			Assert.assertNotNull("ProblemListSection section exists", problemListSection);
			int count = problemListGenerators.size();
			Assert.assertEquals("Problem List entry count", count, problemListSection.getEntry().size());
			for (int index = 0; index < count; ++index) {
				String reference = problemListSection.getEntry().get(index).getReference();
				Condition condition = bundleUtil.getResourceFromReference(reference, Condition.class);
				Assert.assertNotNull("Condition exists", condition);
			}
		}

	}
}
