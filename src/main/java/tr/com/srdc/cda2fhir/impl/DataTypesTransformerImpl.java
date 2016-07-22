package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xml.type.SimpleAnyType;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQR;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.URL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.SetOperator;

import tr.com.srdc.cda2fhir.DataTypesTransformer;

/**
 * Created by mustafa on 7/21/2016.
 */
public class DataTypesTransformerImpl implements DataTypesTransformer {

    public CodingDt CV2Coding(CV cv) {
    	CodingDt codingDt= new CodingDt();
    	codingDt.setSystem(cv.getCodeSystem());
    	codingDt.setVersion(cv.getCodeSystemVersion());
    	codingDt.setCode(cv.getCode());
    	codingDt.setDisplay(cv.getDisplayName());
        return codingDt;
    }

    public CodeableConceptDt CD2CodeableConcept(CD cd) {
        return null;
    }
    
//    public Base64BinaryDt BIN2Base64Binary(BIN bin){
//    	return bin.isSetNullFlavor() ? null :  SOMETHING REASONABLE;
//    }
    
    public BooleanDt BL2Boolean(BL bl){
     	return bl.isSetNullFlavor() ? null : new BooleanDt(bl.getValue());
    }
    public DateDt TS2Date(TS ts){
    	if( ts.isSetNullFlavor() ) return null;
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
    	return real.isSetNullFlavor() ? null : new DecimalDt(real.getValue());
    }
    
    public StringDt ST2String(ST st){
    	return st.isSetNullFlavor() ? null : new StringDt(st.getText());
    }
    
    public UriDt URL2Uri(URL url){
    	return url.isSetNullFlavor() ? null : new UriDt(url.getValue());
    }
    /* QuantityDt is required. 
     * When ready, please fill in the gaps: <<QuantityDt>> */
//    public RatioDt RTO2Ratio(RTO rto){
//    	if( rto.isNullFlavorDefined() ) return null;
//    	else{
//    		RatioDt myRatioDt = new RatioDt();
//    		myRatioDt.setNumerator( <<QuantityDt>> );
//    		myRatioDt.setDenominator( <<QuantityDt>> );
//    		return myRatioDt;
//    	}
//    }

	public PeriodDt IVL_TS2Period(IVL_TS ivlts) {
		
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
	
	public DateTimeDt TS2DateTime(TS ts){
			DateTimeDt dateTimeDt =new DateTimeDt();
			boolean isNullFlavor=ts.isSetNullFlavor();
			if(!isNullFlavor)
			{
				String date=ts.getValue();
				return dateParser(date);
			}
			else
			{
				return null;
			}
	}
	public QuantityDt PQ2Quantity(PQ pq)
	{
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
	public RangeDt IVL_PQ2Range(IVL_PQ ivlpq){
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
    
	public DateTimeDt dateParser(String date)
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
