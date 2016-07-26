package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
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
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.BIN;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQR;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
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
	    	// Mapping from Coding.userSelected to CD.codingRationale doesn't exist
	        return codingDt;
    	}
    }

    public CodeableConceptDt CD2CodeableConcept(CD cd) {
        if( cd == null || cd.isSetNullFlavor() ) return null;
        else{
        	List<CodingDt> myCodingDtList = new ArrayList<CodingDt>();
        	CodeableConceptDt myCodeableConceptDt = new CodeableConceptDt();
        	for(CD myCd : cd.getTranslations() ){
        		if(myCd.getCodeSystem().isEmpty() || myCd.getCode().isEmpty()) continue;
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
    		case 0: /* lenght of the date string is zero. we need to return null */
    			return null;
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
    public IntegerDt INT2Integer(INT myInt){
    	return (myInt == null || myInt.isSetNullFlavor() ) ? null : new IntegerDt(myInt.getValue().toString());
    }
    public Base64BinaryDt BIN2Base64Binary(BIN bin){
    	
    	if(bin.getRepresentation().getLiteral()!=null)
    	{
    		Base64BinaryDt base64BinaryDt = new Base64BinaryDt();
        	base64BinaryDt.setValue(bin.getRepresentation().getLiteral().getBytes());
        	return base64BinaryDt;
    	}
    	else
    	{
    		return null;
    	}
    	
    }

    public RatioDt RTO2Ratio(RTO rto){
    	if( rto == null || rto.isSetNullFlavor() ) return null;
    	else{
    		RatioDt myRatioDt = new RatioDt();
    		if( ! rto.getNumerator().isSetNullFlavor() ) {
    			// TODO: Test requirement: Check whether casting QTY to PQ is OK
    			myRatioDt.setNumerator( PQ2Quantity( (PQ) rto.getNumerator()) );
    		}
    		if( !rto.getDenominator().isSetNullFlavor() ){
    			myRatioDt.setDenominator( PQ2Quantity( (PQ) rto.getDenominator()) );
    		}
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
			if(pq.isSetNullFlavor())
			{
				if( pq.getValue() != null)
					quantityDt.setValue(pq.getValue());
				if( pq.getUnit() != null && !pq.getUnit().isEmpty())
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
						if( !person.getNames().get(0).getText().isEmpty() ){
							myAnnotationDt.setAuthor( new StringDt(person.getNames().get(0).getText()) );
						}
					}
					myAnnotationDt.setTime( IVL_TS2Period(act.getEffectiveTime()).getStartElement() );
					//TODO: While setTime is waiting a parameter as DateTime, act.effectiveTime gives output as IVL_TS (Interval)
					//In sample XML, it gets the effective time as the low time
					//Check if it is ok
					if( !act.getText().isSetNullFlavor() && !act.getText().toString().isEmpty() )
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
				if(ivlpq.getLow()!=null || !ivlpq.isSetNullFlavor())
				{
					SimpleQuantityDt simpleQuantity=new SimpleQuantityDt();
					simpleQuantity.setValue(ivlpq.getLow().getValue().doubleValue());
					rangeDt.setLow(simpleQuantity);
				}
				if(ivlpq.getHigh()!=null || !ivlpq.isSetNullFlavor())
				{
					SimpleQuantityDt simpleQuantity=new SimpleQuantityDt();
					simpleQuantity.setValue(ivlpq.getHigh().getValue().doubleValue());
					rangeDt.setHigh(simpleQuantity);
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
				dateTimeDt.setPrecision(TemporalPrecisionEnum.DAY);
			case 6:
				String month=date.substring(4,6);
				int monthInt=Integer.parseInt(month);
				dateTimeDt.setMonth(monthInt);
				dateTimeDt.setPrecision(TemporalPrecisionEnum.MONTH);
				
			case 4:
				String year=date.substring(0,4);
				int yearInt=Integer.parseInt(year);
				dateTimeDt.setYear(yearInt);
				dateTimeDt.setPrecision(TemporalPrecisionEnum.YEAR);
		}//end switch
		return dateTimeDt;
	}
    
	public HumanNameDt EN2HumanName(EN en) {
		
		if( en != null && !en.isSetNullFlavor()){
			
			HumanNameDt myHumanName = new HumanNameDt();
			
			if( en.getText() != null){
				myHumanName.setText( en.getText() );
			}
			
			if(en.getUses() != null ){
				myHumanName.setUse(NameUseEnum.valueOf( en.getUses().get(0).toString() ));
			}
			
			if(en.getFamilies() != null){
				for(ENXP element: en.getFamilies()){
					myHumanName.addFamily( element.getPartType().toString() );
				}
			}
			if(en.getGivens() != null){
				for(ENXP element: en.getGivens()){
					myHumanName.addGiven( element.getPartType().toString() );
				}
			}
			if(en.getPrefixes() != null){
				for(ENXP element: en.getPrefixes( )){
					myHumanName.addPrefix( element.getPartType().toString() );
				}
			}
			if(en.getSuffixes() != null){
					for(ENXP element: en.getSuffixes()){
					myHumanName.addSuffix( element.getPartType().toString() );
				}
			}
			
			if( en.getValidTime() != null ){
				PeriodDt periodDt = IVL_TS2Period( en.getValidTime() );
				myHumanName.setPeriod(periodDt);
			}
			
			return myHumanName;
						
		}
						
	return null;
	
	}

	public IdentifierDt II2Identifier(II ii) {
		
		if( ii != null  && !ii.isSetNullFlavor()){
			
			IdentifierDt identifierDt = new IdentifierDt();
			
			if(ii.getRoot() != null){
				if( !ii.getRoot().isEmpty() )
					identifierDt.setSystem( ii.getRoot() );
			}
			
			if(ii.getExtension() != null){
				if( !ii.getExtension().isEmpty() )
					identifierDt.setValue( ii.getExtension() );
			}
			
			if( ii.getAssigningAuthorityName() != null){
				ResourceReferenceDt resourceReference = new ResourceReferenceDt( ii.getAssigningAuthorityName() );
				if( !resourceReference.isEmpty() )
					identifierDt.setAssigner( resourceReference );
			}
			
			// TODO : Use, Type and Period attributes will be handled after the data types are finished.
			
			return identifierDt;

		}
		return null;

	}

	public ContactPointDt TEL2ContactPoint(TEL tel) {
		
		if( tel!=null && !tel.isSetNullFlavor()){
			
			ContactPointDt contactPointDt = new ContactPointDt();
			
			if(tel.getValue() != null ){
				contactPointDt.setValue( tel.getValue() );
			}
			
			PeriodDt period = new PeriodDt();
			tel.getUseablePeriods().get(0).getValue();
			DateTimeDt dateTime = new DateTimeDt();
			dateTime.setValueAsString(tel.getUseablePeriods().get(0).getValue());

			//dateTime.setValue(new Date());
			period.setStart(dateTime);
			contactPointDt.setPeriod(period);
			
			contactPointDt.setRank(1);
			contactPointDt.setSystem(ContactPointSystemEnum.PHONE);
			
			if(tel.getUses() != null){
				
				if(tel.getUses().get(0).toString().equals("HP")){
					contactPointDt.setUse(ContactPointUseEnum.HOME);
				}
				else if(tel.getUses().get(0).toString().equals("WP")){
					contactPointDt.setUse(ContactPointUseEnum.WORK);
					
				}else if(tel.getUses().get(0).toString().equals("HV")){
					contactPointDt.setUse(ContactPointUseEnum.TEMP);
					
				}else{
					contactPointDt.setUse(ContactPointUseEnum.MOBILE);
				}	
			}
			
			return contactPointDt;
		}
		
		return null;
	}
	public AttachmentDt ED2Attachment(ED ed) {
		if(ed==null || ed.isSetNullFlavor())
		{
			return null;
		}
		else
		{
			AttachmentDt attachmentDt = new AttachmentDt();
			if(ed.isSetMediaType() && ed.getMediaType()!=null)
			{
				attachmentDt.setContentType(ed.getMediaType());
			}
			if(ed.getLanguage()!=null)
			{
				attachmentDt.setLanguage(ed.getLanguage());
			}
			if(ed.isSetRepresentation() && ed.getRepresentation().getLiteral()!=null)
			{
				attachmentDt.setData( ed.getText().getBytes() );
//				Base64BinaryDt base64BinaryDt = new Base64BinaryDt();
//				base64BinaryDt.setValue(ed.getRepresentation().getLiteral().getBytes());
//				attachmentDt.setData(base64BinaryDt);
			}
			if(ed.getReference().getValue()!=null)
			{
				attachmentDt.setUrl(ed.getReference().getValue());
			}
			if(ed.getIntegrityCheck()!=null)
			{
				attachmentDt.setHash(ed.getIntegrityCheck());
			}
			if(ed.isSetRepresentation() && ed.getRepresentation().getName()!=null)//If this contains a title.
			{
				// TODO: Not sure if ed.title.data is compensated by ed.getRepresentation().getName(). Please check.
				attachmentDt.setTitle(ed.getRepresentation().getName());
			}
			return attachmentDt;
		}
	}//end attachmentDt
    
}

