package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.io.FileWriter;

import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.rim.InfrastructureRoot;

public class CDAUtilExtension {
	public static File writeAsXML(InfrastructureRoot infrastructureRoot, String outputPath, String caseName)
			throws Exception {
		File xmlFile = new File(outputPath, caseName + ".xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(infrastructureRoot, fw);
		fw.close();
		return xmlFile;
	}
}
