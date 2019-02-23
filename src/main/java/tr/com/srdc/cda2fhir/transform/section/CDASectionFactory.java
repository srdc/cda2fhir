package tr.com.srdc.cda2fhir.transform.section;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.Section;

public class CDASectionFactory {
	private List<CDASectionTypeEnum> allowedSections;
	
	public CDASectionFactory() {
		allowedSections.add(CDASectionTypeEnum.ALLERGIES_SECTION);
	}

	public ICDASection getInstance(Section section) {
		for (CDASectionTypeEnum allowedSection: allowedSections) {
			if (allowedSection.supports(section)) {
				return allowedSection.toCDASection(section);
			}
		}
		return null;
	}
}
