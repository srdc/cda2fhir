package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ImmunizationActivityGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	private Boolean negationInd;

	private List<EffectiveTimeGenerator> effectiveTimeGenerators = new ArrayList<>();

	private ImmunizationMedicationInformationGenerator medInfoGenerator;

	private List<PerformerGenerator> performerGenerators = new ArrayList<>();

	public ImmunizationActivity generate(CDAFactories factories) {
		ImmunizationActivity ia = factories.consol.createImmunizationActivity();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ia.getIds().add(ii);
		});

		if (negationInd != null) {
			ia.setNegationInd(negationInd.booleanValue());
		}

		if (effectiveTimeGenerators != null) {
			effectiveTimeGenerators.forEach(ef -> {
				IVL_TS ivlTs = ef.generate(factories);
				ia.getEffectiveTimes().add(ivlTs);
			});
		}

		if (medInfoGenerator != null) {
			ManufacturedProduct mp = medInfoGenerator.generate(factories);
			Consumable consumable = factories.base.createConsumable();
			consumable.setManufacturedProduct(mp);
			ia.setConsumable(consumable);
		}

		if (!performerGenerators.isEmpty()) {
			performerGenerators.forEach(pg -> {
				Performer2 performer = pg.generate(factories);
				ia.getPerformers().add(performer);
			});
		}

		return ia;
	}

	public static ImmunizationActivityGenerator getDefaultInstance() {
		ImmunizationActivityGenerator ma = new ImmunizationActivityGenerator();

		ma.idGenerators.add(IDGenerator.getNextInstance());
		ma.negationInd = true;
		ma.effectiveTimeGenerators.add(EffectiveTimeGenerator.getValueOnlyInstance("20190204"));
		ma.medInfoGenerator = ImmunizationMedicationInformationGenerator.getDefaultInstance();
		ma.performerGenerators.add(PerformerGenerator.getDefaultInstance());

		return ma;
	}

	public void verify(Immunization immunization) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No immunization identifier", !immunization.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(immunization.getIdentifier().get(index));
			}
		}

		if (negationInd == null) {
			Assert.assertTrue("No immunization not given", !immunization.hasNotGiven());
		} else {
			Assert.assertEquals("Immunization not given", negationInd.booleanValue(), immunization.getNotGiven());
		}

		if (effectiveTimeGenerators.isEmpty()) {
			Assert.assertTrue("No immunization date", !immunization.hasDate());
		} else {
			EffectiveTimeGenerator etg = effectiveTimeGenerators.get(effectiveTimeGenerators.size() - 1);
			etg.verifyValue(immunization.getDateElement().asStringValue());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		verify(immunization);

		if (medInfoGenerator == null) {
			Assert.assertTrue("No vaccine code", !immunization.hasVaccineCode());
			Assert.assertTrue("No manufacturer", !immunization.hasManufacturer());
			Assert.assertTrue("No lot number", !immunization.hasLotNumber());
		} else {
			medInfoGenerator.verify(bundle);
		}

		if (performerGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner", !immunization.hasPractitioner());
		} else {
			Assert.assertEquals("Practitioner count", 1, immunization.getPractitioner().size());
			String practitionerId = immunization.getPractitioner().get(0).getActor().getReference();
			PerformerGenerator pg = performerGenerators.get(performerGenerators.size() - 1);
			pg.verifyFromPractionerId(bundle, practitionerId);
		}
	}
}
