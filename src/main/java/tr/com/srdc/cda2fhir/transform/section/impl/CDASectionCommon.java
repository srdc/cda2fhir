package tr.com.srdc.cda2fhir.transform.section.impl;

import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDASectionCommon {
	public static SectionResultSingular<Immunization> transformImmunizationActivityList(EList<ImmunizationActivity> actList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for(ImmunizationActivity act : actList) {
    		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Immunization.class);
	}

	public static SectionResultSingular<Observation> transformVitalSignsOrganizerList(EList<VitalSignsOrganizer> orgList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (VitalSignsOrganizer org : orgList) {
    		for(VitalSignObservation obs : org.getVitalSignObservations()) {
    	   		Bundle bundle = rt.tVitalSignObservation2Observation(obs);
        		FHIRUtil.mergeBundle(bundle, result);
    		}
    	}
    	return SectionResultSingular.getInstance(result, Observation.class);
	}
	
	public static SectionResultSingular<Encounter> transformEncounterActivitiesList(EList<EncounterActivities> encounterList, IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (EncounterActivities act : encounterList) {
    		Bundle bundle = rt.tEncounterActivity2Encounter(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Encounter.class);		
	}
}
