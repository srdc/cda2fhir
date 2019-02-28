package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Encounter;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAEncountersSectionEntriesOptional implements ICDASection {
	private EncountersSectionEntriesOptional section;
	
	@SuppressWarnings("unused")
	private CDAEncountersSectionEntriesOptional() {};
	
	public CDAEncountersSectionEntriesOptional(EncountersSectionEntriesOptional section) {
		this.section = section;
	}
	
	@Override
	public SectionResultSingular<Encounter> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformEncounterActivitiesList(section.getEncounterActivitiess(), bundleInfo);
	}
}
