package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProcedureActivityProcedureGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProcedureActivityProcedureTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProcedureActivityProcedure/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	@SuppressWarnings("unchecked")
	private static void compareProcedures(String caseName, Procedure procedure, Map<String, Object> joltProcedure)
			throws Exception {
		Assert.assertNotNull("Jolt procedure exists", joltProcedure);
		Assert.assertNotNull("Jolt procedure id exists", joltProcedure.get("id"));

		joltProcedure.put("id", procedure.getIdElement().getIdPart()); // ids do not have to match
		JoltUtil.putReference(joltProcedure, "subject", procedure.getSubject()); // patient is not yet implemented

		if (procedure.hasPerformer()) {
			List<Object> joltPerformers = (List<Object>) joltProcedure.get("performer");
			List<ProcedurePerformerComponent> performers = procedure.getPerformer();
			Assert.assertEquals("Precedure performer count", performers.size(), joltPerformers.size());
			for (int index = 0; index < performers.size(); ++index) {
				ProcedurePerformerComponent performer = performers.get(index);
				Map<String, Object> joltPerformer = (Map<String, Object>) joltPerformers.get(index);
				if (performer.hasActor()) {
					Map<String, Object> joltActor = (Map<String, Object>) joltPerformer.get("actor");
					Assert.assertNotNull("Performer actor", joltActor);
					Object reference = joltActor.get("reference");
					Assert.assertNotNull("Performer actor reference", reference);
					Assert.assertTrue("Reference is string", reference instanceof String);
					JoltUtil.putReference(joltPerformer, "actor", performer.getActor());
				} else {
					Assert.assertNull("No performer actor", joltPerformer.get("actor"));
				}
				if (performer.hasOnBehalfOf()) {
					Map<String, Object> joltOnBehalfOf = (Map<String, Object>) joltPerformer.get("onBehalfOf");
					Assert.assertNotNull("Performer on behalf of", joltOnBehalfOf);
					Object reference = joltOnBehalfOf.get("reference");
					Assert.assertNotNull("Performer on behalf of reference", reference);
					Assert.assertTrue("Reference is string", reference instanceof String);
					JoltUtil.putReference(joltPerformer, "onBehalfOf", performer.getOnBehalfOf());
				} else {
					Assert.assertNull("No performer on behalf", joltPerformer.get("onBehalf"));
				}
			}
		} else {
			Assert.assertNull("No jolt procedure performer", joltProcedure.get("performer"));
		}

		String joltProcedureJson = JsonUtils.toPrettyJsonString(joltProcedure);
		File joltProcedureFile = new File(OUTPUT_PATH + caseName + "JoltProcedure.json");
		FileUtils.writeStringToFile(joltProcedureFile, joltProcedureJson, Charset.defaultCharset());

		String procedureJson = FHIRUtil.encodeToJSON(procedure);
		JSONAssert.assertEquals("Jolt procedure", procedureJson, joltProcedureJson, true);
	}

	private static void runTest(ProcedureActivityProcedureGenerator generator, String caseName) throws Exception {
		ProcedureActivityProcedure pap = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tProcedure2Procedure(pap, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Procedure bundle", bundle);

		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		String filepath = String.format("%s%s%s.%s", OUTPUT_PATH, caseName, "CDA2FHIRProcedure", "json");
		FHIRUtil.printJSON(procedure, filepath);

		generator.verify(bundle);

		List<Practitioner> practitioners = FHIRUtil.findResources(bundle, Practitioner.class);
		List<PractitionerRole> practitionerRoles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		List<Organization> organizations = FHIRUtil.findResources(bundle, Organization.class);

		File xmlFile = CDAUtilExtension.writeAsXML(pap, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProcedureActivityProcedure", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		joltUtil.verifyOrganizations(organizations);
		joltUtil.verifyPractitioners(practitioners);
		joltUtil.verifyPractitionerRoles(practitionerRoles);

		Map<String, Object> joltProcedure = TransformManager.chooseResource(joltResult, "Procedure");
		if (procedure == null) {
			Assert.assertNull("No procedure", joltProcedure);
		} else {
			compareProcedures(caseName, procedure, joltProcedure);
		}
	}

	@Test
	public void testDefault() throws Exception {
		ProcedureActivityProcedureGenerator generator = ProcedureActivityProcedureGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}
}
