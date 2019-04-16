package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationDispenseGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class MedicationDispenseTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationDispense/";

	private static Consumer<Map<String, Object>> customJoltUpdate; // Hack for now

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(MedicationDispenseGenerator generator, String caseName) throws Exception {
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense md = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tMedicationDispense2MedicationDispense(md, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Medication dispense bundle", bundle);

		MedicationDispense medDispense = BundleUtil.findOneResource(bundle, MedicationDispense.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRMedicationDispense", "json");
		FHIRUtil.printJSON(medDispense, filepath);

		generator.verify(medDispense);
		generator.verify(bundle);

		File xmlFile = CDAUtilExtension.writeAsXML(md, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "MedicationDispense", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);
		if (customJoltUpdate != null) {
			Map<String, Object> joltDispense = TransformManager.chooseResource(joltResult, "MedicationDispense");
			if (joltDispense != null) {
				customJoltUpdate.accept(joltDispense);
			}
		}

		joltUtil.verify(medDispense);
	}

	@Test
	public void testDefault() throws Exception {
		customJoltUpdate = (r) -> {
			Map<String, Object> map = JoltUtil.findPathMap(r, "dosageInstruction[].doseQuantity");
			map.remove("xsi:type");
		};
		MedicationDispenseGenerator generator = MedicationDispenseGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
		customJoltUpdate = null;
	}
}
