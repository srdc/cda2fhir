package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.AttachmentDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;

import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by necip on 7/25/2016
 */
public class DataTypesTransformerTestNecip {

    DataTypesTransformer dtt = new DataTypesTransformerImpl();
    
    @Test
    public void testED2Attachment(){
    	// simple instance test
    	
    	// TODO: After the implementation of ed.title.data is completed, Attachment.title will be tested
    	// Also, check the note about the test of ED.integrityCheck
    	
    	ED ed = DatatypesFactory.eINSTANCE.createED();
    	ed.setMediaType("theMediaType");
    	ed.setLanguage("theLanguage");
    	ed.addText("theData");
    		TEL theTel = DatatypesFactory.eINSTANCE.createTEL();
    		theTel.setValue("theUrl");
    	ed.setReference(theTel);
    	ed.setIntegrityCheck("theIntegrityCheck".getBytes());
    	
    	
    	AttachmentDt attachment = dtt.ED2Attachment(ed);
    	
    	Assert.assertEquals("ED.mediaType was not transformed","theMediaType",attachment.getContentType());
    	Assert.assertEquals("ED.language was not transformed","theLanguage",attachment.getLanguage());
    	Assert.assertEquals("ED.data was not transformed","theData",attachment.getData());
    	Assert.assertEquals("ED.reference.literal was not transformed","theUrl",attachment.getUrl());
    	Assert.assertEquals("ED.integrityCheck was not transformed","theIntegrityCheck".getBytes(),attachment.getHash());
    	// Although test on the IntegrityCheck gives the same result as string, it gives different results when transformed to bytes[]
    	
    	
    	// null instance test
    	ED ed2 = null;
    	AttachmentDt attachment2 = dtt.ED2Attachment(ed2);
    	Assert.assertNull("ED null instance transform failed", attachment2);
    	
    	// nullFlavor instance test
    	ED ed3 = DatatypesFactory.eINSTANCE.createED();
    	ed3.setNullFlavor(NullFlavor.NI);
    	AttachmentDt attachment3 = dtt.ED2Attachment(ed3);
    	Assert.assertNull("ED.nullFlavor set instance transform failed",attachment3);
    	
    }
    
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
    	
    	Assert.assertEquals("CV.codeSystem was not transformed","theCodeSystem",coding.getSystem());
    	Assert.assertEquals("CV.codeSystemVersion was not transformed","theCodeSystemVersion",coding.getVersion());
    	Assert.assertEquals("CV.code was not transformed","theCode",coding.getCode());
    	Assert.assertEquals("CV.displayName was not transformed","theDisplayName",coding.getDisplay());
    	
    	
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
