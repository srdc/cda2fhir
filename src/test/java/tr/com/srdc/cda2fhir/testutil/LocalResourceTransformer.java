package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class LocalResourceTransformer extends ResourceTransformerImpl {
	private static final long serialVersionUID = 1L;

	private CDAFactories factories;

	private List<AllergyProblemAct> allergyProblemActs = new ArrayList<>();
	private List<EncounterActivities> encounterActivities = new ArrayList<>();
	private List<ImmunizationActivity> immunizationActivities = new ArrayList<>();

	public LocalResourceTransformer(CDAFactories factories) {
		this.factories = factories;
	}

	public void clearEntries() {
		allergyProblemActs.clear();
		encounterActivities.clear();
		immunizationActivities.clear();
	}

	public List<EncounterActivities> getEncounterActivities() {
		return Collections.unmodifiableList(encounterActivities);
	}

	public List<AllergyProblemAct> getAllergyProblemActs() {
		return Collections.unmodifiableList(allergyProblemActs);
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

	public void reorderSection(ImmunizationsSection section, List<Object> activities) {
		section.getEntries().clear();
		immunizationActivities.forEach(activity -> {
			Entry entry = factories.base.createEntry();
			entry.setSubstanceAdministration(activity);
			section.getEntries().add(entry);
		});
	}
}
