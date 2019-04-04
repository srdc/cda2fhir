package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
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

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

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

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testReports() throws Exception {

		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		DiagnosticReport becEntry = new DiagnosticReport();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testAllergies() throws Exception {

		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.2.768076";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		AllergyIntolerance becEntry = new AllergyIntolerance();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testMedStatement() throws Exception {

		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		MedicationStatement becEntry = new MedicationStatement();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testMedRequest() throws Exception {

		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.2.798268";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		MedicationRequest becEntry = new MedicationRequest();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));

	}

	@Test
	public void testProcedure() throws Exception {
		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.1.1988.1";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Procedure becEntry = new Procedure();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));
	}

	@Test
	public void testVitalSigns() throws Exception {
		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.1.2109.1";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Observation becEntry = new Observation();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));
	}

	@Test
	public void testResults() throws Exception {
		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.6.798268.2000";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Observation becEntry = new Observation();
		Identifier becId = new Identifier();
		becId.setSystem(sys);
		becId.setValue(val);
		becEntry.addIdentifier(becId);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys + "|" + val));
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
