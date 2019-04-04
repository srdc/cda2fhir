package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.VitalSignsOrganizerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.section.impl.CDASectionCommon;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class VitalSignsOrganizerTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/VitalSignsOrganizer/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(VitalSignsOrganizerGenerator generator, String caseName) throws Exception {
		VitalSignsOrganizer vso = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		EList<VitalSignsOrganizer> list = new BasicEList<>();
		list.add(vso);

		ISectionResult sectionResult = CDASectionCommon.transformVitalSignsOrganizerList(list, new BundleInfo(rt));

		Bundle bundle = sectionResult.getBundle();
		Assert.assertNotNull("Observation bundle", bundle);

		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRBundle", "json");
		FHIRUtil.printJSON(bundle, filepath);

		generator.verify(bundle);

		File xmlFile = CDAUtilExtension.writeAsXML(vso, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "VitalSignsOrganizer", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, caseName, OUTPUT_PATH);

		List<Observation> observations = FHIRUtil.findResources(bundle, Observation.class);
		joltUtil.verifyObservations(observations);
	}

	@Test
	public void testDefault() throws Exception {
		VitalSignsOrganizerGenerator generator = VitalSignsOrganizerGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
