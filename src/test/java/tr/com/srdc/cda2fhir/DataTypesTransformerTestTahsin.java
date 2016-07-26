package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by mustafa on 7/25/2016.
 */
public class DataTypesTransformerTestTahsin {

    DataTypesTransformer dtt = new DataTypesTransformerImpl();

    @Test
    public void testINT2Integer() {
        // simple instance test
        INT myInt = DatatypesFactory.eINSTANCE.createINT();
        myInt.setValue(65);
        IntegerDt integer = dtt.INT2Integer(myInt);

        Assert.assertEquals("INT.value was not transformed", 65.0, integer.getValue().doubleValue(),0.001);

        // null instance test
    	INT int2 = null;
    	IntegerDt integer2=dtt.INT2Integer(int2);
        Assert.assertNull("INT null instance transform failed",integer2);
    
    }
    @Test
    public void testST2String(){
    	//simple instance test
    	ST st=DatatypesFactory.eINSTANCE.createST();
    	st.addText("selam");
    	StringDt string=dtt.ST2String(st);
        Assert.assertEquals("ST.text was not transformed", "selam", string.getValue());

        //null instance test
        ST st2=null;
        StringDt string2=dtt.ST2String(st2);
        Assert.assertNull("ST null instance transform failed",string2);

    }
    @Test
    public void testREAL2Decimal(){
    	//simple instance test
    	REAL real=DatatypesFactory.eINSTANCE.createREAL();
    	real.setValue(78965.0);
    	DecimalDt decimal=dtt.REAL2Decimal(real);
        Assert.assertEquals("REAL.value was not transformed", 78965.0, decimal.getValue().doubleValue(),0.001);

        //null instance test
        REAL real2=null;
        DecimalDt decimal2=dtt.REAL2Decimal(real2);
        Assert.assertNull("REAL null instance transform failed",decimal2);

    }

    @Test
    public void testBL2Boolean(){
    	//simple instance test
    	BL bl=DatatypesFactory.eINSTANCE.createBL();
    	bl.setValue(true);
    	BooleanDt bool=dtt.BL2Boolean(bl);
    	Assert.assertEquals("BL.value was not transformed",true, bool.getValue());
    
    	//null instance test
    	BL bl2=null;
    	BooleanDt bool2=dtt.BL2Boolean(bl2);
        Assert.assertNull("BL null instance transform failed",bool2);

    }
    @Test
    public void testIVL_PQ2Range(){
    	//simple instance test
    	IVL_PQ ivl_pq=DatatypesFactory.eINSTANCE.createIVL_PQ();
    	IVXB_PQ ivxb_pq=DatatypesFactory.eINSTANCE.createIVXB_PQ();
    	IVXB_PQ ivxb_pq2=DatatypesFactory.eINSTANCE.createIVXB_PQ();

    	ivxb_pq.setValue(67.0);
    	ivxb_pq2.setValue(178.5);
    	ivl_pq.setLow(ivxb_pq);
    	ivl_pq.setHigh(ivxb_pq2);
    	RangeDt range=dtt.IVL_PQ2Range(ivl_pq);
    	Assert.assertEquals("IVL_PQ.low was not transformed", 67.0,range.getLow().getValue().doubleValue(),0.001);
    	Assert.assertEquals("IVL_PQ.high was not transformed", 178.5,range.getHigh().getValue().doubleValue(),0.001);
    	
    	//null instance test
    	IVL_PQ ivl_pq2=null;
    	RangeDt range2=dtt.IVL_PQ2Range(ivl_pq2);
        Assert.assertNull("IVL_PQ null instance transform failed",range2);

    
    
    }
}
