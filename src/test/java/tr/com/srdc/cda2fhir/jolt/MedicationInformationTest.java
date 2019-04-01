package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationInformationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class MedicationInformationTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationInformation/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareMedication(String caseName, Medication med, Map<String, Object> joltMed)
			throws Exception {
		Assert.assertNotNull("Jolt medication", joltMed);
		Assert.assertNotNull("Jolt medication id", joltMed.get("id"));

		joltMed.put("id", med.getIdElement().getIdPart()); // ids do not have to match

		JoltUtil.verifyUpdateReference(med.hasManufacturer(), med.getManufacturer(), joltMed, "manufacturer");

		String joltMedJson = JsonUtils.toPrettyJsonString(joltMed);
		File joltMedFile = new File(OUTPUT_PATH + caseName + "JoltMedication.json");
		FileUtils.writeStringToFile(joltMedFile, joltMedJson, Charset.defaultCharset());

		String medJson = FHIRUtil.encodeToJSON(med);
		JSONAssert.assertEquals("Jolt medication", medJson, joltMedJson, true);
	}

	private static void runTest(MedicationInformationGenerator generator, String caseName) throws Exception {
		ManufacturedProduct mf = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tManufacturedProduct2Medication(mf, new BundleInfo(rt));
		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Medication bundle", bundle);

		Medication med = BundleUtil.findOneResource(bundle, Medication.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRMed", "json");
		FHIRUtil.printJSON(med, filepath);

		generator.verify(bundle);

		File xmlFile = CDAUtilExtension.writeAsXML(mf, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "MedicationInformation", caseName);

		Map<String, Object> joltMed = TransformManager.chooseResource(joltResult, "Medication");
		compareMedication(caseName, med, joltMed);

		Organization organization = BundleUtil.findOneResource(bundle, Organization.class);
		Map<String, Object> joltOrg = TransformManager.chooseResource(joltResult, "Organization");
		JoltUtil.compareOrganization(organization, joltOrg);
	}

	@Test
	public void testDefault() throws Exception {
		MedicationInformationGenerator generator = MedicationInformationGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
