package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.codesystems.MedicationRequestIntent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
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
import tr.com.srdc.cda2fhir.util.FHIRUtil;

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
		EntryResult entryResult = rt.medicationcdaSupplyOrder2MedicationRequest(defaultMedSupplyOrder, bInfo);
		Bundle resultBundle = entryResult.getBundle();
		MedicationRequest medRequest =  BundleUtil.findOneResource(resultBundle, MedicationRequest.class);
		Medication medication = BundleUtil.findOneResource(resultBundle, Medication.class);
		
		FHIRUtil.printJSON(resultBundle, "src/test/resources/output/test_123.json");
//		FHIRUtil.printJSON(resultBundle, "src/test/resources/output/test_123.json");

		Assert.assertEquals("MedicationRequest", medRequest.getResourceType().toString());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_ROOT_ID, medRequest.getIdentifierFirstRep().getValue());
		Assert.assertEquals(MedicationSupplyOrderGenerator.DEFAULT_STATUS_CODE, medRequest.getStatus().toCode());
		Assert.assertEquals(MedicationRequestIntent.INSTANCEORDER.toCode(), medRequest.getIntent().toCode());
		Assert.assertEquals(medication.getId(), medRequest.getMedicationReference().getReference());
		Assert.assertEquals("Patient/0", medRequest.getSubject().getReference());
		Assert.assertEquals("2019-01-01T00:00:00.000-05:00", medRequest.getAuthoredOnElement().getValueAsString());

		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_CODE_CODE, medication.getCode().getCodingFirstRep().getCode());
		Assert.assertEquals("urn:oid:" + vst.tOid2Url(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_CODE_SYSTEM), vst.tOid2Url(medication.getCode().getCodingFirstRep().getSystem()));
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_MANU_MATERIAL_DISPLAY_NAME, medication.getCode().getCodingFirstRep().getDisplay());
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_TRANSLATION_CODE, medication.getCode().getCoding().get(1).getCode());
		Assert.assertEquals("urn:oid:" + vst.tOid2Url(ManufacturedProductGenerator.DEFAULT_TRANSLATION_CODE_SYSTEM), vst.tOid2Url(medication.getCode().getCoding().get(1).getSystem()));
		Assert.assertEquals(ManufacturedProductGenerator.DEFAULT_TRANSLATION_DISPLAY_NAME, medication.getCode().getCoding().get(1).getDisplay());
	}
	
	
	public void testMedicationSupplyOrder2MedicationRequestNoEffectiveTime() throws Exception {
		
		BundleInfo bInfo = new BundleInfo(rt);
		MedicationSupplyOrder defaultMedSupplyOrder = medSupplyOrderGenerator.generateDefaultMedicationSupplyOrder();
		defaultMedSupplyOrder.getEffectiveTimes().clear();
		EntryResult entryResult = rt.medicationcdaSupplyOrder2MedicationRequest(defaultMedSupplyOrder, bInfo);
		Bundle resultBundle = entryResult.getBundle();
		MedicationRequest medRequest =  BundleUtil.findOneResource(resultBundle, MedicationRequest.class);

		Assert.assertFalse(medRequest.hasAuthoredOn());
	}

}
