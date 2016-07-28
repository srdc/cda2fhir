package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.AnnotationDt;
import ca.uhn.fhir.model.dstu2.composite.AttachmentDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
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
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.BIN;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.BN;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
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
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.rim.RIMFactory;
import org.openhealthtools.mdht.uml.hl7.rim.Role;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by mustafa on 7/25/2016.
 */
public class DataTypesTransformerTest{

    //private static final EDataType QTY = null;
	DataTypesTransformer dtt = new DataTypesTransformerImpl();

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
    public void testAD2Address(){

    	// simple instance test
    	
    	AD ad = DatatypesFactory.eINSTANCE.createAD();
    	// Visit https://www.hl7.org/fhir/valueset-address-use.html to see valuset of address use
    	ad.getUses().add(PostalAddressUse.H); // PostalAddressUse.H maps to home
    	ad.getUses().add(PostalAddressUse.PST); // PST maps to physical
    	ad.addText("theText");
    	String[] lineArray = new String[2];
    	lineArray[0] = "streetLine";
    	lineArray[1] = "deliveryLine";
    	ad.addStreetAddressLine(lineArray[0]);
    	ad.addDeliveryAddressLine(lineArray[1]);
    	ad.addCity("theCity");
    	ad.addCounty("theDistrict"); // Notice that it is county, not country
    	ad.addState("theState");
    	ad.addPostalCode("thePostalCode");
    	ad.addCountry("theCountry");

    	SXCM_TS start = DatatypesFactory.eINSTANCE.createSXCM_TS();
    	SXCM_TS end  = DatatypesFactory.eINSTANCE.createSXCM_TS();
    	start.setValue("1963-05-16");
    	end.setValue("2013-07-21");
    	ad.getUseablePeriods().add(start);
    	ad.getUseablePeriods().add(end);
    	
    	AddressDt address = dtt.AD2Address(ad);
    	
    	Assert.assertEquals("AD.use was not transformed","home",address.getUse());
    	Assert.assertEquals("AD.type was not transformed","physical",address.getType());
    	Assert.assertEquals("AD.text was not transformed","theText",address.getText());
    	
    	/* line array controls */
    	int matchingElements = 0;
    	for( StringDt line : address.getLine() ){
    		for( String line2 : lineArray ){
    			if(line.getValue().equals(line2)){
    				matchingElements++;
    			}
    		}
    	}
    	Assert.assertTrue("AD.line was not transformed",matchingElements == lineArray.length);
    	
    	Assert.assertEquals("AD.city was not transformed","theCity",address.getCity());
    	Assert.assertEquals("AD.district was not transformed","theDistrict",address.getDistrict());
    	Assert.assertEquals("AD.state was not transformed","theState",address.getState());
    	Assert.assertEquals("AD.postalCode was not transformed","thePostalCode",address.getPostalCode());
    	Assert.assertEquals("AD.country was not transformed","theCountry",address.getCountry());
    	
    	// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016 since 2016-1900 = 116.
    	Assert.assertEquals("AD.period.start.year was not transformed",1963-1900,address.getPeriod().getStart().getYear());
    	// Notice that Date.getMonth() returns THE_MONTH - 1 (since the months are indexed btw the range 0-11)
    	Assert.assertEquals("AD.period.start.month was not transformed",5-1,address.getPeriod().getStart().getMonth());
    	Assert.assertEquals("AD.period.start.date was not transformed",16,address.getPeriod().getStart().getDate());
    	
    	Assert.assertEquals("AD.period.end.year was not transformed",2013-1900,address.getPeriod().getEnd().getYear());
    	Assert.assertEquals("AD.period.end.month was not transformed",7-1,address.getPeriod().getEnd().getMonth());
    	Assert.assertEquals("AD.period.end.date was not transformed",21,address.getPeriod().getEnd().getDate());
    	
    	
    	// instance test: there exists an instance of ED but no setter is called
    	AD ad4 = DatatypesFactory.eINSTANCE.createAD();
    	AddressDt address4 = dtt.AD2Address(ad4);
    	Assert.assertNull("AD.use was not transformed",address4.getUse());
    	Assert.assertNull("AD.type was not transformed",address4.getType());
    	Assert.assertNull("AD.text was not transformed",address4.getText());
    	
    	Assert.assertTrue("AD.line was not transformed",address4.getLine().size() == 0);
    	Assert.assertNull("AD.city was not transformed",address4.getCity());
    	Assert.assertNull("AD.district was not transformed",address4.getDistrict());
    	Assert.assertNull("AD.state was not transformed",address4.getState());
    	Assert.assertNull("AD.postalCode was not transformed",address4.getPostalCode());
    	Assert.assertNull("AD.country was not transformed",address4.getCountry());
    	
    	// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016 since 2016-1900 = 116.
    	Assert.assertNull("AD.period.start was not transformed",address4.getPeriod().getStart());
    	
    	Assert.assertNull("AD.period.end was not transformed",address4.getPeriod().getEnd());
    	
    	
    	
    	
    	// null instance test
    	AD ad2 = null;
    	AddressDt address2 = dtt.AD2Address(ad2);
    	Assert.assertNull("AD null instance transform failed", address2);
    	
    	// nullFlavor instance test
    	AD ad3 = DatatypesFactory.eINSTANCE.createAD();
    	ad3.setNullFlavor(NullFlavor.NI);
    	AddressDt address3 = dtt.AD2Address(ad3);
    	Assert.assertNull("AD.nullFlavor set instance transform failed",address3);
    }
    
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
   
    
    @Test
    public void testIVL_TS2Period(){
    	// simple instance test 1
    	
    	IVL_TS ivl_ts = DatatypesFactory.eINSTANCE.createIVL_TS();
    	
    	IVXB_TS ivxb_tsLow = DatatypesFactory.eINSTANCE.createIVXB_TS();
    	IVXB_TS ivxb_tsHigh = DatatypesFactory.eINSTANCE.createIVXB_TS();
    	
    	ivxb_tsLow.setValue("19630116");
    	ivxb_tsHigh.setValue("20151122");
    	
    	ivl_ts.setLow(ivxb_tsLow);
    	ivl_ts.setHigh(ivxb_tsHigh);
    	
    	PeriodDt period = dtt.IVL_TS2Period(ivl_ts);
    	
    	// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016 since 2016-1900 = 116.
    	Assert.assertEquals("IVL_TS.low(year) was not transformed",1963-1900,period.getStart().getYear());
    	// Notice that Date.getMonth() returns THE_MONTH - 1 (since the months are indexed btw the range 0-11)
    	Assert.assertEquals("IVL_TS.low(month) was not transformed",1-1,period.getStart().getMonth());
    	Assert.assertEquals("IVL_TS.low(date[1-31]) was not transformed",16,period.getStart().getDate());
    	
    	Assert.assertEquals("IVL_TS.high(year) was not transformed",2015-1900,period.getEnd().getYear());
    	Assert.assertEquals("IVL_TS.high(month) was not transformed",11-1,period.getEnd().getMonth());
    	Assert.assertEquals("IVL_TS.high(date[1-31]) was not transformed",22,period.getEnd().getDate());
    	
    	
    	// instance test: there exists an instance of ED but no setter is called
    	IVL_TS ivl_ts4 = DatatypesFactory.eINSTANCE.createIVL_TS();
    	PeriodDt period4 = dtt.IVL_TS2Period(ivl_ts4);
    	
    	Assert.assertNull("IVL_TS.low was not transformed",period4.getStart());
    	
    	Assert.assertNull("IVL_TS.high(year) was not transformed",period4.getEnd());
    	
    	
    	// null instance test
    	IVL_TS ivl_ts2 = null;
    	PeriodDt period2 = dtt.IVL_TS2Period(ivl_ts2);
    	Assert.assertNull("IVL_TS null instance transform failed", period2);
    	
    	// nullFlavor instance test
    	IVL_TS ivl_ts3 = DatatypesFactory.eINSTANCE.createIVL_TS();
    	ivl_ts3.setNullFlavor(NullFlavor.NI);
    	PeriodDt period3 = dtt.IVL_TS2Period(ivl_ts3);
    	Assert.assertNull("IVL_TS.nullFlavor set instance transform failed",period3);
    	
    	
    }
    
