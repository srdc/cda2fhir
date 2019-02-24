package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAVitalSignsSectionEntriesOptional implements ICDASection {
	private VitalSignsSectionEntriesOptional section;
	
	@SuppressWarnings("unused")
	private CDAVitalSignsSectionEntriesOptional() {};
	
	public CDAVitalSignsSectionEntriesOptional(VitalSignsSectionEntriesOptional section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformVitalSignsOrganizerList(section.getVitalSignsOrganizers(), bundleInfo);
	}
}
