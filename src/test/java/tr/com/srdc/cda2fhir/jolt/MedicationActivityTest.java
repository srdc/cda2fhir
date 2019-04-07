package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class MedicationActivityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/MedicationActivity/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void compareMedStatements(String caseName, MedicationStatement medStatement,
			Map<String, Object> joltMedStatement) throws Exception {
		Assert.assertNotNull("Jolt med statement", joltMedStatement);
		Assert.assertNotNull("Jolt  med statement id", joltMedStatement.get("id"));

		joltMedStatement.put("id", medStatement.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltMedStatement, "subject", medStatement.getSubject()); // patient is not yet implemented

		JoltUtil.verifyUpdateReference(medStatement.hasInformationSource(), medStatement.getInformationSource(),
				joltMedStatement, "informationSource");
		JoltUtil.verifyUpdateReference(medStatement.hasMedicationReference(), medStatement.getMedicationReference(),
				joltMedStatement, "medicationReference");

		JoltUtil.verifyUpdateReferences(medStatement.hasMedicationReference(), medStatement.getReasonReference(),
				joltMedStatement, "reasonReference");

		String joltMedStatementJson = JsonUtils.toPrettyJsonString(joltMedStatement);
		File joltMedStatementFile = new File(OUTPUT_PATH + caseName + "JoltMedStatement.json");
		FileUtils.writeStringToFile(joltMedStatementFile, joltMedStatementJson, Charset.defaultCharset());

		String medStatementJson = FHIRUtil.encodeToJSON(medStatement);
		JSONAssert.assertEquals("Jolt Med Statement", medStatementJson, joltMedStatementJson, true);
	}

	private static void runTest(MedicationActivityGenerator generator, String caseName) throws Exception {
		MedicationActivity ma = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tMedicationActivity2MedicationStatement(ma, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Problem bundle", bundle);

		MedicationStatement medicationStatement = BundleUtil.findOneResource(bundle, MedicationStatement.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRMedicationStatement", "json");
		FHIRUtil.printJSON(medicationStatement, filepath);

		generator.verify(bundle);

		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		List<PractitionerRole> practitionerRoles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		List<Organization> organizations = FHIRUtil.findResources(bundle, Organization.class);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		List<Medication> medications = FHIRUtil.findResources(bundle, Medication.class);

		File xmlFile = CDAUtilExtension.writeAsXML(ma, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "MedicationActivity", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		joltUtil.verifyOrganizations(organizations);
		joltUtil.verifyPractitioners(practitioners);
		joltUtil.verifyPractitionerRoles(practitionerRoles);
		joltUtil.verifyConditions(conditions);
		joltUtil.verifyMedications(medications);

		BundleUtil.findOneResource(bundle, MedicationRequest.class);

		Map<String, Object> joltMedStatement = TransformManager.chooseResource(joltResult, "MedicationStatement");
		if (medicationStatement == null) {
			Assert.assertNull("No medication statement", joltMedStatement);
		} else {
			compareMedStatements(caseName, medicationStatement, joltMedStatement);
		}
	}

	@Test
	public void testDefault() throws Exception {
		MedicationActivityGenerator generator = MedicationActivityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
