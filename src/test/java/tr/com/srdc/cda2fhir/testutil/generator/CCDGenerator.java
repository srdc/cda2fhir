package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttestationMode;
import org.hl7.fhir.dstu3.model.Composition.CompositionAttesterComponent;
import org.hl7.fhir.dstu3.model.Composition.CompositionEventComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Authenticator;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.DocumentationOf;
import org.openhealthtools.mdht.uml.cda.LegalAuthenticator;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;

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
				legalAuthenticator.setTime(legalAuthenticatorTimeGenerator.create(factories));
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

	public void verify(Bundle bundle) throws Exception {
		Composition composition = BundleUtil.findOneResource(bundle, Composition.class);

		verify(composition);

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
	}
}
