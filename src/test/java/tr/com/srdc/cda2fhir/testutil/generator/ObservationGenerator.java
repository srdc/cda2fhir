package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ObservationGenerator {
	private static final Map<String, Object> OBSERVATION_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ObservationStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private CDGenerator codeGenerator;

	private StatusCodeGenerator statusCodeGenerator;

	private IVL_TSPeriodGenerator effectiveTimeGenerator;

	private List<CDGenerator> targetSiteCodeGenerators = new ArrayList<>();

	public Observation generate(CDAFactories factories) {
		Observation obs = factories.base.createObservation();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			obs.getIds().add(ii);
		});

		if (codeGenerator != null) {
			CD code = codeGenerator.generate(factories);
			obs.setCode(code);
		}

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			obs.setStatusCode(cs);
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			obs.setEffectiveTime(ivlTs);
		}

		if (!targetSiteCodeGenerators.isEmpty()) {
			targetSiteCodeGenerators.forEach(tscg -> {
				CD cd = tscg.generate(factories);
				obs.getTargetSiteCodes().add(cd);
			});
		}

		return obs;
	}

	public static ObservationGenerator getDefaultInstance() {
		ObservationGenerator obs = new ObservationGenerator();

		obs.idGenerators.add(IDGenerator.getNextInstance());
		obs.codeGenerator = CDGenerator.getNextInstance();
		obs.statusCodeGenerator = new StatusCodeGenerator(OBSERVATION_STATUS, "unknown");
		obs.statusCodeGenerator.set("active");
		obs.effectiveTimeGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		obs.targetSiteCodeGenerators.add(CDGenerator.getNextInstance());

		return obs;
	}

	public void verify(org.hl7.fhir.dstu3.model.Observation observation) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No observation identifier", !observation.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(observation.getIdentifier().get(index));
			}
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No observation status", !observation.hasCode());
		} else {
			codeGenerator.verify(observation.getCode());
		}

		if (statusCodeGenerator == null) {
			Assert.assertTrue("No observation status", !observation.hasStatus());
		} else {
			statusCodeGenerator.verify(observation.getStatus().toCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No observation effective period", !observation.hasEffectivePeriod());
		} else {
			effectiveTimeGenerator.verify(observation.getEffectivePeriod());
		}

		if (targetSiteCodeGenerators.isEmpty()) {
			Assert.assertTrue("No observation target site code", !observation.hasBodySite());
		} else {
			CDGenerator cdg = targetSiteCodeGenerators.get(targetSiteCodeGenerators.size() - 1);
			cdg.verify(observation.getBodySite());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		org.hl7.fhir.dstu3.model.Observation obs = BundleUtil.findOneResource(bundle,
				org.hl7.fhir.dstu3.model.Observation.class);
		verify(obs);
	}
}
