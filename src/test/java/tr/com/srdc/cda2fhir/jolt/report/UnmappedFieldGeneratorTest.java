package tr.com.srdc.cda2fhir.jolt.report;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class UnmappedFieldGeneratorTest {
	final private static String INPUT_PATH = "src/test/resources/";
	final private static String OUTPUT_PATH = System.getProperty("user.dir") + "/src/test/resources/unmapped/";

	@Before
	public void createUnmappedDirectory() {
		File directory = new File(OUTPUT_PATH);
		directory.mkdir();
	}

	// Was told not to do the following ones:
	// 42348-3 Advanced directives
	// 10157-6 Family history
	// 47420-5 Functional status
	// 46264-8 Medical equipment
	// 48768-6 Insurance providers
	// 18776-5 Treatment plan
	// 29762-2 Social history

	@Test // Also known as "Allergies and Adverse Reactions" Code: 48765-2
	public void testAllergyConcernAct() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "AllergyConcernAct",
				"//section[code/@code='48765-2']");
	}

//	@Test // Also known as "Encounters" Code: 46240-8, but this was not mentioned as
//			// needed.
//	public void testEncounterActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
//			SAXException, TransformerFactoryConfigurationError, TransformerException {
//		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "EncounterActivity",
//				"//section[code/@code='46240-8']");
//	}

	@Test // Also just called "Medication" Code: 10160-0
	public void testMedicationActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "MedicationActivity",
				"//section[code/@code='10160-0']");
	}

	@Test // "Immunizations" Code: 11369-6
	public void testImmunizationActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "ImmunizationActivity",
				"//section[code/@code='11369-6']");
	}

	// "Results" 30954-2

	@Test // "PatientRole" no code.
	public void testPatientRole() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "PatientRole",
				"//recordTarget");
	}

	@Test // "Problems" Code: 11450-4
	public void testProblemConcernAct() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "ProblemConcernAct",
				"//section[code/@code='11450-4']");
	}

	@Test // "Procedures" Code: 47519-4
	public void testProcedureActivityProcedure() throws IOException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "ProcedureActivityProcedure",
				"//section[code/@code='47519-4']");
	}

	@Test // "Vital Signs" Code 8716-3
	public void testVitalSignsOrganizer() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		UnmappedFieldGenerator.unmappedFieldGenerator(INPUT_PATH + "C-CDA_R2-1_CCD.xml", "VitalSignsOrganizer",
				"//section[code/@code='8716-3']");
	}
}