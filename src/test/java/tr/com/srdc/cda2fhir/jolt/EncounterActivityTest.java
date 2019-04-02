package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.EncounterActivityGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EncounterActivityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/EncounterActivity/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	@SuppressWarnings("unchecked")
	private static void compareEncounters(String caseName, Encounter encounter, Map<String, Object> joltEncounter)
			throws Exception {
		Assert.assertNotNull("Jolt encounter exists", joltEncounter);
		Assert.assertNotNull("Jolt encounter id exists", joltEncounter.get("id"));

		String joltEncounterJsonPre = JsonUtils.toPrettyJsonString(joltEncounter);
		File joltEncounterFilePre = new File(OUTPUT_PATH + caseName + "JoltEncounterPre.json");
		FileUtils.writeStringToFile(joltEncounterFilePre, joltEncounterJsonPre, Charset.defaultCharset());

		joltEncounter.put("id", encounter.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltEncounter, "subject", encounter.getSubject()); // patient is not yet implemented

		if (encounter.hasParticipant()) {
			List<Object> joltParticipants = (List<Object>) joltEncounter.get("participant");
			List<EncounterParticipantComponent> participants = encounter.getParticipant();
			Assert.assertEquals("Encounter participant count", participants.size(), joltParticipants.size());
			for (int index = 0; index < participants.size(); ++index) {
				EncounterParticipantComponent participant = participants.get(index);
				Map<String, Object> joltParticipant = (Map<String, Object>) joltParticipants.get(index);
				if (participant.hasIndividual()) {
					Map<String, Object> joltIndividual = (Map<String, Object>) joltParticipant.get("individual");
					Assert.assertNotNull("Participant individual", joltIndividual);
					Object reference = joltIndividual.get("reference");
					Assert.assertNotNull("Participant individual reference", reference);
					Assert.assertTrue("Reference is string", reference instanceof String);
					JoltUtil.putReference(joltParticipant, "individual", participant.getIndividual());
				} else {
					Assert.assertNull("No performer actor", joltParticipant.get("individual"));
				}
			}
		} else {
			Assert.assertNull("No jolt procedure performer", joltEncounter.get("participant"));
		}

		if (encounter.hasDiagnosis()) {
			List<Object> joltDiagnoses = (List<Object>) joltEncounter.get("diagnosis");
			List<DiagnosisComponent> diagnoses = encounter.getDiagnosis();
			Assert.assertEquals("Encounter diagnosis count", diagnoses.size(), joltDiagnoses.size());
			for (int index = 0; index < diagnoses.size(); ++index) {
				DiagnosisComponent diagnosis = diagnoses.get(index);
				Map<String, Object> joltDiagnosis = (Map<String, Object>) joltDiagnoses.get(index);
				if (diagnosis.hasCondition()) {
					Map<String, Object> joltECondition = (Map<String, Object>) joltDiagnosis.get("condition");
					Assert.assertNotNull("Participant condition", joltECondition);
					Object reference = joltECondition.get("reference");
					Assert.assertNotNull("Participant condition reference", reference);
					Assert.assertTrue("Reference is string", reference instanceof String);
					JoltUtil.putReference(joltDiagnosis, "condition", diagnosis.getCondition());
				} else {
					Assert.assertNull("No encounter diagnosis", joltDiagnosis.get("condition"));
				}
			}
		} else {
			Assert.assertNull("No jolt procedure performer", joltEncounter.get("participant"));
		}

		if (encounter.hasLocation()) {
			List<Object> joltLocations = (List<Object>) joltEncounter.get("location");
			List<EncounterLocationComponent> locations = encounter.getLocation();
			Assert.assertEquals("Encounter location count", locations.size(), joltLocations.size());
			for (int index = 0; index < locations.size(); ++index) {
				EncounterLocationComponent location = locations.get(index);
				Map<String, Object> joltLocation = (Map<String, Object>) joltLocations.get(index);
				if (location.hasLocation()) {
					Map<String, Object> joltLLocation = (Map<String, Object>) joltLocation.get("location");
					Assert.assertNotNull("Participant location", joltLLocation);
					Object reference = joltLLocation.get("reference");
					Assert.assertNotNull("Participant location reference", reference);
					Assert.assertTrue("Reference is string", reference instanceof String);
					JoltUtil.putReference(joltLocation, "location", location.getLocation());
				} else {
					Assert.assertNull("No encounter diagnosis", joltLocation.get("location"));
				}
			}
		} else {
			Assert.assertNull("No jolt procedure performer", joltEncounter.get("participant"));
		}

		String joltEncounterJson = JsonUtils.toPrettyJsonString(joltEncounter);
		File joltEncounterFile = new File(OUTPUT_PATH + caseName + "JoltEncounter.json");
		FileUtils.writeStringToFile(joltEncounterFile, joltEncounterJson, Charset.defaultCharset());

		String procedureJson = FHIRUtil.encodeToJSON(encounter);
		JSONAssert.assertEquals("Jolt encounter", procedureJson, joltEncounterJson, true);
	}

	private static void runTest(EncounterActivityGenerator generator, String caseName) throws Exception {
		EncounterActivities ec = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tEncounterActivity2Encounter(ec, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		FHIRUtil.printJSON(bundle, "srs/test/resources/output/encounterBundle.json");
		Assert.assertNotNull("Encounter bundle", bundle);

		Encounter encounter = BundleUtil.findOneResource(bundle, Encounter.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIREncounter", "json");
		FHIRUtil.printJSON(encounter, filepath);

		generator.verify(bundle);

		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		List<PractitionerRole> practitionerRoles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		List<Organization> organizations = FHIRUtil.findResources(bundle, Organization.class);

		File xmlFile = CDAUtilExtension.writeAsXML(ec, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "EncounterActivity", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, caseName, OUTPUT_PATH);

		joltUtil.verifyOrganizations(organizations);
		joltUtil.verifyPractitioners(practitioners);
		joltUtil.verifyPractitionerRoles(practitionerRoles);

		Map<String, Object> joltEncounter = TransformManager.chooseResource(joltResult, "Encounter");
		if (encounter == null) {
			Assert.assertNull("No encounter", joltEncounter);
		} else {
			compareEncounters(caseName, encounter, joltEncounter);
		}
	}

	@Test
	public void testDefault() throws Exception {
		EncounterActivityGenerator generator = EncounterActivityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testFull() throws Exception {
		EncounterActivityGenerator generator = EncounterActivityGenerator.getFullInstance();
		runTest(generator, "fullCase");
	}

}
