package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.BLGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.EDGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.IVL_PQRangeGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ObservationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.PQGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.RTOGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.STGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.TSGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ObservationTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/Observation/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(ObservationGenerator generator, String caseName) throws Exception {
		org.openhealthtools.mdht.uml.cda.Observation cdaObs = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tObservation2Observation(cdaObs, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Observation bundle", bundle);

		Observation observation = BundleUtil.findOneResource(bundle, Observation.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRObservation", "json");
		FHIRUtil.printJSON(observation, filepath);

		generator.verify(bundle);

		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		List<PractitionerRole> practitionerRoles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		List<Organization> organizations = FHIRUtil.findResources(bundle, Organization.class);

		File xmlFile = CDAUtilExtension.writeAsXML(cdaObs, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "Observation", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, caseName, OUTPUT_PATH);

		joltUtil.verifyOrganizations(organizations);
		joltUtil.verifyPractitioners(practitioners);
		joltUtil.verifyPractitionerRoles(practitionerRoles);

		joltUtil.verify(observation);
	}

	@Test
	public void testDefault() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testQuantityValue() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(PQGenerator.getDefaultInstance());
		runTest(generator, "quantityValueCase");
	}

	@Test
	public void testStringValue() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(STGenerator.getNextInstance());
		runTest(generator, "stringValueCase");
	}

	@Test
	public void testRange() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(IVL_PQRangeGenerator.getDefaultInstance());
		runTest(generator, "rangeValueCase");
	}

	@Test
	public void testRatio() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(RTOGenerator.getDefaultInstance());
		runTest(generator, "rangeRatioCase");
	}

	@Ignore
	@Test
	public void testEDValue() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(EDGenerator.getNextInstance());
		runTest(generator, "edValueCase");
	}

	@Test
	public void testTSValue() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(TSGenerator.getNextInstance());
		runTest(generator, "tsValueCase");
	}

	@Test
	public void testBLValue() throws Exception {
		ObservationGenerator generator = ObservationGenerator.getDefaultInstance();
		generator.replaceValueGenerator(new BLGenerator(true));
		runTest(generator, "blValueCase");
	}
}
