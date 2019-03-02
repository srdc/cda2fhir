package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

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
		SectionResultSingular<AllergyIntolerance> result = SectionResultSingular.getInstance(AllergyIntolerance.class);
    	for (AllergyProblemAct act : section.getAllergyProblemActs()) {
    		IEntryResult er = rt.tAllergyProblemAct2AllergyIntolerance(act, bundleInfo);
    		result.updateFrom(er);
    	}
    	return result;
	}
}
