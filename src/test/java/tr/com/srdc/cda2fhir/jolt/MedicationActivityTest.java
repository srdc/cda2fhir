package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class MedicationActivityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationActivity/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(MedicationActivityGenerator generator, String caseName) throws Exception {
		MedicationActivity ma = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tMedicationActivity2MedicationStatement(ma, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Problem bundle", bundle);

		MedicationStatement medicationStatement = BundleUtil.findOneResource(bundle, MedicationStatement.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRMedicationStatement", "json");
		FHIRUtil.printJSON(medicationStatement, filepath);

		generator.verify(bundle);

		File xmlFile = CDAUtilExtension.writeAsXML(ma, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "MedicationActivity", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		joltUtil.verify(medicationStatement);

		List<MedicationRequest> resources = FHIRUtil.findResources(bundle, MedicationRequest.class);
		Assert.assertTrue("One or zero request", resources.size() < 2);
		if (resources.size() == 1) {
			joltUtil.verify(resources.get(0));
		} else {
			List<Map<String, Object>> joltRequest = joltUtil.findResources("MedicationRequest");
			Assert.assertTrue("No request", joltRequest.size() == 0);
		}
	}

	@Test
	public void testDefault() throws Exception {
		MedicationActivityGenerator generator = MedicationActivityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
