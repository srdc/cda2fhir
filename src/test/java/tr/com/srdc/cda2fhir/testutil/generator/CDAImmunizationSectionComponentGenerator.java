package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipHasComponent;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CDAImmunizationSectionComponentGenerator {

	private List<SubstanceAdministration> substanceAdministrations;
	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.2.2.1";
	static final public String DEFAULT_TEMPLATE_ID_EXT = "2019-01-01";
	static final public String DEFAULT_CODE = "11369-6";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_DISPLAY_NAME = "History of Immunization Narrative";
	static final public String DEFAULT_CODE_SYSTEM_NAME = "LOINC";
	static final public String DEFAULT_ID = "D0FA39F0-3099-11E9-BCAA-5102195700F0";

	public Component3 generate(CDAFactories factories) {

		Component3 component = factories.base.createComponent3();
		component.setTypeCode(ActRelationshipHasComponent.COMP);
		ImmunizationsSection section = factories.consol.createImmunizationsSection();
		CE code = factories.datatype.createCE(DEFAULT_CODE, DEFAULT_CODE_SYSTEM, DEFAULT_CODE_SYSTEM_NAME,
				DEFAULT_DISPLAY_NAME);
		II templateId = factories.datatype.createII(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT);
		section.setClassCode(ActClass.DOCSECT);
		section.setMoodCode(ActMood.EVN);
		section.setCode(code);
		section.getTemplateIds().add(templateId);
		section.setId(factories.datatype.createII(DEFAULT_ID));

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
