package tr.com.srdc.cda2fhir.jolt.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UnmappedFieldTest {
	final private static String INPUT_PATH = "src/test/resources/";
	final private static String OUTPUT_PATH = System.getProperty("user.dir") + "/src/test/resources/unmapped/";
	final private static String REPORT_PATH = "src/test/resources/gold/jolt-report/";
	final private static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	final private static XPath XPATH = XPathFactory.newInstance().newXPath();

	// Returns the comparison list from the CSV.
	public static List<List<String>> csvToList(String fileName) throws IOException {
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
	public static Document convertFileToDocument(String sourcePath)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilder dbBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();

		return dbBuilder.parse(new File(sourcePath));
	}

	public static void convertNodeListToFile(NodeList nodeList, String sourcePath) throws ParserConfigurationException,
			TransformerFactoryConfigurationError, IOException, TransformerException {
		Document xmlDocument = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
		Element root = xmlDocument.createElement("root");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xmlns:sdtc", "urn:hl7-org:sdtc");
		xmlDocument.appendChild(root);

		for (int i = 0; i < nodeList.getLength(); i++) {
			root.appendChild(xmlDocument.importNode(nodeList.item(i), true));
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

		DOMSource source = new DOMSource(xmlDocument);
		FileWriter writer = new FileWriter(sourcePath);
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}

	@SuppressWarnings("unchecked")
	public boolean searchNode(Node node, ArrayList<String> searchTerms) {
		boolean deleteNode = false;
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			if (node.getChildNodes().item(i).getNodeName().contentEquals(searchTerms.get(0))) {
				if (searchTerms.size() == 1
						&& node.getChildNodes().item(i).getNodeName().contentEquals(searchTerms.get(0))) {
					node.removeChild(node.getChildNodes().item(i));
					deleteNode = true;
					for (int j = 0; j < node.getChildNodes().getLength(); j++) {
						if (node.getChildNodes().item(j).getNodeType() < 3) { // Ignore comments and text.
							deleteNode = false;
						}
					}
				} else if (searchTerms.size() == 2
						&& node.getChildNodes().item(i).getAttributes().getNamedItem(searchTerms.get(1)) != null) {
					node.removeChild(node.getChildNodes().item(i));
					for (int j = 0; j < node.getChildNodes().getLength(); j++) {
						deleteNode = true;
						if (node.getChildNodes().item(j).getNodeType() < 3) { // Ignore comments and text.
							deleteNode = false;
						}
					}
				} else {
					ArrayList<String> newSearchTerms = (ArrayList<String>) searchTerms.clone();
					newSearchTerms.remove(0);
					if (searchNode(node.getChildNodes().item(i), newSearchTerms)) {
						node.removeChild(node.getChildNodes().item(i));
					}
				}
			}
		}
		return deleteNode;
	}

	public void generateFilteredNodeList(List<List<String>> csvRecords, NodeList xPathDocument) {
		for (int i = 1; i < csvRecords.size(); i++) { // The list at 0 is just the headers.
			ArrayList<String> searchTerms = new ArrayList<String>();
			searchTerms.add("entry");
			String[] csvFields = csvRecords.get(i).get(0).replaceAll("\\[]", "").split("\\.");
			for (String csvField : csvFields) {
				searchTerms.add(csvField);
			}
			searchNode(xPathDocument.item(0), searchTerms);
		}
	}

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
		List<List<String>> csvRecords = csvToList("AllergyConcernAct");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='48765-2']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-AllergyConcernAct-unmapped.xml");
	}

	@Test // Also known as "Encounters" Code: 46240-8, but this was not mentioned as
			// needed.
	public void testEncounterActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("EncounterActivity");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='46240-8']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-EncounterActivity-unmapped.xml");
	}

	@Test // Also just called "Medications" Code: 10160-0
	public void testMedicationActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("MedicationActivity");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='10160-0']", body,
				XPathConstants.NODESET);

//		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-MedicationActivity-unmapped.xml");
	}

	@Test // "Immunizations" Code: 11369-6
	public void testImmunizationActivity() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("ImmunizationActivity");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='11369-6']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-ImmunizationActivity-unmapped.xml");
	}

	// "Results" 30954-2

	@Test // "Problems" Code: 11450-4
	public void testProblemConcernAct() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("ProblemConcernAct");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='11450-4']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-ProblemConcernAct-unmapped.xml");
	}

	@Test // "Procedures" Code: 47519-4
	public void testProcedureActivityProcedure() throws IOException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("ProcedureActivityProcedure");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='47519-4']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-ProcedureActivityProcedure-unmapped.xml");
	}

	@Test // "Vital Signs" Code 8716-3
	public void testVitalSignsOrganizer() throws IOException, XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList("VitalSignsOrganizer");
		Document body = convertFileToDocument(INPUT_PATH + "C-CDA_R2-1_CCD.xml");

		NodeList xPathDocument = (NodeList) XPATH.evaluate("//section[code/@code='8716-3']", body,
				XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument, OUTPUT_PATH + "C-CDA_R2-1_CCD-VitalSignsOrganizer-unmapped.xml");
	}
}