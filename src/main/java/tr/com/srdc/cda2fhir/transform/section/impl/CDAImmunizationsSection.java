package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAImmunizationsSection implements ICDASection {
	private ImmunizationsSection section;
	
	@SuppressWarnings("unused")
	private CDAImmunizationsSection() {};
	
	public CDAImmunizationsSection(ImmunizationsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Immunization> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for(ImmunizationActivity act : section.getImmunizationActivities()) {
    		Bundle bundle = rt.tImmunizationActivity2Immunization(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Immunization.class);
	}
}
