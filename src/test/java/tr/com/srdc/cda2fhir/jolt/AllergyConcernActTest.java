package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ADGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ADXPGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AllergyConcernActGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AllergyObservationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AuthorGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ENXPGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.OrganizationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PNGenerator;
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

	private static void runTest(AllergyProblemAct act, String caseName, AllergyConcernActGenerator generator)
			throws Exception {
		File xmlFile = CDAUtilExtension.writeAsXML(act, OUTPUT_PATH, caseName);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tAllergyProblemAct2AllergyIntolerance(act, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Allergy bundle", bundle);

		AllergyIntolerance allergy = BundleUtil.findOneResource(bundle, AllergyIntolerance.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRAllergyIntolerance", "json");
		FHIRUtil.printJSON(bundle, filepath);

		if (generator != null) {
			generator.verify(bundle);
		}

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "AllergyConcernAct", caseName);

		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		joltUtil.verify(allergy);
	}

	private static void runTest(AllergyConcernActGenerator generator, String caseName) throws Exception {
		AllergyProblemAct act = generator.generate(factories);
		runTest(act, caseName, generator);
	}

	private static void runSampleTest(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());

		AllergiesSection section = cda.getAllergiesSection();

		int index = 0;
		for (AllergyProblemAct act : section.getAllergyProblemActs()) {
			String caseName = sourceName.substring(0, sourceName.length() - 4) + "_" + index;
			runTest(act, caseName, null);
			++index;
		}
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

	@Test
	public void testNullFlavorRecorderPersonWithName() throws Exception {
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();
		AuthorGenerator ag = new AuthorGenerator();
		ag.setCode("thecode", "The Code Print");
		PNGenerator pnGenerator = new PNGenerator();
		pnGenerator.setFamilyNullFlavor();
		pnGenerator.setGivensNullFlavor();
		ag.setPNGenerator(pnGenerator);
		acag.setAuthorGenerator(ag);
		runTest(acag, "nullFlavorRecorderPersonWithName");
	}

	@Test
	public void testNullFlavorRecorderPersonNoName() throws Exception {
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();
		AuthorGenerator ag = new AuthorGenerator();
		ag.setCode("thecode", "The Code Print");
		PNGenerator pnGenerator = new PNGenerator();
		ENXPGenerator nullFlavorGenerator = new ENXPGenerator(true);
		nullFlavorGenerator.setNullFlavor("UNK");
		pnGenerator.setFamilyGenerator(nullFlavorGenerator);
		pnGenerator.setGivensGenerator(nullFlavorGenerator);
		ag.setPNGenerator(pnGenerator);
		acag.setAuthorGenerator(ag);
		runTest(acag, "nullFlavorRecorderPersonNoName");
	}

	@Test
	public void testNullFlavorOrgAddress() throws Exception {
		AllergyConcernActGenerator acag = new AllergyConcernActGenerator();

		AuthorGenerator ag = new AuthorGenerator();
		ag.setCode("thecode", "The Code Print");
		OrganizationGenerator orgGenerator = new OrganizationGenerator("The Org Name");

		ADGenerator adGenerator = new ADGenerator();

		ADXPGenerator nullFlavorGenerator = new ADXPGenerator("Text");
		nullFlavorGenerator.setNullFlavor("UNK");
		adGenerator.setCity(nullFlavorGenerator);
		adGenerator.setCountry(nullFlavorGenerator);
		adGenerator.setCounty(nullFlavorGenerator);
		adGenerator.setLine(nullFlavorGenerator);
		adGenerator.setPostalCode(new ADXPGenerator("20876"));
		adGenerator.setState(new ADXPGenerator("MD"));

		orgGenerator.setADGenerator(adGenerator);
		ag.setOrganizationGenerator(orgGenerator);
		acag.setAuthorGenerator(ag);
		runTest(acag, "nullFlavorOrgAddress");
	}

	@Test
	public void testSample1() throws Exception {
		runSampleTest("C-CDA_R2-1_CCD.xml");
	}

	@Test
	public void testSample2() throws Exception {
		runSampleTest("170.315_b1_toc_gold_sample2_v1.xml");
	}

	@Test
	public void testSample3() throws Exception {
		runSampleTest("Vitera_CCDA_SMART_Sample.xml");
	}

	@Ignore
	@Test
	public void testEpicSample1() throws Exception {
		runSampleTest("Epic/DOC0001.XML");
	}

	@Ignore
	@Test
	public void testEpicSample2() throws Exception {
		runSampleTest("Epic/DOC0001 2.XML");
	}

	@Ignore
	@Test
	public void testEpicSample3() throws Exception {
		runSampleTest("Epic/DOC0001 3.XML");
	}

	@Ignore
	@Test
	public void testEpicSample4() throws Exception {
		runSampleTest("Epic/DOC0001 4.XML");
	}

	@Ignore
	@Test
	public void testEpicSample5() throws Exception {
		runSampleTest("Epic/DOC0001 5.XML");
	}

	@Ignore
	@Test
	public void testEpicSample6() throws Exception {
		runSampleTest("Epic/DOC0001 6.XML");
	}

	@Ignore
	@Test
	public void testEpicSample7() throws Exception {
		runSampleTest("Epic/DOC0001 7.XML");
	}

	@Ignore
	@Test
	public void testEpicSample8() throws Exception {
		runSampleTest("Epic/DOC0001 8.XML");
	}

	@Ignore
	@Test
	public void testEpicSample9() throws Exception {
		runSampleTest("Epic/DOC0001 9.XML");
	}

	@Ignore
	@Test
	public void testEpicSample10() throws Exception {
		runSampleTest("Epic/DOC0001 10.XML");
	}

	@Ignore
	@Test
	public void testEpicSample11() throws Exception {
		runSampleTest("Epic/DOC0001 11.XML");
	}

	@Ignore
	@Test
	public void testEpicSample12() throws Exception {
		runSampleTest("Epic/DOC0001 12.XML");
	}

	@Ignore
	@Test
	public void testEpicSample13() throws Exception {
		runSampleTest("Epic/DOC0001 13.XML");
	}

	@Ignore
	@Test
	public void testEpicSample14() throws Exception {
		runSampleTest("Epic/DOC0001 14.XML");
	}

	@Ignore
	@Test
	public void testEpicSample15() throws Exception {
		runSampleTest("Epic/DOC0001 15.XML");
	}

	@Ignore
	@Test
	public void testEpicSample16() throws Exception {
		runSampleTest("Epic/HannahBanana_EpicCCD.xml");
	}

	@Test
	public void testCernerSample1() throws Exception {
		runSampleTest("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
	}

	@Ignore
	@Test
	public void testCernerSample2() throws Exception {
		runSampleTest("Cerner/Encounter-RAKIA_TEST_DOC00001.XML");
	}
}