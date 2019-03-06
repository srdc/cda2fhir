package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.MedicationDispenseGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class MedicationDispenseTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testBasic() throws Exception {
		MedicationDispenseGenerator mdg = MedicationDispenseGenerator.getDefaultInstance();
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense md = mdg.generate(factories);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tMedicationDispense2MedicationDispense(md, bundleInfo).getBundle();
		
		Practitioner p = BundleUtil.findOneResource(bundle, Practitioner.class);
		mdg.verify(p);

		PractitionerRole role = BundleUtil.findOneResource(bundle, PractitionerRole.class);
		mdg.verify(role);
		
		Organization org = BundleUtil.findOneResource(bundle, Organization.class);
		mdg.verify(org);
		
		MedicationDispense dispense = BundleUtil.findOneResource(bundle, MedicationDispense.class);
		String reference = dispense.getPerformer().get(0).getActor().getReference(); 
		Assert.assertEquals("Medication Dispense should have the performer actor", reference, p.getId());
	}
}
