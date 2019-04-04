package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleRequest;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class addRequestTest {

	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testPatient() throws Exception {

		String sys = "urn:oid:2.16.840.1.113883.3.552.1.3.11.13.1.8.2";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Patient becEntry = new Patient();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("is a thing", bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testCondition() throws Exception {

		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Condition becEntry = new Condition();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("is a thing", bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testIntegration() throws Exception {

		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);

		String sourceName = "Epic/robust CCD.XML";
		String documentBody = "<ClinicalDoc>\n</ClinicalDoc>";
		Identifier assemblerDevice = new Identifier();
		assemblerDevice.setValue("Higgs");
		assemblerDevice.setSystem("http://www.amida.com");
		// create transaction bundle from ccda bundle

		Bundle transactionBundle = ccdTransformer.transformDocument("src/test/resources/" + sourceName,
				BundleType.TRANSACTION, null, documentBody, assemblerDevice);

		FHIRUtil.printXML(transactionBundle, "src/test/resources/output/robust CCD.bundle.xml");

	}

}
