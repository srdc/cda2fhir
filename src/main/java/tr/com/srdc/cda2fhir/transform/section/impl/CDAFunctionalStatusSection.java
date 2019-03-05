package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAFunctionalStatusSection implements ICDASection {
	private FunctionalStatusSection section;
	
	@SuppressWarnings("unused")
	private CDAFunctionalStatusSection() {};
	
	public CDAFunctionalStatusSection(FunctionalStatusSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (FunctionalStatusResultOrganizer org : section.getFunctionalStatusResultOrganizers()) {
    		for(org.openhealthtools.mdht.uml.cda.Observation obs : org.getObservations()) {
    	   		Bundle bundle = rt.tFunctionalStatus2Observation(obs);
        		FHIRUtil.mergeBundle(bundle, result);
    		}
    	}
    	return SectionResultSingular.getInstance(result, Observation.class);
	}
}
