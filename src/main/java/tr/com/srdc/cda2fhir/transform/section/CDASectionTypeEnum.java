package tr.com.srdc.cda2fhir.transform.section;

import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.PayersSection;
import org.openhealthtools.mdht.uml.cda.consol.PlanOfCareSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.consol.AdvanceDirectivesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSection;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicalEquipmentSection;

import tr.com.srdc.cda2fhir.transform.section.impl.CDAAllergiesSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAEncountersSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAFamilyHistorySection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAFunctionalStatusSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAImmunizationsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAMedicalEquipmentSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAMedicationsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAProblemsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAProceduresSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAResultsSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDASocialHistorySection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAUnplementedSection;
import tr.com.srdc.cda2fhir.transform.section.impl.CDAVitalSignsSection;

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
	},
	ENCOUNTERS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof EncountersSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAEncountersSection((EncountersSection) section);
		}		
	},
	VITAL_SIGNS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof VitalSignsSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAVitalSignsSection((VitalSignsSection) section);
		}		
	},
	SOCIAL_HISTORY_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof SocialHistorySection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDASocialHistorySection((SocialHistorySection) section);
		}		
	},
	RESULTS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof ResultsSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAResultsSection((ResultsSection) section);
		}		
	},
	FUNCTIONAL_STATUS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof FunctionalStatusSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAFunctionalStatusSection((FunctionalStatusSection) section);
		}		
	},
	FAMILY_HISTORY_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof FamilyHistorySection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAFamilyHistorySection((FamilyHistorySection) section);
		}		
	},
	MEDICAL_EQUIPMENT_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof MedicalEquipmentSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAMedicalEquipmentSection((MedicalEquipmentSection) section);
		}		
	},
	ADVANCED_DIRECTIVES_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof AdvanceDirectivesSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAUnplementedSection();
		}		
	},
	PAYERS_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof PayersSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAUnplementedSection();
		}		
	},
	PLAN_OF_CARE_SECTION {
		@Override
		public boolean supports(Section section) {
			return section instanceof PlanOfCareSection;
		}
		
		@Override
		public ICDASection toCDASection(Section section) {
			return new CDAUnplementedSection();
		}		
	};
	
	public abstract boolean supports(Section section);
	
	public abstract ICDASection toCDASection(Section section);
}
