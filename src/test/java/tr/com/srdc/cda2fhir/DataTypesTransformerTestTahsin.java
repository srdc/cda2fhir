package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.AnnotationDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.cda.util.CDAAdapterFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.BIN;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.BN;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.QTY;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO_QTY_QTY;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.rim.RIMFactory;
import org.openhealthtools.mdht.uml.hl7.rim.Role;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by mustafa on 7/25/2016.
 */
public class DataTypesTransformerTestTahsin {

    private static final EDataType QTY = null;
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

        //nullFlawor instance test
        IVL_PQ ivl_pq3=DatatypesFactory.eINSTANCE.createIVL_PQ();
        IVXB_PQ ivxb_pq3=DatatypesFactory.eINSTANCE.createIVXB_PQ();
        IVXB_PQ ivxb_pq4=DatatypesFactory.eINSTANCE.createIVXB_PQ();
        
        ivxb_pq3.setValue(236.5);
        ivxb_pq4.setNullFlavor(NullFlavor.NI);
        
        ivl_pq3.setLow(ivxb_pq3);
        ivl_pq3.setHigh(ivxb_pq4);
        
        RangeDt range3=dtt.IVL_PQ2Range(ivl_pq3);
        Assert.assertEquals("IVL_PQ.low was not transformed",236.5,range3.getLow().getValue().doubleValue(),0.001);
        Assert.assertNull("IVL_PQ.high.nullFlavor set instance transform failed",range3.getHigh().getValue());
    
    }//end test RangeDt
    
    @Test
    public void testAct2Annotation(){
    	//simple instance test
    	Act act=CDAFactory.eINSTANCE.createAct();
    	IVL_TS ivl_ts=DatatypesFactory.eINSTANCE.createIVL_TS();
    	IVXB_TS ivxb_ts=DatatypesFactory.eINSTANCE.createIVXB_TS();
        ivxb_ts.setValue("20170625");
        IVXB_TS ivxb_ts2=DatatypesFactory.eINSTANCE.createIVXB_TS();
        ivxb_ts2.setValue("20180417");
    	
        ivl_ts.setLow(ivxb_ts);
        ivl_ts.setHigh(ivxb_ts2);
    	act.setEffectiveTime(ivl_ts);
    	
    	ED ed=DatatypesFactory.eINSTANCE.createED();
    	ed.setMediaType("application/xml");
    	ed.setLanguage("English");
    	ed.addText("this is data I think");
    	TEL tel=DatatypesFactory.eINSTANCE.createTEL();
    	tel.setValue("www.facebook.com");
    	ed.setReference(tel);
    	ed.setIntegrityCheck("hello".getBytes());
    	act.setText(ed);
    	Participant2 myParticipant = CDAFactory.eINSTANCE.createParticipant2();
    	myParticipant.setTypeCode(ParticipationType.AUT);
    	
    	/*PN pn=DatatypesFactory.eINSTANCE.createPN();
    	pn.addGiven("Tahsin");
    	pn.addFamily("Kose");
    	pn.addPrefix("Colonel");
    	pn.addSuffix("Kurt");
    	pn.addText("Tahsincan Kose");
    	person.getNames().add(pn);
    	*/
    	
        act.getParticipants().add(myParticipant);
        
    	AnnotationDt annotation=dtt.Act2Annotation(act);
        Assert.assertEquals("Act.EffectiveTime was not transformed","2017-06-25",annotation.getTime());

    	/*TODO:Participants cannot be added since there isn't any convenient method to do that.
    	 * Also the test doesn't work since effectiveTimes and texts need their corresponding participants.
    	 */
    	
    }//end Annotation test

    @Test
    public void testTS2DateTime(){
    	//simple instance test,yyyy
    	TS ts=DatatypesFactory.eINSTANCE.createTS();
    	ts.setValue("2016");
    	DateTimeDt datetime=dtt.TS2DateTime(ts);
        Assert.assertEquals("TS.value was not transformed","2016",datetime.getValueAsString());
        
        //simple instance test,yyyymm
        TS ts2=DatatypesFactory.eINSTANCE.createTS();
    	ts2.setValue("201605");
    	DateTimeDt datetime2=dtt.TS2DateTime(ts2);
        Assert.assertEquals("TS.value was not transformed","2016-05",datetime2.getValueAsString());
        
        //simple instance test,yyyymmdd
        TS ts3=DatatypesFactory.eINSTANCE.createTS();
    	ts3.setValue("20160527");
    	DateTimeDt datetime3=dtt.TS2DateTime(ts3);
        Assert.assertEquals("TS.value was not transformed","2016-05-27",datetime3.getValueAsString());
        
        //simple instance test,yyyymmddhhmm
        TS ts4=DatatypesFactory.eINSTANCE.createTS();
    	ts4.setValue("201605271540");
    	DateTimeDt datetime4=dtt.TS2DateTime(ts4);
        Assert.assertEquals("TS.value was not transformed","2016-05-27T15:40",datetime4.getValueAsString());
        
    	//complex instance test,with timezone
    	TS ts5=DatatypesFactory.eINSTANCE.createTS();
    	ts5.setValue("201605271540-0800");
    	DateTimeDt datetime5=dtt.TS2DateTime(ts5);
        Assert.assertEquals("TS.value was not transformed","2016-05-27T15:40+08:00",datetime5.getValueAsString());
        
        //null instance test
        
        TS ts6=null;
        DateTimeDt datetime6=dtt.TS2DateTime(ts6);
        Assert.assertNull("TS null instance set was failed",datetime6);
    }//end Datetime test

    
    @Test
    public void testPQ2Quantity() {
        // simple instance test
        PQ pq = DatatypesFactory.eINSTANCE.createPQ();
        pq.setValue(120.0);
        pq.setUnit("mg");
        QuantityDt quantity = dtt.PQ2Quantity(pq);

        Assert.assertEquals("PQ.value was not transformed", 120.0, quantity.getValue().doubleValue(), 0.001);
        Assert.assertEquals("PQ.unit was not transformed", "mg", quantity.getUnit());

        // null instance test
        PQ pq2 = null;
        QuantityDt quantity2 = dtt.PQ2Quantity(pq2);
        Assert.assertNull("PQ null instance transform failed", quantity2);

        // nullFlavor instance test
        PQ pq3 = DatatypesFactory.eINSTANCE.createPQ();
        pq3.setNullFlavor(NullFlavor.NI);
        QuantityDt quantity3 = dtt.PQ2Quantity(pq3);
        Assert.assertNull("PQ.nullFlavor set instance transform failed", quantity3);
        
        PQ pq4=DatatypesFactory.eINSTANCE.createPQ();
        pq4.setValue(25.0);
        pq4.setUnit(null);
        
        QuantityDt quantity4=dtt.PQ2Quantity(pq4);
        Assert.assertEquals("PQ.value was not transformed", 25.0,quantity4.getValue().doubleValue(),0.001);
        Assert.assertNull("PQ.unit null was not transformed",quantity4.getUnit());
    }//end Quantity test
    
    @Test
    public void testII2Identifier(){
    	//simple instance test
    	II ii=DatatypesFactory.eINSTANCE.createII();
    	ii.setRoot("myIdentifierRoot");
    	ii.setExtension("myIdentifierExtension");
    	ii.setAssigningAuthorityName("Tahsin");
    	
    	IdentifierDt identifier=dtt.II2Identifier(ii);
    	Assert.assertEquals("II.root was not transformed","myIdentifierRoot",identifier.getSystem());
    	Assert.assertEquals("II.extension was not transformed","myIdentifierExtension",identifier.getValue());
    	Assert.assertEquals("II.AssigningAuthorityName was not transformed","Tahsin",identifier.getAssigner().getReference().getValue());
    	
    	//null instance test
    	
    	II ii2=null;
    	IdentifierDt identifier2=dtt.II2Identifier(ii2);
    	Assert.assertNull("II null instance was not transformed",identifier2);
    	
    	//nullFlavor instance test
    	II ii3=DatatypesFactory.eINSTANCE.createII();
    	ii3.setNullFlavor(NullFlavor.MSK);
    	IdentifierDt identifier3=dtt.II2Identifier(ii3);
    	Assert.assertNull("II nullFlavor set instance transform failed",identifier3);
    }//end IdentifierDt (from II) Test
   
    
    
    @Test
    public void testTEL2ContactPoint(){
    	TEL tel=DatatypesFactory.eINSTANCE.createTEL();
    	tel.setValue("myNumber");
    	/*Lack of setter methods */
    	ContactPointDt contactPoint=dtt.TEL2ContactPoint(tel);
    	/*TODO:Find convenient set methods to fill the attributes*/
    	
    	Assert.assertEquals("TEL.value was not transformed","myNumber",contactPoint.getValue());
    	/*Only one attribute could be tested because of the lack of setter methods */
    }//end ContactPoint test
    
    @Test
    public void testRTO2Ratio(){
    	//simple instance test
    	RTO rto=DatatypesFactory.eINSTANCE.createRTO();
    	REAL real=DatatypesFactory.eINSTANCE.createREAL();
    	real.setValue(65.0);
    	REAL real2=DatatypesFactory.eINSTANCE.createREAL();
    	real2.setValue(137.6);
    	rto.setNumerator(real);
    	rto.setDenominator(real2);
    	RatioDt ratio=dtt.RTO2Ratio(rto);
    	Assert.assertEquals("RTO.numerator was not transformed",65.0,ratio.getNumerator().getValue().doubleValue(),0.001);
    	Assert.assertEquals("RTO.denominator was not transformed",137.6,ratio.getDenominator().getValue().doubleValue(),0.001);
    	// null instance test
    	
    	RTO rto2=null;
    	RatioDt ratio2=dtt.RTO2Ratio(rto2);
    	Assert.assertNull("RTO null instance set was failed",ratio2);
    	 
    	// nullFlavor instance test
    	RTO rto3=DatatypesFactory.eINSTANCE.createRTO();
    	rto3.setNullFlavor(NullFlavor.NINF);
    	RatioDt ratio3=dtt.RTO2Ratio(rto3);
    	Assert.assertNull("RTO nullFlavor instance set was failed",ratio3);
    }//end Ratio test
    
    
    @Test
    public void testTS2Date(){
    	//simple instance test
    	TS ts=DatatypesFactory.eINSTANCE.createTS();
    	ts.setValue("2016-09-23");
    	DateDt date=dtt.TS2Date(ts);
    	
    	Assert.assertEquals("TS.value was not transformed","2016-09-23",date.getValueAsString());
    	
    	//null instance test
    	TS ts2=null;
    	DateDt date2=dtt.TS2Date(ts2);
    	Assert.assertNull("TS null was not transformed",date2);
    	
    	//nullFlavor instance test
    	TS ts3=DatatypesFactory.eINSTANCE.createTS();
    	ts3.setNullFlavor(NullFlavor.UNK);
    	DateDt date3=dtt.TS2Date(ts3);
    	Assert.assertNull("TS.nullFlavor was not transformed",date3);
    }
}

