package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.AnnotationDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

import java.util.ArrayList;
import java.util.List;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQR;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.URL;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import tr.com.srdc.cda2fhir.DataTypesTransformer;

/**
 * Created by mustafa on 7/21/2016.
 */
public class DataTypesTransformerImpl implements DataTypesTransformer {

    public CodingDt CV2Coding(CV cv) {
    	if(cv == null || cv.isSetNullFlavor()) return null;
    	else{
	    	CodingDt codingDt= new CodingDt();
	    	codingDt.setSystem(cv.getCodeSystem());
	    	codingDt.setVersion(cv.getCodeSystemVersion());
	    	codingDt.setCode(cv.getCode());
	    	codingDt.setDisplay(cv.getDisplayName());
	        return codingDt;
    	}
    }

    public CodeableConceptDt CD2CodeableConcept(CD cd) {
        if( cd == null || cd.isSetNullFlavor() ) return null;
        else{
        	List<CodingDt> myCodingDtList = new ArrayList<CodingDt>();
        	CodeableConceptDt myCodeableConceptDt = new CodeableConceptDt();
        	for(CD myCd : cd.getTranslations() ){
        		CodingDt toAdd = new CodingDt(myCd.getCodeSystem(),myCd.getCode());
        		myCodingDtList.add(toAdd);
        	}
        	return myCodeableConceptDt;
        }
    }
    
//    public Base64BinaryDt BIN2Base64Binary(BIN bin){
//    	return bin.isSetNullFlavor() ? null :  SOMETHING REASONABLE;
//    }
    
    public BooleanDt BL2Boolean(BL bl){
     	return (  bl == null || bl.isSetNullFlavor() ) ? null : new BooleanDt(bl.getValue());
    }
    public DateDt TS2Date(TS ts){
    	if( ts == null || ts.isSetNullFlavor() ) return null;
    	else {
    		DateDt resultDateDt = new DateDt();
    		int lengthOfTheDateString = ts.getValue().length();
    		String dateString = ts.getValue();
    		boolean isPrecisionSet = false;
    		
    		switch(lengthOfTheDateString){
    		/* The cases where the length of the string is 10, 7 or 4 is for the date of forms yyyy-mm-dd, yyyy-mm and yyyy
    		 * The cases will leave the switch block at case 4 (by break). 
    		 * If the date is of another form (yyyymmdd or yyyymm  ), then the following cases will apply (case 8,6) 
    		 * */ 
    		case 10: /* yyyy-mm-dd */
    			if( !isPrecisionSet ) {
    				/* If the precision hasn't set yet, set it and set isPrecisionSet to be true. */
    				resultDateDt.setPrecision(TemporalPrecisionEnum.DAY);
    				isPrecisionSet = true; 
    			}
    			resultDateDt.setDay( Integer.parseInt(dateString.substring(8,10)) );
    		case 7: /* yyyy-mm */
    			if( !isPrecisionSet ) {
    				resultDateDt.setPrecision(TemporalPrecisionEnum.MONTH);
    				isPrecisionSet = true; 
    			}
    			resultDateDt.setMonth( Integer.parseInt(dateString.substring(5,7)) );
    		case 4: /* yyyy */
    			if( !isPrecisionSet ) {
    				resultDateDt.setPrecision(TemporalPrecisionEnum.YEAR);
    				isPrecisionSet = true; 
    			}
    			resultDateDt.setYear( Integer.parseInt(dateString.substring(0,4)) );
    			break;
    		case 8: /* yyyymmdd */
    			if( !isPrecisionSet ) {
    				resultDateDt.setPrecision(TemporalPrecisionEnum.DAY);
    				isPrecisionSet = true; 
    			}
    			resultDateDt.setDay( Integer.parseInt(dateString.substring(7,9)) );
    			
    		case 6: /* yyyymm */
    			if( !isPrecisionSet ) {
    				resultDateDt.setPrecision(TemporalPrecisionEnum.MONTH);
    				isPrecisionSet = true; 
    			}
    			resultDateDt.setMonth( Integer.parseInt(dateString.substring(4,6)) );
    			/* The case where the date is of form yyyy is covered in case 4.
    			 * Therefore, we just set the year in one line for the cases yyyymm and yyyymmdd 
    			 * */
    			resultDateDt.setYear( Integer.parseInt(dateString.substring(0,4)) );
    		}
    	return resultDateDt;
    	}
    }
    
    public DecimalDt REAL2Decimal(REAL real){
    	return (real == null || real.isSetNullFlavor() ) ? null : new DecimalDt(real.getValue());
    }
    
    public StringDt ST2String(ST st){
    	return ( st == null || st.isSetNullFlavor() ) ? null : new StringDt(st.getText());
    }
    
    public UriDt URL2Uri(URL url){
    	return ( url == null || url.isSetNullFlavor() ) ? null : new UriDt(url.getValue());
    }

    public RatioDt RTO2Ratio(RTO rto){
    	if( rto == null || rto.isSetNullFlavor() ) return null;
    	else{
    		RatioDt myRatioDt = new RatioDt();
    		myRatioDt.setNumerator( PQ2Quantity( (PQ) rto.getNumerator()) );
    		myRatioDt.setDenominator( PQ2Quantity( (PQ) rto.getDenominator()) );
    		// TODO: Test requirement: Check whether casting QTY to PQ is OK
    		return myRatioDt;
    	}
    }

