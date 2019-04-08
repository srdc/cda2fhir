package tr.com.srdc.cda2fhir.jolt.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UnmappedFieldTest {
	final private static String INPUT_PATH = "src/test/resources/";
	final private static String REPORT_PATH = "src/test/resources/gold/jolt-report/";

	// Returns the comparison list from the CSV.
	private static void csvToList(String fileName) throws IOException {
		File csv = new File(REPORT_PATH + fileName + ".csv");

		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
			String line = br.readLine();
			while (line != null) {
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
				line = br.readLine();
			}
		}
		System.out.println(records);
	}

	@Test
	public void testAllergyConcernAct() throws IOException {
		csvToList("AllergyConcernAct");
	}

}