package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ImmunizationMedicationInformationGenerator {
	private CEGenerator codeGenerator;
	private OrganizationGenerator orgGenerator;
	private STGenerator lotNumberGenerator;

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

		if (lotNumberGenerator != null) {
			Material material = mf.getManufacturedMaterial();
			if (material == null) {
				material = factories.base.createMaterial();
				mf.setManufacturedMaterial(material);
			}
			ST st = lotNumberGenerator.generate(factories);
			material.setLotNumberText(st);
		}

		return mf;
	}

	public static ImmunizationMedicationInformationGenerator getDefaultInstance() {
		ImmunizationMedicationInformationGenerator imig = new ImmunizationMedicationInformationGenerator();

		imig.codeGenerator = CEGenerator.getNextInstance();
		imig.orgGenerator = OrganizationGenerator.getDefaultInstance();
		imig.lotNumberGenerator = STGenerator.getNextInstance();

		return imig;
	}

	public void verify(Immunization immunization) {
		if (codeGenerator == null) {
			Assert.assertTrue("No vaccine code", !immunization.hasVaccineCode());
		} else {
			codeGenerator.verify(immunization.getVaccineCode());
		}

		if (lotNumberGenerator == null) {
			Assert.assertTrue("No vaccine code", !immunization.hasLotNumber());
		} else {
			lotNumberGenerator.verify(immunization.getLotNumber());
		}
	}

	public void verify(Organization organization) {
		if (orgGenerator == null) {
			Assert.assertNull("No immunization manufacturer", organization);
		} else {
			orgGenerator.verify(organization);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);

		verify(immunization);

		BundleUtil util = new BundleUtil(bundle);

		if (orgGenerator == null) {
			Assert.assertTrue("No immunization manufacturer", immunization.hasManufacturer());
		} else {
			String orgId = immunization.getManufacturer().getReference();
			Organization org = util.getResourceFromReference(orgId, Organization.class);
			orgGenerator.verify(org);
		}
	}
}
