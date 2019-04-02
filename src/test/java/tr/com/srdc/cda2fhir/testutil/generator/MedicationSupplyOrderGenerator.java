package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class MedicationSupplyOrderGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public MedicationSupplyOrder generate(CDAFactories factories) {
		MedicationSupplyOrder mso = factories.consol.createMedicationSupplyOrder();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			mso.getIds().add(ii);
		});

		return mso;
	}

	public static MedicationSupplyOrderGenerator getDefaultInstance() {
		MedicationSupplyOrderGenerator indication = new MedicationSupplyOrderGenerator();

		indication.idGenerators.add(IDGenerator.getNextInstance());

		return indication;
	}

	public void verify(MedicationRequest medRequest) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(medRequest.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No med request identifier", !medRequest.hasIdentifier());
		}
	}
}
