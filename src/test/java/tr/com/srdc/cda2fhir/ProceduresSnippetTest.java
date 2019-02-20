package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.json.JSONException;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class ProceduresSnippetTest {
    @BeforeClass
    public static void init() {
    	// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }

    static private String findAttribute(FeatureMap attributes, String name) {
    	if (attributes != null) {
    		for (Entry attribute: attributes) {
                String attrName = attribute.getEStructuralFeature().getName();                	
                if (name.equalsIgnoreCase(attrName)) {
                	return attribute.getValue().toString();
                }
    		}
    	}
    	return null;
    }
        
    static private void putReferences(FeatureMap featureMap, Map<String, String> result) {
    	if (featureMap == null) {
    		return;
    	}
 		for (Entry entry: featureMap) {
			EStructuralFeature feature = entry.getEStructuralFeature();
			if (feature instanceof EReference) {
                AnyType anyType = (AnyType) entry.getValue();
                if ("content".equalsIgnoreCase(feature.getName())) {
                	String id = findAttribute(anyType.getAnyAttribute(), "id");
                	if (id != null) {
                		FeatureMap idValueMap = anyType.getMixed();
                		if (idValueMap != null && !idValueMap.isEmpty()) {
                			Object value = idValueMap.get(0).getValue();
                			if (value != null) {
                				result.put(id, value.toString());
                			}
                		}
                	}
                	continue;
                }
                putReferences(anyType.getMixed(), result);
			}    
		}
    }

    static private Map<String, String> findReferences(StrucDocText text) {
    	Map<String, String> result = new HashMap<String, String>();
    	FeatureMap featureMap = text.getMixed();
    	putReferences(featureMap, result);
    	return result;
    }
    
    private static Bundle generateBundle(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        for (Section section: cda.getSections()) {        
        	//Object obj = section.getText().getMixed().get(0).getValue();
        	if (section.getCode().getCode().equals("47519-4")) {
         		Map<String, String> map = findReferences(section.getText());
        		
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
