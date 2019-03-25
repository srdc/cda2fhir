package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Device;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.Procedure;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.MedicalEquipmentSection;
import org.openhealthtools.mdht.uml.cda.consol.NonMedicinalSupplyActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAMedicalEquipmentSection implements ICDASection {
	private MedicalEquipmentSection section;

	@SuppressWarnings("unused")
	private CDAMedicalEquipmentSection() {
	};

	public CDAMedicalEquipmentSection(MedicalEquipmentSection section) {
		this.section = section;
	}

	@Override
	public SectionResultDynamic transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultDynamic result = new SectionResultDynamic();
		// Case 1: Entry is a Non-Medicinal Supply Activity (V2)
		for (NonMedicinalSupplyActivity act : section.getNonMedicinalSupplyActivities()) {
			Device device = rt.tSupply2Device(act);
			result.add(device);
		}
		// Case 2: Entry is a Medical Equipment Organizer, which is indeed a collection
		// of Non-Medicinal Supply Activity (V2)
		for (Organizer org : section.getOrganizers()) {
			for (Supply supply : org.getSupplies()) {
				if (supply instanceof NonMedicinalSupplyActivity) {
					Device device = rt.tSupply2Device(supply);
					result.add(device);
				}
			}
		}
		// Case 3: Entry is a Procedure Activity Procedure (V2)
		for (Procedure procedure : section.getProcedures()) {
			if (procedure instanceof ProcedureActivityProcedure) {
				IEntryResult entryResult = rt.tProcedure2Procedure(procedure, bundleInfo);
				result.updateFrom(entryResult, org.hl7.fhir.dstu3.model.Procedure.class);
			}
		}
		return result;
	}
}
