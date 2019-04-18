package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.Product;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class MedicationDispenseGenerator {
	private static final Map<String, Object> STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/MedicationDispenseStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private List<PerformerGenerator> performerGenerators = new ArrayList<>();

	private CSCodeGenerator statusCodeGenerator;

	private MedicationInformationGenerator medInfoGenerator;

	private IVL_PQSimpleQuantityGenerator quantityGenerator;

	private List<SXCM_TSGenerator> effectiveTimeGenerators = new ArrayList<>();

	public MedicationDispenseGenerator() {
	}

	public void setIDGenerator(IDGenerator idGenerator) {
		idGenerators.add(idGenerator);
	}

	public void setStatusCodeGenerator(String value) {
		if (statusCodeGenerator == null) {
			statusCodeGenerator = CSCodeGenerator.getInstanceWithValue(STATUS, value);
		} else {
			statusCodeGenerator.set(value);
		}
	}

	public org.openhealthtools.mdht.uml.cda.consol.MedicationDispense generate(CDAFactories factories) {
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense md = factories.consol.createMedicationDispense();

		{
			II ii = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.18");
			md.getTemplateIds().add(ii);
		}

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			md.getIds().add(ii);
		});

		performerGenerators.forEach(performerGenerator -> {
			Performer2 performer = performerGenerator.generate(factories);
			md.getPerformers().add(performer);
		});

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			md.setStatusCode(cs);
		}

		if (medInfoGenerator != null) {
			ManufacturedProduct med = medInfoGenerator.generate(factories);
			Product product = factories.base.createProduct();
			product.setManufacturedProduct(med);
			md.setProduct(product);
		}

		if (quantityGenerator != null) {
			PQ pq = quantityGenerator.generate(factories);
			md.setQuantity(pq);
		}

		effectiveTimeGenerators.forEach(effectiveTimeGenerator -> {
			SXCM_TS ts = effectiveTimeGenerator.generate(factories);
			md.getEffectiveTimes().add(ts);
		});

		return md;
	}

	public static MedicationDispenseGenerator getDefaultInstance() {
		MedicationDispenseGenerator md = new MedicationDispenseGenerator();

		md.idGenerators.add(IDGenerator.getNextInstance());
		md.performerGenerators.add(PerformerGenerator.getDefaultInstance());
		md.statusCodeGenerator = CSCodeGenerator.getInstanceWithValue(STATUS, "completed");
		md.medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();
		md.quantityGenerator = IVL_PQSimpleQuantityGenerator.getDefaultInstance();
		md.effectiveTimeGenerators.add(new SXCM_TSGenerator("20180422"));
		md.effectiveTimeGenerators.add(new SXCM_TSGenerator("20190123"));

		return md;
	}

	public void verify(MedicationDispense medDispense) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No med dispense identifier", !medDispense.hasIdentifier());
		} else {
			IDGenerator.verifyList(medDispense.getIdentifier(), idGenerators);
		}

		if (statusCodeGenerator == null) {
			Assert.assertTrue("No med dispense status", !medDispense.hasStatus());
		} else {
			statusCodeGenerator.verify(medDispense.getStatus().toCode());
		}

		if (quantityGenerator == null) {
			Assert.assertTrue("No med dispense quantity", !medDispense.hasQuantity());
		} else {
			quantityGenerator.verify(medDispense.getQuantity());
		}

		if (effectiveTimeGenerators.isEmpty()) {
			Assert.assertTrue("No med dispense when prepared", !medDispense.hasWhenPrepared());
			Assert.assertTrue("No med dispense when handed over", !medDispense.hasWhenHandedOver());
		} else {
			effectiveTimeGenerators.get(0).verify(medDispense.getWhenPreparedElement().asStringValue());
			int count = effectiveTimeGenerators.size();
			if (count < 2) {
				Assert.assertTrue("No med dispense when handed over", !medDispense.hasWhenHandedOver());
			} else {
				effectiveTimeGenerators.get(1).verify(medDispense.getWhenHandedOverElement().asStringValue());
			}
		}
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationDispense md = BundleUtil.findOneResource(bundle, MedicationDispense.class);

		verify(md);

		BundleUtil util = new BundleUtil(bundle);

		if (medInfoGenerator == null) {
			Assert.assertTrue("No med dispense medication", !md.hasMedicationReference());
		} else {
			String medId = md.getMedicationReference().getReference();
			Medication medication = util.getResourceFromReference(medId, Medication.class);
			medInfoGenerator.verify(medication);
		}

		if (performerGenerators.isEmpty()) {
			Assert.assertTrue("No med dispense dispenser", !md.hasPerformer());
		} else if (!md.hasPerformer()) {
			int count = performerGenerators.size();
			Assert.assertEquals("Medication count", count, md.getPerformer().size());
			for (int index = 0; index < count; ++index) {
				String practitionerId = md.getPerformer().get(index).getActor().getReference();
				performerGenerators.get(index).verifyFromPractionerId(bundle, practitionerId);
			}
		}
	}

	public static Set<String> getAvailableStatusCodes() {
		return Collections.unmodifiableSet(STATUS.keySet());
	}
}
