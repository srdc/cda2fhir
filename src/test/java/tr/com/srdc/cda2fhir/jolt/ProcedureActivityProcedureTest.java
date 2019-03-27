package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProcedureActivityProcedureGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProcedureActivityProcedureTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProcedureActivityProcedure/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareProcedures(String caseName, Procedure procedure, Map<String, Object> joltProcedure)
			throws Exception {
		Assert.assertNotNull("Jolt procedure exists", joltProcedure);
		Assert.assertNotNull("Jolt procedure id exists", joltProcedure.get("id"));

		joltProcedure.put("id", procedure.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltProcedure, "subject", procedure.getSubject()); // patient is not yet implemented

		String joltProcedureJson = JsonUtils.toPrettyJsonString(joltProcedure);
		File joltProcedureFile = new File(OUTPUT_PATH + caseName + "JoltProcedure.json");
		FileUtils.writeStringToFile(joltProcedureFile, joltProcedureJson, Charset.defaultCharset());

		String procedureJson = FHIRUtil.encodeToJSON(procedure);
		JSONAssert.assertEquals("Jolt condition", procedureJson, joltProcedureJson, true);
	}

	private static File writeProcedureActivityProcedureAsXML(String caseName, ProcedureActivityProcedure pap)
			throws Exception {
		File xmlFile = new File(OUTPUT_PATH + caseName + ".xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(pap, fw);
		fw.close();
		return xmlFile;
	}

	private static void runTest(ProcedureActivityProcedureGenerator generator, String caseName) throws Exception {
		ProcedureActivityProcedure pap = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tProcedure2Procedure(pap, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Procedure bundle", bundle);

		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRProcedure", "json");
		FHIRUtil.printJSON(procedure, filepath);

		generator.verify(procedure);

		File xmlFile = writeProcedureActivityProcedureAsXML(caseName, pap);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProcedureActivityProcedure", caseName);

		Map<String, Object> joltProcedure = TransformManager.chooseResource(joltResult, "Procedure");
		if (procedure == null) {
			Assert.assertNull("No procedure", joltProcedure);
		} else {
			compareProcedures(caseName, procedure, joltProcedure);
		}
	}

	@Test
	public void testDefault() throws Exception {
		ProcedureActivityProcedureGenerator generator = ProcedureActivityProcedureGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
