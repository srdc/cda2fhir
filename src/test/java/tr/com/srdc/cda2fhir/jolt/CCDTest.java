package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.CCDGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CCDTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/CCD/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(ContinuityOfCareDocument ccd, String caseName, CCDGenerator generator)
			throws Exception {
		File xmlFile = CDAUtilExtension.writeAsXML(ccd, OUTPUT_PATH, caseName);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tClinicalDocument2Bundle(ccd, true);

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("CCD bundle", bundle);

		Composition composition = BundleUtil.findOneResource(bundle, Composition.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRCCD", "json");
		FHIRUtil.printJSON(bundle, filepath);

		if (generator != null) {
			generator.verify(composition);
		}

		List<Object> joltResult = JoltUtil.findJoltDocumentResult(xmlFile, "CCD", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		joltUtil.verify(composition);
	}

	private static void runTest(CCDGenerator generator, String caseName) throws Exception {
		ContinuityOfCareDocument ccd = generator.generate(factories);
		runTest(ccd, caseName, generator);
	}

	@Test
	public void testDefault() throws Exception {
		CCDGenerator generator = CCDGenerator.getDefaultInstance();
		runTest(generator, "default");
	}
}