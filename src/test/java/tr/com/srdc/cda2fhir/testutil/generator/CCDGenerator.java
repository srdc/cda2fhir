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
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
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
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
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
			Component2 component2 = factories.base.createComponent2();
			StructuredBody structuredBody = factories.base.createStructuredBody();
			structuredBody.getComponents().add(component3);
			component2.setStructuredBody(structuredBody);
			ccd.setComponent(component2);
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

	}
}
