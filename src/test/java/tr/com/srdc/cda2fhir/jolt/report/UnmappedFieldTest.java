package tr.com.srdc.cda2fhir.jolt.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UnmappedFieldTest {
	final private static String INPUT_PATH = "src/test/resources/";
	final private static String REPORT_PATH = "src/test/resources/gold/jolt-report/";
	final private static XPath XPATH = XPathFactory.newInstance().newXPath();

	// Returns the comparison list from the CSV.
	private static List<List<String>> csvToList(String fileName) throws IOException {
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
		return records;
	}

	// Returns the original XML as a Document.
	public static Document convertBody(String sourcePath)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();

		return dbBuilder.parse(new File(sourcePath));
	}

	@Test
	public void testAllergyConcernAct()
			throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		List<List<String>> records = csvToList("AllergyConcernAct");
		Document body = convertBody(INPUT_PATH + "C-CDA_R2-1_CCD.xml");
		NodeList xPathDocument = (NodeList) XPATH.evaluate("//*[local-name()='act']", body, XPathConstants.NODESET);
		// /ClinicalDocument/component/structuredBody/component/section/entry/act/effectiveTime
		// //act/(* except effectiveTime except entryRelationship except statusCode.code
		// except Practitioner)
		for (int i = 0; i < xPathDocument.getLength(); i++) {
			System.out.println(xPathDocument.item(i).getNodeName());
		}
//		for (int i = 0; i < xmlNodes.getLength(); i++) {
//			System.out.println(xmlNodes.item(i).getNodeName());
//		}
		for (List<String> list : records) {
			System.out.println(list.get(0));
		}

	}

}