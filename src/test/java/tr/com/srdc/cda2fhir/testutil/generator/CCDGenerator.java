package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CCDGenerator {
	private IDGenerator idGenerator;

	private TSGenerator effectiveTimeGenerator;

	private CEGenerator codeGenerator;

	private STGenerator titleGenerator;

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

		return ccd;
	}

	public static CCDGenerator getDefaultInstance() {
		CCDGenerator generator = new CCDGenerator();

		generator.idGenerator = IDGenerator.getNextInstance();
		generator.effectiveTimeGenerator = TSGenerator.getNextInstance();
		generator.codeGenerator = CEGenerator.getNextInstance();
		generator.titleGenerator = STGenerator.getNextInstance();

		return generator;
	}

	public void verify(Composition composition) {
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

		Assert.assertEquals("Composition status", "preliminary", composition.getStatus().toCode());
	}
}
