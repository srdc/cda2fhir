package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component2;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.DocumentationOf;
import org.openhealthtools.mdht.uml.cda.Performer1;
import org.openhealthtools.mdht.uml.cda.ServiceEvent;
import org.openhealthtools.mdht.uml.cda.StructuredBody;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ClinicalDocumentMetadataGenerator {

	ContinuityOfCareDocument doc;
	CDAFactories factories;

	private static AssignedEntityGenerator assignedEntityGenerator = new AssignedEntityGenerator();

	static final public String DEFAULT_REALM_CODE = "US";
	static final public String DEFAULT_ID_ROOT = "1.2.345.678901.7.89.10.1.2.3.123456.12345678";
	static final public String DEFAULT_ASSN_AUTH = "Assigning Authority";
	static final public String DEFAULT_CODE_CODE = "34133-9";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_CODE_SYSTEM_NAME = "LOINC";
	static final public String DEFAULT_CODE_DISPLAY = "Summarization of Episode Note";
	static final public String DEFAULT_TITLE = "Patient Health Summary";
	static final public String DEFAULT_EFFCT_TIME = "20190119161413-0500";
	static final public String DEFAULT_EVENT_TIME_LOW = "19700919";
	static final public String DEFAULT_EVENT_TIME_HIGH = "20190214";
	static final public String DEFAULT_CONF_CODE_CODE = "N";
	static final public String DEFAULT_CONF_CODE_SYSTEM = "1.23.456.7.890123.4.56";
	static final public String DEFAULT_CONF_CODE_DSP = "Normal";
	static final public String DEFAULT_LANG_CODE = "en-US";
	static final public String DEFAULT_SET_ID_EXT = "12345678-9012-3456-7890-123456789012";
	static final public String DEFAULT_SET_ID_ROOT = "1.2.345.678901.2.34.56.7.8.9.0";
	static final public String DEFAULT_VERSION_NUMBER = "1";

	public ClinicalDocumentMetadataGenerator() {
		factories = CDAFactories.init();
	}

	public ContinuityOfCareDocument generateClinicalDoc(CDAFactories factories) {

		doc = factories.consol.createContinuityOfCareDocument();
		// No way to set realm code?
		// CS realmCode = genRealmCode(factories, DEFAULT_REALM_CODE);
		II id = genId(factories, DEFAULT_ID_ROOT, DEFAULT_ASSN_AUTH);
		CS code = genCode(factories, DEFAULT_CODE_CODE, DEFAULT_CODE_SYSTEM, DEFAULT_CODE_SYSTEM_NAME,
				DEFAULT_CODE_DISPLAY);
		ST title = genTitle(factories, DEFAULT_TITLE);
		TS effTime = genTime(factories, DEFAULT_EFFCT_TIME);
		CE confidentiality = genConfidentiality(factories, DEFAULT_CONF_CODE_CODE, DEFAULT_CONF_CODE_SYSTEM,
				DEFAULT_CONF_CODE_DSP);
		CS langCode = genLangCode(factories, DEFAULT_LANG_CODE);
		II setId = genSetId(factories, DEFAULT_ASSN_AUTH, DEFAULT_SET_ID_ROOT, DEFAULT_SET_ID_EXT);
		INT version = genVersion(factories, DEFAULT_VERSION_NUMBER);

		IVL_TS eventEffTime = genTime(factories, DEFAULT_EVENT_TIME_LOW, DEFAULT_EVENT_TIME_HIGH);
		DocumentationOf docOf = genDocumentationOf(eventEffTime, assignedEntityGenerator.generate(factories));
		doc.getDocumentationOfs().add(docOf);
		doc.setId(id);
		doc.setCode(code);
		doc.setTitle(title);
		doc.setEffectiveTime(effTime);
		doc.setConfidentialityCode(confidentiality);
		doc.setLanguageCode(langCode);
		doc.setSetId(setId);
		doc.setVersionNumber(version);
		return doc;
	}

	public DocumentationOf genDocumentationOf(IVL_TS effTime, AssignedEntity assignedEntity) {
		DocumentationOf docOf = factories.base.createDocumentationOf();
		ServiceEvent event = factories.base.createServiceEvent();
		event.setEffectiveTime(effTime);
		Performer1 performer = factories.base.createPerformer1();
		performer.setAssignedEntity(assignedEntity);
		event.getPerformers().add(performer);
		docOf.setServiceEvent(event);
		return docOf;
	}

	public CS genRealmCode(CDAFactories factories, String code) {
		CS realmCode = factories.datatype.createCS();
		realmCode.setCode(code);
		return realmCode;
	}

	public II genId(CDAFactories factories, String root, String assnAuth) {
		II id = factories.datatype.createII(root);
		id.setAssigningAuthorityName(assnAuth);
		return id;
	}

	public CS genCode(CDAFactories factories, String codeCode, String codeSystem, String codeSystemName,
			String displayName) {
		CS code = factories.datatype.createCS(codeCode);
		code.setCodeSystem(codeSystem);
		code.setCodeSystemName(codeSystemName);
		code.setDisplayName(displayName);
		return code;
	}

	public ST genTitle(CDAFactories factories, String titleTxt) {
		ST title = factories.datatype.createST();
		title.addText(titleTxt);
		return title;
	}

	public TS genTime(CDAFactories factories, String dttm) {
		return factories.datatype.createTS(dttm);
	}

	public IVL_TS genTime(CDAFactories factories, String timeLow, String timeHigh) {
		return factories.datatype.createIVL_TS(timeLow, timeHigh);
	}

	public CE genConfidentiality(CDAFactories factories, String code, String codeSystem, String codeDisp) {
		CE confidentiality = factories.datatype.createCE(code, codeSystem);
		confidentiality.setDisplayName(codeDisp);
		return confidentiality;
	}

	public CS genLangCode(CDAFactories factories, String code) {
		return factories.datatype.createCS(code);
	}

	public II genSetId(CDAFactories factories, String root, String assnAuth, String ext) {
		II setId = factories.datatype.createII(root);
		setId.setAssigningAuthorityName(assnAuth);
		setId.setExtension(ext);
		return setId;
	}

	public INT genVersion(CDAFactories factories, String versionNum) {
		INT version = factories.datatype.createINT();
		version.setValue(Integer.getInteger(versionNum));
		return version;
	}

	public static void setStructuredBody(CDAFactories factories, ClinicalDocument document,
			List<Component3> components) {
		Component2 component2 = factories.base.createComponent2();
		StructuredBody structuredBody = factories.base.createStructuredBody();
		structuredBody.getComponents().addAll(components);
		component2.setStructuredBody(structuredBody);
		document.setComponent(component2);
	}

}
