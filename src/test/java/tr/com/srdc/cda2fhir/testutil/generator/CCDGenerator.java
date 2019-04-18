package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CCDGenerator {
	private IDGenerator idGenerator;

	public ContinuityOfCareDocument generate(CDAFactories factories) {
		ContinuityOfCareDocument ccd = factories.consol.createContinuityOfCareDocument();

		if (idGenerator != null) {
			ccd.setId(idGenerator.generate(factories));
		}

		return ccd;
	}

	public static CCDGenerator getDefaultInstance() {
		CCDGenerator generator = new CCDGenerator();

		generator.idGenerator = IDGenerator.getNextInstance();

		return generator;
	}

	public void verify(Composition composition) {
		if (idGenerator == null) {
			Assert.assertTrue("No composition identifier", !composition.hasIdentifier());
		} else {
			Identifier identifier = composition.getIdentifier();
			idGenerator.verify(identifier);
		}

		Assert.assertEquals("Composition status", "preliminary", composition.getStatus().toCode());
	}
}
