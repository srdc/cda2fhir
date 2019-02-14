package tr.com.srdc.cda2fhir;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ImmunizationActivityImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ImmunizationActivityTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();
	}

	static private void verifyNotGiven(ImmunizationActivity act, Boolean value) throws Exception {
		act.setNegationInd(value);
		DiagnosticChain dxChain = new BasicDiagnostic();
		Boolean validation = act.validateImmunizationActivityNegationInd(dxChain, null);
		Assert.assertTrue("Invalid Immunization Activity in Test", validation);
		
		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected not given", value, immunization.getNotGiven());				
	}
	
	@Test
	public void testNegationInd() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) cdaObjFactory.createImmunizationActivity();
		verifyNotGiven(act, true);
		verifyNotGiven(act, false);
	}
}
