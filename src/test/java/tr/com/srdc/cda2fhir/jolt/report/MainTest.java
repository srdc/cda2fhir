package tr.com.srdc.cda2fhir.jolt.report;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class MainTest {
	final private static String GOLD_PATH = "src/test/resources/gold/jolt-report/";
	final private static String OUTPUT_PATH = "src/test/resources/output/jolt-report/";

	private static void actualTest(String name) throws Exception {
		String actualContent = Main.transformationCSV(name);
		File outputFile = new File(OUTPUT_PATH + name + ".csv");
		FileUtils.writeStringToFile(outputFile, actualContent, Charset.defaultCharset());
		List<String> actualLines = Arrays.asList(actualContent.split("\\n"));

		File goldFile = new File(GOLD_PATH + name + ".csv");
		List<String> rawGoldLines = FileUtils.readLines(goldFile, Charset.defaultCharset());
		List<String> goldLines = rawGoldLines.stream().filter(line -> line.length() > 0).collect(Collectors.toList());

		Assert.assertEquals("Number of CSV file lines", goldLines.size(), actualLines.size());

		for (int index = 0; index < goldLines.size(); ++index) {
			Assert.assertEquals("CSV line " + index, goldLines.get(index), actualLines.get(index));
		}
	}

	@Test
	public void testAllergyConcernAct() throws Exception {
		actualTest("AllergyConcernAct");
	}

	@Test
	public void testProblemConcernAct() throws Exception {
		actualTest("ProblemConcernAct");
	}

	@Test
	public void testProblemObservation() throws Exception {
		actualTest("ProblemObservation");
	}

	@Test
	public void testID() throws Exception {
		actualTest("ID");
	}

	@Test
	public void testCD() throws Exception {
		actualTest("CD");
	}

	@Test
	public void testIVL_TSPeriod() throws Exception {
		actualTest("IVL_TSPeriod");
	}

	@Test
	public void testIVL_PQRange() throws Exception {
		actualTest("IVL_PQRange");
	}

	@Test
	public void testIVL_PQSimpleQuantity() throws Exception {
		actualTest("IVL_PQSimpleQuantity");
	}

	@Test
	public void testPIVL_TSTiming() throws Exception {
		actualTest("PIVL_TSTiming");
	}

	@Test
	public void testPN() throws Exception {
		actualTest("PN");
	}

	@Test
	public void testTEL() throws Exception {
		actualTest("TEL");
	}

	@Test
	public void testAD() throws Exception {
		actualTest("AD");
	}

	@Test
	public void testAuthorParticipation() throws Exception {
		actualTest("AuthorParticipation");
	}

	@Test
	public void testEntityPractitioner() throws Exception {
		actualTest("EntityPractitioner");
	}

	@Test
	public void testEntityPractitionerRole() throws Exception {
		actualTest("EntityPractitionerRole");
	}

	@Test
	public void testEntityOrganization() throws Exception {
		actualTest("EntityOrganization");
	}

	@Test
	public void testProcedureActivityProcedure() throws Exception {
		actualTest("ProcedureActivityProcedure");
	}

	@Test
	public void testIndication() throws Exception {
		actualTest("Indication");
	}

	@Test
	public void testMedicationIndication() throws Exception {
		actualTest("MedIndication");
	}

	@Test
	public void testServiceDeliveryLocation() throws Exception {
		actualTest("ServiceDeliveryLocation");
	}

	@Test
	public void testEncounterActivity() throws Exception {
		actualTest("EncounterActivity");
	}

	@Test
	public void testMedicationInformation() throws Exception {
		actualTest("MedicationInformation");
	}

	@Test
	public void testMedicationSupplyOrder() throws Exception {
		actualTest("MedicationSupplyOrder");
	}

	@Test
	public void testMedicationActivity() throws Exception {
		actualTest("MedicationActivity");
	}

	@Test
	public void testImmunizationActivity() throws Exception {
		actualTest("ImmunizationActivity");
	}

	@Test
	public void testImmunizationMedicationInformation() throws Exception {
		actualTest("ImmunizationMedicationInformation");
	}

	@Test
	public void testObservation() throws Exception {
		actualTest("Observation");
	}

	@Test
	public void testVitalSignsOrganizer() throws Exception {
		actualTest("VitalSignsOrganizer");
	}

	@Test
	public void testResultOrganizer() throws Exception {
		actualTest("VitalSignsOrganizer");
	}
}
