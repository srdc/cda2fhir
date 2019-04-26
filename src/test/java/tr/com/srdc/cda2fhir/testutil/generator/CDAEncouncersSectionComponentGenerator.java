package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipHasComponent;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CDAEncouncersSectionComponentGenerator {
	private List<EncounterActivities> encounters;
	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.2.22.1";
	static final public String DEFAULT_TEMPLATE_ID_EXT = "2019-01-01";
	static final public String DEFAULT_CODE = "46240-8";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_DISPLAY_NAME = "History of encounters";
	static final public String DEFAULT_CODE_SYSTEM_NAME = "LOINC";

	public Component3 generate(CDAFactories factories) {

		Component3 component = factories.base.createComponent3();
		component.setTypeCode(ActRelationshipHasComponent.COMP);
		ProblemSection section = factories.consol.createProblemSection();
		CE code = factories.datatype.createCE(DEFAULT_CODE, DEFAULT_CODE_SYSTEM, DEFAULT_CODE_SYSTEM_NAME,
				DEFAULT_DISPLAY_NAME);
		II templateId = factories.datatype.createII(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT);
		section.setClassCode(ActClass.DOCSECT);
		section.setMoodCode(ActMood.EVN);
		section.setCode(code);
		section.getTemplateIds().add(templateId);

		if (encounters != null && !encounters.isEmpty()) {
			for (EncounterActivities encounter : encounters) {
				section.addEncounter(encounter);
			}
		}

		component.setSection(section);
		return component;
	}

	public void setEncounterActivities(List<EncounterActivities> encounters) {
		this.encounters = encounters;
	}

}
