package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.BaseDateTimeDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import java.util.TimeZone;

import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;

import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

/**
 * Created by mustafa on 7/21/2016.
 */
public class DataTypesTransformerImpl implements DataTypesTransformer {
	ValueSetsTransformer vst = new ValueSetsTransformerImpl();
	public AddressDt AD2Address(AD ad) {
	    
	    if(ad == null || ad.isSetNullFlavor()) return null;
	    else{
	        
	        AddressDt address = new AddressDt();
	        
	        if( !ad.getUses().isEmpty() && ad.getUses() != null ){
	        	
	        	
	        	
	        	// We get the address.type and address.use from the list ad.uses
	        	for(PostalAddressUse postalAddressUse : ad.getUses()){
	        		// If we catch a valid value for type or use, we assign it
	        		if( postalAddressUse == PostalAddressUse.PHYS || postalAddressUse == PostalAddressUse.PST ){
	        			address.setType( vst.PostalAddressUse2AddressTypeEnum( postalAddressUse ) );
	        		} else if( postalAddressUse == PostalAddressUse.H ||
	        				postalAddressUse == PostalAddressUse.HP ||
	        				postalAddressUse == PostalAddressUse.WP ||
	        				postalAddressUse == PostalAddressUse.TMP ||
	        				postalAddressUse == PostalAddressUse.BAD ){
	        			address.setUse( vst.PostalAdressUse2AddressUseEnum( postalAddressUse ) );
	        		}
	        	}
	        }       
	        
	        if( ad.getText() != null && !ad.getText().isEmpty() ){
	        	address.setText( ad.getText() );
	        }
	        
	        if( !ad.getStreetAddressLines().isEmpty() && ad.getStreetAddressLines() != null){
	        	for(ADXP adxp : ad.getStreetAddressLines()){
	                address.addLine(adxp.getText());
	            }
	        }
	        if(!ad.getDeliveryAddressLines().isEmpty() && ad.getDeliveryAddressLines() != null){
	        	for(ADXP adxp : ad.getDeliveryAddressLines()){
	                address.addLine(adxp.getText());
	            }
	        }
	        
	        if(!ad.getCities().isEmpty() && ad.getCities() != null){
	            address.setCity(ad.getCities().get(0).getText());
	        }
	        
	        if(!ad.getCounties().isEmpty() && ad.getCounties() != null ){
	            address.setDistrict(ad.getCounties().get(0).getText());
	        }
	        
	        if(!ad.getCities().isEmpty() && ad.getCities() != null){
	            address.setCity(ad.getCities().get(0).getText());
	        }
	        
	        if(!ad.getStates().isEmpty() && ad.getStates() != null){
	            address.setState(ad.getStates().get(0).getText());
	        }
	        
	        if( !ad.getPostalCodes().isEmpty() && ad.getPostalCodes() != null){
	            address.setPostalCode(ad.getPostalCodes().get(0).getText());
	        }
	        
	        if(!ad.getCountries().isEmpty() && ad.getCounties() != null){
	            address.setCountry(ad.getCountries().get(0).getText());
	        }
	        
	        if(!ad.getUseablePeriods().isEmpty() && ad.getUseablePeriods() != null){
	            PeriodDt period = new PeriodDt();
	            DateTimeDt dateTimeStart = new DateTimeDt();
	            dateTimeStart.setValueAsString( ad.getUseablePeriods().get(0).getValue() );
	            period.setStart( dateTimeStart);
	            
	            if(ad.getUseablePeriods().get(1) != null){
	                DateTimeDt dateTimeEnd = new DateTimeDt();
	                dateTimeEnd.setValueAsString( ad.getUseablePeriods().get(1).getValue() );
	                period.setEnd(dateTimeEnd);
	            }
	            
	            address.setPeriod(period);
	
	        }
	        
	        return address;
	    }
	}//end AddressDt

	//TODO: Mustafa: This will be revisited and updated for Act.author; not any participant
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
	