    @Test
    public void testEN2HumanName(){
    	// simple instance test 1
    	
    	EN en = DatatypesFactory.eINSTANCE.createEN();

    	// Notice that EntityNameUse.P maps to NameUseEnum.NICKNAME.
    	// For further info, visit https://www.hl7.org/fhir/valueset-name-use.html
    	en.getUses().add(EntityNameUse.P);
    	en.addText("theText");
    	en.addFamily("theFamily");
    	en.addGiven("theGiven");
    	en.addPrefix("thePrefix");
    	en.addSuffix("theSuffix");
    	
    	// Data for ivl_ts:  low: 19950127, high: 20160228
    	IVL_TS ivl_ts = DatatypesFactory.eINSTANCE.createIVL_TS("19950115","20160228");
    	en.setValidTime(ivl_ts);
    	
    	HumanNameDt humanName = dtt.EN2HumanName(en);
    	
    	Assert.assertEquals("EN.use was not transformed","nickname",humanName.getUse());
    	Assert.assertEquals("EN.text was not transformed","theText",humanName.getText());
    	Assert.assertEquals("EN.family was not transformed","theFamily",humanName.getFamily().get(0).getValue());
    	Assert.assertEquals("EN.given was not transformed","theGiven",humanName.getGiven().get(0).getValue());
    	Assert.assertEquals("EN.prefix was not transformed","thePrefix",humanName.getPrefix().get(0).getValue());
    	Assert.assertEquals("EN.suffix was not transformed","theSuffix",humanName.getSuffix().get(0).getValue());
    	
    	// EN.period tests for the simple instance test 1
    	
    	PeriodDt en_period = dtt.IVL_TS2Period(ivl_ts);
    	Assert.assertEquals("EN.period(low) was not transformed",en_period.getStart(),humanName.getPeriod().getStart());
    	Assert.assertEquals("EN.period(high) was not transformed",en_period.getEnd(),humanName.getPeriod().getEnd());
    	
    	// instance test: there exists an instance of ED but no setter is called
    	EN en4 = DatatypesFactory.eINSTANCE.createEN();
    	HumanNameDt humanName4 = dtt.EN2HumanName(en4);
    	Assert.assertNull("EN.use was not transformed",humanName4.getUse());
    	Assert.assertNull("EN.text was not transformed",humanName4.getText());
    	Assert.assertTrue("EN.family was not transformed",humanName4.getFamily().size() == 0);
    	Assert.assertTrue("EN.given was not transformed",humanName4.getGiven().size() == 0);
    	Assert.assertTrue("EN.prefix was not transformed",humanName4.getPrefix().size() == 0);
    	Assert.assertTrue("EN.suffix was not transformed",humanName4.getSuffix().size() == 0);
    	
    	// null instance test
    	EN en2 = null;
    	HumanNameDt humanName2 = dtt.EN2HumanName(en2);
    	Assert.assertNull("ED null instance transform failed", humanName2);
    	
    	
    	// nullFlavor instance test
    	EN en3 = DatatypesFactory.eINSTANCE.createEN();
    	en3.setNullFlavor(NullFlavor.NI);
    	HumanNameDt humanName3 = dtt.EN2HumanName(en3);
    	Assert.assertNull("EN.nullFlavor set instance transform failed",humanName3);
    }
    
