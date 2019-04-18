package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
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
	}
}
