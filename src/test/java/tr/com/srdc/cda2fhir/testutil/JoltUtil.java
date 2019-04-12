package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationPractitionerComponent;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationReactionComponent;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.jolt.TransformManager;
import tr.com.srdc.cda2fhir.jolt.report.ReportException;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class JoltUtil {
	private static final Path TEMPLATE_PATH = Paths.get("src", "test", "resources", "jolt");
	private static final Path TEST_CASE_PATH = Paths.get("src", "test", "resources", "jolt-verify");

	private static abstract class ResourceInfo {
		public String getPatientPropertyName() {
			return null;
		}

		public Reference getPatientReference() {
			return null;
		}
	}

	private static class ImmunizationInfo extends ResourceInfo {
		private Immunization immunization;

		public ImmunizationInfo(Immunization immunization) {
			this.immunization = immunization;
		}

		@Override
		public String getPatientPropertyName() {
			return "patient";
		}

		@Override
		public Reference getPatientReference() {
			return immunization.getPatient();
		}
	}

	private static class ObservationInfo extends ResourceInfo {
		private Observation observation;

		public ObservationInfo(Observation observation) {
			this.observation = observation;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return observation.getSubject();
		}
	}

	private static class DiagnosticReportInfo extends ResourceInfo {
		private DiagnosticReport report;

		public DiagnosticReportInfo(DiagnosticReport report) {
			this.report = report;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return report.getSubject();
		}
	}

	private static class AllergyIntoleranceInfo extends ResourceInfo {
		private AllergyIntolerance allergyIntolerance;

		public AllergyIntoleranceInfo(AllergyIntolerance allergyIntolerance) {
			this.allergyIntolerance = allergyIntolerance;
		}

		@Override
		public String getPatientPropertyName() {
			return "patient";
		}

		@Override
		public Reference getPatientReference() {
			return allergyIntolerance.getPatient();
		}
	}

	private static class ConditionInfo extends ResourceInfo {
		private Condition condition;

		public ConditionInfo(Condition condition) {
			this.condition = condition;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return condition.getSubject();
		}
	}

	private static class ProcedureInfo extends ResourceInfo {
		private Procedure procedure;

		public ProcedureInfo(Procedure procedure) {
			this.procedure = procedure;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return procedure.getSubject();
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Object> findPathNextValue(List<Object> inputs, String path, String fullPath) {
		List<Object> result = new ArrayList<>();

		String actualPath = path.replace("[]", "");

		inputs.forEach(input -> {
			if (!(input instanceof Map)) {
				throw new ReportException(path + " of " + fullPath + " is not an object.");
			}
			Map<String, Object> inputAsMap = (Map<String, Object>) input;
			Object next = inputAsMap.get(actualPath);
			if (next == null) {
				return;
			}
			if (!(next instanceof List)) {
				if (path.endsWith("[]")) {
					throw new ReportException(path + " of " + fullPath + " is not a list.");
				}
				result.add(next);
				return;
			}
			if (!path.endsWith("[]")) {
				throw new ReportException(path + " of " + fullPath + " is an unexpected list.");
			}
			List<Object> list = (List<Object>) next;
			list.forEach(element -> result.add(element));
		});
		return result;
	}

	public static List<Object> findPathValue(Map<String, Object> input, String path) {
		List<Object> result = new ArrayList<>();
		result.add(input);

		String[] pathPieces = path.split("\\.");
		for (int index = 0; index < pathPieces.length; ++index) {
			String pathPiece = pathPieces[index];
			result = findPathNextValue(result, pathPiece, path);
		}
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}

	public static String findPathString(Map<String, Object> input, String path) {
		if (input == null) {
			return null;
		}
		List<Object> value = findPathValue(input, path);
		if (value == null) {
			return null;
		}
		Assert.assertEquals(path + " value count", 1, value.size());
		Assert.assertTrue(path + " value is string", value.get(0) instanceof String);
		String valueAsString = (String) value.get(0);
		return valueAsString;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> findPathMap(Map<String, Object> input, String path) {
		if (input == null) {
			return null;
		}
		List<Object> value = findPathValue(input, path);
		if (value == null) {
			return null;
		}
		Assert.assertEquals(path + " value count", 1, value.size());
		Assert.assertTrue(path + " value is map", value.get(0) instanceof Map);
		return (Map<String, Object>) value.get(0);
	}

	@SuppressWarnings("unchecked")
	public static Consumer<Map<String, Object>> getFloatUpdate(String current, String replacement) {
		return r -> {
			List<Object> codings = JoltUtil.findPathValue(r, "code.coding[]");
			codings.forEach(coding -> {
				Map<String, Object> codingAsMap = (Map<String, Object>) coding;
				Object code = codingAsMap.get("code");
				if (code != null && current.equals(code.toString())) {
					codingAsMap.put("code", replacement);
				}
			});
		};
	}

	private static final Map<String, String> PATIENT_PROPERTY = new HashMap<>();
	static {
		PATIENT_PROPERTY.put("Immunization", "patient");
	}

	private List<Object> result;
	private String caseName;
	private String outputPath;
	private BundleUtil bundleUtil;

	public JoltUtil(List<Object> result, Bundle bundle, String caseName, String outputPath) {
		this.result = result;
		this.caseName = caseName;
		this.outputPath = outputPath;
		this.bundleUtil = new BundleUtil(bundle);
	}

	public void verifyOrganizations(List<Organization> organizations) throws Exception {
		List<Map<String, Object>> joltOrganizations = TransformManager.chooseResources(result, "Organization");
		if (organizations.isEmpty()) {
			Assert.assertTrue("No organizations", joltOrganizations.isEmpty());
		} else {
			Assert.assertEquals("Organization count", organizations.size(), joltOrganizations.size());
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
				boolean found = false;
				for (int index2 = 0; index2 < joltPractitioners.size(); ++index2) {
					try {
						comparePractitioner(practitioners.get(index), joltPractitioners.get(index2));
						found = true;
					} catch (Error ex) {
					}
				}
				Assert.assertTrue("Matched practitioner found", found);
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

	public void verifyConditions(List<Condition> conditions) throws Exception {
		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(result, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No jolt condition", joltConditions.isEmpty());
		} else {
			Assert.assertEquals("Condition count", conditions.size(), joltConditions.size());
			for (int index = 0; index < conditions.size(); ++index) {
				verifyCondition(conditions.get(index), joltConditions.get(index));
			}
		}
	}

	public void verifyMedications(List<Medication> medications) throws Exception {
		List<Map<String, Object>> joltMedications = TransformManager.chooseResources(result, "Medication");
		if (medications.isEmpty()) {
			Assert.assertTrue("No jolt medication", joltMedications.isEmpty());
		} else {
			Assert.assertEquals("Medication count", medications.size(), joltMedications.size());
			for (int index = 0; index < medications.size(); ++index) {
				verifyMedication(medications.get(index), joltMedications.get(index));
			}
		}
	}

	public static void compareOrganization(Organization organization, Map<String, Object> joltOrganization)
			throws Exception {
		joltOrganization.put("id", organization.getIdElement().getIdPart()); // ids do not have to match

		String expected = FHIRUtil.encodeToJSON(organization);
		String actual = JsonUtils.toJsonString(joltOrganization);
		JSONAssert.assertEquals("Jolt organization", expected, actual, true);
	}

	public void verifyMedication(Medication med, Map<String, Object> joltMed) throws Exception {
		Assert.assertNotNull("Jolt medication", joltMed);
		Assert.assertNotNull("Jolt medication id", joltMed.get("id"));

		joltMed.put("id", med.getIdElement().getIdPart()); // ids do not have to match

		verifyUpdateReference(med.hasManufacturer(), med.getManufacturer(), joltMed, "manufacturer");

		String joltMedJson = JsonUtils.toPrettyJsonString(joltMed);
		String medJson = FHIRUtil.encodeToJSON(med);
		JSONAssert.assertEquals("Jolt medication", medJson, joltMedJson, true);
	}

	public void verifyMedication(Medication med) throws Exception {
		Map<String, Object> joltMed = TransformManager.chooseResource(result, "Medication");

		Assert.assertNotNull("Jolt medication", joltMed);
		Assert.assertNotNull("Jolt medication id", joltMed.get("id"));

		joltMed.put("id", med.getIdElement().getIdPart()); // ids do not have to match

		verifyUpdateReference(med.hasManufacturer(), med.getManufacturer(), joltMed, "manufacturer");

		String joltMedJson = JsonUtils.toPrettyJsonString(joltMed);
		String medJson = FHIRUtil.encodeToJSON(med);
		JSONAssert.assertEquals("Jolt medication", medJson, joltMedJson, true);
	}

	public void verifyCondition(Condition condition, Map<String, Object> joltCondition) throws Exception {
		Assert.assertNotNull("Jolt condition", joltCondition);
		Assert.assertNotNull("Jolt condition id", joltCondition.get("id"));

		joltCondition.put("id", condition.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltCondition, "subject", condition.getSubject()); // patient is not yet implemented

		String joltConditionJson = JsonUtils.toPrettyJsonString(joltCondition);
		String conditionJson = FHIRUtil.encodeToJSON(condition);
		JSONAssert.assertEquals("Jolt condition", conditionJson, joltConditionJson, true);
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

	private static File toJsonFile(File xmlFile, String templateName, String caseName) throws Exception {
		OrgJsonUtil util = OrgJsonUtil.readXML(xmlFile.toString());
		JSONObject json = util.getJSONObject();
		String parentPath = xmlFile.getParent();
		String[] pieces = caseName.split("/");
		String filename = pieces[pieces.length - 1];
		File jsonFile = new File(parentPath, filename + ".json");
		FileUtils.writeStringToFile(jsonFile, json.toString(4), Charset.defaultCharset());
		return jsonFile;
	}

	public static List<Object> findJoltResult(File xmlFile, String templateName, String caseName) throws Exception {
		File jsonFile = toJsonFile(xmlFile, templateName, caseName);
		List<Object> joltResult = TransformManager.transformEntryInFile(templateName, jsonFile.toString());
		return joltResult;
	}

	public static List<Object> findJoltSectionResult(File xmlFile, String templateName, String caseName)
			throws Exception {
		File jsonFile = toJsonFile(xmlFile, templateName, caseName);
		List<Object> joltResult = TransformManager.transformSectionInFile(templateName, jsonFile.toString());
		return joltResult;
	}

	public static void putReference(Map<String, Object> joltResult, String property, Reference reference) {
		Map<String, Object> r = new LinkedHashMap<String, Object>();
		r.put("reference", reference.getReference());
		if (reference.getDisplay() != null) {
			r.put("display", reference.getDisplay());
		}
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

	@SuppressWarnings("unchecked")
	public static void verifyUpdateReferences(boolean has, List<Reference> referenceRoots,
			Map<String, Object> joltObject, String key) {
		if (has) {
			Object joltResult = joltObject.get(key);
			Assert.assertNotNull("Jolt list " + key, joltResult);
			List<Object> joltList = (List<Object>) joltResult;
			Assert.assertEquals("Jolt list count" + key, referenceRoots.size(), joltList.size());
			ListIterator<Object> itr = joltList.listIterator();
			int index = 0;
			while (itr.hasNext()) {
				Map<String, Object> joltElement = (Map<String, Object>) itr.next();
				Reference referenceObject = referenceRoots.get(index);
				Object reference = joltElement.get("reference");
				Assert.assertNotNull("Jolt " + key + " reference", reference);
				Assert.assertTrue("Reference is string", reference instanceof String);
				Map<String, Object> r = new LinkedHashMap<String, Object>();
				r.put("reference", referenceObject.getReference());
				itr.set(r);
				++index;
			}
		} else {
			Assert.assertNull("No jolt reference parent", joltObject.get(key));
		}
	}

	public void verify(Resource resource, Map<String, Object> joltResource, ResourceInfo info) throws Exception {
		if (caseName.equals("empty")) {
			Assert.assertNull("No jolt resource", joltResource);
			return;
		}

		String resourceType = resource.getResourceType().name();

		Assert.assertNotNull("Jolt resource exists", joltResource);
		Assert.assertNotNull("Jolt resource id exists", joltResource.get("id"));

		Map<String, Object> joltClone = new LinkedHashMap<String, Object>(joltResource);

		joltClone.put("id", resource.getIdElement().getIdPart()); // ids do not have to match
		if (info != null) {
			String patientProperty = info.getPatientPropertyName();
			if (patientProperty != null) {
				Reference patientReference = info.getPatientReference();
				JoltUtil.putReference(joltClone, patientProperty, patientReference); // patient not implemented
			}
		}

		String joltResourceJson = JsonUtils.toPrettyJsonString(joltClone);
		File joltResourceFile = new File(outputPath + caseName + "Jolt" + resourceType + ".json");
		FileUtils.writeStringToFile(joltResourceFile, joltResourceJson, Charset.defaultCharset());

		String resourceJson = FHIRUtil.encodeToJSON(resource);
		JSONAssert.assertEquals("Jolt resource", resourceJson, joltResourceJson, true);
	}

	private void verify(Resource resource, ResourceInfo info) throws Exception {
		String resourceType = resource.getResourceType().name();

		Map<String, Object> joltResource = TransformManager.chooseResource(result, resourceType);

		if (caseName.equals("empty")) {
			Assert.assertNull("No jolt resource", joltResource);
			return;
		}

		verify(resource, joltResource, info);
	}

	public void verify(Patient patient) throws Exception {
		Map<String, Object> joltPatient = TransformManager.chooseResource(result, "Patient");

		if (patient.hasManagingOrganization()) {
			String reference = findPathString(joltPatient, "managingOrganization.reference");
			Assert.assertNotNull("Managing organization reference exists", reference);
			Map<String, Object> resource = TransformManager.chooseResourceByReference(result, reference);
			Assert.assertNotNull("Managing organization", resource);

			String cda2FhirReference = patient.getManagingOrganization().getReference();
			Organization organization = bundleUtil.getResourceFromReference(cda2FhirReference, Organization.class);
			verify(organization, null);

			Map<String, Object> managingOrganization = findPathMap(joltPatient, "managingOrganization");
			managingOrganization.put("reference", cda2FhirReference);
		} else {
			String value = findPathString(joltPatient, "managingOrganization.reference");
			Assert.assertNull("No managing organization", value);
		}

		verify(patient, null);
	}

	public Map<String, Object> findPractitionerRoleForPractitionerId(String practitionerId) {
		List<Map<String, Object>> roles = TransformManager.chooseResources(result, "PractitionerRole");
		for (Map<String, Object> role : roles) {
			String reference = findPathString(role, "practitioner.reference");
			if (reference != null && reference.equals(practitionerId)) {
				return role;
			}
		}
		return null;
	}

	public void verify(PractitionerRole role, Map<String, Object> joltRole) throws Exception {
		Assert.assertNotNull("Jolt role exists", joltRole);

		Assert.assertTrue("Role has practitioner", role.hasPractitioner());
		Assert.assertTrue("Role has organization", role.hasOrganization());

		String joltPractitionerId = findPathString(joltRole, "practitioner.reference");
		String joltOrgId = findPathString(joltRole, "organization.reference");
		Assert.assertNotNull("Jolt role has practitioner", joltPractitionerId);
		Assert.assertNotNull("Jolt role has organization", joltOrgId);

		Map<String, Object> joltClone = new LinkedHashMap<String, Object>(joltRole);

		Map<String, Object> rolePractitioner = findPathMap(joltRole, "practitioner");
		Map<String, Object> roleOrg = findPathMap(joltRole, "organization");

		Map<String, Object> rolePractitionerClone = new LinkedHashMap<String, Object>(rolePractitioner);
		Map<String, Object> roleOrgClone = new LinkedHashMap<String, Object>(roleOrg);

		joltClone.put("practitioner", rolePractitionerClone);
		joltClone.put("organization", roleOrgClone);

		rolePractitionerClone.put("reference", role.getPractitioner().getReference());
		roleOrgClone.put("reference", role.getOrganization().getReference());

		verify(role, joltClone, null);
	}

	public void verifyEntity(String reference, String joltReference) throws Exception {
		Assert.assertNotNull("Entity reference exists", joltReference);

		Practitioner practitioner = bundleUtil.getResourceFromReference(reference, Practitioner.class);
		Assert.assertNotNull("Practitioner", practitioner);

		Map<String, Object> joltPractitioner = TransformManager.chooseResourceByReference(result, joltReference);
		Assert.assertNotNull("Jolt practitioner", joltPractitioner);

		if (practitioner.hasAddress()) {
			practitioner.getAddress().forEach(address -> {
				if (address.hasLine()) {
					address.getLine().forEach(line -> {
						String lineStr = line.asStringValue();
						String cleanLineStr = line.asStringValue().trim();
						if (!cleanLineStr.equals(lineStr)) {
							line.setValueAsString(cleanLineStr);
						}
					});
				}
			});
		}

		verify(practitioner, joltPractitioner, null);

		PractitionerRole role = bundleUtil.getPractitionerRole(reference);
		Map<String, Object> joltRole = findPractitionerRoleForPractitionerId(joltReference);
		if (role == null) {
			Assert.assertNull("No jolt role", joltRole);
		} else {
			String orgReference = role.getOrganization().getReference();
			Organization org = bundleUtil.getResourceFromReference(orgReference, Organization.class);
			String joltOrgReference = findPathString(joltRole, "organization.reference");
			Map<String, Object> joltOrg = TransformManager.chooseResourceByReference(result, joltOrgReference);
			verify(org, joltOrg, null);

			verify(role, joltRole);
		}
	}

	public void verify(AllergyIntolerance allergy, Map<String, Object> joltAllergy) throws Exception {
		AllergyIntoleranceInfo info = new AllergyIntoleranceInfo(allergy);

		Map<String, Object> joltClone = joltAllergy == null ? null : new LinkedHashMap<>(joltAllergy);

		if (allergy.hasRecorder()) {
			String reference = allergy.getRecorder().getReference();

			String joltReference = findPathString(joltAllergy, "recorder.reference");
			Assert.assertNotNull("Jolt recorder reference exists", joltReference);

			verifyEntity(reference, joltReference);

			Map<String, Object> recorder = findPathMap(joltAllergy, "recorder");
			Map<String, Object> recorderClone = new LinkedHashMap<>(recorder);
			joltClone.put("recorder", recorderClone);

			recorderClone.put("reference", reference);
		} else {
			String value = findPathString(joltAllergy, "recorder.reference");
			Assert.assertNull("No recorder", value);
		}

		verify(allergy, joltClone, info);
	}

	public void verify(AllergyIntolerance allergy) throws Exception {
		Map<String, Object> joltAllergy = TransformManager.chooseResource(result, "AllergyIntolerance");
		verify(allergy, joltAllergy);
	}

	@SuppressWarnings("unchecked")
	public void verify(Immunization immunization, Map<String, Object> joltImmunization) throws Exception {
		ImmunizationInfo info = new ImmunizationInfo(immunization);

		Map<String, Object> joltClone = joltImmunization == null ? null : new LinkedHashMap<>(joltImmunization);

		if (immunization.hasPractitioner()) {
			List<ImmunizationPractitionerComponent> practitioners = immunization.getPractitioner();
			List<Object> joltPractitioners = (List<Object>) joltClone.get("practitioner");

			Assert.assertEquals("Practitioner count", joltPractitioners.size(), practitioners.size());

			for (int index = 0; index < practitioners.size(); ++index) {
				ImmunizationPractitionerComponent practitioner = practitioners.get(index);
				Map<String, Object> joltPractitioner = (Map<String, Object>) joltPractitioners.get(index);

				joltPractitioner = new LinkedHashMap<String, Object>(joltPractitioner);
				joltPractitioners.set(index, joltPractitioner);

				String reference = practitioner.getActor().getReference();
				String joltReference = findPathString(joltPractitioner, "actor.reference");

				Assert.assertNotNull("Jolt actor reference exists", joltReference);

				verifyEntity(reference, joltReference);

				Map<String, Object> joltActor = (Map<String, Object>) joltPractitioner.get("actor");
				joltActor = new LinkedHashMap<String, Object>(joltActor);
				joltPractitioner.put("actor", joltActor);

				joltActor.put("reference", reference);
			}
		} else {
			Object value = joltImmunization.get("practitioner");
			Assert.assertNull("No practitioner", value);
		}

		if (immunization.hasManufacturer()) {
			String reference = immunization.getManufacturer().getReference();

			String joltReference = findPathString(joltImmunization, "manufacturer.reference");
			Assert.assertNotNull("Jolt manufacturer reference exists", joltReference);

			Organization org = bundleUtil.getResourceFromReference(reference, Organization.class);
			Map<String, Object> joltOrg = TransformManager.chooseResourceByReference(result, joltReference);
			verify(org, joltOrg, null);

			Map<String, Object> manufacturer = findPathMap(joltImmunization, "manufacturer");
			Map<String, Object> manufacturerClone = new LinkedHashMap<>(manufacturer);
			joltClone.put("manufacturer", manufacturerClone);

			manufacturerClone.put("reference", reference);
		} else {
			Object value = joltImmunization.get("manufacturer");
			Assert.assertNull("No manufacturer", value);
		}

		if (immunization.hasReaction()) {
			List<ImmunizationReactionComponent> reactions = immunization.getReaction();
			List<Object> joltReactions = (List<Object>) joltClone.get("reaction");

			Assert.assertEquals("Reaction count", joltReactions.size(), reactions.size());

			for (int index = 0; index < reactions.size(); ++index) {
				ImmunizationReactionComponent reaction = reactions.get(index);
				Map<String, Object> joltReaction = (Map<String, Object>) joltReactions.get(index);

				joltReaction = new LinkedHashMap<String, Object>(joltReaction);
				joltReactions.set(index, joltReaction);

				String reference = reaction.getDetail().getReference();
				String joltReference = findPathString(joltReaction, "detail.reference");

				Assert.assertNotNull("Jolt reaction reference exists", joltReference);

				Observation observation = bundleUtil.getResourceFromReference(reference, Observation.class);
				Map<String, Object> joltObservation = TransformManager.chooseResourceByReference(result, joltReference);

				verify(observation, joltObservation);

				Map<String, Object> joltDetail = (Map<String, Object>) joltReaction.get("detail");
				joltDetail = new LinkedHashMap<String, Object>(joltDetail);
				joltReaction.put("detail", joltDetail);

				joltDetail.put("reference", reference);
			}
		} else {
			Object value = joltImmunization.get("reaction");
			Assert.assertNull("No reaction", value);
		}

		verify(immunization, joltClone, info);
	}

	public void verify(Immunization immunization) throws Exception {
		Map<String, Object> joltImmunization = TransformManager.chooseResource(result, "Immunization");
		verify(immunization, joltImmunization);
	}

	@SuppressWarnings("unchecked")
	public void verify(Observation observation, Map<String, Object> joltObservation) throws Exception {
		ObservationInfo info = new ObservationInfo(observation);

		Map<String, Object> joltClone = joltObservation == null ? null : new LinkedHashMap<>(joltObservation);

		if (observation.hasPerformer()) {
			List<Reference> references = observation.getPerformer();
			List<Object> joltReferences = (List<Object>) joltClone.get("performer");

			Assert.assertEquals("Performer count", references.size(), joltReferences.size());

			joltReferences = new ArrayList<Object>(joltReferences);
			joltClone.put("performer", joltReferences);

			for (int index = 0; index < references.size(); ++index) {
				String reference = references.get(index).getReference();

				Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index);
				Assert.assertNotNull("Jolt performer exists", joltReferenceObject);
				String joltReference = (String) joltReferenceObject.get("reference");

				verifyEntity(reference, joltReference);

				joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
				joltReferences.set(index, joltReferenceObject);
				joltReferenceObject.put("reference", reference);
			}
		} else {
			Object value = joltObservation.get("performer");
			Assert.assertNull("No performer", value);
		}

		verify(observation, joltClone, info);
	}

	public void verify(Observation observation) throws Exception {
		Map<String, Object> joltObservation = TransformManager.chooseResource(result, "Observation");
		verify(observation, joltObservation);
	}

	@SuppressWarnings("unchecked")
	public void verify(DiagnosticReport report, Map<String, Object> joltReport) throws Exception {
		DiagnosticReportInfo info = new DiagnosticReportInfo(report);

		Map<String, Object> joltClone = joltReport == null ? null : new LinkedHashMap<>(joltReport);

		if (report.hasPerformer()) {
			List<DiagnosticReport.DiagnosticReportPerformerComponent> performers = report.getPerformer();
			List<Object> joltPerformers = (List<Object>) joltClone.get("performer");

			Assert.assertEquals("Performer count", joltPerformers.size(), performers.size());

			for (int index = 0; index < performers.size(); ++index) {
				DiagnosticReport.DiagnosticReportPerformerComponent performer = performers.get(index);
				Map<String, Object> joltPerformer = (Map<String, Object>) joltPerformers.get(index);

				if (performer.hasActor()) {
					joltPerformer = new LinkedHashMap<String, Object>(joltPerformer);
					joltPerformers.set(index, joltPerformer);

					String reference = performer.getActor().getReference();
					String joltReference = findPathString(joltPerformer, "actor.reference");

					Assert.assertNotNull("Jolt actor reference exists", joltReference);

					verifyEntity(reference, joltReference);

					Map<String, Object> joltActor = (Map<String, Object>) joltPerformer.get("actor");
					joltActor = new LinkedHashMap<String, Object>(joltActor);
					joltPerformer.put("actor", joltActor);

					joltActor.put("reference", reference);
				} else {
					Assert.assertNull("No performer actor", joltPerformer.get("actor"));
				}
			}
		} else {
			String value = findPathString(joltReport, "performer");
			Assert.assertNull("No performer", value);
		}

		if (report.hasResult()) {
			List<Reference> references = report.getResult();
			List<Object> joltReferences = (List<Object>) joltClone.get("result");

			Assert.assertEquals("result count", references.size(), joltReferences.size());

			joltReferences = new ArrayList<Object>(joltReferences);
			joltClone.put("result", joltReferences);

			for (int index = 0; index < references.size(); ++index) {
				String reference = references.get(index).getReference();

				Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index);
				Assert.assertNotNull("Jolt result exists", joltReferenceObject);
				String joltReference = (String) joltReferenceObject.get("reference");

				Observation obs = bundleUtil.getResourceFromReference(reference, Observation.class);
				Map<String, Object> joltObs = TransformManager.chooseResourceByReference(result, joltReference);

				verify(obs, joltObs);

				joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
				joltReferences.set(index, joltReferenceObject);
				joltReferenceObject.put("reference", reference);
			}
		} else {
			Object value = joltReport.get("result");
			Assert.assertNull("No report", value);
		}

		verify(report, joltClone, info);
	}

	public void verify(DiagnosticReport report) throws Exception {
		Map<String, Object> joltReport = TransformManager.chooseResource(result, "DiagnosticReport");
		verify(report, joltReport);
	}

	public void verify(Condition condition, Map<String, Object> joltCondition) throws Exception {
		ConditionInfo info = new ConditionInfo(condition);

		Map<String, Object> joltClone = joltCondition == null ? null : new LinkedHashMap<>(joltCondition);

		if (condition.hasAsserter()) {
			String reference = condition.getAsserter().getReference();

			String joltReference = findPathString(joltCondition, "asserter.reference");
			Assert.assertNotNull("Jolt asserter reference exists", joltReference);

			verifyEntity(reference, joltReference);

			Map<String, Object> asserter = findPathMap(joltCondition, "asserter");
			Map<String, Object> asserterClone = new LinkedHashMap<>(asserter);
			joltClone.put("asserter", asserterClone);

			asserterClone.put("reference", reference);
		} else {
			String value = findPathString(joltCondition, "asserter.reference");
			Assert.assertNull("No asserter", value);
		}

		verify(condition, joltClone, info);
	}

	public void verifyObservations(List<Observation> observations) throws Exception {
		List<Map<String, Object>> joltObservations = TransformManager.chooseResources(result, "Observation");
		if (observations.isEmpty()) {
			Assert.assertTrue("No observations", joltObservations.isEmpty());
		} else {
			Assert.assertEquals("Organization count", observations.size(), joltObservations.size());
			for (int index = 0; index < observations.size(); ++index) {
				verify(observations.get(index), joltObservations.get(index));
			}
		}
	}

	public List<Map<String, Object>> findResources(String resourceType) {
		return TransformManager.chooseResources(result, resourceType);
	}

	@SuppressWarnings("unchecked")
	public void verify(Procedure procedure, Map<String, Object> joltProcedure) throws Exception {
		ProcedureInfo info = new ProcedureInfo(procedure);

		Map<String, Object> joltClone = joltProcedure == null ? null : new LinkedHashMap<>(joltProcedure);

		if (procedure.hasPerformer()) {
			List<ProcedurePerformerComponent> performers = procedure.getPerformer();
			List<Object> joltPerformers = (List<Object>) joltClone.get("performer");

			Assert.assertEquals("Performer count", joltPerformers.size(), performers.size());

			for (int index = 0; index < performers.size(); ++index) {
				ProcedurePerformerComponent performer = performers.get(index);
				Map<String, Object> joltPerformer = (Map<String, Object>) joltPerformers.get(index);

				if (performer.hasActor() || performer.hasOnBehalfOf()) {
					joltPerformer = new LinkedHashMap<String, Object>(joltPerformer);
					joltPerformers.set(index, joltPerformer);
				}

				if (performer.hasActor()) {
					String reference = performer.getActor().getReference();
					String joltReference = findPathString(joltPerformer, "actor.reference");

					Assert.assertNotNull("Jolt actor reference exists", joltReference);

					verifyEntity(reference, joltReference);

					Map<String, Object> joltActor = (Map<String, Object>) joltPerformer.get("actor");
					joltActor = new LinkedHashMap<String, Object>(joltActor);
					joltPerformer.put("actor", joltActor);

					joltActor.put("reference", reference);
				} else {
					Assert.assertNull("No performer actor", joltPerformer.get("actor"));
				}
				if (performer.hasOnBehalfOf()) {
					String reference = performer.getOnBehalfOf().getReference();
					String joltReference = findPathString(joltPerformer, "onBehalfOf.reference");

					Assert.assertNotNull("Jolt ob behalf reference exists", joltReference);

					Organization org = bundleUtil.getResourceFromReference(reference, Organization.class);
					Map<String, Object> joltOrg = TransformManager.chooseResourceByReference(result, joltReference);

					verify(org, joltOrg, null);

					Map<String, Object> joltOnBehalfOf = (Map<String, Object>) joltPerformer.get("onBehalfOf");
					joltOnBehalfOf = new LinkedHashMap<String, Object>(joltOnBehalfOf);
					joltPerformer.put("onBehalfOf", joltOnBehalfOf);

					joltOnBehalfOf.put("reference", reference);
				} else {
					Assert.assertNull("No performer on behalf", joltPerformer.get("onBehalf"));
				}
			}
		} else {
			String value = findPathString(joltProcedure, "recorder.reference");
			Assert.assertNull("No recorder", value);
		}

		verify(procedure, joltClone, info);
	}

	public void verify(Procedure procedure) throws Exception {
		Map<String, Object> joltProcedure = TransformManager.chooseResource(result, "Procedure");
		verify(procedure, joltProcedure);
	}
}
