package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.IndicationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class IndicationTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/Indication/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareConditions(String caseName, Condition condition, Map<String, Object> joltCondition)
			throws Exception {
		Assert.assertNotNull("Jolt condition", joltCondition);
		Assert.assertNotNull("Jolt condition id", joltCondition.get("id"));

		joltCondition.put("id", condition.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltCondition, "subject", condition.getSubject()); // patient is not yet implemented

		String joltConditionJson = JsonUtils.toPrettyJsonString(joltCondition);
		File joltConditionFile = new File(OUTPUT_PATH + caseName + "JoltCondition.json");
		FileUtils.writeStringToFile(joltConditionFile, joltConditionJson, Charset.defaultCharset());

		String conditionJson = FHIRUtil.encodeToJSON(condition);
		JSONAssert.assertEquals("Jolt condition", conditionJson, joltConditionJson, true);
	}

	private static void runTest(IndicationGenerator generator, String caseName) throws Exception {
		Indication indication = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);
		
		Condition condition = null;
		
		IEntryResult condResult = rt.tIndication2ConditionEncounter(indication, new BundleInfo(rt));
				

		if(condResult.hasResult()) {
			Bundle bundle = condResult.getBundle();
			if(bundle != null) {
				condition = FHIRUtil.findFirstResource(bundle, Condition.class);		
			}

		} else {
			Bundle fullBundle = condResult.getFullBundle();
			
			if(fullBundle != null) {
				condition = FHIRUtil.findFirstResource(fullBundle, Condition.class);
			}
		}
				
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRCondition", "json");
		FHIRUtil.printJSON(condition, filepath);

		generator.verify(condition);

		File xmlFile = CDAUtilExtension.writeAsXML(indication, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "Indication", caseName);

		Map<String, Object> joltCondition = TransformManager.chooseResource(joltResult, "Condition");
		compareConditions(caseName, condition, joltCondition);
	}

	@Test
	public void testDefault() throws Exception {
		IndicationGenerator generator = IndicationGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
