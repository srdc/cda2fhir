package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class MedicationDispenseGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	private List<PerformerGenerator> performerGenerators = new ArrayList<>();

	MedicationDispenseGenerator() {
	}

	public org.openhealthtools.mdht.uml.cda.consol.MedicationDispense generate(CDAFactories factories) {
		org.openhealthtools.mdht.uml.cda.consol.MedicationDispense md = factories.consol.createMedicationDispense();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			md.getIds().add(ii);
		});

		performerGenerators.forEach(performerGenerator -> {
			Performer2 performer = performerGenerator.generate(factories);
			md.getPerformers().add(performer);
		});

		return md;
	}

	public static MedicationDispenseGenerator getDefaultInstance() {
		MedicationDispenseGenerator md = new MedicationDispenseGenerator();

		md.idGenerators.add(IDGenerator.getNextInstance());
		md.performerGenerators.add(PerformerGenerator.getDefaultInstance());

		return md;
	}

	public void verify(MedicationDispense medDispense) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No med dispense identifier", !medDispense.hasIdentifier());
		} else {
			IDGenerator.verifyList(medDispense.getIdentifier(), idGenerators);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationDispense md = BundleUtil.findOneResource(bundle, MedicationDispense.class);

		verify(md);

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
}
