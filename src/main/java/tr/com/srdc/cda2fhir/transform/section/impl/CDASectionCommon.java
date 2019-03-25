package tr.com.srdc.cda2fhir.transform.section.impl;

import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDASectionCommon {
	public static SectionResultSingular<Immunization> transformImmunizationActivityList(
			EList<ImmunizationActivity> actList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Immunization> result = SectionResultSingular.getInstance(Immunization.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (ImmunizationActivity act : actList) {
			IEntryResult er = rt.tImmunizationActivity2Immunization(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;
	}

	public static SectionResultSingular<Observation> transformVitalSignsOrganizerList(
			EList<VitalSignsOrganizer> orgList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Observation> result = SectionResultSingular.getInstance(Observation.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (VitalSignsOrganizer org : orgList) {
			for (VitalSignObservation obs : org.getVitalSignObservations()) {
				IEntryResult er = rt.tVitalSignObservation2Observation(obs, localBundleInfo);
				result.updateFrom(er);
				localBundleInfo.updateFrom(er);
			}
		}
		return result;
	}

	public static SectionResultSingular<Encounter> transformEncounterActivitiesList(
			EList<EncounterActivities> encounterList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Encounter> result = SectionResultSingular.getInstance(Encounter.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (EncounterActivities act : encounterList) {
			IEntryResult er = rt.tEncounterActivity2Encounter(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;
	}
}
