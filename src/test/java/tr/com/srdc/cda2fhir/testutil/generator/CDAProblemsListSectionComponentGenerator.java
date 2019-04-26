package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipHasComponent;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CDAProblemsListSectionComponentGenerator {

	private List<ProblemConcernAct> problemConcernActs;
	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.2.5.1";
	static final public String DEFAULT_TEMPLATE_ID_EXT = "2019-01-01";
	static final public String DEFAULT_CODE = "11450-4";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_DISPLAY_NAME = "PROBLEM LIST";
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

		if (problemConcernActs != null && !problemConcernActs.isEmpty()) {
			for (ProblemConcernAct act : problemConcernActs) {
				section.addAct(act);
			}
		}

		component.setSection(section);
		return component;
	}

	public void setProblemConcernActs(List<ProblemConcernAct> problemConcernActs) {
		this.problemConcernActs = problemConcernActs;
	}

}
