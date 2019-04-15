package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.io.FileWriter;

import org.eclipse.emf.common.util.EList;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.rim.InfrastructureRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

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

	public static NullFlavor toNullFlavor(String nullFlavor) {
		NullFlavor nf = NullFlavor.get(nullFlavor);
		if (nf == null) {
			throw new TestSetupException("Invalid null flavor enumeration.");
		}
		return nf;
	}

	public static int idValue(String fhirType, EList<II> iis, IdentifierMap<Integer> orderMap) {
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		II ii = iis.get(0);
		String root = ii.getRoot();
		String extension = ii.getExtension();
		if (extension == null) {
			Integer index = orderMap.get(fhirType, root);
			return index.intValue();
		} else {
			String value = extension;
			String system = vst.tOid2Url(root);
			Integer index = orderMap.get(fhirType, system, value);
			return index.intValue();
		}

	}
}
