package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistorySection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAFamilyHistorySection implements ICDASection {
	private FamilyHistorySection section;
	
	@SuppressWarnings("unused")
	private CDAFamilyHistorySection() {};
	
	public CDAFamilyHistorySection(FamilyHistorySection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<FamilyMemberHistory> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
        for (FamilyHistoryOrganizer org : section.getFamilyHistories()) {
            FamilyMemberHistory fmh = rt.tFamilyHistoryOrganizer2FamilyMemberHistory(org);
            result.addEntry().setResource(fmh);
        }
    	return SectionResultSingular.getInstance(result, FamilyMemberHistory.class);
	}
}
