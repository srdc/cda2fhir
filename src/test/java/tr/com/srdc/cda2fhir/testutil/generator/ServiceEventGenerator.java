package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition.CompositionEventComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Performer1;
import org.openhealthtools.mdht.uml.cda.ServiceEvent;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ServiceEventGenerator {
	private IVL_TSPeriodGenerator effectiveTimeGenerator;

	private List<AssignedEntityGenerator> performerGenerators = new ArrayList<>();

	public ServiceEvent generate(CDAFactories factories) {
		ServiceEvent se = factories.base.createServiceEvent();

		if (effectiveTimeGenerator != null) {
			se.setEffectiveTime(effectiveTimeGenerator.generate(factories));
		}

		performerGenerators.forEach(aeg -> {
			AssignedEntity ae = aeg.generate(factories);
			Performer1 performer = factories.base.createPerformer1();
			performer.setAssignedEntity(ae);
			se.getPerformers().add(performer);
		});

		return se;
	}

	public static ServiceEventGenerator getDefaultInstance() {
		ServiceEventGenerator g = new ServiceEventGenerator();

		g.effectiveTimeGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		g.performerGenerators.add(AssignedEntityGenerator.getDefaultInstance());

		return g;
	}

	public void verify(CompositionEventComponent event) {
		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No event period", !event.hasPeriod());
		} else {
			effectiveTimeGenerator.verify(event.getPeriod());
		}
	}

	public void verify(Bundle bundle, CompositionEventComponent event) throws Exception {
		verify(event);

		if (performerGenerators.isEmpty()) {
			Assert.assertTrue("No event performer", !event.hasDetail());
		} else {
			int count = performerGenerators.size();
			Assert.assertEquals("Performer count", count, event.getDetail().size());

			for (int index = 0; index < count; ++index) {
				Reference performer = event.getDetail().get(index);
				String practitionerId = performer.getReference();
				performerGenerators.get(index).verifyFromPractionerId(bundle, practitionerId);
			}
		}
	}
}
