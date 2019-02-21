package tr.com.srdc.cda2fhir;

import java.util.List;

import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;

public class ProceduresSectionSnippetTest {
    @BeforeClass
    public static void init() {
    	// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }

    private static Procedure findProcedureById(List<Procedure> procedures, String id) {
    	for (Procedure procedure: procedures) {
    		for (Identifier identifier: procedure.getIdentifier()) {
    			if (id.equals(identifier.getValue())) {
    				return procedure;
    			}
    		}
    	}
    	return null;
    }

    private static void verifyProcedure(Procedure procedure, String expectedCode, String expectedAnnotation) {
    	Assert.assertNotNull("Expect procedure has been found by id", procedure);
    	    	
    	CodeableConcept code = procedure.getCode();
    	
     	Assert.assertFalse("Expect no coding for procedure code", code.hasCoding());
     	Assert.assertEquals("Expect the code text in the snippet for procedure code", expectedCode, code.getText());

     	Assert.assertTrue("Expect procedure note", procedure.hasNote());
     	String actualAnnotation = procedure.getNote().get(0).getText();
     	Assert.assertEquals("Expect the annotation in the snippet for procedure note", expectedAnnotation, actualAnnotation);
    }
    
    @Test
    public void testTextWithReferenceDefinitions() throws Exception {
     	Bundle bundle = BundleUtil.generateBundle("snippets/procedure_text.xml");
     	List<Procedure> procedures = BundleUtil.findResources(bundle, Procedure.class, 3);

     	// Looks like EMF queries messes up procedure order in the ccda so use ids instead
     	Procedure procedure0 = findProcedureById(procedures, "77baeec3-124e-4348-bcec-fbe2fd25e7ef");
     	verifyProcedure(procedure0, "APPENDECTOMY LAPAROSCOPIC", "auto-populated from documented surgical case");
     	Procedure procedure1 = findProcedureById(procedures, "4372357a-30bb-48d9-a612-8f459ae8c00c");
     	verifyProcedure(procedure1, "REMOVAL IMPLANTED DEVICES FROM BONE", "auto-populated from documented surgical case");
     	Procedure procedure2 = findProcedureById(procedures, "e39bdae7-7b35-4ceb-88b7-8891414c3bc2");
     	verifyProcedure(procedure2, "REPAIR ELBOW", "auto-populated from documented surgical case");
    }
}
