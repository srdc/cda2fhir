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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UnmappedFieldGenerator {
	final private static String OUTPUT_PATH = System.getProperty("user.dir") + "/src/test/resources/unmapped/";
	final private static String REPORT_PATH = "src/test/resources/gold/jolt-report/full/";
	final private static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	final private static XPath XPATH = XPathFactory.newInstance().newXPath();

	// Returns the comparison list from the CSV.
	public static List<List<String>> csvToList(String csvReportType) throws IOException {
		File csv = new File(REPORT_PATH + csvReportType + ".csv");

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
	public static boolean searchNode(Node node, ArrayList<String> searchTerms) {
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
					deleteNode = true;
					for (int j = 0; j < node.getChildNodes().getLength(); j++) {
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

	public static void generateFilteredNodeList(List<List<String>> csvRecords, NodeList xPathDocument) {
		for (int i = 1; i < csvRecords.size(); i++) { // The list at 0 is just the headers.
			ArrayList<String> searchTerms = new ArrayList<String>();
			System.out.println(xPathDocument.item(0).getNodeName());
			if (!xPathDocument.item(0).getNodeName().contentEquals("recordTarget")) {
				searchTerms.add("entry");
			}
			String[] csvFields = csvRecords.get(i).get(0).replaceAll("\\[]", "").split("\\.");
			for (String csvField : csvFields) {
				searchTerms.add(csvField);
			}
			searchNode(xPathDocument.item(0), searchTerms);
		}
	}

	public static void unmappedFieldGenerator(String filePath, String csvReportType, String xPath)
			throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
			TransformerFactoryConfigurationError, TransformerException {
		List<List<String>> csvRecords = csvToList(csvReportType);
		Document body = convertFileToDocument(filePath);
		NodeList xPathDocument = (NodeList) XPATH.evaluate(xPath, body, XPathConstants.NODESET);

		generateFilteredNodeList(csvRecords, xPathDocument);
		convertNodeListToFile(xPathDocument,
				OUTPUT_PATH + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf(".xml")) + "-"
						+ csvReportType + "-unmapped.xml");
	}
}