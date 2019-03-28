package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.EncounterActivityGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EncounterActivityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/EncounterActivity/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareEncounters(String caseName, Encounter encounter, Map<String, Object> joltEncounter)
			throws Exception {
		Assert.assertNotNull("Jolt encounter exists", joltEncounter);
		Assert.assertNotNull("Jolt encounter id exists", joltEncounter.get("id"));

		joltEncounter.put("id", encounter.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltEncounter, "subject", encounter.getSubject()); // patient is not yet implemented

		String joltEncounterJson = JsonUtils.toPrettyJsonString(joltEncounter);
		File joltEncounterFile = new File(OUTPUT_PATH + caseName + "JoltProcedure.json");
		FileUtils.writeStringToFile(joltEncounterFile, joltEncounterJson, Charset.defaultCharset());

		String procedureJson = FHIRUtil.encodeToJSON(encounter);
		JSONAssert.assertEquals("Jolt encounter", procedureJson, joltEncounterJson, true);
	}

	private static void runTest(EncounterActivityGenerator generator, String caseName) throws Exception {
		EncounterActivities ec = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tEncounterActivity2Encounter(ec, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Encounter bundle", bundle);

		Encounter encounter = BundleUtil.findOneResource(bundle, Encounter.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIREncounter", "json");
		FHIRUtil.printJSON(encounter, filepath);

		generator.verify(bundle);

		File xmlFile = CDAUtilExtension.writeAsXML(ec, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "EncounterActivity", caseName);
		// JoltUtil joltUtil = new JoltUtil(joltResult, caseName, OUTPUT_PATH);

		Map<String, Object> joltProcedure = TransformManager.chooseResource(joltResult, "Encounter");
		if (encounter == null) {
			Assert.assertNull("No procedure", joltProcedure);
		} else {
			compareEncounters(caseName, encounter, joltProcedure);
		}
	}

	@Test
	public void testDefault() throws Exception {
		EncounterActivityGenerator generator = EncounterActivityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
