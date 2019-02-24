package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAVitalSignsSection implements ICDASection {
	private VitalSignsSection section;
	
	@SuppressWarnings("unused")
	private CDAVitalSignsSection() {};
	
	public CDAVitalSignsSection(VitalSignsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (VitalSignsOrganizer org : section.getVitalSignsOrganizers()) {
    		for(VitalSignObservation obs : org.getVitalSignObservations()) {
    	   		Bundle bundle = rt.tVitalSignObservation2Observation(obs);
        		FHIRUtil.mergeBundle(bundle, result);
    		}
    	}
    	return SectionResultSingular.getInstance(result, Observation.class);
	}
}
