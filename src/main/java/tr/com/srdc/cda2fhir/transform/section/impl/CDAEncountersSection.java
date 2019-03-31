package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Encounter;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSection;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAEncountersSection implements ICDASection {
	private EncountersSection section;

	@SuppressWarnings("unused")
	private CDAEncountersSection() {
	};

	public CDAEncountersSection(EncountersSection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Encounter> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformEncounterActivitiesList(section.getEncounterActivitiess(), bundleInfo);
	}
}
