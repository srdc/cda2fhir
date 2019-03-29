package tr.com.srdc.cda2fhir;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.codesystems.MedicationRequestIntent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.ManufacturedProductGenerator;
import tr.com.srdc.cda2fhir.testutil.MedicationSupplyOrderGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class MedicationRequestTest {

	private static MedicationSupplyOrderGenerator medSupplyOrderGenerator;
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;
	private static ValueSetsTransformerImpl vst;

	@BeforeClass
	public static void init() {

		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		medSupplyOrderGenerator = new MedicationSupplyOrderGenerator(factories);
		rt = new ResourceTransformerImpl();
		vst = new ValueSetsTransformerImpl();
	}

	@Test
	public void testMedicationSupplyOrder2MedicationRequest() throws Exception {

		BundleInfo bInfo = new BundleInfo(rt);
		MedicationSupplyOrder defaultMedSupplyOrder = medSupplyOrderGenerator.generateDefaultMedicationSupplyOrder();
		EntryResult entryResult = rt.medicationSupplyOrder2MedicationRequest(defaultMedSupplyOrder, bInfo);
		Bundle resultBundle = entryResult.getBundle();
		MedicationRequest medRequest = BundleUtil.findOneResource(resultBundle, MedicationRequest.class);
		Medication medication = BundleUtil.findOneResource(resultBundle, Medication.class);
		Practitioner practitioner = BundleUtil.findOneResource(resultBundle, Practitioner.class);

		Assert.assertEquals("MedicationRequest", medRequest.getResourceType().toString());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_ROOT_ID,
				medRequest.getIdentifierFirstRep().getValue());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_STATUS_CODE, medRequest.getStatus().toCode());
		Assert.assertEquals(MedicationRequestIntent.INSTANCEORDER.toCode(), medRequest.getIntent().toCode());
		Assert.assertEquals(medication.getId(), medRequest.getMedicationReference().getReference());
		Assert.assertEquals("Patient/0", medRequest.getSubject().getReference());
		Assert.assertEquals(1, medRequest.getDispenseRequest().getNumberOfRepeatsAllowed());

		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_QUANTITY_UNIT,
				medRequest.getDispenseRequest().getQuantity().getUnit());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_QUANTITY_VALUE,
				medRequest.getDispenseRequest().getQuantity().getValue().toString());
		Assert.assertEquals("2019-01-01T00:00:00.000-05:00",
				medRequest.getDispenseRequest().getValidityPeriod().getStartElement().getValueAsString());
		Assert.assertEquals("2019-01-01T12:34:56.000-05:00",
				medRequest.getDispenseRequest().getValidityPeriod().getEndElement().getValueAsString());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_INSTRUCTION, medRequest.getNoteFirstRep().getText());
		Assert.assertEquals(medRequest.getRequester().getAgent().getReference(), practitioner.getId());

		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_CODE_CODE,
				medication.getCode().getCodingFirstRep().getCode());
		Assert.assertEquals("urn:oid:" + vst.tOid2Url(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_CODE_SYSTEM),
				vst.tOid2Url(medication.getCode().getCodingFirstRep().getSystem()));
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_DISPLAY_NAME,
				medication.getCode().getCodingFirstRep().getDisplay());
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_TRANSLATION_CODE,
				medication.getCode().getCoding().get(1).getCode());
		Assert.assertEquals("urn:oid:" + vst.tOid2Url(ManufacturedProductGenerator.DEFAULT_TRANSLATION_CODE_SYSTEM),
				vst.tOid2Url(medication.getCode().getCoding().get(1).getSystem()));
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_TRANSLATION_DISPLAY_NAME,
				medication.getCode().getCoding().get(1).getDisplay());

	}

	@Test
	public void testMedicationActivity() throws Exception {
		BundleInfo bInfo = new BundleInfo(rt);
		MedicationActivity medActivity = factories.consol.createMedicationActivity();
		Supply supply = medSupplyOrderGenerator.generateDefaultMedicationSupplyOrder();
		medActivity.addSupply(supply);
		EntryResult entryResult = rt.tMedicationActivity2MedicationStatement(medActivity, bInfo);
		List<MedicationRequest> medRequests = BundleUtil.findResources(entryResult.getBundle(), MedicationRequest.class,
				1);
		List<Medication> medications = BundleUtil.findResources(entryResult.getBundle(), Medication.class, 1);
		List<Practitioner> practitioners = BundleUtil.findResources(entryResult.getBundle(), Practitioner.class, 1);
		List<Organization> organizations = BundleUtil.findResources(entryResult.getBundle(), Organization.class, 1);

	}

}
