package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDASocialHistorySection implements ICDASection {
	private SocialHistorySection section;
	
	@SuppressWarnings("unused")
	private CDASocialHistorySection() {};
	
	public CDASocialHistorySection(SocialHistorySection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
        /**
         * The generic observation transformer should be able to transform all the possible entries:
         *    Caregiver Characteristics
         *    Characteristics of Home Environment
         *    Cultural and Religious Observation
         *    Pregnancy Observation
         *    Smoking Status - Meaningful Use (V2)
         *    Social History Observation (V3)
         *    Tobacco Use (V2)
         */
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (org.openhealthtools.mdht.uml.cda.Observation obs : section.getObservations()) {
    		Bundle bundle = rt.tObservation2Observation(obs);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Observation.class);
	}
}
