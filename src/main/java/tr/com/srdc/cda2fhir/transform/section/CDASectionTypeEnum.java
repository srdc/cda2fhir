package tr.com.srdc.cda2fhir.transform.section;

import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;

import tr.com.srdc.cda2fhir.transform.section.impl.CDAAllergiesSection;

public enum CDASectionTypeEnum {
	ALLERGIES_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof AllergiesSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAAllergiesSection((AllergiesSection) section);
		}
	};
	
	public abstract boolean supports(Section section);
	
	public abstract ICDASection toCDASection(Section section);
}
