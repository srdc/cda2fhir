package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class PlayingEntityGenerator {
	private static int INDEX = 1;

	private String name;

	private CEGenerator codeGenerator;

	public PlayingEntity generate(CDAFactories factories) {
		PlayingEntity pe = factories.base.createPlayingEntity();

		if (name != null) {
			PN pn = factories.datatype.createPN();
			pn.addText(name);
			pe.getNames().add(pn);
		}

		if (codeGenerator != null) {
			CE ce = codeGenerator.generate(factories);
			pe.setCode(ce);
		}

		return pe;
	}

	public static PlayingEntityGenerator getDefaultInstance() {
		PlayingEntityGenerator peg = new PlayingEntityGenerator();

		peg.name = "name_" + (++INDEX);
		peg.codeGenerator = CEGenerator.getNextInstance();

		return peg;
	}

	public void verify(CodeableConcept codeableConcept) {
		if (name == null) {
			Assert.assertTrue("No text", !codeableConcept.hasText());
		} else {
			Assert.assertEquals("Text", name, codeableConcept.getText());
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No coding", !codeableConcept.hasCoding());
		} else {
			codeGenerator.verify(codeableConcept);
		}
	}
}
