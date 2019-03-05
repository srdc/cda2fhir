package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAVitalSignsSection implements ICDASection {
	private VitalSignsSection section;
	
	@SuppressWarnings("unused")
	private CDAVitalSignsSection() {};
	
	public CDAVitalSignsSection(VitalSignsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformVitalSignsOrganizerList(section.getVitalSignsOrganizers(), bundleInfo);
	}
}
