package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAAllergiesSection implements ICDASection {
	private AllergiesSection section;
	
	@SuppressWarnings("unused")
	private CDAAllergiesSection() {};
	
	public CDAAllergiesSection(AllergiesSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<AllergyIntolerance> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (AllergyProblemAct act : section.getAllergyProblemActs()) {
    		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, AllergyIntolerance.class);
	}
}
