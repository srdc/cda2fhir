package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class MedicationInformationGenerator {
	private CEGenerator codeGenerator;
	private OrganizationGenerator orgGenerator;

	public ManufacturedProduct generate(CDAFactories factories) {
		ManufacturedProduct mf = factories.base.createManufacturedProduct();

		if (codeGenerator != null) {
			Material material = factories.base.createMaterial();
			CE ce = codeGenerator.generate(factories);
			material.setCode(ce);
			mf.setManufacturedMaterial(material);
		}

		if (orgGenerator != null) {
			org.openhealthtools.mdht.uml.cda.Organization org = orgGenerator.generate(factories);
			mf.setManufacturerOrganization(org);
		}

		return mf;
	}

	public static MedicationInformationGenerator getDefaultInstance() {
		MedicationInformationGenerator mig = new MedicationInformationGenerator();

		mig.codeGenerator = CEGenerator.getNextInstance();
		mig.orgGenerator = OrganizationGenerator.getDefaultInstance();

		return mig;
	}

	public void verify(Medication med) {
		if (codeGenerator == null) {
			Assert.assertTrue("No medication code", !med.hasCode());
		} else {
			codeGenerator.verify(med.getCode());
		}
	}

	public void verify(Organization organization) {
		if (orgGenerator == null) {
			Assert.assertNull("No medication manufacturer", organization);
		} else {
			orgGenerator.verify(organization);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Medication med = BundleUtil.findOneResource(bundle, Medication.class);

		verify(med);

		BundleUtil util = new BundleUtil(bundle);

		if (orgGenerator == null) {
			Assert.assertTrue("No medication manufacturer", med.hasManufacturer());
		} else {
			String orgId = med.getManufacturer().getReference();
			Organization org = util.getResourceFromReference(orgId, Organization.class);
			orgGenerator.verify(org);
		}
	}
}
