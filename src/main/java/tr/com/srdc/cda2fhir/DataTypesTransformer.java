package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;

/**
 * Created by mustafa on 7/21/2016.
 */
public interface DataTypesTransformer {

	/**
	* Transforms a CDA AD instance to a FHIR AddressDt composite datatype instance.
	* @param ad A CDA AD instance
	* @return An AddressDt composite datatype instance
	*/
	AddressDt AD2Address(AD ad);
	
	/**
	* Transforms a CDA BIN instance to a FHIR Base64BinaryDt primitive datatype instance.
	* @param bin A CDA BIN instance
	* @return A Base64BinaryDt primitive datatype instance
	*/
	Base64BinaryDt tBIN2Base64Binary(BIN bin);
	
	/**
	* Transforms a CDA BL instance to a FHIR BooleanDt primitive datatype instance.
	* @param bl A CDA BL instance
	* @return A BooleanDt primitive datatype instance
	*/
	BooleanDt tBL2Boolean(BL bl);
	
	/**
	* Transforms a CDA CD instance to a FHIR CodeableConceptDt composite datatype instance. Translations of the CD instance are also included.
	* @param cd A CDA CD instance
	* @return A CodeableConceptDt composite datatype instance
	*/
	CodeableConceptDt tCD2CodeableConcept(CD cd);

	/**
	* Transforms a CDA CD instance to a FHIR CodeableConceptDt composite datatype instance. Translations of the CD instance are excluded.
	* @param cd A CDA CD instance
	* @return A CodeableConceptDt composite datatype instance
	*/
	CodeableConceptDt tCD2CodeableConceptExcludingTranslations(CD cd);
	
	/**
	* Transforms a CDA CV instance to a FHIR CodingDt composite datatype instance.
	* @param cv A CDA CV instance
	* @return A CodingDt composite datatype instance
	*/
	CodingDt tCV2Coding(CV cv);
	
	/**
	* Transforms a CDA ED instance to a FHIR AttachmentDt composite datatype instance.
	* @param ed A CDA ED instance
	* @return An AttachmentDt composite datatype instance
	*/
	AttachmentDt tED2Attachment(ED ed);
	
	/**
	* Transforms a CDA EN instance to a FHIR HumanNameDt composite datatype instance.
	* @param en A CDA EN instance
	* @return A HumanNameDt composite datatype instance
	*/
	HumanNameDt tEN2HumanName(EN en); 
	
	/**
	* Transforms a CDA II instance to a FHIR IdentifierDt composite datatype instance.
	* @param ii A CDA II instance
	* @return A IdentifierDt composite datatype instance
	*/
	IdentifierDt tII2Identifier(II ii); 
    
	/**
	* Transforms a CDA INT instance to a FHIR IntegerDt primitive datatype instance.
	* @param myInt A CDA INT instance
	* @return A  IntegerDt primitive datatype instance
	*/
	IntegerDt tINT2Integer(INT myInt);
    
	/**
	* Transforms a CDA IVL_TS instance to a FHIR PeriodDt composite datatype instance.
	* @param ivlts A CDA IVL_TS instance
	* @return A PeriodDt composite datatype instance
	*/
	PeriodDt tIVL_TS2Period(IVL_TS ivlts);
    
	/**
	* Transforms a CDA IVL_PQ instance to a FHIR RangeDt composite datatype instance.
	* @param ivlpq A CDA IVL_PQ instance
	* @return A RangeDt composite datatype instance
	*/
	RangeDt tIVL_PQ2Range(IVL_PQ ivlpq);

	/**
	* Transforms a CDA PIVL_TS instance to a FHIR TimingDt composite datatype instance.
	* @param pivlts A CDA PIVL_TS instance
	* @return A TimingDt composite datatype instance
	*/
	TimingDt tPIVL_TS2Timing(PIVL_TS pivlts);
	
	/**
	* Transforms a CDA PQ instance to a FHIR SimpleQuantityDt composite datatype instance.
	* @param pq A CDA PQ instance
	* @return A SimpleQuantityDt composite datatype instance
	*/
	SimpleQuantityDt tPQ2SimpleQuantityDt(PQ pq);
	
	/**
	* Transforms a CDA PQ instance to a FHIR QuantityDt composite datatype instance.
	* @param pq A CDA PQ instance
	* @return A QuantityDt composite datatype instance
	*/
	QuantityDt tPQ2Quantity(PQ pq);
    
	/**
	* Transforms a CDA REAL instance to a FHIR DecimalDt primitive datatype instance.
	* @param real A CDA REAL instance
	* @return A DecimalDt primitive datatype instance
	*/
	DecimalDt tREAL2Decimal(REAL real);
    
	/**
	* Transforms a CDA RTO instance to a FHIR RatioDt composite datatype instance.
	* @param rto A CDA RTO instance
	* @return A RatioDt composite datatype instance
	*/
	RatioDt tRTO2Ratio(RTO rto);
	
	/**
	* Transforms a CDA ST instance to a FHIR StringDt primitive datatype instance.
	* @param st A CDA ST instance
	* @return A StringDt datatype
	*/
	StringDt tST2String(ST st);

	/**
	* Transforms a String that includes a date in CDA format to a FHIR DateTimeDt primitive datatype instance.
	* @param date A String that includes a date in CDA format
	* @return A DateTimeDt primitive datatype instance
	*/
	DateTimeDt tString2DateTime(String date);

	/**
	* Transforms a CDA StrucDocText instance to a FHIR NarrativeDt composite datatype instance.
	* @param sdt A CDA StrucDocText instance
	* @return A NarrativeDt composite datatype instance
	*/
	NarrativeDt tStrucDocText2Narrative(StrucDocText sdt);
	
	/**
	* Transforms a CDA TEL instance to a FHIR ContactPointDt composite datatype instance.
	* @param tel A CDA TEL instance
	* @return A ContactPointDt composite datatype instance
	*/
	ContactPointDt tTEL2ContactPoint(TEL tel);
	
	/**
	* Transforms a CDA TS instance to a FHIR DateDt primitive datatype instance.
	* @param ts A CDA TS instance
	* @return A DateDt primitive datatype instance
	*/
	DateDt tTS2Date(TS ts);
	
	/**
	* Transforms a CDA TS instance to a FHIR DateTimeDt primitive datatype instance.
	* @param ts A CDA TS instance
	* @return A DateTimeDt primitive datatype instance
	*/
	DateTimeDt tTS2DateTime(TS ts);
	
	/**
	* Transforms a CDA TS instance to a FHIR InstantDt primitive datatype instance.
	* @param ts A CDA TS instance
	* @return A InstantDt primitive datatype instance
	*/
	InstantDt tTS2Instant(TS ts);
	
	/**
	* Transforms a CDA URL instance to a FHIR UriDt primitive datatype instance.
	* @param url A CDA URL instance
	* @return A UriDt primitive datatype instance
	*/
	UriDt tURL2Uri(URL url);
    
}
