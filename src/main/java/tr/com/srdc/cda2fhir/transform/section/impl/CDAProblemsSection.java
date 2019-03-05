package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAProblemsSection implements ICDASection {
	private ProblemSection section;
	
	@SuppressWarnings("unused")
	private CDAProblemsSection() {};
	
	public CDAProblemsSection(ProblemSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Condition> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (ProblemConcernAct act : section.getConsolProblemConcerns()) {
    		Bundle bundle = rt.tProblemConcernAct2Condition(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, Condition.class);
	}
}
