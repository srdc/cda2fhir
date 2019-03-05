package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAResultsSection implements ICDASection {
	private ResultsSection section;
	
	@SuppressWarnings("unused")
	private CDAResultsSection() {};
	
	public CDAResultsSection(ResultsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<DiagnosticReport> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (ResultOrganizer org : section.getResultOrganizers()) {
    		Bundle bundle = rt.tResultOrganizer2DiagnosticReport(org);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, DiagnosticReport.class);
	}
}
