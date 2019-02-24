package tr.com.srdc.cda2fhir.transform.section;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.Section;

public class CDASectionFactory {
	private List<CDASectionTypeEnum> sections = new ArrayList<CDASectionTypeEnum>();
	
	public CDASectionFactory() {
		sections.add(CDASectionTypeEnum.ALLERGIES_SECTION);
		sections.add(CDASectionTypeEnum.IMMUNIZATIONS_SECTION);
		sections.add(CDASectionTypeEnum.MEDICATIONS_SECTION);
		sections.add(CDASectionTypeEnum.PROBLEM_SECTION);
		sections.add(CDASectionTypeEnum.PROCEDURES_SECTION);
	}

	public void addSection(CDASectionTypeEnum sectionEnum) {
		sections.add(sectionEnum);
	}
	
	public ICDASection getInstance(Section section) {
		for (CDASectionTypeEnum Section: sections) {
			if (Section.supports(section)) {
				return Section.toCDASection(section);
			}
		}
		return null;
	}
}
