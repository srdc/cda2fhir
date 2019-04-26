package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class LocalResourceTransformer extends ResourceTransformerImpl {
	private static final long serialVersionUID = 1L;

	private List<AllergyProblemAct> allergyProblemActs = new ArrayList<>();
	private List<EncounterActivities> encounterActivities = new ArrayList<>();

	public void clearEntries() {
		allergyProblemActs.clear();
		encounterActivities.clear();
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
}
