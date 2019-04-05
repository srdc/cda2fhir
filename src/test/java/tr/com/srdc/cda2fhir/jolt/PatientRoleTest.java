package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.PatientRoleGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class PatientRoleTest {
	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/PatientRole/";

	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(PatientRoleGenerator generator, String caseName) throws Exception {
		PatientRole pr = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		Bundle bundle = rt.tPatientRole2Patient(pr);

		Assert.assertNotNull("Patient bundle", bundle);

		Patient patient = BundleUtil.findOneResource(bundle, Patient.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRPatient", "json");
		FHIRUtil.printJSON(patient, filepath);

		generator.verify(patient);

		File xmlFile = CDAUtilExtension.writeAsXML(pr, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "PatientRole", caseName);

		JoltUtil joltUtil = new JoltUtil(joltResult, caseName, OUTPUT_PATH);
		joltUtil.verify(patient);
	}

	@Test
	public void testEmpty() throws Exception {
		PatientRoleGenerator generator = new PatientRoleGenerator();
		runTest(generator, "empty");
	}

	@Test
	public void testDefault() throws Exception {
		PatientRoleGenerator generator = PatientRoleGenerator.getDefaultInstance();
		runTest(generator, "default");
	}
}