	public Base64BinaryDt BIN2Base64Binary(BIN bin){
    	
    	if(bin.getRepresentation().getLiteral()!=null)
    	{
    		// TODO: It doesn't seem convenient. There should be a way to get the value of BIN.
    		Base64BinaryDt base64BinaryDt = new Base64BinaryDt();
        	base64BinaryDt.setValue(bin.getRepresentation().getLiteral().getBytes());
        	return base64BinaryDt;
    	}
    	else
    	{
    		return null;
    	}
    	
    }
	
	public BooleanDt BL2Boolean(BL bl){
	     	return (  bl == null || bl.isSetNullFlavor() ) ? null : new BooleanDt(bl.getValue());
	}
	
	public CodeableConceptDt CD2CodeableConcept(CD cd) {
        if( cd == null || cd.isSetNullFlavor() ) return null;
        else{
        	//List<CodingDt> myCodingDtList = new ArrayList<CodingDt>();
        	CodeableConceptDt myCodeableConceptDt = new CodeableConceptDt();
        	for(CD myCd : cd.getTranslations() ){

        		CodingDt codingDt = new CodingDt();
        		boolean isEmpty = true;
            	
            	if( myCd.getCodeSystem() != null ){
            		codingDt.setSystem( vst.oid2Url(myCd.getCodeSystem()) );
            		isEmpty = false;
            	}
            	if( myCd.getCode() !=null ){
            		codingDt.setCode( myCd.getCode() );
            		isEmpty = false;
            	}
            	if( myCd.getCodeSystemVersion() !=null ){
            		codingDt.setVersion( myCd.getCodeSystemVersion() );
            		isEmpty = false;
            	}
            	if( myCd.getDisplayName() != null ){
            		codingDt.setDisplay( myCd.getDisplayName() );
            		isEmpty = false;
            	}
            	if (isEmpty == false)
            		myCodeableConceptDt.addCoding( codingDt );
        	}
        	
        	boolean isEmpty = true;
        	
        	CodingDt codingDt = new CodingDt();
        	if( cd.getCodeSystem() != null ){
        		codingDt.setSystem(vst.oid2Url(cd.getCodeSystem())  );
        		isEmpty = false;
        	}
        	if( cd.getCode() !=null ){
        		codingDt.setCode( cd.getCode() );
        		isEmpty = false;
        	}
        	if( cd.getCodeSystemVersion() !=null ){
        		codingDt.setVersion( cd.getCodeSystemVersion() );
        		isEmpty = false;
        	}
        	if( cd.getDisplayName() != null ){
        		codingDt.setDisplay( cd.getDisplayName() );
        		isEmpty = false;
        	}
        	if (isEmpty == false)
        		myCodeableConceptDt.addCoding( codingDt );
        	
        	return myCodeableConceptDt;
        }
    }
	
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
	
	public AttachmentDt ED2Attachment(ED ed) {
		if(ed==null || ed.isSetNullFlavor())
		{
			return null;
		}
		else
		{
			AttachmentDt attachmentDt = new AttachmentDt();
			if(ed.isSetMediaType() && ed.getMediaType()!=null && !ed.getMediaType().isEmpty())
			{
				attachmentDt.setContentType(ed.getMediaType());
			}
			if(ed.getLanguage()!=null && !ed.getLanguage().isEmpty())
			{
				attachmentDt.setLanguage(ed.getLanguage());
			}
			if( !ed.getText().isEmpty() && ed.getText() != null )
			{
				attachmentDt.setData( ed.getText().getBytes() );				
			}
			if( ed.getReference()!=null )
			{
				attachmentDt.setUrl(ed.getReference().getValue());
			}
			if(ed.getIntegrityCheck()!=null)
			{
				attachmentDt.setHash(ed.getIntegrityCheck());
			}
			// ED.title.data doesn't exist
			// Therefore, couldn't map ED.title.data <=> Attachment.title
			return attachmentDt;
		}
	}//end attachmentDt
	
