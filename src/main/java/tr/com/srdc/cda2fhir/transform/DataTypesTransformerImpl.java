package tr.com.srdc.cda2fhir.transform;

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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import tr.com.srdc.cda2fhir.util.Constants;
import tr.com.srdc.cda2fhir.util.StringUtil;

/**
 * Created by mustafa on 7/21/2016.
 */
public class DataTypesTransformerImpl implements IDataTypesTransformer {

	private IValueSetsTransformer vst = new ValueSetsTransformerImpl();

	public AddressDt AD2Address(AD ad) {
	    if(ad == null || ad.isSetNullFlavor())
	    	return null;
        
        AddressDt address = new AddressDt();
        
        // use -> use
        if(ad.getUses() != null && !ad.getUses().isEmpty()) {
        	// We get the address.type and address.use from the list ad.uses
        	for(PostalAddressUse postalAddressUse : ad.getUses()) {
        		// If we catch a valid value for type or use, we assign it
        		if(postalAddressUse == PostalAddressUse.PHYS || postalAddressUse == PostalAddressUse.PST) {
        			address.setType(vst.tPostalAddressUse2AddressTypeEnum(postalAddressUse));
        		} else if(postalAddressUse == PostalAddressUse.H ||
        				postalAddressUse == PostalAddressUse.HP ||
        				postalAddressUse == PostalAddressUse.WP ||
        				postalAddressUse == PostalAddressUse.TMP ||
        				postalAddressUse == PostalAddressUse.BAD) {
        			address.setUse(vst.tPostalAdressUse2AddressUseEnum(postalAddressUse));
        		}
        	}
        }       
        
        // text -> text
        if(ad.getText() != null && !ad.getText().isEmpty()) {
        	address.setText(ad.getText());
        }
        
        // streetAddressLine -> line
        if(ad.getStreetAddressLines() != null && !ad.getStreetAddressLines().isEmpty()) {
        	for(ADXP adxp : ad.getStreetAddressLines()){
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.addLine(adxp.getText());
        		}
            }
        }
        
