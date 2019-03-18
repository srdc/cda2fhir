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

	@Test
	public void test0() throws Exception {
		File goldFile = new File(GOLD_PATH + "AllergyConcernAct.csv");

		List<String> rawGoldLines = FileUtils.readLines(goldFile, Charset.defaultCharset());
		List<String> goldLines = rawGoldLines.stream().filter(line -> line.length() > 0).collect(Collectors.toList());

		String actualContent = Main.transformationCSV("AllergyConcernAct");
		File outputFile = new File(OUTPUT_PATH + "AllergyConcernAct.csv");
		FileUtils.writeStringToFile(outputFile, actualContent, Charset.defaultCharset());
		List<String> actualLines = Arrays.asList(actualContent.split("\\n"));

		Assert.assertEquals("Number of CSV file lines", goldLines.size(), actualLines.size());

		for (int index = 0; index < goldLines.size(); ++index) {
			Assert.assertEquals("CSV line " + index, goldLines.get(index), actualLines.get(index));
		}
	}
}
