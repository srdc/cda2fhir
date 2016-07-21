package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
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
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.SetOperator;

import tr.com.srdc.cda2fhir.DataTypesTransformer;

/**
 * Created by mustafa on 7/21/2016.
 */
public class DataTypesTransformerImpl implements DataTypesTransformer {

    public CodingDt CV2Coding(CV cv) {
        return null;
    }

    public CodeableConceptDt CD2CodeableConcept(CD cd) {
        return null;
    }

	public PeriodDt IVL_TS2Period(IVL_TS ivlts) {
		
		PeriodDt periodDt =new PeriodDt();
		String date=ivlts.getLow().getValue();
		periodDt.setStart(dateParser(date));
		
		date=ivlts.getHigh().getValue();
		periodDt.setEnd(dateParser(date)); 
		return periodDt;
		
	}
	
	public DateTimeDt TS2DateTime(TS ts){
			DateTimeDt dateTimeDt =new DateTimeDt();
			boolean isNull=ts.isSetNullFlavor();
			if(!isNull)
			{
				String date=ts.getValue();
				return dateParser(date);
			}
			else
			{
				//NullFlavor should be set.
				return null;
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
