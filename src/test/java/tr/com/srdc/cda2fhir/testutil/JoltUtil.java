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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationPractitionerComponent;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationReactionComponent;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestRequesterComponent;
import org.hl7.fhir.dstu3.model.MedicationStatement;
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

	private static class EncounterInfo extends ResourceInfo {
		private Encounter encounter;

		public EncounterInfo(Encounter encounter) {
			this.encounter = encounter;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return encounter.getSubject();
		}
	}

	private static class MedicationStatementInfo extends ResourceInfo {
		private MedicationStatement medStatement;

		public MedicationStatementInfo(MedicationStatement medStatement) {
			this.medStatement = medStatement;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return medStatement.getSubject();
		}
	}

	private static class MedicationRequestInfo extends ResourceInfo {
		private MedicationRequest medRequest;

		public MedicationRequestInfo(MedicationRequest medRequest) {
			this.medRequest = medRequest;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return medRequest.getSubject();
		}
	}

	private static class MedicationDispenseInfo extends ResourceInfo {
		private MedicationDispense medDispense;

		public MedicationDispenseInfo(MedicationDispense medDispense) {
			this.medDispense = medDispense;
		}

		@Override
		public String getPatientPropertyName() {
			return "subject";
		}

		@Override
		public Reference getPatientReference() {
			return medDispense.getSubject();
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

	@SuppressWarnings("unchecked")
	public static Consumer<Map<String, Object>> getIdentierValueUpdate(String current, String replacement) {
		return r -> {
			if (r == null) {
				return;
			}
			List<Object> identifiers = JoltUtil.findPathValue(r, "identifier[]");
			identifiers.forEach(identifier -> {
				Map<String, Object> identifierAsMap = (Map<String, Object>) identifier;
				Object value = identifierAsMap.get("value");
				if (value != null && current.equals(value.toString())) {
					identifierAsMap.put("value", replacement);
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
	private Consumer<Map<String, Object>> valueChanger;
	private BiConsumer<Map<String, Object>, Resource> customComparer;

	public JoltUtil(List<Object> result, Bundle bundle, String caseName, String outputPath) {
		this.result = result;
		this.caseName = caseName;
		this.outputPath = outputPath;
		this.bundleUtil = new BundleUtil(bundle);
	}

	public void setValueChanger(Consumer<Map<String, Object>> valueChanger) {
		this.valueChanger = valueChanger;
	}

	public void setValueChanger(BiConsumer<Map<String, Object>, Resource> customComparer) {
		this.customComparer = customComparer;
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

	public static List<Object> findJoltDocumentResult(File xmlFile, String templateName, String caseName)
			throws Exception {
		File jsonFile = toJsonFile(xmlFile, templateName, caseName);
		List<Object> joltResult = TransformManager.transformDocumentInFile(templateName, jsonFile.toString());
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

		if (valueChanger != null) {
			valueChanger.accept(joltClone);
		}

		if (customComparer != null) {
			customComparer.accept(joltClone, resource);
		}

		String joltResourceJson = JsonUtils.toPrettyJsonString(joltClone);
		File joltResourceFile = new File(outputPath + caseName + "Jolt" + resourceType + ".json");
		FileUtils.writeStringToFile(joltResourceFile, joltResourceJson, Charset.defaultCharset());

		String resourceJson = FHIRUtil.encodeToJSON(resource);
		try {
			JSONAssert.assertEquals("Jolt resource", resourceJson, joltResourceJson, true);
		} catch (Throwable t) {
			File joltErrorFileCDA2FHIR = new File(outputPath + caseName + "ErrorCDA2FHIR" + ".json");
			FileUtils.writeStringToFile(joltErrorFileCDA2FHIR, resourceJson, Charset.defaultCharset());

			File joltErrorFileJolt = new File(outputPath + caseName + "ErrorJolt" + ".json");
			FileUtils.writeStringToFile(joltErrorFileJolt, joltResourceJson, Charset.defaultCharset());
			throw t;
		}
	}

	@SuppressWarnings("unchecked")
	public void verify(Patient patient, Map<String, Object> joltPatient) throws Exception {
		Map<String, Object> joltClone = joltPatient == null ? null : new LinkedHashMap<>(joltPatient);

		if (patient.hasManagingOrganization()) {
			String reference = findPathString(joltPatient, "managingOrganization.reference");
			Assert.assertNotNull("Managing organization reference exists", reference);
			Map<String, Object> joltOrganization = TransformManager.chooseResourceByReference(result, reference);
			Assert.assertNotNull("Managing organization", joltOrganization);

			String cda2FhirReference = patient.getManagingOrganization().getReference();
			Organization organization = bundleUtil.getResourceFromReference(cda2FhirReference, Organization.class);
			verify(organization, joltOrganization, null);

			Map<String, Object> joltManagingOrg = (Map<String, Object>) joltClone.get("managingOrganization");
			joltManagingOrg = new LinkedHashMap<>(joltManagingOrg); // clone
			joltClone.put("managingOrganization", joltManagingOrg);
			joltManagingOrg.put("reference", cda2FhirReference);
		} else {
			String value = findPathString(joltPatient, "managingOrganization.reference");
			Assert.assertNull("No managing organization", value);
		}

		verify(patient, joltClone, null);
	}

	public void verify(Patient patient) throws Exception {
		Map<String, Object> joltPatient = TransformManager.chooseResource(result, "Patient");
		verify(patient, joltPatient);
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
			Assert.assertNotNull("Jolt role exists", joltRole);

			String orgReference = role.getOrganization().getReference();
			Organization org = bundleUtil.getResourceFromReference(orgReference, Organization.class);
			String joltOrgReference = findPathString(joltRole, "organization.reference");
			Map<String, Object> joltOrg = TransformManager.chooseResourceByReference(result, joltOrgReference);
			verify(org, joltOrg, null);

			verify(role, joltRole);
		}
	}

	public void verifyEntity(Practitioner practitioner, PractitionerRole role, Organization org) throws Exception {
		Map<String, Object> joltPractitioner = TransformManager.chooseResource(result, "Practitioner");
		Map<String, Object> joltRole = TransformManager.chooseResource(result, "PractitionerRole");
		Map<String, Object> joltOrganization = TransformManager.chooseResource(result, "Organization");

		if (practitioner == null) {
			Assert.assertNull("No practitioner", joltPractitioner);
		} else {
			verify(practitioner, joltPractitioner, null);
		}

		if (org == null) {
			Assert.assertNull("No organization", joltOrganization);
		} else {
			verify(org, joltOrganization, null);
		}

		if (role == null) {
			Assert.assertNull("No role", joltRole);
		} else {
			verify(role, joltRole);

			String joltPractRef = findPathString(joltRole, "practitioner.reference");
			Map<String, Object> joltPract2 = TransformManager.chooseResourceByReference(result, joltPractRef);
			Assert.assertTrue("Same practitioner", joltPractitioner == joltPract2);

			String joltOrgRef = findPathString(joltRole, "organization.reference");
			Map<String, Object> joltOrg2 = TransformManager.chooseResourceByReference(result, joltOrgRef);
			Assert.assertTrue("Same organization", joltOrganization == joltOrg2);
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
			Assert.assertNotNull("results exists", joltReferences);

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
		} else if (joltProcedure != null) {
			String value = findPathString(joltProcedure, "recorder.reference");
			Assert.assertNull("No recorder", value);
		}

		if (procedure.hasContext()) {
			Assert.assertNotNull("Jolt procedure exists", joltClone);

			String reference = procedure.getContext().getReference();

			Map<String, Object> joltContext = (Map<String, Object>) joltClone.get("context");
			Assert.assertNotNull("Jolt procedure context exists", joltContext);
			joltContext = new LinkedHashMap<String, Object>(joltContext);
			joltClone.put("context", joltContext);

			String joltReference = (String) joltContext.get("reference");
			Assert.assertNotNull("Jolt procedure context reference exists", joltReference);

			Encounter enc = bundleUtil.getResourceFromReference(reference, Encounter.class);
			Map<String, Object> joltEnc = TransformManager.chooseResourceByReference(result, joltReference);
			verify(enc, joltEnc);

			joltContext.put("reference", reference);
		} else if (joltProcedure != null) {
			String value = findPathString(joltProcedure, "context.reference");
			Assert.assertNull("No context reference", value);
		}

		verify(procedure, joltClone, info);
	}

	public void verify(Procedure procedure) throws Exception {
		Map<String, Object> joltProcedure = TransformManager.chooseResource(result, "Procedure");
		verify(procedure, joltProcedure);
	}

	@SuppressWarnings("unchecked")
	public void verify(Encounter encounter, Map<String, Object> joltEncounter) throws Exception {
		EncounterInfo info = new EncounterInfo(encounter);

		Map<String, Object> joltClone = joltEncounter == null ? null : new LinkedHashMap<>(joltEncounter);

		if (encounter.hasParticipant()) {
			List<EncounterParticipantComponent> participants = encounter.getParticipant();
			List<Object> joltParticipants = (List<Object>) joltClone.get("participant");
			joltParticipants = new ArrayList<Object>(joltParticipants);
			joltClone.put("participant", joltParticipants);

			Assert.assertEquals("Participant count", joltParticipants.size(), participants.size());

			for (int index = 0; index < participants.size(); ++index) {
				EncounterParticipantComponent participant = participants.get(index);
				Map<String, Object> joltParticipant = (Map<String, Object>) joltParticipants.get(index);

				if (participant.hasIndividual()) {
					joltParticipant = new LinkedHashMap<String, Object>(joltParticipant);
					joltParticipants.set(index, joltParticipant);

					String reference = participant.getIndividual().getReference();
					String joltReference = findPathString(joltParticipant, "individual.reference");

					Assert.assertNotNull("Jolt individual reference exists", joltReference);

					verifyEntity(reference, joltReference);

					Map<String, Object> joltIndividual = (Map<String, Object>) joltParticipant.get("individual");
					joltIndividual = new LinkedHashMap<String, Object>(joltIndividual);
					joltParticipant.put("individual", joltIndividual);

					joltIndividual.put("reference", reference);
				} else {
					Assert.assertNull("No participant individual", joltParticipant.get("individual"));
				}
			}
		} else {
			Object joltParticipant = joltEncounter.get("participant");
			Assert.assertNull("No participant", joltParticipant);
		}

		if (encounter.hasDiagnosis()) {
			List<DiagnosisComponent> diagnoses = encounter.getDiagnosis();
			List<Object> joltDiagnoses = (List<Object>) joltClone.get("diagnosis");
			joltDiagnoses = new ArrayList<Object>(joltDiagnoses);
			joltClone.put("diagnosis", joltDiagnoses);

			Assert.assertEquals("Diagnosis count", joltDiagnoses.size(), diagnoses.size());

			for (int index = 0; index < diagnoses.size(); ++index) {
				DiagnosisComponent diagnosis = diagnoses.get(index);
				Map<String, Object> joltDiagnosis = (Map<String, Object>) joltDiagnoses.get(index);

				if (diagnosis.hasCondition()) {
					joltDiagnosis = new LinkedHashMap<String, Object>(joltDiagnosis);
					joltDiagnoses.set(index, joltDiagnosis);

					String reference = diagnosis.getCondition().getReference();
					String joltReference = findPathString(joltDiagnosis, "condition.reference");

					Assert.assertNotNull("Jolt condition reference exists", joltReference);

					Condition condition = bundleUtil.getResourceFromReference(reference, Condition.class);
					Map<String, Object> joltCCondition = TransformManager.chooseResourceByReference(result,
							joltReference);
					verify(condition, joltCCondition);

					Map<String, Object> joltCondition = (Map<String, Object>) joltDiagnosis.get("condition");
					joltCondition = new LinkedHashMap<String, Object>(joltCondition);
					joltDiagnosis.put("condition", joltCondition);

					joltCondition.put("reference", reference);
				} else {
					Assert.assertNull("No diagnosis condition", joltDiagnosis.get("condition"));
				}
			}
		} else {
			Object joltDiagnosis = joltEncounter.get("diagnosis");
			Assert.assertNull("No diagnosis", joltDiagnosis);
		}

		if (encounter.hasLocation()) {
			List<EncounterLocationComponent> locations = encounter.getLocation();
			List<Object> joltLocations = (List<Object>) joltClone.get("location");
			joltLocations = new ArrayList<Object>(joltLocations);
			joltClone.put("location", joltLocations);

			Assert.assertEquals("Location count", joltLocations.size(), locations.size());

			for (int index = 0; index < locations.size(); ++index) {
				EncounterLocationComponent location = locations.get(index);
				Map<String, Object> joltLocation = (Map<String, Object>) joltLocations.get(index);

				if (location.hasLocation()) {
					joltLocation = new LinkedHashMap<String, Object>(joltLocation);
					joltLocations.set(index, joltLocation);

					String reference = location.getLocation().getReference();
					String joltReference = findPathString(joltLocation, "location.reference");

					Assert.assertNotNull("Jolt location reference exists", joltReference);

					Location rlocation = bundleUtil.getResourceFromReference(reference, Location.class);
					Map<String, Object> joltRLocation = TransformManager.chooseResourceByReference(result,
							joltReference);
					verify(rlocation, joltRLocation, null);

					Map<String, Object> joltLLocation = (Map<String, Object>) joltLocation.get("location");
					joltLLocation = new LinkedHashMap<String, Object>(joltLLocation);
					joltLocation.put("location", joltLLocation);

					joltLLocation.put("reference", reference);
				} else {
					Assert.assertNull("No location location", joltLocation.get("location"));
				}
			}
		} else {
			Object joltDiagnosis = joltEncounter.get("diagnosis");
			Assert.assertNull("No diagnosis", joltDiagnosis);
		}

		verify(encounter, joltClone, info);
	}

	public void verify(Encounter encounter) throws Exception {
		Map<String, Object> joltEncounter = TransformManager.chooseResource(result, "Encounter");
		verify(encounter, joltEncounter);
	}

	public void verify(Medication med, Map<String, Object> joltMed) throws Exception {
		Map<String, Object> joltClone = joltMed == null ? null : new LinkedHashMap<>(joltMed);

		if (med.hasManufacturer()) {
			String reference = med.getManufacturer().getReference();

			String joltReference = findPathString(joltMed, "manufacturer.reference");
			Assert.assertNotNull("Jolt manufacturer reference exists", joltReference);

			Organization org = bundleUtil.getResourceFromReference(reference, Organization.class);
			Map<String, Object> joltOrg = TransformManager.chooseResourceByReference(result, joltReference);
			verify(org, joltOrg, null);

			Map<String, Object> manufacturer = findPathMap(joltMed, "manufacturer");
			Map<String, Object> manufacturerClone = new LinkedHashMap<>(manufacturer);
			joltClone.put("manufacturer", manufacturerClone);

			manufacturerClone.put("reference", reference);
		} else {
			String value = findPathString(joltMed, "manufacturer.reference");
			Assert.assertNull("No manufacturer", value);
		}

		verify(med, joltClone, null);
	}

	public void verify(Medication med) throws Exception {
		Map<String, Object> joltMed = TransformManager.chooseResource(result, "Medication");
		verify(med, joltMed);
	}

	@SuppressWarnings("unchecked")
	public void verify(MedicationStatement medStatement, Map<String, Object> joltMedStatement) throws Exception {
		MedicationStatementInfo info = new MedicationStatementInfo(medStatement);

		Map<String, Object> joltClone = joltMedStatement == null ? null : new LinkedHashMap<>(joltMedStatement);

		if (medStatement.hasInformationSource()) {
			String reference = medStatement.getInformationSource().getReference();

			String joltReference = findPathString(joltMedStatement, "informationSource.reference");
			Assert.assertNotNull("Jolt information source reference exists", joltReference);

			verifyEntity(reference, joltReference);

			Map<String, Object> source = findPathMap(joltMedStatement, "informationSource");
			Map<String, Object> sourceClone = new LinkedHashMap<>(source);
			joltClone.put("informationSource", sourceClone);

			sourceClone.put("reference", reference);
		} else {
			String value = findPathString(joltMedStatement, "informationSource.reference");
			Assert.assertNull("No source", value);
		}

		if (medStatement.hasMedicationReference()) {
			String reference = medStatement.getMedicationReference().getReference();

			String joltReference = findPathString(joltMedStatement, "medicationReference.reference");
			Assert.assertNotNull("Jolt medication reference exists", joltReference);

			Medication mMed = bundleUtil.getResourceFromReference(reference, Medication.class);
			Map<String, Object> joltMMed = TransformManager.chooseResourceByReference(result, joltReference);
			verify(mMed, joltMMed);

			Map<String, Object> med = findPathMap(joltMedStatement, "medicationReference");
			Map<String, Object> medClone = new LinkedHashMap<>(med);
			joltClone.put("medicationReference", medClone);

			medClone.put("reference", reference);
		} else {
			String value = findPathString(joltMedStatement, "medicationReference.reference");
			Assert.assertNull("No med", value);
		}

		if (medStatement.hasReasonReference()) {
			List<Reference> references = medStatement.getReasonReference();
			List<Object> joltReferences = (List<Object>) joltClone.get("reasonReference");
			Assert.assertNotNull("reason exists", joltReferences);

			Assert.assertEquals("reason count", references.size(), joltReferences.size());

			joltReferences = new ArrayList<Object>(joltReferences);
			joltClone.put("reasonReference", joltReferences);

			for (int index = 0; index < references.size(); ++index) {
				String reference = references.get(index).getReference();

				Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index);
				Assert.assertNotNull("Jolt reason exists", joltReferenceObject);
				String joltReference = (String) joltReferenceObject.get("reference");

				Condition condition = bundleUtil.getResourceFromReference(reference, Condition.class);
				Map<String, Object> joltCondition = TransformManager.chooseResourceByReference(result, joltReference);

				verify(condition, joltCondition);

				joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
				joltReferences.set(index, joltReferenceObject);
				joltReferenceObject.put("reference", reference);
			}
		} else {
			Object value = joltMedStatement.get("reasonReference");
			Assert.assertNull("No reason", value);
		}

		verify(medStatement, joltClone, info);
	}

	public void verify(MedicationStatement medStatement) throws Exception {
		Map<String, Object> joltMedStatement = TransformManager.chooseResource(result, "MedicationStatement");
		verify(medStatement, joltMedStatement);
	}

	@SuppressWarnings("unchecked")
	public void verify(MedicationRequest request, Map<String, Object> joltRequest) throws Exception {
		MedicationRequestInfo info = new MedicationRequestInfo(request);

		Map<String, Object> joltClone = joltRequest == null ? null : new LinkedHashMap<>(joltRequest);

		if (request.hasRequester()) {
			MedicationRequestRequesterComponent requester = request.getRequester();

			Map<String, Object> joltRequester = (Map<String, Object>) joltClone.get("requester");
			Assert.assertNotNull("Jolt requester  exists", joltRequester);

			if (requester.hasAgent()) {
				joltRequester = new LinkedHashMap<String, Object>(joltRequester);
				joltClone.put("requester", joltRequester);

				String reference = requester.getAgent().getReference();
				String joltReference = findPathString(joltRequester, "agent.reference");

				verifyEntity(reference, joltReference);

				Map<String, Object> joltAgent = (Map<String, Object>) joltRequester.get("agent");
				joltAgent = new LinkedHashMap<String, Object>(joltAgent);
				joltRequester.put("agent", joltAgent);

				joltAgent.put("reference", reference);
			} else {
				Assert.assertNull("No requester agent", joltRequester.get("agent"));
			}
		} else {
			Object value = joltRequest.get("requester");
			Assert.assertNull("No requester", value);
		}

		if (request.hasMedicationReference()) {
			String reference = request.getMedicationReference().getReference();

			String joltReference = findPathString(joltRequest, "medicationReference.reference");
			Assert.assertNotNull("Jolt medication reference exists", joltReference);

			Medication mMed = bundleUtil.getResourceFromReference(reference, Medication.class);
			Map<String, Object> joltMMed = TransformManager.chooseResourceByReference(result, joltReference);
			verify(mMed, joltMMed);

			Map<String, Object> med = findPathMap(joltRequest, "medicationReference");
			Map<String, Object> medClone = new LinkedHashMap<>(med);
			joltClone.put("medicationReference", medClone);

			medClone.put("reference", reference);
		} else {
			String value = findPathString(joltRequest, "medicationReference.reference");
			Assert.assertNull("No med", value);
		}

		verify(request, joltClone, info);
	}

	public void verify(MedicationRequest request) throws Exception {
		Map<String, Object> joltRequest = TransformManager.chooseResource(result, "MedicationRequest");
		verify(request, joltRequest);
	}

	@SuppressWarnings("unchecked")
	public void verify(MedicationDispense dispense, Map<String, Object> joltDispense) throws Exception {
		MedicationDispenseInfo info = new MedicationDispenseInfo(dispense);

		Map<String, Object> joltClone = joltDispense == null ? null : new LinkedHashMap<>(joltDispense);

		if (dispense.hasPerformer()) {
			Assert.assertNotNull("Dispense exists", joltDispense);

			List<MedicationDispense.MedicationDispensePerformerComponent> performers = dispense.getPerformer();
			List<Object> joltPerformers = (List<Object>) joltClone.get("performer");

			Assert.assertNotNull("Performer exists", joltPerformers);
			Assert.assertEquals("Performer count", joltPerformers.size(), performers.size());

			for (int index = 0; index < performers.size(); ++index) {
				MedicationDispense.MedicationDispensePerformerComponent performer = performers.get(index);
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
			if (joltDispense != null) {
				String value = (String) joltDispense.get("performer");
				Assert.assertNull("No performer", value);
			}
		}

		if (dispense.hasMedicationReference()) {
			String reference = dispense.getMedicationReference().getReference();

			String joltReference = findPathString(joltDispense, "medicationReference.reference");
			Assert.assertNotNull("Jolt medication reference exists", joltReference);

			Medication mMed = bundleUtil.getResourceFromReference(reference, Medication.class);
			Map<String, Object> joltMMed = TransformManager.chooseResourceByReference(result, joltReference);
			verify(mMed, joltMMed);

			Map<String, Object> med = findPathMap(joltDispense, "medicationReference");
			Map<String, Object> medClone = new LinkedHashMap<>(med);
			joltClone.put("medicationReference", medClone);

			medClone.put("reference", reference);
		} else {
			String value = findPathString(joltDispense, "medicationReference.reference");
			Assert.assertNull("No med", value);
		}

		verify(dispense, joltClone, info);
	}

	public void verify(MedicationDispense dispense) throws Exception {
		Map<String, Object> joltDispense = TransformManager.chooseResource(result, "MedicationDispense");
		verify(dispense, joltDispense);
	}

	@SuppressWarnings("unchecked")
	public void sortSections(List<SectionComponent> sections, List<Object> joltSections) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		int index = 0;
		for (SectionComponent section : sections) {
			String code = section.getCode().getCoding().get(0).getCode();
			map.put(code, index);
			++index;
		}
		joltSections.sort((a, b) -> {
			Map<String, Object> mapa = (Map<String, Object>) a;
			Map<String, Object> mapb = (Map<String, Object>) b;

			String codea = findPathString(mapa, "code.coding[].code");
			String codeb = findPathString(mapb, "code.coding[].code");

			Integer inta = map.get(codea);
			Integer intb = map.get(codeb);

			return inta.intValue() - intb.intValue();
		});
	}

	@SuppressWarnings("unchecked")
	public void verify(Composition composition, Map<String, Object> joltComposition) throws Exception {
		Map<String, Object> joltClone = joltComposition == null ? null : new LinkedHashMap<>(joltComposition);

		if (composition.hasAuthor()) {
			Assert.assertNotNull("Author exists", joltClone);

			List<Reference> references = composition.getAuthor();
			List<Object> joltReferences = (List<Object>) joltClone.get("author");

			Assert.assertNotNull("Author exists", joltReferences);

			Assert.assertEquals("Author count", references.size(), joltReferences.size());

			joltReferences = new ArrayList<Object>(joltReferences);
			joltClone.put("author", joltReferences);

			for (int index = 0; index < references.size(); ++index) {
				String reference = references.get(index).getReference();

				Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index);
				Assert.assertNotNull("Jolt author exists", joltReferenceObject);
				String joltReference = (String) joltReferenceObject.get("reference");

				verifyEntity(reference, joltReference);

				joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
				joltReferences.set(index, joltReferenceObject);
				joltReferenceObject.put("reference", reference);
			}
		} else {
			if (joltClone != null) {
				Object value = joltClone.get("author");
				Assert.assertNull("No author", value);
			}
		}

		if (composition.hasAttester()) {
			Assert.assertNotNull("Composition exists", joltClone);

			List<Composition.CompositionAttesterComponent> attesters = composition.getAttester();
			List<Object> joltAttesters = (List<Object>) joltClone.get("attester");

			Assert.assertNotNull("Attester exists", joltAttesters);
			Assert.assertEquals("Attester count", joltAttesters.size(), attesters.size());

			for (int index = 0; index < attesters.size(); ++index) {
				Composition.CompositionAttesterComponent attester = attesters.get(index);
				Map<String, Object> joltAttester = (Map<String, Object>) joltAttesters.get(index);

				if (attester.hasParty()) {
					joltAttester = new LinkedHashMap<String, Object>(joltAttester);
					joltAttesters.set(index, joltAttester);

					String reference = attester.getParty().getReference();
					String joltReference = findPathString(joltAttester, "party.reference");

					Assert.assertNotNull("Jolt party reference exists", joltReference);

					verifyEntity(reference, joltReference);

					Map<String, Object> joltParty = (Map<String, Object>) joltAttester.get("party");
					joltParty = new LinkedHashMap<String, Object>(joltParty);
					joltAttester.put("party", joltParty);

					joltParty.put("reference", reference);
				} else {
					Assert.assertNull("No attestester", joltAttester.get("party"));
				}
			}
		} else {
			if (joltClone != null) {
				Object value = joltClone.get("attester");
				Assert.assertNull("No attester", value);
			}
		}

		if (composition.hasEvent()) {
			Assert.assertNotNull("Composition exists", joltClone);

			List<Composition.CompositionEventComponent> events = composition.getEvent();
			List<Object> joltEvents = (List<Object>) joltClone.get("event");

			Assert.assertNotNull("Events exists", joltEvents);
			Assert.assertEquals("Event count", joltEvents.size(), events.size());

			for (int index = 0; index < events.size(); ++index) {
				Composition.CompositionEventComponent event = events.get(index);
				Map<String, Object> joltEvent = (Map<String, Object>) joltEvents.get(index);

				if (event.hasDetail()) {
					joltEvent = new LinkedHashMap<String, Object>(joltEvent);
					joltEvents.set(index, joltEvent);

					List<Reference> references = event.getDetail();
					List<Object> joltReferences = (List<Object>) joltEvent.get("detail");

					Assert.assertNotNull("Details exists", joltReferences);

					Assert.assertEquals("Detail count", references.size(), joltReferences.size());

					joltReferences = new ArrayList<Object>(joltReferences);
					joltEvent.put("detail", joltReferences);

					for (int index2 = 0; index2 < references.size(); ++index2) {
						String reference = references.get(index2).getReference();

						Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index2);
						Assert.assertNotNull("Jolt detail exists", joltReferenceObject);
						String joltReference = (String) joltReferenceObject.get("reference");

						verifyEntity(reference, joltReference);

						joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
						joltReferences.set(index2, joltReferenceObject);
						joltReferenceObject.put("reference", reference);
					}
				} else {
					Assert.assertNull("No details", joltEvent.get("detail"));
				}
			}
		} else {
			if (joltClone != null) {
				String value = (String) joltClone.get("attester");
				Assert.assertNull("No attester", value);
			}
		}

		if (composition.hasCustodian()) {
			String reference = composition.getCustodian().getReference();
			Map<String, Object> joltReferenceObject = (Map<String, Object>) joltClone.get("custodian");
			Assert.assertNotNull("Jolt custodian exists", joltReferenceObject);
			String joltReference = (String) joltReferenceObject.get("reference");

			Organization organization = bundleUtil.getResourceFromReference(reference, Organization.class);
			Map<String, Object> joltOrganization = TransformManager.chooseResourceByReference(result, joltReference);

			verify(organization, joltOrganization, null);

			joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
			joltClone.put("custodian", joltReferenceObject);
			joltReferenceObject.put("reference", reference);
		} else {
			if (joltClone != null) {
				Object value = joltClone.get("custodian");
				Assert.assertNull("No custodian", value);
			}
		}

		if (composition.hasSubject()) {
			String reference = composition.getSubject().getReference();
			Map<String, Object> joltReferenceObject = (Map<String, Object>) joltClone.get("subject");
			Assert.assertNotNull("Jolt subject exists", joltReferenceObject);
			String joltReference = (String) joltReferenceObject.get("reference");

			Patient patient = bundleUtil.getResourceFromReference(reference, Patient.class);
			Map<String, Object> joltPatient = TransformManager.chooseResourceByReference(result, joltReference);

			verify(patient, joltPatient);

			joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
			joltClone.put("subject", joltReferenceObject);
			joltReferenceObject.put("reference", reference);
		} else {
			if (joltClone != null) {
				Object value = joltClone.get("subject");
				Assert.assertNull("No subject", value);
			}
		}

		if (composition.hasSection()) {
			List<SectionComponent> sections = composition.getSection();
			List<Object> joltSections = (List<Object>) joltClone.get("section");
			Assert.assertEquals("Section count", sections.size(), joltSections.size());

			sortSections(sections, joltSections);

			for (int index = 0; index < sections.size(); ++index) {
				SectionComponent section = sections.get(index);

				Map<String, Object> joltSection = (Map<String, Object>) joltSections.get(index);
				joltSection = new LinkedHashMap<String, Object>(joltSection);
				joltSections.set(index, joltSection);

				String code = section.getCode().getCoding().get(0).getCode();

				List<Reference> references = section.getEntry();
				List<Object> joltReferences = (List<Object>) joltSection.get("entry");
				joltReferences = new ArrayList<Object>(joltReferences);
				joltSection.put("entry", joltReferences);

				Assert.assertEquals("Entry count", references.size(), joltReferences.size());

				for (int index2 = 0; index2 < references.size(); ++index2) {
					String reference = references.get(index2).getReference();

					Map<String, Object> joltReferenceObject = (Map<String, Object>) joltReferences.get(index2);
					Assert.assertNotNull("Jolt entry exists", joltReferenceObject);
					String joltReference = (String) joltReferenceObject.get("reference");

					joltReferenceObject = new LinkedHashMap<String, Object>(joltReferenceObject);
					joltReferences.set(index2, joltReferenceObject);
					joltReferenceObject.put("reference", reference);

					if (code.equals(CDAUtilExtension.ALLERGIES_CODE)) {
						AllergyIntolerance allergy = bundleUtil.getResourceFromReference(reference,
								AllergyIntolerance.class);
						Map<String, Object> joltAllergy = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(allergy, joltAllergy);
					}
					if (code.equals(CDAUtilExtension.MEDICATIONS_CODE)) {
						MedicationStatement medStatement = bundleUtil.getResourceFromReference(reference,
								MedicationStatement.class);
						Map<String, Object> joltMedStatement = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(medStatement, joltMedStatement);
					}
					if (code.equals(CDAUtilExtension.IMMUNIZATIONS_CODE)) {
						Immunization immunization = bundleUtil.getResourceFromReference(reference, Immunization.class);
						Map<String, Object> joltImmunization = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(immunization, joltImmunization);
					}
					if (code.equals(CDAUtilExtension.RESULTS_CODE)) {
						DiagnosticReport report = bundleUtil.getResourceFromReference(reference,
								DiagnosticReport.class);
						Map<String, Object> joltReport = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(report, joltReport);
					}
					if (code.equals(CDAUtilExtension.VITALS_CODE)) {
						Observation observation = bundleUtil.getResourceFromReference(reference, Observation.class);
						Map<String, Object> joltObservation = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(observation, joltObservation);
					}
					if (code.equals(CDAUtilExtension.CONDITIONS_CODE)) {
						Condition condition = bundleUtil.getResourceFromReference(reference, Condition.class);
						Map<String, Object> joltCondition = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(condition, joltCondition);
					}
					if (code.equals(CDAUtilExtension.ENCOUNTERS_CODE)) {
						Encounter encounter = bundleUtil.getResourceFromReference(reference, Encounter.class);
						Map<String, Object> joltEncounter = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(encounter, joltEncounter);
					}
					if (code.equals(CDAUtilExtension.PROCEDURES_CODE)) {
						Procedure procedure = bundleUtil.getResourceFromReference(reference, Procedure.class);
						Map<String, Object> joltProcedure = TransformManager.chooseResourceByReference(result,
								joltReference);
						verify(procedure, joltProcedure);
					}

				}
			}
		} else {
			if (joltClone != null) {
				Object value = joltClone.get("section");
				Assert.assertNull("No section", value);
			}

		}

		verify(composition, joltClone, null);
	}

	public void verify(Composition composition) throws Exception {
		Map<String, Object> joltComposition = TransformManager.chooseResource(result, "Composition");
		verify(composition, joltComposition);
	}
}
