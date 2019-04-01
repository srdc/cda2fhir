package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.jolt.TransformManager;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class JoltUtil {
	private static final Path TEMPLATE_PATH = Paths.get("src", "test", "resources", "jolt");
	private static final Path TEST_CASE_PATH = Paths.get("src", "test", "resources", "jolt-verify");

	private List<Object> result;
	private String caseName;
	private String outputPath;

	public JoltUtil(List<Object> result, String caseName, String outputPath) {
		this.result = result;
		this.caseName = caseName;
		this.outputPath = outputPath;
	}

	public void verifyOrganizations(List<Organization> organizations) throws Exception {
		List<Map<String, Object>> joltOrganizations = TransformManager.chooseResources(result, "Organization");
		if (organizations.isEmpty()) {
			Assert.assertTrue("No organizations", joltOrganizations.isEmpty());
		} else {
			for (int index = 0; index < organizations.size(); ++index) {
				compareOrganization(organizations.get(index), joltOrganizations.get(index));
			}
		}
	}

	public void verifyPractitioners(List<Practitioner> practitioners) throws Exception {
		List<Map<String, Object>> joltPractitioners = TransformManager.chooseResources(result, "Practitioner");
		if (practitioners.isEmpty()) {
			Assert.assertTrue("No practitioner", joltPractitioners.isEmpty());
		} else {
			for (int index = 0; index < practitioners.size(); ++index) {
				comparePractitioner(practitioners.get(index), joltPractitioners.get(index));
			}
		}
	}

	public void verifyPractitionerRoles(List<PractitionerRole> roles) throws Exception {
		List<Map<String, Object>> joltRoles = TransformManager.chooseResources(result, "PractitionerRole");
		if (roles.isEmpty()) {
			Assert.assertTrue("No jolt practitioner role", joltRoles.isEmpty());
		} else {
			for (int index = 0; index < roles.size(); ++index) {
				compareRole(roles.get(index), joltRoles.get(index));
			}
		}
	}

	private static void compareOrganization(Organization organization, Map<String, Object> joltOrganization)
			throws Exception {
		joltOrganization.put("id", organization.getIdElement().getIdPart()); // ids do not have to match

		String expected = FHIRUtil.encodeToJSON(organization);
		String actual = JsonUtils.toJsonString(joltOrganization);
		JSONAssert.assertEquals("Jolt organization", expected, actual, true);
	}

	private static void comparePractitioner(Practitioner practitioner, Map<String, Object> joltPractitioner)
			throws Exception {
		joltPractitioner.put("id", practitioner.getIdElement().getIdPart()); // ids do not have to match

		String expected = FHIRUtil.encodeToJSON(practitioner);
		String actual = JsonUtils.toJsonString(joltPractitioner);
		JSONAssert.assertEquals("Jolt practitioner", expected, actual, true);
	}

	@SuppressWarnings("unchecked")
	private void compareRole(PractitionerRole practitionerRole, Map<String, Object> joltPractitionerRole)
			throws Exception {
		Assert.assertNotNull("Jolt practitioner", joltPractitionerRole);
		Assert.assertNotNull("Jolt practitioner id", joltPractitionerRole.get("id"));
		joltPractitionerRole.put("id", practitionerRole.getIdElement().getIdPart()); // ids do not have to match

		Map<String, Object> practitioner = (Map<String, Object>) joltPractitionerRole.get("practitioner");
		Assert.assertNotNull("Jolt role practitioner", practitioner);
		Object practitionerReference = practitioner.get("reference");
		Assert.assertNotNull("Jolt role practitioner reference", practitionerReference);
		Assert.assertTrue("practitioner reference is string", practitionerReference instanceof String);
		JoltUtil.putReference(joltPractitionerRole, "practitioner", practitionerRole.getPractitioner()); // reference
																											// values
																											// may
		// not match

		Map<String, Object> organization = (Map<String, Object>) joltPractitionerRole.get("organization");
		Assert.assertNotNull("Jolt role organization", organization);
		Object organizationReference = organization.get("reference");
		Assert.assertNotNull("Jolt role organization reference", organizationReference);
		Assert.assertTrue("organization reference is string", organizationReference instanceof String);
		JoltUtil.putReference(joltPractitionerRole, "organization", practitionerRole.getOrganization()); // reference
																											// values
																											// may
		// not match

		String joltPractitionerJson = JsonUtils.toPrettyJsonString(joltPractitionerRole);
		File joltPractitionerFile = new File(outputPath, caseName + "JoltPractitionerRole.json");
		FileUtils.writeStringToFile(joltPractitionerFile, joltPractitionerJson, Charset.defaultCharset());
	}

	public static List<Object> findJoltResult(File xmlFile, String templateName, String caseName) throws Exception {
		OrgJsonUtil util = OrgJsonUtil.readXML(xmlFile.toString());
		JSONObject json = util.getJSONObject();
		String parentPath = xmlFile.getParent();
		File jsonFile = new File(parentPath, caseName + ".json");
		FileUtils.writeStringToFile(jsonFile, json.toString(4), Charset.defaultCharset());

		List<Object> joltResult = TransformManager.transformEntryInFile(templateName, jsonFile.toString());
		return joltResult;
	}

	public static void putReference(Map<String, Object> joltResult, String property, Reference reference) {
		Map<String, Object> r = new LinkedHashMap<String, Object>();
		r.put("reference", reference.getReference());
		joltResult.put(property, r);
	}

	@SuppressWarnings("unchecked")
	public static void checkReference(Map<String, Object> resource, String path, String id) {
		Map<String, Object> parent = (Map<String, Object>) resource.get(path);
		Assert.assertNotNull("Resource " + path, parent);
		String actualId = (String) parent.get("reference");
		Assert.assertEquals("Id for " + path, id, actualId);
	}

	@SuppressWarnings("unchecked")
	public static void copyStringArray(Map<String, Object> source, List<String> target, String key) {
		List<Object> sourceArray = (List<Object>) source.get(key);
		if (sourceArray != null) {
			sourceArray.forEach(e -> {
				String value = (String) e;
				if (value != null) {
					target.add(value);
				}
			});
		}
	}

	private static Path getDataTypeTemplatePath(String dataTypeSpec) {
		String[] pieces = dataTypeSpec.split("/");
		if (pieces.length < 2) {
			return Paths.get("data-type", pieces[0] + ".json");
		}
		return Paths.get(pieces[0], pieces[1] + ".json");
	}

	public static JSONArray getDataTypeTestCases(String dataTypeSpec) throws Exception {
		Path dataTypePath = getDataTypeTemplatePath(dataTypeSpec);
		Path testCasesPath = Paths.get(TEST_CASE_PATH.toString(), dataTypePath.toString());
		File file = new File(testCasesPath.toString());
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		JSONArray testCases = new JSONArray(content);
		return testCases;
	}

	public static List<Object> getDataTypeTemplate(String dataTypeSpec) throws Exception {
		Path dataTypePath = getDataTypeTemplatePath(dataTypeSpec);
		Path templatePath = Paths.get(TEMPLATE_PATH.toString(), dataTypePath.toString());
		List<Object> template = JsonUtils.filepathToList(templatePath.toString());
		return template;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getDataTypeGeneratorTestCases(String dataType) throws Exception {
		String testCasesPath = String.format("src/test/resources/jolt-verify/data-type/%s.json", dataType);
		List<Object> rawTestCases = JsonUtils.filepathToList(testCasesPath);
		List<Object> template = getDataTypeTemplate(dataType);
		Map<String, Object> transform = (Map<String, Object>) template.get(0);
		Chainr chainr = null;
		if ("cardinality".equals(transform.get("operation"))) {
			chainr = Chainr.fromSpec(Collections.singletonList(transform));
		}
		final Chainr loopChainr = chainr;
		return rawTestCases.stream().map(rawCase -> {
			Map<String, Object> testCase = (Map<String, Object>) rawCase;
			Map<String, Object> input = (Map<String, Object>) testCase.get("input");
			Map<String, Object> expected = (Map<String, Object>) testCase.get("expected");
			if (loopChainr != null) {
				input = (Map<String, Object>) loopChainr.transform(input);
			}
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("input", input);
			result.put("expected", expected);
			return result;
		}).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public static void verifyUpdateReference(boolean has, Reference referenceRoot, Map<String, Object> joltObject,
			String key) {
		if (has) {
			Map<String, Object> joltObjectAsMap = (Map<String, Object>) joltObject.get(key);
			Assert.assertNotNull("Jolt " + key, joltObjectAsMap);
			Object reference = joltObjectAsMap.get("reference");
			Assert.assertNotNull("Jolt " + key + " reference", reference);
			Assert.assertTrue("Reference is string", reference instanceof String);
			JoltUtil.putReference(joltObject, key, referenceRoot); // reference values may not match
		} else {
			Assert.assertNull("No jolt reference parent", joltObject.get(key));
		}
	}
}
