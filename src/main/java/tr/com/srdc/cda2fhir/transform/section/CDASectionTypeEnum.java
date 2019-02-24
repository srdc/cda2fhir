package tr.com.srdc.cda2fhir.transform.section;

import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;

import tr.com.srdc.cda2fhir.transform.section.impl.CDAAllergiesSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAImmunizationsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAMedicationsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAProblemsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAProceduresSection;

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
	},
	IMMUNIZATIONS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof ImmunizationsSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAImmunizationsSection((ImmunizationsSection) section);
		}		
	},
	MEDICATIONS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof MedicationsSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAMedicationsSection((MedicationsSection) section);
		}		
	},
	PROBLEM_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof ProblemSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAProblemsSection((ProblemSection) section);
		}		
	},
	PROCEDURES_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof ProceduresSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAProceduresSection((ProceduresSection) section);
		}		
	};
	
	public abstract boolean supports(Section section);
	
	public abstract ICDASection toCDASection(Section section);
}
