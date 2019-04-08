package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class VitalSignsOrganizerGenerator {
	private List<VitalSignObservationGenerator> generators = new ArrayList<>();

	public VitalSignsOrganizer generate(CDAFactories factories) {
		VitalSignsOrganizer vso = factories.consol.createVitalSignsOrganizer();

		generators.forEach(g -> {
			Observation o = g.generate(factories);
			vso.addObservation(o);
		});

		return vso;
	}

	public static VitalSignsOrganizerGenerator getDefaultInstance() {
		VitalSignsOrganizerGenerator og = new VitalSignsOrganizerGenerator();

		og.generators.add(VitalSignObservationGenerator.getDefaultInstance());

		return og;
	}

	public void verify(Bundle bundle) {
		List<org.hl7.fhir.dstu3.model.Observation> result = FHIRUtil.findResources(bundle,
				org.hl7.fhir.dstu3.model.Observation.class);

		Assert.assertEquals("Observation count", generators.size(), result.size());
		for (int index = 0; index < result.size(); ++index) {
			generators.get(index).verify(result.get(index));
		}
	}
}