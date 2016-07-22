package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;

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
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQR;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
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
    	CodeableConceptDt codeableConceptDt = new CodeableConceptDt();
    	
    	return null;
    }

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
