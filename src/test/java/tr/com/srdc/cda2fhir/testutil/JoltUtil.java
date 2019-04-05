package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportPerformerComponent;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationPractitionerComponent;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
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

	private static abstract class ResourceInfo {
		public String getPatientPropertyName() {
			return null;
		}

		public Reference getPatientReference() {
			return null;
		}

		public abstract void copyReferences(Map<String, Object> joltResult);
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

		@SuppressWarnings("unchecked")
		@Override
		public void copyReferences(Map<String, Object> joltResult) {
			JoltUtil.verifyUpdateReference(immunization.hasManufacturer(), immunization.getManufacturer(), joltResult,
					"manufacturer");
			List<Object> joltPractitioners = (List<Object>) joltResult.get("practitioner");
			if (immunization.getPractitioner().isEmpty()) {
				Assert.assertNull("No practitioner reference", joltPractitioners);
			} else {
				List<ImmunizationPractitionerComponent> practitioners = immunization.getPractitioner();
				for (int index = 0; index < practitioners.size(); ++index) {
					ImmunizationPractitionerComponent p = practitioners.get(index);
					Map<String, Object> joltElement = (Map<String, Object>) joltPractitioners.get(index);
					JoltUtil.verifyUpdateReference(p.hasActor(), p.getActor(), joltElement, "actor");
					++index;
				}
			}
			if (immunization.hasReaction()) {
				List<Object> joltReactions = (List<Object>) joltResult.get("reaction");
				Map<String, Object> joltReaction = (Map<String, Object>) joltReactions.get(0);
				JoltUtil.verifyUpdateReference(immunization.hasReaction(),
						immunization.getReaction().get(0).getDetail(), joltReaction, "detail");
			}
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

		@SuppressWarnings("unchecked")
		@Override
		public void copyReferences(Map<String, Object> joltResult) {
			List<Object> joltPerformers = (List<Object>) joltResult.get("performer");
			if (observation.getPerformer().isEmpty()) {
				Assert.assertNull("No observation performer reference", joltPerformers);
			} else {
				List<Reference> performers = observation.getPerformer();
				for (int index = 0; index < performers.size(); ++index) {
					Map<String, Object> r = new LinkedHashMap<String, Object>();
					r.put("reference", performers.get(index).getReference());
					joltPerformers.set(index, r);
					++index;
				}
			}
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

		@SuppressWarnings("unchecked")
		@Override
		public void copyReferences(Map<String, Object> joltResult) {
			List<Object> joltPerformers = (List<Object>) joltResult.get("performer");
			if (report.getPerformer().isEmpty()) {
				Assert.assertNull("No observation performer reference", joltPerformers);
			} else {
				List<DiagnosticReportPerformerComponent> performers = report.getPerformer();
				for (int index = 0; index < performers.size(); ++index) {
					Map<String, Object> joltElement = (Map<String, Object>) joltPerformers.get(index);
					Map<String, Object> joltElementActor = (Map<String, Object>) joltElement.get("actor");
					joltElementActor.put("reference", performers.get(index).getActor().getReference());
				}
			}

			List<Object> joltResults = (List<Object>) joltResult.get("result");
			if (report.getResult().isEmpty()) {
				Assert.assertNull("No observation result reference", joltResults);
			} else {
				List<Reference> results = report.getResult();
				for (int index = 0; index < results.size(); ++index) {
					Map<String, Object> r = new LinkedHashMap<String, Object>();
					r.put("reference", results.get(index).getReference());
					joltResults.set(index, r);
					++index;
				}
			}
		}
	}

	private static class PatientInfo extends ResourceInfo {
		private Patient patient;

		public PatientInfo(Patient patient) {
			this.patient = patient;
		}

		@Override
		public void copyReferences(Map<String, Object> joltResult) {
		}
	}

	private static final Map<String, String> PATIENT_PROPERTY = new HashMap<>();
	static {
		PATIENT_PROPERTY.put("Immunization", "patient");
	}

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
		String resourceType = resource.getResourceType().name();

		Assert.assertNotNull("Jolt resource exists", joltResource);
		Assert.assertNotNull("Jolt resource id exists", joltResource.get("id"));

		joltResource.put("id", resource.getIdElement().getIdPart()); // ids do not have to match
		Reference patientReference = info.getPatientReference();
		String patientProperty = info.getPatientPropertyName();
		JoltUtil.putReference(joltResource, patientProperty, patientReference); // patient is not yet implemented

		info.copyReferences(joltResource);

		String joltResourceJson = JsonUtils.toPrettyJsonString(joltResource);
		File joltResourceFile = new File(outputPath + caseName + "Jolt" + resourceType + ".json");
		FileUtils.writeStringToFile(joltResourceFile, joltResourceJson, Charset.defaultCharset());

		String resourceJson = FHIRUtil.encodeToJSON(resource);
		JSONAssert.assertEquals("Jolt resource", resourceJson, joltResourceJson, true);
	}

	public void verify(Resource resource, ResourceInfo info) throws Exception {
		String resourceType = resource.getResourceType().name();

		Map<String, Object> joltResource = TransformManager.chooseResource(result, resourceType);

		if (caseName.equals("empty")) {
			Assert.assertNull("No jolt resource", joltResource);
			return;
		}

		Assert.assertNotNull("Jolt resource exists", joltResource);
		Assert.assertNotNull("Jolt resource id exists", joltResource.get("id"));

		joltResource.put("id", resource.getIdElement().getIdPart()); // ids do not have to match
		String patientProperty = info.getPatientPropertyName();
		if (patientProperty != null) {
			Reference patientReference = info.getPatientReference();
			JoltUtil.putReference(joltResource, patientProperty, patientReference); // patient is not yet implemented
		}

		info.copyReferences(joltResource);

		String joltResourceJson = JsonUtils.toPrettyJsonString(joltResource);
		File joltResourceFile = new File(outputPath + caseName + "Jolt" + resourceType + ".json");
		FileUtils.writeStringToFile(joltResourceFile, joltResourceJson, Charset.defaultCharset());

		String resourceJson = FHIRUtil.encodeToJSON(resource);
		JSONAssert.assertEquals("Jolt resource", resourceJson, joltResourceJson, true);
	}

	public void verify(Immunization immunization) throws Exception {
		ImmunizationInfo info = new ImmunizationInfo(immunization);
		verify(immunization, info);
	}

	public void verify(Observation observation) throws Exception {
		ObservationInfo info = new ObservationInfo(observation);
		verify(observation, info);
	}

	public void verify(DiagnosticReport report) throws Exception {
		DiagnosticReportInfo info = new DiagnosticReportInfo(report);
		verify(report, info);
	}

	public void verify(Patient patient) throws Exception {
		PatientInfo info = new PatientInfo(patient);
		verify(patient, info);
	}

	public void verifyObservations(List<Observation> observations) throws Exception {
		List<Map<String, Object>> joltObservations = TransformManager.chooseResources(result, "Observation");
		if (observations.isEmpty()) {
			Assert.assertTrue("No obervations", joltObservations.isEmpty());
		} else {
			Assert.assertEquals("Organization count", observations.size(), joltObservations.size());
			for (int index = 0; index < observations.size(); ++index) {
				ObservationInfo info = new ObservationInfo(observations.get(index));
				verify(observations.get(index), joltObservations.get(index), info);
			}
		}
	}
}
