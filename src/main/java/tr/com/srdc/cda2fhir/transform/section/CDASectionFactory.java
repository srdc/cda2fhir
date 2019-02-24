package tr.com.srdc.cda2fhir.transform.section;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.Section;

public class CDASectionFactory {
	private List<CDASectionTypeEnum> allowedSections = new ArrayList<CDASectionTypeEnum>();
	
	public CDASectionFactory() {
		allowedSections.add(CDASectionTypeEnum.ALLERGIES_SECTION);
		allowedSections.add(CDASectionTypeEnum.IMMUNIZATIONS_SECTION);
		allowedSections.add(CDASectionTypeEnum.MEDICATIONS_SECTION);
		allowedSections.add(CDASectionTypeEnum.PROBLEM_SECTION);
		allowedSections.add(CDASectionTypeEnum.PROCEDURES_SECTION);
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
