package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationSupplyOrderGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class MedicationSupplyOrderTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationSupplyOrder/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareMedicationRequests(String caseName, MedicationRequest medicationRequest,
			Map<String, Object> joltMedicationRequest) throws Exception {
		Assert.assertNotNull("Jolt medicationRequest", joltMedicationRequest);
		Assert.assertNotNull("Jolt medicationRequest id", joltMedicationRequest.get("id"));

		joltMedicationRequest.put("id", medicationRequest.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltMedicationRequest, "subject", medicationRequest.getSubject()); // patient is not yet
																									// implemented

		String joltMedicationRequestJson = JsonUtils.toPrettyJsonString(joltMedicationRequest);
		File joltMedicationRequestFile = new File(OUTPUT_PATH + caseName + "JoltMedicationRequest.json");
		FileUtils.writeStringToFile(joltMedicationRequestFile, joltMedicationRequestJson, Charset.defaultCharset());

		String medicationRequestJson = FHIRUtil.encodeToJSON(medicationRequest);
		JSONAssert.assertEquals("Jolt medicationRequest", medicationRequestJson, joltMedicationRequestJson, true);
	}

	private static void runTest(MedicationSupplyOrderGenerator generator, String caseName) throws Exception {
		MedicationSupplyOrder mso = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.medicationSupplyOrder2MedicationRequest(mso, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Procedure bundle", bundle);

		MedicationRequest medRequest = BundleUtil.findOneResource(bundle, MedicationRequest.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRMedicationRequest", "json");
		FHIRUtil.printJSON(medRequest, filepath);

		generator.verify(medRequest);

		File xmlFile = CDAUtilExtension.writeAsXML(mso, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "MedicationSupplyOrder", caseName);

		Map<String, Object> joltMedRequest = TransformManager.chooseResource(joltResult, "MedicationRequest");
		compareMedicationRequests(caseName, medRequest, joltMedRequest);
	}

	@Test
	public void testDefault() throws Exception {
		MedicationSupplyOrderGenerator generator = MedicationSupplyOrderGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
