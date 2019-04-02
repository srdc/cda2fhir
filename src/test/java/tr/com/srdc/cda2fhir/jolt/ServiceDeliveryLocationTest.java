package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Location;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ServiceDeliveryLocationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ServiceDeliveryLocationTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ServiceDeliveryLocation/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareLocations(String caseName, Location location, Map<String, Object> joltLocation)
			throws Exception {
		Assert.assertNotNull("Jolt location", joltLocation);
		Assert.assertNotNull("Jolt location id", joltLocation.get("id"));

		joltLocation.put("id", location.getIdElement().getIdPart()); // ids do not have to match

		String joltLocationJson = JsonUtils.toPrettyJsonString(joltLocation);
		File joltLocationFile = new File(OUTPUT_PATH + caseName + "joltLocation.json");
		FileUtils.writeStringToFile(joltLocationFile, joltLocationJson, Charset.defaultCharset());

		String locationJson = FHIRUtil.encodeToJSON(location);
		JSONAssert.assertEquals("Jolt location", locationJson, joltLocationJson, true);
	}

	private static void runTest(ServiceDeliveryLocationGenerator generator, String caseName) throws Exception {
		ParticipantRole participantRole = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		Location location = rt.tParticipantRole2Location(participantRole);

		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRLocation", "json");
		FHIRUtil.printJSON(location, filepath);

		generator.verify(location);

		File xmlFile = CDAUtilExtension.writeAsXML(participantRole, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ServiceDeliveryLocation", caseName);

		Map<String, Object> joltLocation = TransformManager.chooseResource(joltResult, "Location");
		compareLocations(caseName, location, joltLocation);
	}

	@Test
	public void testDefault() throws Exception {
		ServiceDeliveryLocationGenerator generator = ServiceDeliveryLocationGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
