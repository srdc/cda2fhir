package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.List;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ADGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.AssignedEntityGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.OrganizationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AssignedEntityTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/AssignedEntity/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void runTest(AssignedEntityGenerator generator, String caseName) throws Exception {
		AssignedEntity entity = generator.generate(factories);

		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntityResult cda2FhirResult = rt.tAssignedEntity2Practitioner(entity, new BundleInfo(rt));

		Practitioner practitioner = cda2FhirResult.getPractitioner();
		PractitionerRole practitionerRole = cda2FhirResult.getPractitionerRole();
		Organization organization = cda2FhirResult.getOrganization();

		generator.verify(practitioner);
		generator.verify(practitionerRole);
		generator.verify(organization);

		File xmlFile = CDAUtilExtension.writeAsXML(entity, OUTPUT_PATH, caseName);

		FHIRUtil.printJSON(practitioner, OUTPUT_PATH + caseName + "CDA2FHIRPractitioner.json");
		if (practitionerRole != null) {
			FHIRUtil.printJSON(practitionerRole, OUTPUT_PATH + caseName + "CDA2FHIRPractitionerRole.json");
		}
		if (organization != null) {
			FHIRUtil.printJSON(organization, OUTPUT_PATH + caseName + "CDA2FHIROrganization.json");
		}

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "AssignedEntity", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, cda2FhirResult.getBundle(), caseName, OUTPUT_PATH);

		joltUtil.verifyEntity(practitioner, practitionerRole, organization);
	}

	@Test
	public void testDefault() throws Exception {
		AssignedEntityGenerator generator = AssignedEntityGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testFull() throws Exception {
		AssignedEntityGenerator generator = AssignedEntityGenerator.getFullInstance();
		runTest(generator, "fullCase");
	}

	@Test
	public void testNoNameIdentifierOrganization() throws Exception {
		AssignedEntityGenerator generator = AssignedEntityGenerator.getFullInstance();

		OrganizationGenerator og = new OrganizationGenerator();
		og.setADGenerator(ADGenerator.getDefaultInstance()); // something that is not name/identifier
		generator.setOrganizationGenerator(og);

		runTest(generator, "orgNoNameIdentifier");
	}
}