        // deliveryAddressLine -> line
        if(ad.getDeliveryAddressLines() != null && !ad.getDeliveryAddressLines().isEmpty()) {
        	for(ADXP adxp : ad.getDeliveryAddressLines()) {
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.addLine(adxp.getText());
        		}
            }
        }
        
        // city -> city
        if(ad.getCities() != null && !ad.getCities().isEmpty()) {
        	for(ADXP adxp : ad.getCities()) {
        		// Asserting that at most one city information exists
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.setCity(adxp.getText());
        		}
        	}
        }
        
        // county -> district
        if(ad.getCounties() != null && !ad.getCounties().isEmpty()) {
        	for(ADXP adxp : ad.getCounties()) {
        		// Asserting that at most one county information exists
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.setDistrict(adxp.getText());
        		}
        	}
            
        }
        
        // country -> country
        if( ad.getCountries() != null && !ad.getCountries().isEmpty()) {
        	for(ADXP adxp : ad.getCountries()) {
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.setCountry(adxp.getText());
        		}
        	}
            
        }
        
        // state -> state
        if(ad.getStates() != null && !ad.getStates().isEmpty()) {
        	for(ADXP adxp : ad.getStates()) {
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.setState(adxp.getText());
        		}
        	}
        }
        
        // postalCode -> postalCode
        if(ad.getPostalCodes() != null && !ad.getPostalCodes().isEmpty()) {
        	for(ADXP adxp : ad.getPostalCodes()) {
        		if(adxp != null && !adxp.isSetNullFlavor()) {
        			address.setPostalCode(adxp.getText());
        		}
        	}
        }
        
        // useablePeriods[0] -> start, usablePeriods[1] -> end
        if(ad.getUseablePeriods() != null && !ad.getUseablePeriods().isEmpty()) {
        	PeriodDt period = new PeriodDt();
        	int sxcmCounter = 0;
        	for(SXCM_TS sxcmts : ad.getUseablePeriods()) {
        		if(sxcmts != null && !sxcmts.isSetNullFlavor()) {
        			if(sxcmCounter == 0) {
        				period.setStart(tString2DateTime(sxcmts.getValue()));
        				sxcmCounter++;
        			} else if(sxcmCounter == 1) {
        				period.setEnd(tString2DateTime(sxcmts.getValue()));
        				sxcmCounter++;
        			}
        		}
        	}
        	address.setPeriod(period);
        }
        return address;
    }
	
	public Base64BinaryDt tBIN2Base64Binary(BIN bin) {
		if(bin == null || bin.isSetNullFlavor())
			return null;
    	if(bin.getRepresentation().getLiteral()!=null) {
    		// TODO: It doesn't seem convenient. There should be a way to get the value of BIN.
    		Base64BinaryDt base64BinaryDt = new Base64BinaryDt();
        	base64BinaryDt.setValue(bin.getRepresentation().getLiteral().getBytes());
        	return base64BinaryDt;
    	}
    	else {
    		return null;
    	}
    	
    }
	
	public BooleanDt tBL2Boolean(BL bl) {
	     return (bl == null || bl.isSetNullFlavor()) ? null : new BooleanDt(bl.getValue());
	}
	
	public CodeableConceptDt tCD2CodeableConcept(CD cd) {
       	CodeableConceptDt myCodeableConceptDt = tCD2CodeableConceptExcludingTranslations(cd);

		if(myCodeableConceptDt == null)
			return null;
       	
       	// translation
       	if(cd.getTranslations() != null && !cd.getTranslations().isEmpty()) {
       		for(CD myCd : cd.getTranslations()) {
				CodingDt codingDt = new CodingDt();
           		boolean isEmpty = true;
           		
           		// codeSystem -> system
               	if(myCd.getCodeSystem() != null && !myCd.getCodeSystem().isEmpty()) {
               		codingDt.setSystem(vst.tOid2Url(myCd.getCodeSystem()));
               		isEmpty = false;
               	}
               	
               	// code -> code
               	if(myCd.getCode() !=null && !myCd.getCode().isEmpty()) {
               		codingDt.setCode(myCd.getCode());
               		isEmpty = false;
               	}
               	
               	// codeSystemVersion -> version
               	if(myCd.getCodeSystemVersion() !=null && !myCd.getCodeSystemVersion().isEmpty()) {
               		codingDt.setVersion(myCd.getCodeSystemVersion());
               		isEmpty = false;
               	}
               	
               	// displayName -> display
               	if(myCd.getDisplayName() != null && !myCd.getDisplayName().isEmpty()) {
               		codingDt.setDisplay(myCd.getDisplayName());
               		isEmpty = false;
               	}

               	if(isEmpty == false)
               		myCodeableConceptDt.addCoding(codingDt);
           	}
       	}

       	return myCodeableConceptDt;
    }

	public CodeableConceptDt tCD2CodeableConceptExcludingTranslations(CD cd) {
		if(cd == null || cd.isSetNullFlavor())
			return null;

		CodeableConceptDt myCodeableConceptDt = new CodeableConceptDt();

		// .
		CodingDt codingDt = new CodingDt();
		boolean isEmpty = true;

		// codeSystem -> system
		if(cd.getCodeSystem() != null && !cd.getCodeSystem().isEmpty()){
			codingDt.setSystem(vst.tOid2Url(cd.getCodeSystem()));
			isEmpty = false;
		}

		// code -> code
		if(cd.getCode() !=null && !cd.getCode().isEmpty()) {
			codingDt.setCode(cd.getCode());
			isEmpty = false;
		}

		// codeSystemVersion -> version
		if(cd.getCodeSystemVersion() !=null && !cd.getCodeSystemVersion().isEmpty()){
			codingDt.setVersion(cd.getCodeSystemVersion());
			isEmpty = false;
		}

		// displayName -> display
		if(cd.getDisplayName() != null && !cd.getDisplayName().isEmpty()){
			codingDt.setDisplay(cd.getDisplayName());
			isEmpty = false;
		}

		if(!isEmpty) {
			myCodeableConceptDt.addCoding(codingDt);
			return myCodeableConceptDt;
		}
		else
			return null;
	}
	
	public CodingDt tCV2Coding(CV cv) {
    	if(cv == null || cv.isSetNullFlavor())
    		return null;
    	
	   	CodingDt codingDt= new CodingDt();
	   	
	   	// codeSystem -> system
	   	if(cv.getCodeSystem() != null && !cv.getCodeSystem().isEmpty()) {
	   		codingDt.setSystem(cv.getCodeSystem());
	   	}
	   	
	   	// codeSystemVersion -> version
	   	if(cv.getCodeSystemVersion() != null && !cv.getCodeSystemVersion().isEmpty()) {
	   		codingDt.setVersion(cv.getCodeSystemVersion());
	   	}
	   	
	   	// code -> code
	   	if(cv.getCode() != null && !cv.getCode().isEmpty()) {
	   		codingDt.setCode(cv.getCode());
	   	}
	   	
	   	// displayName -> display
	   	if(cv.getDisplayName() != null && !cv.getDisplayName().isEmpty()) {
	   		codingDt.setDisplay(cv.getDisplayName());
	   	}
	    return codingDt;
    }
	
	public AttachmentDt tED2Attachment(ED ed) {
		if(ed==null || ed.isSetNullFlavor())
			return null;
		
		AttachmentDt attachmentDt = new AttachmentDt();
		
		// mediaType -> contentType
		if(ed.isSetMediaType() && ed.getMediaType()!=null && !ed.getMediaType().isEmpty()) {
			attachmentDt.setContentType(ed.getMediaType());
		}
		
		// language -> language
		if(ed.getLanguage() != null && !ed.getLanguage().isEmpty()) {
			attachmentDt.setLanguage(ed.getLanguage());
		}
		
		// text.bytes -> data
		if(ed.getText() != null && !ed.getText().isEmpty()) {
			if(ed.getText().getBytes() != null){
				attachmentDt.setData(ed.getText().getBytes());	
			}		
		}
		
		// reference.value -> url
		if(ed.getReference() != null && !ed.getReference().isSetNullFlavor()) {
			if(ed.getReference().getValue() != null && !ed.getReference().getValue().isEmpty()) {
				attachmentDt.setUrl(ed.getReference().getValue());
			}
		}
		
		// integrityCheck -> hash
		if(ed.getIntegrityCheck() != null) {
			attachmentDt.setHash(ed.getIntegrityCheck());
		}
		
		return attachmentDt;
	}
	
	public HumanNameDt tEN2HumanName(EN en) {
		if(en == null || en.isSetNullFlavor())
			return null;

		HumanNameDt myHumanName = new HumanNameDt();
		
		// text -> text
		if(en.getText() != null && !en.getText().isEmpty()) {
			myHumanName.setText(en.getText());
		}
		
		// use -> use
		if(en.getUses() != null && !en.getUses().isEmpty()) {
			for(EntityNameUse entityNameUse : en.getUses()) {
				if(entityNameUse != null) {
					myHumanName.setUse(vst.tEntityNameUse2NameUseEnum(entityNameUse));
				}
			}
		}
		
		// family -> family
		if(en.getFamilies() != null && !en.getFamilies().isEmpty()) {
			for(ENXP family: en.getFamilies()) {
				myHumanName.addFamily(family.getText());
			}
		}
		
		// given -> given
		if(en.getGivens() != null && !en.getGivens().isEmpty()) {
			for(ENXP given : en.getGivens()) {
				myHumanName.addGiven(given.getText());
			}
		}
		
		// prefix -> prefix
		if(en.getPrefixes() != null && !en.getPrefixes().isEmpty()) {
			for(ENXP prefix : en.getPrefixes()) {
				myHumanName.addPrefix(prefix.getText());
			}
		}
		
		// suffix -> suffix
		if(en.getSuffixes() != null && !en.getSuffixes().isEmpty()) {
			for(ENXP suffix : en.getSuffixes()) {
				myHumanName.addSuffix(suffix.getText());
			}
		}
		
		// validTime -> period
		if(en.getValidTime() != null && !en.getValidTime().isSetNullFlavor()) {
			myHumanName.setPeriod(tIVL_TS2Period(en.getValidTime()));
		}
		
		return myHumanName;
	
	}
	
	public IdentifierDt tII2Identifier(II ii) {
		if(ii == null || ii.isSetNullFlavor())
			return null;
		
		IdentifierDt identifierDt = new IdentifierDt();

		// if both root and extension are present, then
		// root -> system
		// extension -> value
		if(ii.getRoot() != null && !ii.getRoot().isEmpty()
				&& ii.getExtension() != null && !ii.getExtension().isEmpty()){
			// root is oid
			if(StringUtil.isOID(ii.getRoot()))
				identifierDt.setSystem("urn:oid:" + ii.getRoot());
			// root is uuid
			else if(StringUtil.isUUID(ii.getRoot()))
				identifierDt.setSystem("urn:uuid:" + ii.getRoot());
			else
				identifierDt.setSystem(ii.getRoot());

			identifierDt.setValue(ii.getExtension());
		}
		// else if only the root is present, then
		// root -> value
		else if(ii.getRoot() != null && !ii.getRoot().isEmpty())
			identifierDt.setValue(ii.getRoot());
		// this is not very likely but, if there is only the extension, then
		// extension -> value
		else if(ii.getExtension() != null && !ii.getExtension().isEmpty())
			identifierDt.setValue(ii.getExtension());
		
		return identifierDt;

	}
	
	public IntegerDt tINT2Integer(INT myInt){
    	return (myInt == null || myInt.isSetNullFlavor() || myInt.getValue() == null) ? null : new IntegerDt(myInt.getValue().toString());
    }
	
	public PeriodDt tIVL_TS2Period(IVL_TS ivlts) {
		if(ivlts == null || ivlts.isSetNullFlavor()) 
			return null;
		
		PeriodDt periodDt = new PeriodDt();
		
		// low -> start
		if(ivlts.getLow() != null && !ivlts.getLow().isSetNullFlavor()) {
			String date=ivlts.getLow().getValue();
			periodDt.setStart(tString2DateTime(date));
		}
		
		// high -> end
		if(ivlts.getHigh() != null && !ivlts.getHigh().isSetNullFlavor()) {
			String date=ivlts.getHigh().getValue();
			periodDt.setEnd(tString2DateTime(date));
		}
		
		// low is null, high is null and the value is carrying the low value
		// value -> low
		if(ivlts.getLow() == null && ivlts.getHigh() == null && ivlts.getValue() != null && !ivlts.getValue().equals("")) {
			periodDt.setStart(tString2DateTime(ivlts.getValue()));
		}
		
		return periodDt;
	}
	
	public RangeDt tIVL_PQ2Range(IVL_PQ ivlpq){
		if(ivlpq == null || ivlpq.isSetNullFlavor()) 
			return null;
		
		RangeDt rangeDt = new RangeDt();
		
		// low -> low
		if(ivlpq.getLow() != null && !ivlpq.getLow().isSetNullFlavor()){
			rangeDt.setLow(tPQ2SimpleQuantityDt(ivlpq.getLow()));
			
		}
		
		// high -> high
		if(ivlpq.getHigh() != null && !ivlpq.getHigh().isSetNullFlavor()){
			rangeDt.setHigh(tPQ2SimpleQuantityDt(ivlpq.getHigh()));
		}
		
		// low is null, high is null and the value is carrying the low value
		// value -> low
		if(ivlpq.getLow() == null && ivlpq.getHigh() == null && ivlpq.getValue() != null) {
			SimpleQuantityDt low = new SimpleQuantityDt();
			low.setValue(ivlpq.getValue());
			rangeDt.setLow(low);
		}
		
		return rangeDt;
	}
	
	public TimingDt tPIVL_TS2Timing(PIVL_TS pivlts) {
		// http://wiki.hl7.org/images/c/ca/Medication_Frequencies_in_CDA.pdf
		// http://www.cdapro.com/know/24997
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
				repeat.setPeriodUnits(vst.tPeriodUnit2UnitsOfTimeEnum(pivlts.getPeriod().getUnit()));
			
			// phase -> repeat.bounds
			if(pivlts.getPhase() != null && !pivlts.getPhase().isSetNullFlavor()) {
				repeat.setBounds(tIVL_TS2Period(pivlts.getPhase()));
			}
		}	
		return timing;
	}

	public QuantityDt tPQ2Quantity(PQ pq) {
		if(pq == null || pq.isSetNullFlavor())
			return null;
		
		QuantityDt quantityDt = new QuantityDt();

		// value -> value
		if(pq.getValue() != null) {
			quantityDt.setValue(pq.getValue());
		}
		
		// unit -> unit
		if(pq.getUnit() != null && !pq.getUnit().isEmpty()) {
			quantityDt.setUnit(pq.getUnit());
		}
		
		// translation -> system & code
		for(PQR pqr : pq.getTranslations()) {
			if(pqr != null && !pqr.isSetNullFlavor()) {
				// codeSystem -> system
				if(pqr.getCodeSystem() != null && !pqr.getCodeSystem().isEmpty()) {
					quantityDt.setSystem(pqr.getCodeSystem());
				}
				
				// code -> code
				if(pqr.getCode() != null && !pqr.getCode().isEmpty()) {
					quantityDt.setCode(pqr.getCode());
				}
			}
		}
		return quantityDt;
	}

	public SimpleQuantityDt tPQ2SimpleQuantityDt(PQ pq) {
		if(pq == null || pq.isSetNullFlavor())
			return null;
		
		SimpleQuantityDt simpleQuantity = new SimpleQuantityDt();
		
		// value -> value
		if(pq.getValue() != null) {
			simpleQuantity.setValue(pq.getValue());
		}
		
		// unit -> unit
		if(pq.getUnit() != null && !pq.getUnit().isEmpty()) {
			simpleQuantity.setUnit(pq.getUnit());
		}
		
		// translation -> system and code
		if(pq.getTranslations() != null && !pq.getTranslations().isEmpty()) {
			for(org.openhealthtools.mdht.uml.hl7.datatypes.PQR pqr : pq.getTranslations()) {
				if(pqr != null && !pqr.isSetNullFlavor()) {
					// codeSystem -> system
					if(pqr.getCodeSystem() != null && !pqr.getCodeSystem().isEmpty()) {
						simpleQuantity.setSystem(vst.tOid2Url(pqr.getCodeSystem()));
					}
					
					// code -> code
					if(pqr.getCode() != null && !pqr.getCode().isEmpty()) {
						simpleQuantity.setCode(pqr.getCode());
					}
				}
			}
		}
		return simpleQuantity;
	}
	
	public DecimalDt tREAL2Decimal(REAL real){
    	return (real == null || real.isSetNullFlavor() || real.getValue() == null) ? null : new DecimalDt(real.getValue());
    }
	
	public RatioDt tRTO2Ratio(RTO rto){
    	if(rto == null || rto.isSetNullFlavor())
    		return null;
    	RatioDt myRatioDt = new RatioDt();
    	
    	// numerator -> numerator
    	if(rto.getNumerator() != null && !rto.getNumerator().isSetNullFlavor()) {
    		QuantityDt quantity = new QuantityDt();
    		REAL numerator= (REAL)rto.getNumerator();
    		if(numerator.getValue() != null) {
    			quantity.setValue(numerator.getValue().doubleValue());
    			myRatioDt.setNumerator(quantity);
    		}
    	}
    	
    	// denominator -> denominator
    	if(!rto.getDenominator().isSetNullFlavor()) {
    		QuantityDt quantity=new QuantityDt();
    		REAL denominator= (REAL) rto.getDenominator();
    		if(denominator.getValue() != null) {
    			quantity.setValue(denominator.getValue().doubleValue());
        		myRatioDt.setDenominator(quantity);
    		}
    	}
    	return myRatioDt;
    }
    
	public StringDt tST2String(ST st){
    	return (st == null || st.isSetNullFlavor() || st.getText() == null) ? null : new StringDt(st.getText());
    }

	public NarrativeDt tStrucDocText2Narrative(StrucDocText sdt) {
		if(sdt != null) {
			NarrativeDt narrative = new NarrativeDt();
			narrative.setDiv(tStrucDocText2String(sdt));
			narrative.setStatus(NarrativeStatusEnum.ADDITIONAL);
			return narrative;
		}
		return null;
	}

	public ContactPointDt tTEL2ContactPoint(TEL tel) {
		if(tel == null || tel.isSetNullFlavor())
			return null;
		
		ContactPointDt contactPointDt = new ContactPointDt();
		
		// value and system -> value
		if(tel.getValue() != null && !tel.getValue().isEmpty()) {
			String value = tel.getValue();
			String[] systemType = value.split(":");
			
			// for the values in form tel:+1(555)555-1000
			if(systemType.length > 1){
				ContactPointSystemEnum contactPointSystem = vst.tTelValue2ContactPointSystemEnum(systemType[0]);
				// system
				if(contactPointSystem != null) {
					contactPointDt.setSystem(contactPointSystem);
				}
				// value
				contactPointDt.setValue(systemType[1]);
			}
			// for the values in form +1(555)555-5000
			else if(systemType.length == 1){
				contactPointDt.setValue(systemType[0]);
				// configurable default system value
				contactPointDt.setSystem(Constants.DEFAULT_CONTACT_POINT_SYSTEM);
			}
		}
		
		// useablePeriods -> period
		if(tel.getUseablePeriods() != null && !tel.getUseablePeriods().isEmpty()) {
			PeriodDt period = new PeriodDt();
			int sxcmCounter = 0;
			for(SXCM_TS sxcmts : tel.getUseablePeriods()) {
				if(sxcmts != null && !sxcmts.isSetNullFlavor()) {
					// useablePeriods[0] -> period.start
					// useablePeriods[1] -> period.end
					if(sxcmCounter == 0) {
						if(sxcmts.getValue() != null && !sxcmts.getValue().isEmpty()){
							period.setStart(tString2DateTime(sxcmts.getValue()));
						}
					} else if(sxcmCounter == 1) {
						if(sxcmts.getValue() != null && !sxcmts.getValue().isEmpty()) {
							period.setEnd(tString2DateTime(sxcmts.getValue()));
						}
					}
					sxcmCounter++;
				}
			}
			contactPointDt.setPeriod(period);
		}
		
		// use -> use
		if(tel.getUses() != null && !tel.getUses().isEmpty()) {
			for(TelecommunicationAddressUse telAddressUse : tel.getUses()) {
				if(telAddressUse != null) {
					contactPointDt.setUse(vst.tTelecommunicationAddressUse2ContactPointUseEnum(telAddressUse));
				}
			}
		}
		
		return contactPointDt;
	}

	public DateTimeDt tString2DateTime(String date) {
		TS ts = DatatypesFactory.eINSTANCE.createTS();
		ts.setValue(date);
		return tTS2DateTime(ts);
	}
	
	public DateDt tTS2Date(TS ts){
		DateDt date = (DateDt) tTS2BaseDateTime(ts,DateDt.class);
		if(date == null)
			return null;
		
		// TimeZone is NOT permitted
		if(date.getTimeZone() != null) {
			date.setTimeZone(null);
		}
		
		// precision should be YEAR, MONTH or DAY. otherwise, set it to DAY
		if(date.getPrecision() != TemporalPrecisionEnum.YEAR && date.getPrecision() != TemporalPrecisionEnum.MONTH && date.getPrecision() != TemporalPrecisionEnum.DAY) {
			date.setPrecision(TemporalPrecisionEnum.DAY);
		}
		
		return date;
	}
	
	public DateTimeDt tTS2DateTime(TS ts) {
		DateTimeDt dateTime = (DateTimeDt) tTS2BaseDateTime(ts,DateTimeDt.class);
		
		if(dateTime == null)
			return null;
		
		// if the precision is not YEAR or MONTH, TimeZone SHALL be populated
		if(dateTime.getPrecision() != TemporalPrecisionEnum.YEAR && dateTime.getPrecision() != TemporalPrecisionEnum.MONTH) {
			if(dateTime.getTimeZone() == null) {
				dateTime.setTimeZone(TimeZone.getDefault());
			}
		}
		
		// if the precision is MINUTE, seconds SHALL be populated
		if(dateTime.getPrecision() == TemporalPrecisionEnum.MINUTE) {
			dateTime.setPrecision(TemporalPrecisionEnum.SECOND);
			dateTime.setSecond(0);
		}
		
		return dateTime;
	}
	
	public InstantDt tTS2Instant(TS ts) {
		InstantDt instant = (InstantDt) tTS2BaseDateTime(ts,InstantDt.class);
		if(instant == null)
			return null;
		
		// if the precision is not SECOND or MILLI, convert its precision to SECOND
		if(instant.getPrecision() != TemporalPrecisionEnum.SECOND && instant.getPrecision() != TemporalPrecisionEnum.MILLI) {
			instant.setPrecision(TemporalPrecisionEnum.SECOND);
		}
		
		// if it doesn't include a timezone, add the local timezone
		if(instant.getTimeZone() == null) {
			instant.setTimeZone(TimeZone.getDefault());
		}
		return instant;
	}
	
	public UriDt tURL2Uri(URL url){
    	return (url == null || url.isSetNullFlavor() || url.getValue() == null) ? null : new UriDt(url.getValue());
    }
	
	// Helper Methods

	/**
	 * Transforms a CDA TS instance or a string including the date information in CDA format to a FHIR BaseDateTimeDt primitive datatype instance.
	 * Since BaseDateTimeDt is an abstract class, the second parameter of this method (Class&lt;?&gt; classOfReturningObject) determines the class that initiates the BaseDateTimeDt object the method is to return.
	 * @param tsObject A CDA TS instance or a Java String including the date information in CDA format
	 * @param classOfReturningObject A FHIR class that determines the initiater for the BaseDateTimeDt object the method is to return. DateDt.class, DateTimeDt.class or InstantDt.class are expected.
	 * @return A FHIR BaseDateTimeDt primitive datatype instance
	 */
	private BaseDateTimeDt tTS2BaseDateTime(Object tsObject, Class<?> classOfReturningObject) {
		if(tsObject == null)
			return null;
		
		String dateString;
		// checking the type of tsObject, assigning dateString accordingly
		if(tsObject instanceof TS) {
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
		if(classOfReturningObject == DateDt.class) {
			date = new DateDt();
		} else if(classOfReturningObject == DateTimeDt.class) {
			date = new DateTimeDt();
		} else if(classOfReturningObject == InstantDt.class) {
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
		TimeZone timeZone = null;
		
		// getting the timezone
		// once got the timezone, crop the timezone part from the string
		if(dateString.contains("+")) {
			timeZone = TimeZone.getTimeZone("GMT"+dateString.substring(dateString.indexOf('+')));
			dateString = dateString.substring(0, dateString.indexOf('+'));
		} else if(dateString.contains("-")) {
			timeZone = TimeZone.getTimeZone("GMT"+dateString.substring(dateString.indexOf('-')));
			dateString = dateString.substring(0, dateString.indexOf('-'));
		}
		
		
		// determining precision
		switch(dateString.length()) {
			case 4: // yyyy
				precision = TemporalPrecisionEnum.YEAR; break;
			case 6: // yyyymm
				precision = TemporalPrecisionEnum.MONTH; break;
			case 8: // yyyymmdd
				precision = TemporalPrecisionEnum.DAY; break;
			case 12: // yyyymmddhhmm
				precision = TemporalPrecisionEnum.MINUTE; break;
			case 14: // yyyymmddhhmmss
				precision = TemporalPrecisionEnum.SECOND; break;
			case 16: // yyyymmddhhmmss.s
			case 17: // yyyymmddhhmmss.ss
			case 18: // yyyymmddhhmmss.sss
			case 19: // yyyymmddhhmmss.ssss
				precision = TemporalPrecisionEnum.MILLI; break;
			default:
				precision = null;
		}
		
		// given string may include up to four digits of fractions of a second
		// therefore, there may be cases where the length of the string is 17,18 or 19 and the precision is MILLI.
		// for those of cases where the length causes conflicts, let's check if dot(.) exists in the string
			
		// setting precision
		if(precision != null){
			date.setPrecision(precision);
		} else {
			// incorrect format
			return null;
		}
		
		// if timeZone is present, setting it
		if(timeZone != null) {
			date.setTimeZone(timeZone);
		}
		
		if(precision == TemporalPrecisionEnum.MILLI) {
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
			date.setMonth(new Integer(dateString.substring(4,6))-1);
			date.setYear(new Integer(dateString.substring(0,4)));
			
		} else {
			// since there are strange situtations where the index changes upon the precision, we set every value in its precision block
			switch(precision) {
				case SECOND:
					date.setSecond(new Integer(dateString.substring(12,14)));
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
	
	/**
	 * Transforms A CDA StructDocText instance to a Java String containing the transformed text.
	 * Since the method is a recursive one and handles with different types of object, parameter is taken as Object. However, parameters of type StructDocText should be given by the caller.
	 * @param param A CDA StructDocText instance
	 * @return A Java String containing the transformed text
	 */
	private String tStrucDocText2String(Object param) {
		if(param instanceof org.openhealthtools.mdht.uml.cda.StrucDocText) {
			org.openhealthtools.mdht.uml.cda.StrucDocText paramStrucDocText = (org.openhealthtools.mdht.uml.cda.StrucDocText)param;
			return "<div>" +tStrucDocText2String(paramStrucDocText.getMixed()) + "</div>";
		} 
		else if(param instanceof BasicFeatureMap) {
			String returnValue = "";
			for(Object object : (BasicFeatureMap)param){
				String pieceOfReturn = tStrucDocText2String(object);
				if(pieceOfReturn != null && !pieceOfReturn.isEmpty()) {
					returnValue = returnValue + pieceOfReturn;
				}
			}
			return returnValue;
		} 
		else if(param instanceof EStructuralFeatureImpl.SimpleFeatureMapEntry) {
			String elementBody = ((EStructuralFeatureImpl.SimpleFeatureMapEntry)param).getValue().toString();
			// deletion of unnecessary content (\n, \t)
			elementBody = elementBody.replaceAll("\n", "").replaceAll("\t", "");
			return elementBody;
		} 
		else if(param instanceof EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry) {
			EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry entry = (EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry)param;
			List<String> tagList = getTagsHelperForTStructDocText2String(entry);
			return tagList.get(0) + tStrucDocText2String(entry.getValue()) + tagList.get(1);
		} 
		else if(param instanceof org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl) {
			// since the name and the attributes are taken already, we just send the mixed of anyTypeImpl
			return tStrucDocText2String(((org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl)param).getMixed());
		} 
		else{
			// Undesired situtation
			// Check the class of param
			return null;
		}
	}
	
	// Helper for StrucDocText2String
	/**
	 * Extracts the tags and the attributes of an HTML element.
	 * Also, this method transforms the CDA formatted tags to HTML formatted tags.
	 * This method is the helper for the method tStrucDocText2String.
	 * @param entry A EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry instance
	 * @return A Java String list containing the start tag and end tag of an HTML element in form: &lt;tagName attribute="attributeValue"&gt;. While first element of the list correspons to the start tag, second element of the list corresponds to the end tag.
	 */
	private List<String> getTagsHelperForTStructDocText2String(org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry entry) {
		if(entry == null)
			return null;
		String startTag = "";
		String endTag = "";
		String tagName = entry.getEStructuralFeature().getName();
		List<String> attributeList = getAttributesHelperForTStructDocText2String(entry);
		List<String> tagList = new ArrayList<String>();
		
		String attributeToRemove = null;
		
		// removing id attribute from the attributeList
		for(String attribute : attributeList) {
			if(attribute.toLowerCase().startsWith("id=\"", 0)) {
				attributeToRemove = attribute;
			}
		}
		
		if(attributeToRemove != null)
			attributeList.remove(attributeToRemove);
		
		// removing styleCode attribute from the attributeList
		for(String attribute : attributeList) {
			if(attribute.toLowerCase().startsWith("stylecode=\"", 0)) {
				attributeToRemove = attribute;
			}
		}
		if(attributeToRemove != null)
			attributeList.remove(attributeToRemove);
		
		// case tag.equals("list"). we need to transform it to "ul" or "ol"
		if(tagName.equals("list")) {
			// first, think of the situtation no attribute exists about ordered/unordered
			tagName = "ul";
			attributeToRemove = null;
			for(String attribute : attributeList) {
				// if the attribute is listType, make the transformation
				if(attribute.toLowerCase().contains("listtype")) {
					// notice that the string "unordered" also contains "ordered"
					// therefore, it is vital to check "unordered" firstly.
					// if "unordered" is not contained by the attribute, then we may check for "ordered"
					if(attribute.toLowerCase().contains("unordered")) {
						tagName = "ul";
					} else if(attribute.toLowerCase().contains("ordered")) {
						tagName = "ol";
					}
					attributeToRemove = attribute;
				}
			}
			// if we found the "listType" attribute, we assigned it to attributeToRemove
			// from now on, we have nothing to do with this attribute. let's remove it from the list.
			if(attributeToRemove != null) {
				attributeList.remove(attributeToRemove);
			}
		} else {
			switch(tagName.toLowerCase()) {
				case "paragraph":
					tagName = "p"; break;
				case "content":
					tagName = "span"; break;
				case "item":
					tagName = "li"; break;
				case "linkhtml":
					tagName = "a"; break;
				case "renderMultimedia":
					tagName = "img"; break;
				case "list":
					tagName = "ul"; break;
				default: // do nothing. let the tagName be as it is
			}
		}
		
		// now, it is time to prepare our tag by using tagName and attributes
		startTag = "<" + tagName;
		// adding attributes to the start tag
		for(String attribute : attributeList) {
			startTag += " "+attribute;
		}
		// closing the start tag
		startTag += ">";
		endTag = "</" + tagName + ">";
		
		// 1st element of the returning list: startTag
		tagList.add(startTag);
		// 2nd element of the returning list: endTag
		tagList.add(endTag);
		
		return tagList;
	}
	/**
	 * Extracts the attributes of an HTML element
	 * This method is the helper for the method getTags, which is already a helper for tStrucDocText2String.
	 * @param entry A EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry instance
	 * @return A Java String list containing the attributes of an HTML element in form: attributeName="attributeValue". Each element corresponds to distinct attributes for the same tag
	 */
	private List<String> getAttributesHelperForTStructDocText2String(EStructuralFeatureImpl.ContainmentUpdatingFeatureMapEntry entry) {
		if(entry == null)
			return null;
		
		List<String> attributeList = new ArrayList<String>();
		if(entry.getValue() instanceof org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl) {
			for(FeatureMap.Entry attribute : ((org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl) entry.getValue()).getAnyAttribute()) {
				String name = attribute.getEStructuralFeature().getName();
				String value = attribute.getValue().toString();
				if(name != null && !name.isEmpty()) {
					String attributeToAdd = "";
					// we may have attributes which doesn't have any value
					attributeToAdd = attributeToAdd + name;
					if(value != null && !value.isEmpty()) {
						attributeToAdd = attributeToAdd + "=\""+value+"\"";
					}
					attributeList.add(attributeToAdd);
				}
			}
		}
		return attributeList;
	}
}