	public PeriodDt IVL_TS2Period(IVL_TS ivlts) {
		if( ivlts == null || ivlts.isSetNullFlavor() ) return null;
		else{
			PeriodDt periodDt =new PeriodDt();
			boolean isNullFlavorLow=ivlts.getLow().isSetNullFlavor();
			if(!isNullFlavorLow)
			{
				String date=ivlts.getLow().getValue();
				periodDt.setStart(dateParser(date));
			}
			boolean isNullFlavorHigh=ivlts.getHigh().isSetNullFlavor();
			if(!isNullFlavorHigh)
			{
				String date=ivlts.getHigh().getValue();
				periodDt.setEnd(dateParser(date));
			}
			return periodDt;
		}
		
	}
	
	public DateTimeDt TS2DateTime(TS ts){
		if(ts == null || ts.isSetNullFlavor() ) return null;
		else{
			String date=ts.getValue();
			return dateParser(date);
		}
	}
	public QuantityDt PQ2Quantity(PQ pq)
	{
		if(pq == null || pq.isSetNullFlavor() ) return null;
		else{
			QuantityDt quantityDt = new QuantityDt();
			if(pq.isNullFlavorUndefined())
			{
				quantityDt.setValue(pq.getValue());
				quantityDt.setUnit(pq.getUnit());
				for(PQR pqr : pq.getTranslations())
				{
					if(pqr!=null)
					{
						quantityDt.setSystem(pqr.getCodeSystem());
						quantityDt.setCode(pqr.getCode());
					}
					else
					{
						break;
					}
				}
			}//end if
			return quantityDt;
		}
		
	}
	
	public AnnotationDt Act2Annotation(Act act){
		if( act == null || act.isSetNullFlavor() ) return null;
		else{
			AnnotationDt myAnnotationDt = new AnnotationDt();
			for(Participant2 theParticipant : act.getParticipants()){
				if(theParticipant.getTypeCode() == ParticipationType.AUT){
					//TODO: Annotation.author[x]
					// Type	Reference(Practitioner | Patient | RelatedPerson)|string
					// For now, we are getting the name of the participant as a string
					if (theParticipant.getRole().getPlayer() instanceof Person) {
						Person person = (Person)theParticipant.getRole().getPlayer();
						myAnnotationDt.setAuthor( new StringDt(person.getNames().get(0).getText()) );
					} 
					myAnnotationDt.setTime( IVL_TS2Period(act.getEffectiveTime()).getStartElement() );
					//TODO: While setTime is waiting a parameter as DateTime, act.effectiveTime gives output as IVL_TS (Interval)
					//In sample XML, it gets the effective time as the low time
					//Check if it is ok
					
					myAnnotationDt.setText(act.getText().toString());
				}
			}
			return myAnnotationDt;
		}
	}
	
	
	public RangeDt IVL_PQ2Range(IVL_PQ ivlpq){
		if( ivlpq == null || ivlpq.isSetNullFlavor() ) return null;
		else{
			RangeDt rangeDt = new RangeDt();
			if(ivlpq.getLow()==null && ivlpq.getHigh()==null)
			{
				return null;
			}
			else
			{
				if(ivlpq.getLow()!=null)
				{
					QuantityDt quantityDt = new QuantityDt();
					quantityDt.setValue(ivlpq.getLow().getValue());
					quantityDt.setUnit(ivlpq.getLow().getUnit());
					rangeDt.setLow((SimpleQuantityDt)quantityDt);
				}
				if(ivlpq.getHigh()!=null)
				{
					QuantityDt quantityDt = new QuantityDt();
					quantityDt.setValue(ivlpq.getHigh().getValue());
					quantityDt.setUnit(ivlpq.getHigh().getUnit());
					rangeDt.setHigh((SimpleQuantityDt)quantityDt);
				}
				return rangeDt;
			}
		}
	}
    
	private DateTimeDt dateParser(String date)
	{
		DateTimeDt dateTimeDt = new DateTimeDt();
		boolean isPrecisionSet=false;
		switch(date.length())
		{	
			default:
				if(date.length()>14)
				{
					int x=date.length();
					String ms=date.substring(14,x);
					int msInt=Integer.parseInt(ms);
					dateTimeDt.setMinute(msInt);
					if(!isPrecisionSet)
					{
						dateTimeDt.setPrecision(TemporalPrecisionEnum.MILLI);
						isPrecisionSet=true;
					}
				}//end if
				else
				{
					//do nothing
					break;
				}
			case 14:
				String second=date.substring(12,14);
				int secondInt=Integer.parseInt(second);
				dateTimeDt.setMinute(secondInt);
				if(!isPrecisionSet)
				{
					dateTimeDt.setPrecision(TemporalPrecisionEnum.SECOND);
					isPrecisionSet=true;
				}
			case 12:
				String minute=date.substring(10,12);
				int minuteInt=Integer.parseInt(minute);
				dateTimeDt.setMinute(minuteInt);
				if(!isPrecisionSet)
				{
					dateTimeDt.setPrecision(TemporalPrecisionEnum.MINUTE);
					isPrecisionSet=true;
				}
			case 10:
				String hour=date.substring(8,10);
				int hourInt=Integer.parseInt(hour);
				dateTimeDt.setHour(hourInt);
			case 8:
				String day=date.substring(6,8);
				int dayInt=Integer.parseInt(day);
				dateTimeDt.setDay(dayInt);
			case 6:
				String month=date.substring(4,6);
				int monthInt=Integer.parseInt(month);
				dateTimeDt.setMonth(monthInt);
				
			case 4:
				String year=date.substring(0,4);
				int yearInt=Integer.parseInt(year);
				dateTimeDt.setYear(yearInt);
		}//end switch
		return dateTimeDt;
	}
    
}
