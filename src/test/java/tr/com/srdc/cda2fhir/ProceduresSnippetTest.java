package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class ProceduresSnippetTest {
    @BeforeClass
    public static void init() {
    	// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }

    private static Bundle generateBundle(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        for (Section section: cda.getSections()) {        
        	//Object obj = section.getText().getMixed().get(0).getValue();
        	if (section.getCode().getCode().equals("47519-4")) {
         		Map<String, String> map = EMFUtil.findReferences(section.getText());
        		
        		System.out.println(map.toString());
        	}
        }
        CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Reference dummyPatientRef = new Reference(new IdType("Patient", "0"));
        ccdTransformer.setPatientRef(dummyPatientRef);
        
        Config.setGenerateDafProfileMetadata(true);
        Config.setGenerateNarrative(true);
        Bundle bundle = ccdTransformer.transformDocument(cda, false);
        return bundle;
    }
    
     private static void verifyProcedure(Bundle bundle, String baseName) throws IOException, JSONException {
    	List<Procedure> procedures = bundle.getEntry().stream()
    								.map(r -> r.getResource())
    								.filter(r -> (r instanceof Procedure))
    								.map(r -> (Procedure) r)
    								.collect(Collectors.toList());
    	String actual = FHIRUtil.encodeToJSON(procedures);
    	FHIRUtil.printJSON(procedures, "src/test/resources/output/" + baseName + ".procedure.json");
    	
    	Path goldFilePath = Paths.get("src/test/resources/gold/" + baseName + ".procedure.json");
    	String expected = new String(Files.readAllBytes(goldFilePath));
    	JSONAssert.assertEquals(expected, actual, true);
    }
        
    private static void runGoldTest(String sourceName) throws Exception {
     	Bundle bundle = generateBundle(sourceName);   	
    	String baseName = sourceName.substring(0, sourceName.length() - 4);    	
        verifyProcedure(bundle, baseName);
    }
    
    @Test
    public void testGoldSample() throws Exception {
    	runGoldTest("snippets/procedure_text.xml");
    }
}
