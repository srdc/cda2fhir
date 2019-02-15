package tr.com.srdc.cda2fhir;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ImmunizationActivityImpl;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.PerformerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ImmunizationActivityTest {
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
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) cdaObjFactory.createImmunizationActivity();

		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected positive primary source", false, immunization.getPrimarySource());
				
		String lastName = "Doe";
		String firstName = "Joe";
		PerformerGenerator performerGenerator = new PerformerGenerator();
		performerGenerator.setFamilyName(lastName);
		performerGenerator.addGivenName(firstName);
		Performer2 performer = performerGenerator.generate(factories);
		act.getPerformers().add(performer);

		Bundle bundle1 = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization1 = BundleUtil.findOneResource(bundle1, Immunization.class);
		Assert.assertEquals("Unexpected negative primary source", true, immunization1.getPrimarySource());

		String reference = immunization1.getPractitioner().get(0).getActor().getReference();
		Practitioner practitioner = BundleUtil.findOneResource(bundle1, Practitioner.class);
		Assert.assertEquals("Unexpected Reference", reference, practitioner.getId());
		HumanName humanName = practitioner.getName().get(0);
		Assert.assertEquals("Unexpected Last Name", lastName, humanName.getFamily());
		Assert.assertEquals("Unexpected First Name", firstName, humanName.getGiven().get(0).asStringValue());		
	}
		
	static private void verifyNotGiven(ImmunizationActivity act, Boolean value) throws Exception {
		if (value != null) {
			act.setNegationInd(value);
			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateImmunizationActivityNegationInd(dxChain, null);
			Assert.assertTrue("Invalid Immunization Activity in Test", validation);
		}

		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected not given", value == null ? false : value, immunization.getNotGiven());				
	}
	
	@Test
	public void testNegationInd() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) cdaObjFactory.createImmunizationActivity();
		verifyNotGiven(act, true);
		verifyNotGiven(act, false);
	}
}
