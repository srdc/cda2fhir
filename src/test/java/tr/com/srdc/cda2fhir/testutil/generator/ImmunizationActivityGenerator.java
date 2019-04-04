package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationReactionComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationRefusalReason;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ImmunizationActivityGenerator {
	private static final Map<String, Object> IMMUNIZATION_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ImmunizationStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private Boolean negationInd;

	private List<EffectiveTimeGenerator> effectiveTimeGenerators = new ArrayList<>();

	private ImmunizationMedicationInformationGenerator medInfoGenerator;

	private List<PerformerGenerator> performerGenerators = new ArrayList<>();

	private List<CDGenerator> approachSiteCodeGenerators = new ArrayList<>();

	private CEGenerator routeCodeGenerator;

	private IVL_PQSimpleQuantityGenerator doseQuantityGenerator;

	private StatusCodeGenerator statusCodeGenerator;

	private CDGenerator refusalReasonGenerator;

	private CDGenerator indicationGenerator;

	private ObservationGenerator reactionObservationGenerator;

	public void convertToRefused() {
		negationInd = true;
		indicationGenerator = null;
		if (refusalReasonGenerator == null) {
			refusalReasonGenerator = CDGenerator.getNextInstance();
		}
	}

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

		if (!approachSiteCodeGenerators.isEmpty()) {
			approachSiteCodeGenerators.forEach(ascg -> {
				CD cd = ascg.generate(factories);
				ia.getApproachSiteCodes().add(cd);
			});
		}

		if (routeCodeGenerator != null) {
			CE ce = routeCodeGenerator.generate(factories);
			ia.setRouteCode(ce);
		}

		if (doseQuantityGenerator != null) {
			IVL_PQ ivlPq = doseQuantityGenerator.generate(factories);
			ia.setDoseQuantity(ivlPq);
		}

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			ia.setStatusCode(cs);
		}

		if (refusalReasonGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			ia.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.RSON);
			CD refusalReason = refusalReasonGenerator.generate(factories);
			ImmunizationRefusalReason irr = factories.consol.createImmunizationRefusalReason();
			irr.setCode(refusalReason);
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.53");
			irr.getTemplateIds().add(templateId);
			er.setObservation(irr);
		}

		if (indicationGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			ia.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.RSON);
			CD indicationCode = indicationGenerator.generate(factories);
			Indication indication = factories.consol.createIndication();
			indication.getValues().add(indicationCode);
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.19");
			indication.getTemplateIds().add(templateId);
			er.setObservation(indication);
		}

		if (reactionObservationGenerator != null) {
			EntryRelationship er = factories.base.createEntryRelationship();
			ia.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.CAUS);
			Observation obs = reactionObservationGenerator.generate(factories);
			II templateId = factories.datatype.createII("2.16.840.1.113883.10.20.22.4.9");
			obs.getTemplateIds().add(templateId);
			er.setObservation(obs);
		}

		return ia;
	}

	public static ImmunizationActivityGenerator getDefaultInstance() {
		ImmunizationActivityGenerator ma = new ImmunizationActivityGenerator();

		ma.idGenerators.add(IDGenerator.getNextInstance());
		ma.negationInd = false;
		ma.effectiveTimeGenerators.add(EffectiveTimeGenerator.getValueOnlyInstance("20190204"));
		ma.medInfoGenerator = ImmunizationMedicationInformationGenerator.getDefaultInstance();
		ma.performerGenerators.add(PerformerGenerator.getDefaultInstance());
		ma.approachSiteCodeGenerators.add(CDGenerator.getNextInstance());
		ma.routeCodeGenerator = CEGenerator.getNextInstance();
		ma.doseQuantityGenerator = IVL_PQSimpleQuantityGenerator.getDefaultInstance();
		ma.statusCodeGenerator = new StatusCodeGenerator(IMMUNIZATION_STATUS);
		ma.statusCodeGenerator.set("active");
		ma.indicationGenerator = CDGenerator.getNextInstance();
		ma.reactionObservationGenerator = ObservationGenerator.getDefaultInstance();

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

		if (approachSiteCodeGenerators.isEmpty()) {
			Assert.assertTrue("No immunization site", !immunization.hasSite());
		} else {
			CDGenerator cdg = approachSiteCodeGenerators.get(approachSiteCodeGenerators.size() - 1);
			cdg.verify(immunization.getSite());
		}

		if (routeCodeGenerator == null) {
			Assert.assertTrue("No immunization route", !immunization.hasRoute());
		} else {
			routeCodeGenerator.verify(immunization.getRoute());
		}

		if (doseQuantityGenerator == null) {
			Assert.assertTrue("No immunization dose quantity", !immunization.hasDoseQuantity());
		} else {
			doseQuantityGenerator.verify(immunization.getDoseQuantity());
		}

		if (refusalReasonGenerator == null) {
			boolean hasRefusal = immunization.hasExplanation() && immunization.getExplanation().hasReasonNotGiven();
			Assert.assertTrue("No immunization refusal reason", !hasRefusal);
		} else {
			CodeableConcept cc = immunization.getExplanation().getReasonNotGiven().get(0);
			refusalReasonGenerator.verify(cc);
		}

		if (indicationGenerator == null) {
			boolean hasReason = immunization.hasExplanation() && immunization.getExplanation().hasReason();
			Assert.assertTrue("No immunization reason", !hasReason);
		} else {
			boolean hasReason = immunization.hasExplanation() && immunization.getExplanation().hasReason();
			Assert.assertTrue("Has immunization reason", hasReason);
			CodeableConcept cc = immunization.getExplanation().getReason().get(0);
			indicationGenerator.verify(cc);
		}

		if (statusCodeGenerator == null) {
			Assert.assertTrue("No immunization status", !immunization.hasStatus());
		} else {
			statusCodeGenerator.verify(immunization.getStatus().toCode());
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

		if (reactionObservationGenerator == null) {
			Assert.assertTrue("No reaction observation", !immunization.hasReaction());
		} else {
			List<ImmunizationReactionComponent> reactions = immunization.getReaction();
			Assert.assertEquals("Reaction count", 1, reactions.size());
			ImmunizationReactionComponent reaction = reactions.get(0);
			String observationId = reaction.getDetail().getReference();
			BundleUtil util = new BundleUtil(bundle);
			org.hl7.fhir.dstu3.model.Observation observation = util.getResourceFromReference(observationId,
					org.hl7.fhir.dstu3.model.Observation.class);
			reactionObservationGenerator.verify(observation);
		}

	}
}
