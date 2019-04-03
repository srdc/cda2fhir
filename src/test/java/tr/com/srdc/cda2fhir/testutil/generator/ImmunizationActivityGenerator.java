package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ImmunizationActivityGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public ImmunizationActivity generate(CDAFactories factories) {
		ImmunizationActivity ia = factories.consol.createImmunizationActivity();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ia.getIds().add(ii);
		});

		return ia;
	}

	public static ImmunizationActivityGenerator getDefaultInstance() {
		ImmunizationActivityGenerator ma = new ImmunizationActivityGenerator();

		ma.idGenerators.add(IDGenerator.getNextInstance());

		return ma;
	}

	public void verify(Immunization immunization) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(immunization.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No immunization identifier", !immunization.hasIdentifier());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		verify(immunization);
	}
}