	public HumanNameDt EN2HumanName(EN en) {
		
		if( en != null && !en.isSetNullFlavor()){
			
			HumanNameDt myHumanName = new HumanNameDt();
			
			if( en.getText() != null && !en.getText().isEmpty()){
				myHumanName.setText( en.getText() );
			}
			
			if(en.getUses() != null && !en.getUses().isEmpty()){
				ValueSetsTransformerImpl VSTI = new ValueSetsTransformerImpl();
				myHumanName.setUse( VSTI.EntityNameUse2NameUseEnum(en.getUses().get(0)) );
			}
			
			if(en.getFamilies() != null && !en.getFamilies().isEmpty()){
				for(ENXP element: en.getFamilies()){
					myHumanName.addFamily( element.getText() );
				}
			}
			if(en.getGivens() != null && !en.getGivens().isEmpty()){
				for(ENXP element: en.getGivens()){
					myHumanName.addGiven( element.getText() );
				}
			}
			if(en.getPrefixes() != null && !en.getPrefixes().isEmpty()){
				for(ENXP element: en.getPrefixes( )){
					myHumanName.addPrefix( element.getText() );
				}
			}
			if(en.getSuffixes() != null && !en.getSuffixes().isEmpty()){
				for(ENXP element: en.getSuffixes()){
					myHumanName.addSuffix( element.getText() );
				}
			}
			
			if( en.getValidTime() != null && !en.getValidTime().isSetNullFlavor() ){
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
			
//			if(ii.getRoot() != null){
//				if( !ii.getRoot().isEmpty() )
//				{
//					identifierDt.setSystem( ii.getExtension() );
//				}
//			}//end if
			
			if(ii.getRoot() != null && !ii.getRoot().isEmpty()){

					identifierDt.setValue( ii.getRoot() );
			}
			
			if( ii.getAssigningAuthorityName() != null){
				ResourceReferenceDt resourceReference = new ResourceReferenceDt( ii.getAssigningAuthorityName() );
				if( !resourceReference.isEmpty() )
					identifierDt.setAssigner( resourceReference );
			}
			
			return identifierDt;

		}
		return null;

	}//end II2Identifier
	
	public IntegerDt INT2Integer(INT myInt){
    	return (myInt == null || myInt.isSetNullFlavor() ) ? null : new IntegerDt(myInt.getValue().toString());
    }
	
	public PeriodDt IVL_TS2Period(IVL_TS ivlts) {
		if(ivlts == null || ivlts.isSetNullFlavor()) 
			return null;
		
		PeriodDt periodDt = new PeriodDt();
		
		// low
		if(ivlts.getLow() != null && !ivlts.getLow().isSetNullFlavor()) {
			String date=ivlts.getLow().getValue();
			periodDt.setStart(String2DateTime(date));
		}
		
		// high
		if(ivlts.getHigh() != null && !ivlts.getHigh().isSetNullFlavor()) {
			String date=ivlts.getHigh().getValue();
			periodDt.setEnd(String2DateTime(date));
		}
		
		// low is null, high is null and the value is carrying the low value
		if(ivlts.getLow() == null && ivlts.getHigh() == null && ivlts.getValue() != null && !ivlts.getValue().equals("")) {
			periodDt.setStart(String2DateTime(ivlts.getValue()));
		}
		
		return periodDt;
	}
	
	public RangeDt IVL_PQ2Range(IVL_PQ ivlpq){
		if(ivlpq == null || ivlpq.isSetNullFlavor()) 
			return null;
		
		RangeDt rangeDt = new RangeDt();
		
		// low
		if(ivlpq.getLow() != null && !ivlpq.getLow().isSetNullFlavor()){
			rangeDt.setLow(PQ2SimpleQuantityDt(ivlpq.getLow()));
			
		}
		
		// high
		if(ivlpq.getHigh() != null && !ivlpq.getHigh().isSetNullFlavor()){
			rangeDt.setHigh(PQ2SimpleQuantityDt(ivlpq.getHigh()));
		}
		
		// low is null, high is null and the value is carrying the low value
		if(ivlpq.getLow() == null && ivlpq.getHigh() == null && ivlpq.getValue() != null) {
			SimpleQuantityDt low = new SimpleQuantityDt();
			low.setValue(ivlpq.getValue());
			rangeDt.setLow(low);
		}
		
		return rangeDt;
	}

	public TimingDt PIVL_TS2Timing(PIVL_TS pivlts) {
		if(pivlts == null || pivlts.isSetNullFlavor())
			return null;

		TimingDt timing = new TimingDt();

		// period -> period
		if(pivlts.getPeriod() != null && !pivlts.getPeriod().isSetNullFlavor()) {
			TimingDt.Repeat repeat = new TimingDt.Repeat();
			timing.setRepeat(repeat);
			// period.value -> repeat.period
			if(pivlts.getPeriod().getValue() != null)
				repeat.setPeriod(pivlts.getPeriod().getValue());
			// period.unit -> repeat.periodUnits
			if(pivlts.getPeriod().getUnit() != null)
				repeat.setPeriodUnits(vst.PeriodUnit2UnitsOfTimeEnum(pivlts.getPeriod().getUnit()));
		}

		// phase -> phase
		// TODO: Necip buradan devam et


		return timing;
	}

	public QuantityDt PQ2Quantity(PQ pq)
	{
		if(pq == null || pq.isSetNullFlavor() ) return null;
		else{
			QuantityDt quantityDt = new QuantityDt();
			if(!pq.isSetNullFlavor())
			{
				if( pq.getValue() != null)
					quantityDt.setValue(pq.getValue());
				if( pq.getUnit() != null && !pq.getUnit().isEmpty())
				{
					quantityDt.setUnit(pq.getUnit());
				}
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

	public SimpleQuantityDt PQ2SimpleQuantityDt( PQ pq ){
		if( pq == null || pq.isSetNullFlavor() ) return null;
		else{
			SimpleQuantityDt simpleQuantity = new SimpleQuantityDt();
			
			// https://www.hl7.org/fhir/datatypes-mappings.html#simplequantity
			
			// value
			if( pq.getValue() != null ){
				simpleQuantity.setValue(pq.getValue());
			}
			
			// unit
			if( pq.getUnit() != null && !pq.getUnit().isEmpty() ){
				simpleQuantity.setUnit( pq.getUnit() );
			}
			
			// system and code
			if( pq.getTranslations() != null && !pq.getTranslations().isEmpty() ){
				for( org.openhealthtools.mdht.uml.hl7.datatypes.PQR pqr : pq.getTranslations() ){
					if( pqr != null && !pqr.isSetNullFlavor() ){
						
						// system
						simpleQuantity.setSystem( vst.oid2Url( pqr.getCodeSystem() ) );
						
						// code
						simpleQuantity.setCode( pqr.getCode() );
					}
				}
			}
			return simpleQuantity;
		}
	}
	
	public DecimalDt REAL2Decimal(REAL real){
    	return (real == null || real.isSetNullFlavor() ) ? null : new DecimalDt(real.getValue());
    }
	
	public RatioDt RTO2Ratio(RTO rto){
    	if( rto == null || rto.isSetNullFlavor() ) return null;
    	else{
    		RatioDt myRatioDt = new RatioDt();
    		if( ! rto.getNumerator().isSetNullFlavor() ) {
    			QuantityDt quantity=new QuantityDt();
    			REAL numerator= (REAL) rto.getNumerator();
    			quantity.setValue(numerator.getValue().doubleValue());
    			myRatioDt.setNumerator( quantity);
    		}
    		if( !rto.getDenominator().isSetNullFlavor() ){
    			QuantityDt quantity=new QuantityDt();
    			REAL denominator= (REAL) rto.getDenominator();
    			quantity.setValue(denominator.getValue().doubleValue());
    			myRatioDt.setDenominator(quantity);
    		}
    		return myRatioDt;
    	}
    }
	
	public StringDt ST2String(ST st){
    	return ( st == null || st.isSetNullFlavor() ) ? null : new StringDt(st.getText());
    }

	public NarrativeDt StrucDocText2Narrative(StrucDocText sdt) {
		if(sdt != null) {
			NarrativeDt narrative = new NarrativeDt();
			narrative.setDiv(StrucDocText2String(sdt));
			narrative.setStatus(NarrativeStatusEnum.ADDITIONAL);
			return narrative;
		}
		return null;
	}

	public ContactPointDt TEL2ContactPoint(TEL tel) {
		
		if( tel!=null && !tel.isSetNullFlavor()){
			
			ContactPointDt contactPointDt = new ContactPointDt();
			
			if(tel.getValue() != null ){
				String value = tel.getValue();
				String[] systemType = value.split(":");
				if( systemType.length > 1  ){
					// for the values in form tel:+1(555)555-1000
					if( systemType[0].equals("phone") || systemType[0].equals("tel") )
						contactPointDt.setSystem(ContactPointSystemEnum.PHONE);
					else if( systemType[0].equals("email") )
						contactPointDt.setSystem(ContactPointSystemEnum.EMAIL);
					else if( systemType[0].equals("fax") )
						contactPointDt.setSystem(ContactPointSystemEnum.FAX);
					else if( systemType[0].equals("http") || systemType[0].equals("https") )
						contactPointDt.setSystem(ContactPointSystemEnum.URL);
					
					contactPointDt.setValue( systemType[1] );
				}
				else if( systemType.length == 1 ){
					// for the values in form +1(555)555-5000
					contactPointDt.setValue( systemType[0] );
				}
			}
			
			PeriodDt period = new PeriodDt();
			if(!tel.getUseablePeriods().isEmpty())
			{
				DateTimeDt dateTime = new DateTimeDt();
				dateTime.setValueAsString(tel.getUseablePeriods().get(0).getValue());
				period.setStart(dateTime);
				if(tel.getUseablePeriods().get(1) != null ){
					DateTimeDt dateTime2 = new DateTimeDt();
					dateTime2.setValueAsString(tel.getUseablePeriods().get(1).getValue());
					period.setEnd(dateTime2);
				}
				contactPointDt.setPeriod(period);
			}
			
			
			
			if(!tel.getUses().isEmpty()){
				ValueSetsTransformerImpl VSTI = new ValueSetsTransformerImpl();
				contactPointDt.setUse( VSTI.TelecommunicationAddressUse2ContacPointUseEnum( tel.getUses().get(0) ) );
				
			}
			
			return contactPointDt;
		}
		
		return null;
	}

	public DateTimeDt String2DateTime(String date) {
		return (DateTimeDt) TS2BaseDateTime(date,new DateTimeDt());
	}
	
	public DateDt TS2Date(TS ts){
		return (DateDt) TS2BaseDateTime(ts,new DateDt());
	}
	
	public DateTimeDt TS2DateTime(TS ts) {
		return (DateTimeDt) TS2BaseDateTime(ts,new DateTimeDt());
	}
	
	public InstantDt TS2Instant(TS ts) {
		return (InstantDt) TS2BaseDateTime(ts,new InstantDt());
	}
	
	public UriDt URL2Uri(URL url){
    	return (url == null || url.isSetNullFlavor()) ? null : new UriDt(url.getValue());
    }
	

	
	
	
	// Helper Methods
	
	// 1st parameter(tsObject) can be an object of type TS or a String representing the time
	// 2nd parameter(returnObject) is given to determine the type of the returning object
	private BaseDateTimeDt TS2BaseDateTime(Object tsObject, Object returnObject) {
		if(tsObject == null)
			return null;
		
		String dateString;
		// checking the type of tsObject, assigning dateString accordingly
		if(tsObject instanceof TS){
			// null-flavor check
			if(((TS)tsObject).isSetNullFlavor() || ((TS)tsObject).getValue() == null) {
				return null;
			} else {
				dateString = ((TS)tsObject).getValue();
			}
		} else if(tsObject instanceof String) {
			dateString = (String)tsObject;
		} else {
			// unexpected situtation
			// 1st parameter of this method should be either an instanceof TS or String
			return null;
		}
		
		BaseDateTimeDt date;
		// initializing date
		if(returnObject instanceof DateDt) {
			date = new DateDt();
		} else if(returnObject instanceof DateTimeDt) {
			date = new DateTimeDt();
		} else if(returnObject instanceof InstantDt) {
			date = new InstantDt();
		} else {
			// unexpected situtation
			// caller of this method must have a need of DateDt, DateTimeDt or InstantDt
			// otherwise, the returning object will be of type DateDt
			date = new DateDt();
		}
		
		/*
		 * Possible date forms
		 * YYYY: year
		 * YYYYMM: year month
		 * YYYYMMDD: year month day
		 * YYYYMMDDHHMM: year month day hour minute
		 * YYYYMMDDHHMMSS.S: year month day hour minute second
		 * YYYYMMDDHHMM+TIZO: year month day hour minute timezone
		 */
		
		TemporalPrecisionEnum precision = null;
		// determining precision
		switch(dateString.length()) {
			case 4: // yyyy
				precision = TemporalPrecisionEnum.YEAR; break;
			case 6: // yyyymm
				precision = TemporalPrecisionEnum.MONTH; break;
			case 8: // yyyymmdd
				precision = TemporalPrecisionEnum.DAY; break;
			case 12: // yyyymmddhhmm
			case 17: // yyyymmddhhmm+tizo
				precision = TemporalPrecisionEnum.MINUTE; break;
			case 16: // yyyymmddhhmmss.s
				precision = TemporalPrecisionEnum.MILLI; break;
			default:
				precision = null;
		}
		
		// given string may include up to four digits of fractions of a second
		// therefore, there may be cases where the length of the string is 17,18 or 19 and the precision is MILLI.
		// for those of cases where the length causes conflicts, let's check if dot(.) exists in the string
		if(dateString.contains(".")){
			precision =  TemporalPrecisionEnum.MILLI;
		}
			
		// setting precision
		if(precision != null){
			date.setPrecision(precision);
		} else {
			// incorrect format
			return null;
		}
		
		
		
		// YYYYMMDDHHMM+TIZO and YYYYMMDDHHMMSS.S are special cases
		// If our case is one of them, we will treat differently
		
		if(dateString.contains(".")) {
			// get the integer starting from the dot(.) char 'till the end of the string as the millis
			int millis = new Integer(dateString.substring(dateString.indexOf('.')+1));
			
			// if millis is given as .4 , it corresponds to 400 millis. 
			// therefore, we need a conversion.
			if(millis > 0 && millis < 1000) {
				while(millis*10 <1000) {
					millis *= 10;
				}
			} else if(millis >= 1000){
				// unexpected situtation
				millis = 999;
			} else {
				// unexpected situtation
				millis = 0;
			}
			
			// setting millis
			date.setMillis(millis);
			
			// setting second, minute, hour, day, month, year..
			date.setSecond(new Integer(dateString.substring(12,14)));
			date.setMinute(new Integer(dateString.substring(10, 12)));
			date.setHour(new Integer(dateString.substring(8,10)));
			date.setDay(new Integer(dateString.substring(6,8)));
			date.setMonth(new Integer(dateString.substring(4,6))-1); // 0-index
			date.setYear(new Integer(dateString.substring(0,4)));
			
		} else if(dateString.contains("+") || dateString.contains("-")) {
			// getting the timezone part
			date.setTimeZone(TimeZone.getTimeZone("GMT"+dateString.substring(12)));
			
			// minute, hour, day, month, year..
			date.setMinute(new Integer(dateString.substring(10, 12)));
			date.setHour(new Integer(dateString.substring(8,10)));
			date.setDay(new Integer(dateString.substring(6,8)));
			date.setMonth(new Integer(dateString.substring(4,6))-1); // 0-index
			date.setYear(new Integer(dateString.substring(0,4)));
		} else {
			// since there are strange situtations where the index changes upon the precision, we set every value in its precision block
			switch(precision) {
				case MINUTE: 
					date.setMinute(new Integer(dateString.substring(10,12)));
					date.setHour(new Integer(dateString.substring(8,10)));
					date.setDay(new Integer(dateString.substring(6,8)));
					date.setMonth(new Integer(dateString.substring(4,6))-1);
					date.setYear(new Integer(dateString.substring(0,4))); 
					break;
				case DAY:
					date.setDay(new Integer(dateString.substring(6,8)));
					date.setMonth(new Integer(dateString.substring(4,6))-1);
					date.setYear(new Integer(dateString.substring(0,4))); 
					break;
				case MONTH:
					date.setMonth(new Integer(dateString.substring(4,6)));
					date.setYear(new Integer(dateString.substring(0,4))); 
					break;
				case YEAR:
					date.setYear(new Integer(dateString.substring(0,4))+1); 
					break;
				default:
					date = null;
			}
		}
		return date;
	}
	
	// Following method is a recursive one and will be used as helper for StructDocText2Narrative
	// Since it calls itself repeatedly and handles with different types of objects, parameter is taken as Object
	// However, parameters of type StrucDocText should be given by the caller
	private String StrucDocText2String( Object param ){
		if( param instanceof org.openhealthtools.mdht.uml.cda.StrucDocText ) {
			org.openhealthtools.mdht.uml.cda.StrucDocText paramStrucDocText = (org.openhealthtools.mdht.uml.cda.StrucDocText)param;
			return "<div>" +StrucDocText2String(  paramStrucDocText.getMixed() ) + "</div>";
		} 
		else if( param instanceof BasicFeatureMap ){
			String returnValue = "";
			for( Object object : (BasicFeatureMap)param ){
				String pieceOfReturn = StrucDocText2String( object );
				if( pieceOfReturn != null && !pieceOfReturn.isEmpty() ){
					returnValue = returnValue + pieceOfReturn;
				}
			}
			return returnValue;
		} 
		else if( param instanceof EStructuralFeatureImpl.SimpleFeatureMapEntry ){
			return ((EStructuralFeatureImpl.SimpleFeatureMapEntry)param).getValue().toString();
		} 
		else if( param instanceof EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry){
			EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry entry = (EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry)param;
			return "<"+entry.getEStructuralFeature().getName()
					+ getAttributeHelperStrucDocText2String(entry)
					+">" + StrucDocText2String( entry.getValue() ) + "</"+entry.getEStructuralFeature().getName()+">";
		} 
		else if( param instanceof org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl ){
			// since the name and the attributes are taken already, we just send the mixed of anyTypeImpl
			return StrucDocText2String( ((org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl)param).getMixed() );
		} 
		else{
			// Undesired situtation
			// Check the class of param
			return null;
		}
	}
	
	// Helper for StrucDocText2String
	private String getAttributeHelperStrucDocText2String( EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry entry ){
		// This method extracts attributes from AnyTypeImpl
		// Return example: border="1"
		if( entry.getValue() instanceof org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl ){
			String returnValue = "";
			for( FeatureMap.Entry attribute : ((org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl) entry.getValue()).getAnyAttribute() ){
				String name = attribute.getEStructuralFeature().getName();
				String value = attribute.getValue().toString();
				if( name != null && !name.isEmpty()){
					// we may have attributes which doesn't have any value
					returnValue = returnValue + " " + name;
					if( value != null && !value.isEmpty() ){
						returnValue = returnValue + "=\""+value+"\"";
					}
				}
			}
			return returnValue;
		} else{
			// Undesired situtation
			// Check the class of entry.getValue()
			return null;
		}
	}
}
