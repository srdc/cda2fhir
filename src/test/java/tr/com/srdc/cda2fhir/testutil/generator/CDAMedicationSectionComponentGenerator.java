package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipHasComponent;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CDAMedicationSectionComponentGenerator {

	private List<SubstanceAdministration> substanceAdministrations;
	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.2.1";
	static final public String DEFAULT_CODE = "10160-0";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_DISPLAY_NAME = "History of Medication Use";
	static final public String DEFAULT_CODE_SYSTEM_NAME = "LOINC";

	public Component3 generate(CDAFactories factories) {

		Component3 component = factories.base.createComponent3();
		component.setTypeCode(ActRelationshipHasComponent.COMP);
		MedicationsSection section = factories.consol.createMedicationsSection();
		CE code = factories.datatype.createCE(DEFAULT_CODE, DEFAULT_CODE_SYSTEM, DEFAULT_CODE_SYSTEM_NAME,
				DEFAULT_DISPLAY_NAME);
		II templateId = factories.datatype.createII(DEFAULT_TEMPLATE_ID);
		section.setClassCode(ActClass.DOCSECT);
		section.setMoodCode(ActMood.EVN);
		section.setCode(code);
		section.getTemplateIds().add(templateId);

		if (substanceAdministrations != null && !substanceAdministrations.isEmpty()) {
			for (SubstanceAdministration sa : substanceAdministrations) {
				section.addSubstanceAdministration(sa);
			}
		}

		component.setSection(section);
		return component;
	}

	public void setSubstanceAdministrations(List<SubstanceAdministration> substanceAdministrations) {
		this.substanceAdministrations = substanceAdministrations;
	}

}
