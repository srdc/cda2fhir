package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.OrgJsonUtil;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class AllergyConcernActTest {
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
	}

	private static JSONObject getSection(JSONArray component, String code) throws JSONException {
		for (int idx = 0; idx < component.length(); ++idx) {
			JSONObject section = component.getJSONObject(idx).getJSONObject("section");
			String sectionCode = section.getJSONObject("code").getString("code");
			if (code.equals(sectionCode)) {
				return section;
			}
		}
		return null;
	}

	private static BundleUtil getBundle(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ClinicalDocument cda = CDAUtil.load(fis);
		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		Config.setGenerateDafProfileMetadata(false);
		Config.setGenerateNarrative(false);
		Bundle bundle = ccdTransformer.transformDocument(cda);
		return new BundleUtil(bundle);
	}
	
	private static JSONArray getAllergies(String sourceName) throws Exception {
		File file = new File("src/test/resources/" + sourceName);
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		JSONArray component = XML.toJSONObject(content).getJSONObject("ClinicalDocument").getJSONObject("component")
				.getJSONObject("structuredBody").getJSONArray("component");
		JSONObject allergiesSection = getSection(component, "48765-2");
		return allergiesSection.getJSONArray("entry");
	}
	
	@SuppressWarnings("unchecked")
	private static void compare(Map<String, Object> joltResult, BundleUtil bundleUtil) throws Exception{
		List<Object> identifiers = (List<Object>) joltResult.get("identifier");
		Map<String, Object> identifier = (Map<String, Object>) identifiers.get(0);
		String system = (String) identifier.get("system");
		String value = (String) identifier.get("value");
		
		AllergyIntolerance cda2FHIRResource = (AllergyIntolerance) bundleUtil.getIdentifierMap().get("AllergyIntolerance", system, value);
		joltResult.put("id", cda2FHIRResource.getId().split("/")[1]); // ids are not expected to be equal
		Map<String, Object> patient = new LinkedHashMap<String, Object>();
		joltResult.put("patient", patient);
		patient.put("reference", cda2FHIRResource.getPatient().getReference()); // patient is not yet implemented
		Map<String, Object> recorder = new LinkedHashMap<String, Object>();
		joltResult.put("recorder",recorder);
		recorder.put("reference", cda2FHIRResource.getRecorder().getReference()); // do not check recorder for now, ids are different

		String expected = FHIRUtil.encodeToJSON(cda2FHIRResource);
		FHIRUtil.printJSON(cda2FHIRResource);
		String actual = JsonUtils.toJsonString(joltResult);
		JSONAssert.assertEquals("Jolt output vs CDA2FHIR output", expected, actual, false);
	}
	
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> chooseAllergy(List<Object> resources) {
		for (Object resource: resources) {
			Map<String, Object> map = (Map<String, Object>) resource;
			String resourceType = (String) map.get("resourceType");
			if (resourceType.equals("AllergyIntolerance")) {
				return map;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSample1() throws Exception {
		String sourceName = "C-CDA_R2-1_CCD.xml";
		BundleUtil util = getBundle(sourceName);

		JSONArray allergies = getAllergies(sourceName);		
		JSONObject entry = allergies.getJSONObject(0);
		OrgJsonUtil.convertNamedObjectToArray(entry, "templateId");
		OrgJsonUtil.convertNamedObjectToArray(entry, "entryRelationship");

		String cdaJSONFile = "src/test/resources/output/" + "C-CDA_R2-1_CCD allergy entry - jolt.json";
		FileUtils.writeStringToFile(new File(cdaJSONFile), entry.toString(4), Charset.defaultCharset());

		List<Object> transformedOutput = (List<Object>) TransformManager.transformEntryInFile("AllergyConcernAct", cdaJSONFile);
		Map<String, Object> joltAllergy = chooseAllergy(transformedOutput);
		
		String prettyJson = JsonUtils.toPrettyJsonString(transformedOutput);
		String resultFile = "src/test/resources/output/jolt/" + "C-CDA_R2-1_CCD allergy entry result - jolt.json";		
		FileUtils.writeStringToFile(new File(resultFile), prettyJson, Charset.defaultCharset());
		
		compare(joltAllergy, util);
	}
}
