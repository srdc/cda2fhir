package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.PerformerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ProcedureActivityProcedureTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;
	
	private static ConsolFactoryImpl cdaObjFactory;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		factories = CDAFactories.init();
		
		cdaObjFactory = factories.consol;
	}
	
	@Test
	public void testPerformer() throws Exception {
		ProcedureActivityProcedure pap = cdaObjFactory.createProcedureActivityProcedure();

		String organizationName = "PAP Organization";
		
		PerformerGenerator performerGenerator = new PerformerGenerator();
		performerGenerator.setCode();
		performerGenerator.setOrganizationName(organizationName);
		Performer2 performer = performerGenerator.generate(factories);
		pap.getPerformers().add(performer);
		
		Bundle bundle = rt.tProcedure2Procedure(pap);

		Organization organization = BundleUtil.findOneResource(bundle, Organization.class);
		Assert.assertEquals("Unexpected organization name", organizationName, organization.getName());		

		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);

		Assert.assertTrue("Expect a performer", procedure.hasPerformer());
		ProcedurePerformerComponent fhirPerformer = procedure.getPerformer().get(0);
		Assert.assertTrue("Expect a performer on behalf organization", fhirPerformer.hasOnBehalfOf());
		Reference organizationReference = fhirPerformer.getOnBehalfOf();
		Assert.assertEquals("Expect performer on behalf to point bundle organization", organization.getId(), organizationReference.getReference());
		Assert.assertTrue("Expect a performer role", fhirPerformer.hasRole());
		Coding role = fhirPerformer.getRole().getCoding().get(0);
		Assert.assertEquals("Expect the default role code", PerformerGenerator.DEFAULT_CODE_CODE, role.getCode());
	}
}
