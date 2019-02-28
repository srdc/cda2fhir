package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAEncountersSectionEntriesOptional implements ICDASection {
	private EncountersSectionEntriesOptional section;
	
	@SuppressWarnings("unused")
	private CDAEncountersSectionEntriesOptional() {};
	
	public CDAEncountersSectionEntriesOptional(EncountersSectionEntriesOptional section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Encounter> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (EncounterActivities act : section.getEncounterActivitiess()) {
    		Bundle bundle = rt.tEncounterActivity2Encounter(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Encounter.class);
	}
}
