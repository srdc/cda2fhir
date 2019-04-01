package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class MedicationActivityGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public MedicationActivity generate(CDAFactories factories) {
		MedicationActivity ma = factories.consol.createMedicationActivity();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ma.getIds().add(ii);
		});

		return ma;
	}

	public static MedicationActivityGenerator getDefaultInstance() {
		MedicationActivityGenerator papg = new MedicationActivityGenerator();

		papg.idGenerators.add(IDGenerator.getNextInstance());

		return papg;
	}

	public void verify(MedicationStatement medicationStatement) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(medicationStatement.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !medicationStatement.hasIdentifier());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationStatement medicationStatement = BundleUtil.findOneResource(bundle, MedicationStatement.class);

		verify(medicationStatement);
	}
}
