package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.IndicationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ConditionTest {

	private static CDAFactories factories;
	private static IndicationGenerator indicationGenerator;
	private static ResourceTransformerImpl rt = new ResourceTransformerImpl();

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		indicationGenerator = new IndicationGenerator();

	}

	@Test
	public void tIndication2ConditionCategoryTest() {
		Indication indication = indicationGenerator.generate(factories);

		Condition encounterCondition = rt.tIndication2ConditionEncounter(indication);
		Condition problemListItemCondition = rt.tIndication2ConditionProblemListItem(indication);

		String categoryDisplay = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getDisplay();
		String categoryCode = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getCode();
		String categorySystem = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getSystem();

		Assert.assertEquals("category system is http://hl7.org/fhir/condition-category", categorySystem,
				"http://hl7.org/fhir/condition-category");
		Assert.assertEquals("category code is encounter-diagnosis", categoryCode, "encounter-diagnosis");
		Assert.assertEquals("category displauy is Encounter Diagnosis", categoryDisplay, "Encounter Diagnosis");

		categoryDisplay = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getDisplay();
		categoryCode = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getCode();
		categorySystem = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getSystem();

		Assert.assertEquals("category system is http://hl7.org/fhir/condition-category", categorySystem,
				"http://hl7.org/fhir/condition-category");
		Assert.assertEquals("category code is problem-list-item", categoryCode, "problem-list-item");
		Assert.assertEquals("category displauy is Problem List Item", categoryDisplay, "Problem List Item");

	}

}
