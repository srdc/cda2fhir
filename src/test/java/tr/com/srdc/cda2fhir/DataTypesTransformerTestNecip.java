package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;

import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by necip on 7/25/2016
 */
public class DataTypesTransformerTestNecip {

    DataTypesTransformer dtt = new DataTypesTransformerImpl();
    
    
    @Test
    public void testCV2Coding(){
    	// simple instance test
    	CV cv = DatatypesFactory.eINSTANCE.createCV();
    	cv.setCodeSystem("theCodeSystem");
    	cv.setCodeSystemVersion("theCodeSystemVersion");
    	cv.setCode("theCode");
    	cv.setDisplayName("theDisplayName");
    	// TODO: The mapping btw CodingDt.userSelected and CD.codingRationale doesn't exist
    	
    	CodingDt coding = dtt.CV2Coding(cv);
    	
    	Assert.assertEquals("CV.codeSystem was not transformed",coding.getSystem(),"theCodeSystem");
    	Assert.assertEquals("CV.codeSystemVersion was not transformed",coding.getVersion(),"theCodeSystemVersion");
    	Assert.assertEquals("CV.code was not transformed",coding.getCode(),"theCode");
    	Assert.assertEquals("CV.displayName was not transformed",coding.getDisplay(),"theDisplayName");
    	
    	
    	// null instance test
    	CV cv2 = null;
    	CodingDt coding2 = dtt.CV2Coding(cv2);
    	Assert.assertNull("CV null instance transform failed", coding2);
    	
    	// nullFlavor instance test
    	CV cv3 = DatatypesFactory.eINSTANCE.createCV();
    	cv3.setNullFlavor(NullFlavor.NI);
    	CodingDt coding3 = dtt.CV2Coding(cv3);
    	Assert.assertNull("CV.nullFlavor set instance transform failed",coding3);
    }
}
