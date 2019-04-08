package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.cda.consol.SeverityObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class AllergyReactionObservationGenerator {
	private static final Map<String, Object> ALLERGY_INTOLERANCE_SEVERITY = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/AllergyIntoleranceSeverity.json");

	private List<CDGenerator> valueGenerators = new ArrayList<>();

	private EffectiveTimeGenerator effectiveTimeGenerator;

	private CECodeGenerator severityGenerator;

	public ReactionObservation generate(CDAFactories factories) {
		ReactionObservation obs = factories.consol.createReactionObservation();

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			obs.setEffectiveTime(ivlTs);
		}

		valueGenerators.forEach(vg -> {
			ANY any = vg.generate(factories);
			obs.getValues().add(any);
		});

		if (severityGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			obs.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			er.setInversionInd(true);
			SeverityObservation so = factories.consol.createSeverityObservation();
			CE severity = severityGenerator.generate(factories);
			so.getValues().add(severity);
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.8");
			so.getTemplateIds().add(templateId);
			er.setObservation(so);
		}

		return obs;
	}

	public static AllergyReactionObservationGenerator getDefaultInstance() {
		AllergyReactionObservationGenerator arog = new AllergyReactionObservationGenerator();

		arog.effectiveTimeGenerator = new EffectiveTimeGenerator("20150712");

		arog.severityGenerator = new CECodeGenerator(ALLERGY_INTOLERANCE_SEVERITY);
		arog.severityGenerator.set("24484000");

		arog.valueGenerators.add(CDGenerator.getNextInstance());

		return arog;
	}

	public void verify(AllergyIntoleranceReactionComponent reaction) {
		if (valueGenerators.isEmpty()) {
			Assert.assertTrue("No manifestation", !reaction.hasManifestation());
		} else {
			CDGenerator.verifyList(reaction.getManifestation(), valueGenerators);
		}

		if (severityGenerator == null) {
			Assert.assertTrue("No severity", !reaction.hasSeverity());
		} else {
			severityGenerator.verify(reaction.getSeverity().toCode());
		}

		String value = effectiveTimeGenerator.getLowOrValue();
		if (value == null) {
			Assert.assertTrue("No onset", !reaction.hasOnset());
		} else {
			String actual = FHIRUtil.toCDADatetime(reaction.getOnsetElement().asStringValue());
			Assert.assertEquals("Onset", value, actual);
		}
	}

	public static void verifyList(List<AllergyIntoleranceReactionComponent> actual,
			List<AllergyReactionObservationGenerator> expected) {
		Assert.assertEquals("Reaction count", expected.size(), actual.size());
		for (int index = 0; index < actual.size(); ++index) {
			expected.get(index).verify(actual.get(index));
		}
	}
}
