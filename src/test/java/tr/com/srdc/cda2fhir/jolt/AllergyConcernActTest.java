package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.AllergyConcernActGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AllergyObservationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AuthorGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergyConcernActTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/AllergyConcernAct/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(AllergyConcernActGenerator generator, String caseName) throws Exception {
		AllergyProblemAct apa = generator.generate(factories);

		File xmlFile = CDAUtilExtension.writeAsXML(apa, OUTPUT_PATH, caseName);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tAllergyProblemAct2AllergyIntolerance(apa, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Allergy bundle", bundle);

		AllergyIntolerance allergy = BundleUtil.findOneResource(bundle, AllergyIntolerance.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRAllergyIntolerance", "json");
		FHIRUtil.printJSON(allergy, filepath);

		generator.verify(bundle);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "AllergyConcernAct", caseName);

		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		joltUtil.verify(allergy);
	}

	@Test
	public void testEmpty() throws Exception {
		AllergyConcernActGenerator generator = new AllergyConcernActGenerator();
		runTest(generator, "empty");
	}

	@Test
	public void testDefault() throws Exception {
		AllergyConcernActGenerator generator = AllergyConcernActGenerator.getDefaultInstance();
		runTest(generator, "default");
	}

	@Test
	public void testClinicalStatus() throws Exception {
		Set<String> codes = AllergyConcernActGenerator.getPossibleClinicalStatusCodes();
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();
		AllergyObservationGenerator aog = new AllergyObservationGenerator();
		acag.setObservationGenerator(aog);
		for (String code : codes) {
			aog.setClinicalStatusCode(code);
			runTest(acag, "clinicalStatus" + code);
		}
	}

	@Test
	public void testNoOrganization() throws Exception {
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();
		AuthorGenerator ag = AuthorGenerator.getDefaultInstance();
		ag.removeOrganizationGenerator();
		acag.setAuthorGenerator(ag);
		runTest(acag, "noOrganization");
	}
}