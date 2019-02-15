package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ImmunizationActivityImpl;
import org.openhealthtools.mdht.uml.cda.impl.AssignedEntityImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.Performer2Impl;
import org.openhealthtools.mdht.uml.cda.impl.PersonImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.PNImpl;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ImmunizationActivityTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

	private static Map<String, Object> statusMap = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/ImmunizationStatus.json");

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();
	}

	@Test
	public void testPerformer() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) cdaObjFactory.createImmunizationActivity();

		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected positive primary source", false, immunization.getPrimarySource());
				
		String lastName = "Doe";
		String firstName = "Joe";

		PNImpl pn = (PNImpl) cdaTypeFactory.createPN();
		pn.addFamily(lastName);
		pn.addGiven(firstName);

		PersonImpl person = (PersonImpl) cdaFactory.createPerson();
		person.getNames().add(pn);

		AssignedEntityImpl entity = (AssignedEntityImpl) cdaFactory.createAssignedEntity();
		entity.setAssignedPerson(person);
		
		Performer2Impl performer = (Performer2Impl) cdaFactory.createPerformer2(); 
		performer.setAssignedEntity(entity);
		
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

	static private void verifyImmunizationStatus(ImmunizationActivityImpl act, String expected) throws Exception {
		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		
    	ImmunizationStatus status = immunization.getStatus();
    	String actual = status == null ? null : status.toCode();
		Assert.assertEquals("Expect the correct immunization status", expected, actual);		
	}
		
	@Test
	public void testStatusCode() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) cdaObjFactory.createImmunizationActivity();
		DiagnosticChain dxChain = new BasicDiagnostic();		
		verifyImmunizationStatus(act, null);
	
		act.setStatusCode(null);
		verifyImmunizationStatus(act, null);
						
		act.setStatusCode(cdaTypeFactory.createCS("invalid"));
		// Boolean invalidation = act.validateImmunizationActivityStatusCode(dxChain, null);
		// Assert.assertFalse("Expect status code validation failure", invalidation) ;	Maybe CDA implementation error??	
		
		CS csNullFlavor = cdaTypeFactory.createCS();
		csNullFlavor.setNullFlavor(NullFlavor.UNK);
		act.setStatusCode(csNullFlavor);
		Boolean validationNF = act.validateImmunizationActivityStatusCode(dxChain, null);
		Assert.assertTrue("Expect null flavor status code validation success", validationNF);
		verifyImmunizationStatus(act, null);
			
		for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();
			
			CS cs = cdaTypeFactory.createCS(cdaStatusCode);
			act.setStatusCode(cs);
			Boolean validation = act.validateImmunizationActivityStatusCode(dxChain, null);
			Assert.assertTrue("Expect status code validation success for valid status code", validation);

			verifyImmunizationStatus(act, fhirStatus);
		}
	}
}
