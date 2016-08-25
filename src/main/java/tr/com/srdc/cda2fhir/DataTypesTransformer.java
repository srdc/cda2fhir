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

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;

/**
 * Created by mustafa on 7/21/2016.
 */
public interface DataTypesTransformer {

	/**
	* Transforms a CDA Act instance to a FHIR AnnotationDt composite datatype.
	* @param act A CDA Act instance
	* @return An AnnotationDt composite datatype
	*/
	AnnotationDt tAct2Annotation(Act act);
	
	/**
	* Transforms a CDA AD instance to a FHIR AddressDt composite datatype.
	* @param ad A CDA AD instance
	* @return An AddressDt composite datatype
	*/
	AddressDt AD2Address(AD ad);
	
	/**
	* Transforms a CDA BIN instance to a FHIR Base64BinaryDt primitive datatype.
	* @param bin A CDA BIN instance
	* @return A Base64BinaryDt primitive datatype
	*/
	Base64BinaryDt tBIN2Base64Binary(BIN bin);
	
	/**
	* Transforms a CDA BL instance to a FHIR BooleanDt primitive datatype.
	* @param bl A CDA BL instance
	* @return A BooleanDt primitive datatype
	*/
	BooleanDt tBL2Boolean(BL bl);
	
	/**
	* Transforms a CDA CD instance to a FHIR CodeableConceptDt composite datatype. Translations of the CD instance are also included.
	* @param cd A CDA CD instance
	* @return A CodeableConceptDt composite datatype
	*/
	CodeableConceptDt tCD2CodeableConcept(CD cd);

	/**
	* Transforms a CDA CD instance to a FHIR CodeableConceptDt composite datatype. Translations of the CD instance are excluded.
	* @param cd A CDA CD instance
	* @return A CodeableConceptDt composite datatype
	*/
	CodeableConceptDt tCD2CodeableConceptExcludingTranslations(CD cd);
	
	/**
	* Transforms a CDA CV instance to a FHIR CodingDt composite datatype.
	* @param cv A CDA CV instance
	* @return A CodingDt composite datatype
	*/
	CodingDt tCV2Coding(CV cv);
	
	/**
	* Transforms a CDA ED instance to a FHIR AttachmentDt composite datatype.
	* @param ed A CDA ED instance
	* @return An AttachmentDt composite datatype
	*/
	AttachmentDt tED2Attachment(ED ed);
	
	/**
	* Transforms a CDA EN instance to a FHIR HumanNameDt composite datatype.
	* @param en A CDA EN instance
	* @return A HumanNameDt composite datatype
	*/
	HumanNameDt tEN2HumanName(EN en); 
	
	/**
	* Transforms a CDA II instance to a FHIR IdentifierDt composite datatype.
	* @param ii A CDA II instance
	* @return A IdentifierDt composite datatype
	*/
	IdentifierDt tII2Identifier(II ii); 
    
	/**
	* Transforms a CDA INT instance to a FHIR IntegerDt primitive datatype.
	* @param myInt A CDA INT instance
	* @return A  IntegerDt primitive datatype
	*/
	IntegerDt tINT2Integer(INT myInt);
    
	/**
	* Transforms a CDA IVL_TS instance to a FHIR PeriodDt composite datatype.
	* @param ivlts A CDA IVL_TS instance
	* @return A PeriodDt composite datatype
	*/
	PeriodDt tIVL_TS2Period(IVL_TS ivlts);
    
	/**
	* Transforms a CDA IVL_PQ instance to a FHIR RangeDt composite datatype.
	* @param ivlpq A CDA IVL_PQ instance
	* @return A RangeDt composite datatype
	*/
	RangeDt tIVL_PQ2Range(IVL_PQ ivlpq);

	/**
	* Transforms a CDA PIVL_TS instance to a FHIR TimingDt composite datatype.
	* @param pivlts A CDA PIVL_TS instance
	* @return A TimingDt composite datatype
	*/
	TimingDt tPIVL_TS2Timing(PIVL_TS pivlts);
	
	/**
	* Transforms a CDA PQ instance to a FHIR SimpleQuantityDt composite datatype.
	* @param pq A CDA PQ instance
	* @return A SimpleQuantityDt composite datatype
	*/
	SimpleQuantityDt tPQ2SimpleQuantityDt(PQ pq);
	
	/**
	* Transforms a CDA PQ instance to a FHIR QuantityDt composite datatype.
	* @param pq A CDA PQ instance
	* @return A QuantityDt composite datatype
	*/
	QuantityDt tPQ2Quantity(PQ pq);
    
	/**
	* Transforms a CDA REAL instance to a FHIR DecimalDt primitive datatype.
	* @param real A CDA REAL instance
	* @return A DecimalDt primitive datatype
	*/
	DecimalDt tREAL2Decimal(REAL real);
    
	/**
	* Transforms a CDA RTO instance to a FHIR RatioDt composite datatype.
	* @param rto A CDA RTO instance
	* @return A RatioDt composite datatype
	*/
	RatioDt tRTO2Ratio(RTO rto);
	
	/**
	* Transforms a CDA ST instance to a FHIR StringDt primitive datatype.
	* @param st A CDA ST instance
	* @return A StringDt datatype
	*/
	StringDt tST2String(ST st);

	/**
	* Transforms a String that includes a date in CDA format to a FHIR DateTimeDt primitive datatype.
	* @param date A String that includes a date in CDA format
	* @return A DateTimeDt primitive datatype
	*/
	DateTimeDt tString2DateTime(String date);

	/**
	* Transforms a CDA StrucDocText instance to a FHIR NarrativeDt composite datatype.
	* @param sdt A CDA StrucDocText instance
	* @return A NarrativeDt composite datatype
	*/
	NarrativeDt tStrucDocText2Narrative(StrucDocText sdt);
	
	/**
	* Transforms a CDA TEL instance to a FHIR ContactPointDt composite datatype.
	* @param tel A CDA TEL instance
	* @return A ContactPointDt composite datatype
	*/
	ContactPointDt tTEL2ContactPoint(TEL tel);
	
	/**
	* Transforms a CDA TS instance to a FHIR DateDt primitive datatype.
	* @param ts A CDA TS instance
	* @return A DateDt primitive datatype
	*/
	DateDt tTS2Date(TS ts);
	
	/**
	* Transforms a CDA TS instance to a FHIR DateTimeDt primitive datatype.
	* @param ts A CDA TS instance
	* @return A DateTimeDt primitive datatype
	*/
	DateTimeDt tTS2DateTime(TS ts);
	
	/**
	* Transforms a CDA TS instance to a FHIR InstantDt primitive datatype.
	* @param ts A CDA TS instance
	* @return A InstantDt primitive datatype
	*/
	InstantDt tTS2Instant(TS ts);
	
	/**
	* Transforms a CDA URL instance to a FHIR UriDt primitive datatype.
	* @param url A CDA URL instance
	* @return A UriDt primitive datatype
	*/
	UriDt tURL2Uri(URL url);
    
}
