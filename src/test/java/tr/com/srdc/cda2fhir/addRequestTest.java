package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.CDGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleRequest;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class addRequestTest {

	private static CDAFactories factories;
	private CDGenerator codeGenerator;

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
	public void testPatientMultipleIds() throws Exception {
		String sys1 = "urn:oid:1.2.840.114350.1.13.88.3.7.3.698084.8";
		String val1 = "12345";
		String sys2 = "urn:oid:1.2.840.114350.0.0.0.0.0.0.0.0";
		String val2 = "67890";

		BundleEntryComponent bec = new BundleEntryComponent();
		Practitioner becEntry = new Practitioner();
		Identifier becId1 = new Identifier();
		Identifier becId2 = new Identifier();
		becId1.setSystem(sys1);
		becId1.setValue(val1);
		becId2.setSystem(sys2);
		becId2.setValue(val2);
		becEntry.addIdentifier(becId1);
		becEntry.addIdentifier(becId2);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("identifier=" + sys1 + "|" + val1 + "," + sys2 + "|" + val2));
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
	public void testEncounter() throws Exception {
		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.3.698084.8";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Encounter becEntry = new Encounter();
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
	public void testPractitioner() throws Exception {
		String sys = "urn:oid:1.2.840.114350.1.13.88.3.7.3.698084.8";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Practitioner becEntry = new Practitioner();
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
	public void testUnidentifiedMedication() throws Exception {
		String sys = "http://www.nlm.nih.gov/research/umls/rxnorm";
		String val = "12345";

		BundleEntryComponent bec = new BundleEntryComponent();
		Medication becEntry = new Medication();
		CodeableConcept medicationCode = new CodeableConcept();
		Coding code = new Coding();
		code.setCode(val);
		code.setSystem(sys);
		code.setDisplay("drug");

		becEntry.getCode().addCoding(code);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("code=" + sys + "|" + val));
	}

	@Test
	public void testUnidentifiedMedicationMultiCode() throws Exception {
		String sys = "http://www.nlm.nih.gov/research/umls/rxnorm";
		String val1 = "12345";
		String val2 = "67890";

		BundleEntryComponent bec = new BundleEntryComponent();
		Medication becEntry = new Medication();
		CodeableConcept medicationCode = new CodeableConcept();
		Coding code1 = new Coding();
		Coding code2 = new Coding();
		code1.setCode(val1);
		code1.setSystem(sys);
		code1.setDisplay("drug");
		code2.setCode(val2);
		code2.setSystem(sys);
		code2.setDisplay("drug");

		becEntry.getCode().addCoding(code1);
		becEntry.getCode().addCoding(code2);
		bec.setResource(becEntry);

		BundleRequest.addRequestToEntry(bec);

		Assert.assertTrue("ifNoneExists has been populated",
				bec.getRequest().getIfNoneExist().equals("code=" + sys + "|" + val1 + "," + val2));
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
