package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Procedure;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class LocalResourceTransformer extends ResourceTransformerImpl {
	private static class ProblemInfo {
		public ProblemConcernAct act;
		public List<ProblemObservation> observations = new ArrayList<>();

		public ProblemInfo(ProblemConcernAct act) {
			this.act = act;
		}
	};

	public static class ResultInfo {
		public ResultOrganizer organizer;
		public List<ResultObservation> observations = new ArrayList<>();

		public ResultInfo(ResultOrganizer organizer) {
			this.organizer = organizer;
		}
	};

	private static final long serialVersionUID = 1L;

	private CDAFactories factories;

	private List<AllergyProblemAct> allergyProblemActs = new ArrayList<>();
	private List<EncounterActivities> encounterActivities = new ArrayList<>();
	private List<ImmunizationActivity> immunizationActivities = new ArrayList<>();
	private List<MedicationActivity> medActivities = new ArrayList<>();
	private List<ProblemInfo> problemInfos = new ArrayList<>();
	private List<ProcedureActivityProcedure> procActivityProcs = new ArrayList<>();
	private List<ResultInfo> resultInfos = new ArrayList<>();
	private List<VitalSignObservation> observations = new ArrayList<>();

	public LocalResourceTransformer(CDAFactories factories) {
		this.factories = factories;
	}

	public void clearEntries() {
		allergyProblemActs.clear();
		encounterActivities.clear();
		immunizationActivities.clear();
		medActivities.clear();
		problemInfos.clear();
		procActivityProcs.clear();
		resultInfos.clear();
		observations.clear();
	}

	@Override
	public EntryResult tAllergyProblemAct2AllergyIntolerance(AllergyProblemAct cdaAllergyProbAct,
			IBundleInfo bundleInfo) {
		allergyProblemActs.add(cdaAllergyProbAct);
		return super.tAllergyProblemAct2AllergyIntolerance(cdaAllergyProbAct, bundleInfo);
	}

	@Override
	public EntryResult tEncounterActivity2Encounter(EncounterActivities encounterActivity, IBundleInfo bundleInfo) {
		encounterActivities.add(encounterActivity);
		return super.tEncounterActivity2Encounter(encounterActivity, bundleInfo);
	}

	@Override
	public EntryResult tImmunizationActivity2Immunization(ImmunizationActivity cdaImmunizationActivity,
			IBundleInfo bundleInfo) {
		immunizationActivities.add(cdaImmunizationActivity);
		return super.tImmunizationActivity2Immunization(cdaImmunizationActivity, bundleInfo);
	}

	@Override
	public EntryResult tMedicationActivity2MedicationStatement(MedicationActivity medActivity, IBundleInfo bundleInfo) {
		medActivities.add(medActivity);
		return super.tMedicationActivity2MedicationStatement(medActivity, bundleInfo);
	}

	@Override
	public EntryResult tProblemConcernAct2Condition(ProblemConcernAct cdaProblemConcernAct, IBundleInfo bundleInfo) {
		ProblemInfo info = new ProblemInfo(cdaProblemConcernAct);
		problemInfos.add(info);
		return super.tProblemConcernAct2Condition(cdaProblemConcernAct, bundleInfo);
	}

	@Override
	public EntryResult tProblemObservation2Condition(ProblemObservation cdaProbObs, IBundleInfo bundleInfo) {
		ProblemInfo info = problemInfos.get(problemInfos.size() - 1);
		info.observations.add(cdaProbObs);
		return super.tProblemObservation2Condition(cdaProbObs, bundleInfo);
	}

	@Override
	public EntryResult tProcedure2Procedure(Procedure cdaProcedure, IBundleInfo bundleInfo) {
		procActivityProcs.add((ProcedureActivityProcedure) cdaProcedure);
		return super.tProcedure2Procedure(cdaProcedure, bundleInfo);
	}

	@Override
	public EntryResult tResultObservation2Observation(ResultObservation cdaResultObservation, IBundleInfo bundleInfo) {
		ResultInfo info = resultInfos.get(resultInfos.size() - 1);
		info.observations.add(cdaResultObservation);
		return super.tResultObservation2Observation(cdaResultObservation, bundleInfo);
	}

	@Override
	public EntryResult tResultOrganizer2DiagnosticReport(ResultOrganizer cdaResultOrganizer, IBundleInfo bundleInfo) {
		ResultInfo info = new ResultInfo(cdaResultOrganizer);
		resultInfos.add(info);
		return super.tResultOrganizer2DiagnosticReport(cdaResultOrganizer, bundleInfo);
	}

	@Override
	public EntryResult tVitalSignObservation2Observation(VitalSignObservation vitalSignObservation,
			IBundleInfo bundleInfo) {
		observations.add(vitalSignObservation);
		return super.tVitalSignObservation2Observation(vitalSignObservation, bundleInfo);
	}

	public void reorderSection(AllergiesSection section) {
		section.getEntries().clear();
		allergyProblemActs.forEach(act -> {
			Entry entry = factories.base.createEntry();
			entry.setAct(act);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(EncountersSectionEntriesOptional section) {
		section.getEntries().clear();
		encounterActivities.forEach(ea -> {
			Entry entry = factories.base.createEntry();
			entry.setEncounter(ea);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(ImmunizationsSection section) {
		section.getEntries().clear();
		immunizationActivities.forEach(activity -> {
			Entry entry = factories.base.createEntry();
			entry.setSubstanceAdministration(activity);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(MedicationsSection section) {
		section.getEntries().clear();
		medActivities.forEach(ma -> {
			Entry entry = factories.base.createEntry();
			entry.setSubstanceAdministration(ma);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(ProblemSection section) {
		section.getEntries().clear();
		problemInfos.forEach(info -> {
			ProblemConcernAct act = info.act;

			Iterator<EntryRelationship> it = act.getEntryRelationships().iterator();
			while (it.hasNext()) {
				EntryRelationship er = it.next();
				if (er.getObservation() instanceof ProblemObservation) {
					it.remove();
				}
			}
			info.observations.forEach(po -> {
				EntryRelationship er = factories.base.createEntryRelationship();
				act.getEntryRelationships().add(er);
				er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
				er.setObservation(po);
			});

			Entry entry = factories.base.createEntry();
			entry.setAct(act);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(ProceduresSection section) {
		section.getEntries().clear();
		procActivityProcs.forEach(act -> {
			Entry entry = factories.base.createEntry();
			entry.setProcedure(act);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(ResultsSection section) {
		section.getEntries().clear();
		resultInfos.forEach(info -> {
			ResultOrganizer ro = info.organizer;

			ro.getComponents().clear();
			info.observations.forEach(observation -> {
				Component4 component = factories.base.createComponent4();
				component.setObservation(observation);
				ro.getComponents().add(component);
			});

			Entry entry = factories.base.createEntry();
			entry.setOrganizer(ro);
			section.getEntries().add(entry);
		});
	}

	public void reorderSection(VitalSignsSectionEntriesOptional section) {
		Map<VitalSignObservation, Integer> map = new HashMap<>();
		for (int index = 0; index < observations.size(); ++index) {
			VitalSignObservation observation = observations.get(index);
			map.put(observation, index);
		}

		List<VitalSignsOrganizer> organizers = new ArrayList<>(section.getVitalSignsOrganizers());
		organizers.sort((a, b) -> {
			VitalSignObservation obsa = a.getVitalSignObservations().get(0);
			VitalSignObservation obsb = b.getVitalSignObservations().get(0);

			int aval = map.get(obsa).intValue();
			int bval = map.get(obsb).intValue();

			return aval - bval;
		});

		final Map<VitalSignObservation, VitalSignsOrganizer> map2 = new HashMap<>();
		organizers.forEach(organizer -> {
			organizer.getVitalSignObservations().forEach(observation -> {
				map2.put(observation, organizer);
			});
		});

		section.getEntries().clear();
		organizers.forEach(organizer -> {
			organizer.getComponents().clear();
			Entry entry = factories.base.createEntry();
			entry.setOrganizer(organizer);
			section.getEntries().add(entry);
		});

		observations.forEach(observation -> {
			Component4 component = factories.base.createComponent4();
			component.setObservation(observation);
			VitalSignsOrganizer organizer = map2.get(observation);
			organizer.getComponents().add(component);
		});
	}
}
