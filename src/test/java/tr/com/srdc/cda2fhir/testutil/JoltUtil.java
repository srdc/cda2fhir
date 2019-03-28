package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONObject;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.jolt.TransformManager;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class JoltUtil {
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
}
