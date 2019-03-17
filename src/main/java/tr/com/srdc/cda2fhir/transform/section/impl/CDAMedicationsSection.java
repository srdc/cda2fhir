package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;

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
		SectionResultSingular<MedicationStatement> result = SectionResultSingular.getInstance(MedicationStatement.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		
		for(Entry entry : section.getEntries()) {
			if(entry.hasContent() && entry.getSupply() != null) {
				if(entry.getSupply().getClassCode().toString().equalsIgnoreCase("SPLY") 
						&& entry.getSupply().getMoodCode().toString().equalsIgnoreCase("INT")) {
					IEntryResult medRequestResult = rt.medicationSupplyOrder2MedicationRequest((MedicationSupplyOrder)entry.getSupply());
				}
			}
		}
		
		for (MedicationActivity act : section.getMedicationActivities()) {
    		IEntryResult er = rt.tMedicationActivity2MedicationStatement(act, localBundleInfo);
    		result.updateFrom(er);
    		localBundleInfo.updateFrom(er);
    	}
		
    	return result;
	}
	
	
}
