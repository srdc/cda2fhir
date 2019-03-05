package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Immunization;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAImmunizationsSection implements ICDASection {
	private ImmunizationsSection section;
	
	@SuppressWarnings("unused")
	private CDAImmunizationsSection() {};
	
	public CDAImmunizationsSection(ImmunizationsSection section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Immunization> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformImmunizationActivityList(section.getImmunizationActivities(), bundleInfo);
	}
}