    @Test
    public void testED2Attachment(){
    	// simple instance test
    	
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
    	Assert.assertArrayEquals("ED.data was not transformed","theData".getBytes(),attachment.getData());
    	Assert.assertEquals("ED.reference.literal was not transformed","theUrl",attachment.getUrl());
    	Assert.assertArrayEquals("ED.integrityCheck was not transformed","theIntegrityCheck".getBytes(),attachment.getHash());
    	
    	// instance test: there exists an instance of ED but no setter is called
    	ED ed4 = DatatypesFactory.eINSTANCE.createED();	
    	
    	AttachmentDt attachment4 = dtt.ED2Attachment(ed4);
    	Assert.assertNull("ED.mediaType was not transformed",attachment4.getContentType());
    	Assert.assertNull("ED.language was not transformed",attachment4.getLanguage());
    	Assert.assertNull("ED.data was not transformed",attachment4.getData());
    	Assert.assertNull("ED.reference.literal was not transformed",attachment4.getUrl());
    	Assert.assertNull("ED.integrityCheck was not transformed",attachment4.getHash());
    	
    	
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
    	
    	CodingDt coding = dtt.CV2Coding(cv);
    	
    	Assert.assertEquals("CV.codeSystem was not transformed","theCodeSystem",coding.getSystem());
    	Assert.assertEquals("CV.codeSystemVersion was not transformed","theCodeSystemVersion",coding.getVersion());
    	Assert.assertEquals("CV.code was not transformed","theCode",coding.getCode());
    	Assert.assertEquals("CV.displayName was not transformed","theDisplayName",coding.getDisplay());
    	
    	// instance test: there exists an instance of CV but no setter is called
    	CV cv4 = DatatypesFactory.eINSTANCE.createCV();
    	
    	CodingDt coding4 = dtt.CV2Coding(cv4);
    	
    	Assert.assertNull("CV.codeSystem null value was not transformed properly",coding4.getSystem());
    	Assert.assertNull("CV.codeSystemVersion null value was not transformed properly",coding4.getVersion());
    	Assert.assertNull("CV.code null value was not transformed properly",coding4.getCode());
    	Assert.assertNull("CV.displayName null value was not transformed properly",coding4.getDisplay());
    	
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
        
        
        IVL_PQ ivlpq5 = DatatypesFactory.eINSTANCE.createIVL_PQ();
        IVXB_PQ ivxbpqH_2 = DatatypesFactory.eINSTANCE.createIVXB_PQ();
        ivxbpqH_2.setNullFlavor(NullFlavor.NI);
        ivlpq5.setHigh(ivxbpqH_2);
        
        RangeDt range5 = dtt.IVL_PQ2Range( ivlpq5 );
        Assert.assertNull("IVL_PQ.nullFlavor set instance transform failed", range5.getHigh());
        
        
       //non-null empty instance test
        IVL_PQ ivlpq4 = DatatypesFactory.eINSTANCE.createIVL_PQ();
        RangeDt range4 = dtt.IVL_PQ2Range( ivlpq4 );
        Assert.assertNull("IVL_PQ.high.value transform failed", range4.getHigh().getValue() );
        Assert.assertNull("IVL_PQ.low.value transform failed", range4.getLow().getValue() );
        Assert.assertNull("IVL_PQ.high.unit transform failed", range4.getHigh().getUnit() );
        Assert.assertNull("IVL_PQ.low.unit transform failed", range4.getLow().getUnit() );
        
    }

   
    
    
    @Ignore
    public void testCD2CodeableConcept(){
        
        CD cd = DatatypesFactory.eINSTANCE.createCD();
        
        cd.setCode("code");
        cd.setCodeSystem("codeSystem");
        cd.setCodeSystemVersion("codeSystemVersion");
        cd.setDisplayName("displayName");
  
        CodeableConceptDt codeableConcept = dtt.CD2CodeableConcept(cd);
        
        Assert.assertEquals("CD.code transformation failed", "code", codeableConcept.getCoding().get(0).getCode());
        Assert.assertEquals("CD.codeSystem transformation failed", "codeSystem", codeableConcept.getCoding().get(0).getSystem());
        Assert.assertEquals("CD.codeSystemVersion transformation failed", "codeSystemVersion", codeableConcept.getCoding().get(0).getVersion());
        Assert.assertEquals("CD.displayName transformation failed", "displayName", codeableConcept.getCoding().get(0).getDisplay());
        
        // null instance test
        CD cd2 = null;
        CodeableConceptDt codeableConcept2 = dtt.CD2CodeableConcept( cd2 );
        Assert.assertNull("CD null instance transform failed", codeableConcept2);
        
      // nullFlavor instance test
        CD cd3 = DatatypesFactory.eINSTANCE.createCD();
        cd3.setNullFlavor(NullFlavor.NI);
        CodeableConceptDt codeableConcept3 = dtt.CD2CodeableConcept( cd3 );
        Assert.assertNull("CodeableConcept.nullFlavor set instance transform failed", codeableConcept3);
        
       //instance test: non-null but empty instance
        CD cd4 = DatatypesFactory.eINSTANCE.createCD();
        CodeableConceptDt codeableConcept4 = dtt.CD2CodeableConcept( cd4 );
        
        Assert.assertTrue("CD.code transformation failed ", codeableConcept4.getCoding().size() == 0 );
        
    }
    @SuppressWarnings("deprecation")
   	@Ignore
       public void testTEL2ContactPoint(){
       	
       	TEL tel = DatatypesFactory.eINSTANCE.createTEL();
       	
       	tel.setValue("value");
       	
       	SXCM_TS sxcmts = DatatypesFactory.eINSTANCE.createSXCM_TS();
       	sxcmts.setValue("1995-04-24");
       	SXCM_TS sxcmts2 = DatatypesFactory.eINSTANCE.createSXCM_TS();
       	sxcmts2.setValue("1995-04-27");
       	
       	tel.getUseablePeriods().add(sxcmts);
       	tel.getUseablePeriods().add(sxcmts2);
       	
       	tel.getUses().add(TelecommunicationAddressUse.H);
       	
       	ContactPointDt contactPoint = dtt.TEL2ContactPoint(tel);
       	
       	
       	contactPoint.setSystem(ContactPointSystemEnum.PHONE);
       	
       	Assert.assertEquals("Tel.value failed" , "value" , contactPoint.getValue()  );
       	Assert.assertEquals("Tel.periodStart getYear failed" , 95 , contactPoint.getPeriod().getStart().getYear() );
       	Assert.assertEquals("Tel.periodStart getMonth failed" , 3 , contactPoint.getPeriod().getStart().getMonth() );
       	Assert.assertEquals("Tel.periodStart getMonth failed" , 24 , contactPoint.getPeriod().getStart().getDate() );
       	Assert.assertEquals("Tel.periodEnd getYear failed" , 95 , contactPoint.getPeriod().getEnd().getYear() );
       	Assert.assertEquals("Tel.periodEnd getMonth failed" , 3 , contactPoint.getPeriod().getEnd().getMonth() );
       	Assert.assertEquals("Tel.periodEnd getMonth failed" , 27 , contactPoint.getPeriod().getEnd().getDate() );
       	Assert.assertEquals("Tel.use failed" , "home" , contactPoint.getUse() );
       	
       	 // null instance test
           TEL tel2 = null;
           ContactPointDt contactPoint2 = dtt.TEL2ContactPoint( tel2 );
           Assert.assertNull("TEL null instance transform failed", contactPoint2);
           
         // nullFlavor instance test
           TEL tel3 = DatatypesFactory.eINSTANCE.createTEL();
           tel3.setNullFlavor(NullFlavor.NI);
           ContactPointDt contactPoint3 = dtt.TEL2ContactPoint( tel3 );
           Assert.assertNull("ContactPointDt.nullFlavor set instance transform failed", contactPoint3);
       	
           
           //instance test: non-null empty instance
           TEL tel4 = DatatypesFactory.eINSTANCE.createTEL();
           ContactPointDt contactPoint4 = dtt.TEL2ContactPoint( tel4 );
           
           Assert.assertNull("TEL.value transformation failed", contactPoint4.getValue());
           Assert.assertNull( "TEL.period.Start transformation failed", contactPoint4.getPeriod().getStart());
           Assert.assertNull( "TEL.period.End transformation failed", contactPoint4.getPeriod().getEnd());
          
          
       	
       }
}

