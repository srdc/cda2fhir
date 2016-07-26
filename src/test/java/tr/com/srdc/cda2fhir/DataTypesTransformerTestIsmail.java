package tr.com.srdc.cda2fhir;

//import org.apache.commons.codec.binary.Base64;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;






import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
//import org.openhealthtools.mdht.uml.hl7.datatypes.BinaryDataEncoding;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
//import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
//import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
//import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
//import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;


public class DataTypesTransformerTestIsmail {
    
    DataTypesTransformer dtt = new DataTypesTransformerImpl();

    @Test
    public void testTS2DateTime() {
       
        TS ts = DatatypesFactory.eINSTANCE.createTS();
        
        ts.setValue("192310291923");
        DateTimeDt dateTime = dtt.TS2DateTime(ts);

        Assert.assertEquals("Year was not transformed", 1923 , dateTime.getYear(), 0);
        Assert.assertEquals("Month was not transformed", 10 , dateTime.getMonth(), 0);
        Assert.assertEquals("Day was not transformed", 29 , dateTime.getDay(), 0);
        Assert.assertEquals("Hour was not transformed", 19 , dateTime.getHour(), 0);
        Assert.assertEquals("Minute was not transformed", 23 , dateTime.getMinute(), 0);
        Assert.assertEquals("Minute was not transformed", TemporalPrecisionEnum.MINUTE , dateTime.getPrecision());
        
        
        
        // null instance test
        TS ts2 = null;
        DateTimeDt dateTime2 = dtt.TS2DateTime(ts2);
        Assert.assertNull("TS null instance transform failed", dateTime2);

        // nullFlavor instance test
       TS ts3 = DatatypesFactory.eINSTANCE.createTS();
        ts3.setNullFlavor(NullFlavor.NI);
        DateTimeDt dateTime3 = dtt.TS2DateTime(ts3);
        Assert.assertNull("TS.nullFlavor set instance transform failed", dateTime3);
    }
    
    
    
//    @Test
//    public void testED2Attachment(){
//      
//      ED ed = DatatypesFactory.eINSTANCE.createED();
//      TEL tel = DatatypesFactory.eINSTANCE.createTEL();
//      tel.setValue("telValue");
//      
//      ed.setLanguage("eng");
//      ed.setMediaType("mediaType");
//      ed.setReference(tel);
//      ed.getReference().setValue("url");
//      
//    }
    
    @Ignore
    public void testIVL_PQ2Range(){
        
        IVL_PQ ivlpq = DatatypesFactory.eINSTANCE.createIVL_PQ();
        IVXB_PQ ivxbpqH = DatatypesFactory.eINSTANCE.createIVXB_PQ();
        ivxbpqH.setValue(0.2);
        ivxbpqH.setUnit("unit");
        IVXB_PQ ivxbpqL = DatatypesFactory.eINSTANCE.createIVXB_PQ();
        ivxbpqL.setValue(0.1);
        ivxbpqL.setUnit("unit");
        
        ivlpq.setHigh(ivxbpqH);
        ivlpq.setLow(ivxbpqL); 
        
        RangeDt range = dtt.IVL_PQ2Range( ivlpq );
        
        Assert.assertEquals( "IVL_PQ.high.unit was not transformed" ,  ivlpq.getHigh().getUnit()  , range.getHigh().getUnit() );
        Assert.assertEquals( "IVL_PQ.high.value was not transformed" ,  ivlpq.getHigh().getValue()  , range.getHigh().getValue() );
        Assert.assertEquals( "IVL_PQ.low.unit was not transformed" ,  ivlpq.getLow().getUnit()  , range.getLow().getUnit() );
        Assert.assertEquals( "IVL_PQ.low.value was not transformed" ,  ivlpq.getLow().getValue()  , range.getLow().getValue() );
        
      // null instance test
        IVL_PQ ivlpq2 = null;
        RangeDt range2 = dtt.IVL_PQ2Range( ivlpq2 );
        Assert.assertNull("IVL_PQ null instance transform failed", range2);
        
      // nullFlavor instance test
        IVL_PQ ivlpq3 = DatatypesFactory.eINSTANCE.createIVL_PQ();
        ivlpq3.setNullFlavor(NullFlavor.NI);
        RangeDt range3 = dtt.IVL_PQ2Range( ivlpq3 );
        Assert.assertNull("IVL_PQ.nullFlavor set instance transform failed", range3);
        
    }

    @Ignore
    public void testCV2CodingIsmail(){
    	
    	CV cv = DatatypesFactory.eINSTANCE.createCV();
    	
    	cv.setCode("code");
    	cv.setCodeSystem("codeSystem");
    	cv.setCodeSystemName("codeSystemName");
    	cv.setCodeSystemVersion("codeSystemVersion");
    	cv.setDisplayName("displayName");
    	
    	CodingDt coding = dtt.CV2Coding(cv);
    	
    	Assert.assertEquals("CD.code transformation failed", "code", coding.getCode());
    	Assert.assertEquals("CD.codeSystem transformation failed", "codeSystem", coding.getSystem());
    	Assert.assertEquals("CD.codeSystemVersion transformation failed", "codeSystemVersion", coding.getVersion());
    	Assert.assertEquals("CD.displayName transformation failed", "displayName", coding.getDisplay());
    	
    
    
 // null instance test
    CV cv2 = null;
    CodingDt coding2 = dtt.CV2Coding( cv2 );
    Assert.assertNull("CV null instance transform failed", coding2);
    
  // nullFlavor instance test
    CV cv3 = DatatypesFactory.eINSTANCE.createCV();
    cv3.setNullFlavor(NullFlavor.NI);
    CodingDt coding3 = dtt.CV2Coding( cv3 );
    Assert.assertNull("Coding.nullFlavor set instance transform failed", coding3);
	}
    
    
}

