package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import org.hl7.fhir.dstu3.model.DateType;


public class CompositionTest {
	static ResourceTransformerImpl rt;
	static CDAFactories factories;
	static ClinicalDocumentMetadataGenerator metadataGenerator;
	static String defaultExpectedUse =  Config.DEFAULT_IDENTIFIER_USE.getDisplay();
	static String defaultExpectedStatus =  Config.DEFAULT_COMPOSITION_STATUS.getDisplay();
	static String defaultExpectedAssigner =  metadataGenerator.DEFAULT_ASSN_AUTH;
	static String defaultExpectedIdValue =  metadataGenerator.DEFAULT_ID_ROOT;
	static String defaultExpectedTypeCode =  metadataGenerator.DEFAULT_CODE_CODE;
	static String defaultExpectedTypeSystem =  "http://loinc.org";
	static String defaultExpectedTypeDisplay =  metadataGenerator.DEFAULT_CODE_DISPLAY;
	static String defaultExpectedTitle =  metadataGenerator.DEFAULT_TITLE;
	static String defaultExpectedDate =  "Thu Feb 14 16:14:13 EST 2019";
	static String defaultExpectedConfidentiality =  "N";

	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new ResourceTransformerImpl();
		factories = CDAFactories.init();
		metadataGenerator = new ClinicalDocumentMetadataGenerator();
	}
	
	@Test
	public void testComposition() throws Exception {

		ClinicalDocument clinicalDoc = metadataGenerator.generateClinicalDoc(factories);
		EntryResult entryResult = rt.tClinicalDocument2Composition(clinicalDoc);
		Bundle bundle = entryResult.getBundle();
		Composition comp = BundleUtil.findOneResource(bundle, Composition.class);
	
		Assert.assertEquals("Expect assigner to equal assigningAuthorityName", defaultExpectedAssigner, comp.getIdentifier().getAssigner().getReference());
		Assert.assertEquals("Expect use to equal default", defaultExpectedUse, comp.getIdentifier().getUse().getDisplay());
		Assert.assertEquals("Expect status to equal default", defaultExpectedStatus, comp.getStatus().getDisplay());
		Assert.assertEquals("Expect Identifier Value to equal Clinical Document id root", defaultExpectedIdValue, comp.getIdentifier().getValue());
		Assert.assertEquals("Expect type.code to equal code.code", defaultExpectedTypeCode, comp.getType().getCodingFirstRep().getCode());
		Assert.assertEquals("Expect type.system code.codeSystemName", defaultExpectedTypeSystem, comp.getType().getCodingFirstRep().getSystem());
		Assert.assertEquals("Expect type.display code.displayName", defaultExpectedTypeDisplay, comp.getType().getCodingFirstRep().getDisplay());
		Assert.assertEquals("Expect title to equal title", defaultExpectedTitle, comp.getTitle());
		Assert.assertEquals("Expect date to equal date", defaultExpectedDate, comp.getDate().toString());
		Assert.assertEquals("Expect confidentiality to eqial confidentiality", defaultExpectedConfidentiality, comp.getConfidentiality().toString());

	}
	
	

}
