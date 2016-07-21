package tr.com.srdc.cda2fhir.impl;

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
		DateTimeDt dateTimeDt = new DateTimeDt();
		String date=ts.getValue();
		return dateParser(date);
	}
    
	public DateTimeDt dateParser(String date)
	{
		DateTimeDt dateTimeDt = new DateTimeDt();
		if(date.length()==8)
		{
			String year=date.substring(0,4);
			String month=date.substring(4,6);
			String day=date.substring(6,8);
			int yearInt=Integer.parseInt(year);
			int monthInt=Integer.parseInt(month);
			int dayInt=Integer.parseInt(day);
			dateTimeDt.setYear(yearInt);
			dateTimeDt.setMonth(monthInt);
			dateTimeDt.setDay(dayInt);
			return dateTimeDt;
		}
		else
		{
			return null;
		}
	}
    
}
