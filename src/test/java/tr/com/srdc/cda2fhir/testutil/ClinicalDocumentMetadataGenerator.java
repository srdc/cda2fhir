package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.utilities.xhtml.HierarchicalTableGenerator.Title;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

public class ClinicalDocumentMetadataGenerator {
	ClinicalDocument doc;
	static final public String DEFAULT_REALM_CODE = "US";
	static final public String DEFAULT_ID_ROOT = "1.2.840.114350.1.13.88.3.7.8.688883.41197285";
	static final public String DEFAULT_ASSN_AUTH = "EPC";
	static final public String DEFAULT_CODE_CODE = "34133-9";
	static final public String DEFAULT_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	static final public String DEFAULT_CODE_SYSTEM_NAME= "LOINC";
	static final public String DEFAULT_CODE_DISPLAY = "Summarization of Episode Note";
	static final public String DEFAULT_TITLE = "Patient Health Summary";
	static final public String DEFAULT_EFFCT_DTTM = "20190214161413-0500";
	static final public String DEFAULT_CONF_CODE_CODE = "N";
	static final public String DEFAULT_CONF_CODE_SYSTEM = "2.16.840.1.113883.5.25";
	static final public String DEFAULT_CONF_CODE_DSP = "Normal";
	static final public String DEFAULT_LANG_CODE = "en-US";
	static final public String DEFAULT_SET_ID_EXT = "7a8b50d2-309d-11e9-a581-5102195700f0";
	static final public String DEFAULT_SET_ID_ROOT = "1.2.840.114350.1.13.88.3.7.1.1";
	static final public String DEFAULT_VERSION_NUMBER = "1";
	
	public ClinicalDocument generateClinicalDoc(CDAFactories factories) {
		ClinicalDocument doc = factories.base.createClinicalDocument();
		
		// No way to set realm code?
		// CS realmCode = genRealmCode(factories, DEFAULT_REALM_CODE);
		II id = genId(factories, DEFAULT_ID_ROOT, DEFAULT_ASSN_AUTH);
		CS code = genCode(factories, DEFAULT_CODE_CODE, DEFAULT_CODE_SYSTEM, DEFAULT_CODE_SYSTEM_NAME, DEFAULT_CODE_DISPLAY);
		ST title = genTitle(factories, DEFAULT_TITLE);
		TS effDTTM = genDTTM(factories, DEFAULT_EFFCT_DTTM);
		CE confidentiality = genConfidentiality(factories, DEFAULT_CONF_CODE_CODE, DEFAULT_CONF_CODE_SYSTEM, DEFAULT_CONF_CODE_DSP);
		CS langCode = genLangCode(factories,DEFAULT_LANG_CODE);
		II setId = genSetId(factories, DEFAULT_ASSN_AUTH, DEFAULT_SET_ID_ROOT, DEFAULT_SET_ID_EXT);
		INT version = genVersion(factories, DEFAULT_VERSION_NUMBER);

		doc.setId(id);
		doc.setCode(code);
		doc.setTitle(title);
		doc.setEffectiveTime(effDTTM);
		doc.setConfidentialityCode(confidentiality);
		doc.setLanguageCode(langCode);
		doc.setSetId(setId);
		doc.setVersionNumber(version);
		return doc;
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
	
	public CS genCode(CDAFactories factories, String codeCode, String codeSystem, String codeSystemName, String displayName) {
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
	
	public TS genDTTM(CDAFactories factories, String dttm) {
		return factories.datatype.createTS(dttm);
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
	

}
