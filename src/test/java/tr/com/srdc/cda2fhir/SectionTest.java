package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class SectionTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static CDAFactories factories;
	private static String tableText = "<text>\n" + "                        <table border=\"1\" width=\"95%\">\n"
			+ "                            <colgroup>\n" + "                                <col width=\"25%\" />\n"
			+ "                                <col width=\"45%\" />\n"
			+ "                                <col width=\"15%\" />\n"
			+ "                                <col width=\"15%\" />\n" + "                            </colgroup>\n"
			+ "                            <thead>\n" + "                                <tr>\n"
			+ "                                    <th>Substance</th>\n"
			+ "                                    <th>Reaction</th>\n"
			+ "                                    <th>Severity</th>\n"
			+ "                                    <th>Status</th>\n" + "                                </tr>\n"
			+ "                            </thead>\n" + "                            <tbody>\n"
			+ "                                <tr>\n" + "                                    <td>\n"
			+ "                                        <content ID=\"ALLERGEN13278784\">amoxicillin<sup>1</sup>\n"
			+ "                                        </content>\n" + "                                    </td>\n"
			+ "                                    <td></td>\n" + "                                    <td>\n"
			+ "                                        <content ID=\"ALLSEV13278784\">Mild</content>\n"
			+ "                                    </td>\n" + "                                    <td>\n"
			+ "                                        <content ID=\"ALLSTAT13278784\">Active</content>\n"
			+ "                                    </td>\n" + "                                </tr>"
			+ "							 </tbody>" + " 						 </table>";

	@BeforeClass
	public static void init() {
		// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar
		// documents will not be recognised.
		// This has to be called before loading the document; otherwise will have no
		// effect.
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testGenNarrativeFalse() {
		Config.setGenerateNarrative(false);
		Section cdaSection = factories.base.createSection();
		StrucDocText strucDocText = factories.base.createStrucDocText();

		strucDocText.addText(tableText);
		cdaSection.setText(strucDocText);
		SectionComponent fhirSection = rt.tSection2Section(cdaSection);

		Assert.assertFalse(fhirSection.hasText());

	}

	@Test
	public void testGenNarrativeTrue() {
		Config.setGenerateNarrative(true);
		Section cdaSection = factories.base.createSection();
		StrucDocText strucDocText = factories.base.createStrucDocText();

		strucDocText.addText(tableText);
		cdaSection.setText(strucDocText);
		SectionComponent fhirSection = rt.tSection2Section(cdaSection);

		Assert.assertTrue(fhirSection.hasText());
	}
}
