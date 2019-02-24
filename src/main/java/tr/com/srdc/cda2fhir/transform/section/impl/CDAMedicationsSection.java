package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class CDAMedicationsSection implements ICDASection {
	private MedicationsSection section;
	
	@SuppressWarnings("unused")
	private CDAMedicationsSection() {};
	
	public CDAMedicationsSection(MedicationsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<MedicationStatement> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		Bundle result = new Bundle();
    	for (MedicationActivity act : section.getMedicationActivities()) {
    		Bundle bundle = rt.tMedicationActivity2MedicationStatement(act);
    		FHIRUtil.mergeBundle(bundle, result);
    	}
    	return SectionResultSingular.getInstance(result, MedicationStatement.class);
	}
}
