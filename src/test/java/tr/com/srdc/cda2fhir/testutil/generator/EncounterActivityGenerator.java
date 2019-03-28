package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class EncounterActivityGenerator {
	private static final Map<String, Object> ENCOUNTER_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/EncounterStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private String statusCode;
	private String statusNullFlavor;

	private CDGenerator codeGenerator;
	private CEGenerator priorityCodeGenerator;

	private IVL_TSPeriodGenerator effectiveTimeGenerator;

	public EncounterActivities generate(CDAFactories factories) {
		EncounterActivities ec = factories.consol.createEncounterActivities();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ec.getIds().add(ii);
		});

		if (statusCode != null || statusNullFlavor != null) {
			CS cs = factories.datatype.createCS();
			if (statusCode != null) {
				cs.setCode(statusCode);
			}
			if (statusNullFlavor != null) {
				NullFlavor nf = NullFlavor.get(statusNullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				cs.setNullFlavor(nf);
			}
			ec.setStatusCode(cs);
		}

		if (codeGenerator != null) {
			CD cd = codeGenerator.generate(factories);
			ec.setCode(cd);
		}

		if (priorityCodeGenerator != null) {
			CE ce = priorityCodeGenerator.generate(factories);
			ec.setPriorityCode(ce);
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			ec.setEffectiveTime(ivlTs);
		}

		return ec;
	}

	public static EncounterActivityGenerator getDefaultInstance() {
		EncounterActivityGenerator ecg = new EncounterActivityGenerator();

		ecg.idGenerators.add(IDGenerator.getNextInstance());
		ecg.statusCode = "active";
		ecg.codeGenerator = CDGenerator.getNextInstance();
		ecg.priorityCodeGenerator = CEGenerator.getNextInstance();
		ecg.effectiveTimeGenerator = IVL_TSPeriodGenerator.getDefaultInstance();

		return ecg;
	}

	public void verify(Encounter encounter) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(encounter.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No encounter identifier", !encounter.hasIdentifier());
		}

		if (statusCode == null || statusNullFlavor != null) {
			Assert.assertTrue("Missing encounter status", !encounter.hasStatus());
		} else {
			String expected = (String) ENCOUNTER_STATUS.get(statusCode);
			if (expected == null) {
				Assert.assertTrue("Missing encounter status", !encounter.hasStatus());
			} else {
				Assert.assertEquals("Ecnounter status", expected, encounter.getStatus().toCode());
			}
		}

		if (codeGenerator == null) {
			Assert.assertTrue("Missing encounter type", !encounter.hasType());
		} else {
			Assert.assertTrue("One encounter type", encounter.getType().size() == 1);
			codeGenerator.verify(encounter.getType().get(0));
		}

		if (priorityCodeGenerator == null) {
			Assert.assertTrue("Missing encounter priority", !encounter.hasPriority());
		} else {
			priorityCodeGenerator.verify(encounter.getPriority());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("Missing encounter period", !encounter.hasPeriod());
		} else {
			effectiveTimeGenerator.verify(encounter.getPeriod());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Encounter encounter = BundleUtil.findOneResource(bundle, Encounter.class);

		verify(encounter);
	}
}
