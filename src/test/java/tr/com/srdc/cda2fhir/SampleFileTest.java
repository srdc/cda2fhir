package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
import org.json.JSONException;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class SampleFileTest {
    @BeforeClass
    public static void init() {
    	// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
    
    private static void traverse(FeatureMap root) {
        Stack<FeatureMap> stack = new Stack<FeatureMap>();
        stack.push(root);
        while (!stack.isEmpty()) {
            FeatureMap featureMap = stack.pop();
            for (int i = featureMap.size() - 1; i >= 0; i--) {
                Entry entry = featureMap.get(i);
                if (entry.getEStructuralFeature() instanceof EReference) {
                    System.out.println(entry.getEStructuralFeature().getName() + " {");
                    AnyType anyType = (AnyType) entry.getValue();
                    traverseAttributes(anyType.getAnyAttribute());
                    stack.push(anyType.getMixed());
                } else {
                    if (entry.getValue() != null) {
                        String value = entry.getValue().toString();
                        if (value.trim().length() > 0) {
                            System.out.println(" " + value + " }");
                        }
                    } else {
                        System.out.println(" }");
                    }
                }
            }
        }
    }

    private static void traverseAttributes(FeatureMap anyAttribute) {
        for (Entry entry : anyAttribute) {
            System.out.println("attr name: " + entry.getEStructuralFeature().getName() + ", attr value: " +
                    entry.getValue().toString());
        }
    }
    
    private static Bundle generateBundle(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        EStructuralFeature esf = cda.getSections().get(5).getText().getMixed().get(0).getEStructuralFeature();
        boolean isMany = esf.isMany();
        List<EAnnotation> annotations = esf.getEAnnotations();
        for (Section section: cda.getSections()) {        
        	//Object obj = section.getText().getMixed().get(0).getValue();
        	if (section.getCode().getCode().equals("47519-4")) {
        		FeatureMap texts = section.getText().getMixed();
        		for (Entry entry: texts) {
        			if (entry.getEStructuralFeature() instanceof EReference) {
        				EReference ref = (EReference) entry.getEStructuralFeature();
        				if (ref.getName().equals("table")) {
                            AnyType anyType = (AnyType) entry.getValue();
        					traverse(anyType.getMixed());
        				}
        			}
        		}
        	}
        }
        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Config.setGenerateDafProfileMetadata(true);
        Config.setGenerateNarrative(true);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        return bundle;
    }
    
    private static void verifyPatient(Bundle bundle, String baseName) throws IOException, JSONException {
    	Assert.assertNotNull(bundle);
    	FHIRUtil.printJSON(bundle, "src/test/resources/output/" + baseName + ".json");
    	
    	List<Patient> patients = bundle.getEntry().stream()
    								.map(r -> r.getResource())
    								.filter(r -> (r instanceof Patient))
    								.map(r -> (Patient) r)
    								.collect(Collectors.toList());
    	Assert.assertEquals(1, patients.size());
    	Patient patient = patients.get(0);
    	String actual = FHIRUtil.encodeToJSON(patient);
    	FHIRUtil.printJSON(patient, "src/test/resources/output/" + baseName + ".patient.json");
    	
    	Path goldFilePath = Paths.get("src/test/resources/gold/" + baseName + ".patient.json");
    	String expected = new String(Files.readAllBytes(goldFilePath));
    	JSONAssert.assertEquals(expected, actual, true);
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
        verifyPatient(bundle, baseName);
        verifyProcedure(bundle, baseName);
    }
    
    // Cerner/Person-RAKIA_TEST_DOC00001 (1).XML
    @Test
    public void testGoldSample() throws Exception {
    	runGoldTest("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
    }
}
