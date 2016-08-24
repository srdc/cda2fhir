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

	AnnotationDt tAct2Annotation(Act act);
	
	AddressDt AD2Address(AD ad);
	
	Base64BinaryDt tBIN2Base64Binary(BIN bin);
	
	BooleanDt tBL2Boolean(BL bl);
	
	CodeableConceptDt tCD2CodeableConcept(CD cd);

	CodeableConceptDt tCD2CodeableConceptExcludingTranslations(CD cd);
	
	CodingDt tCV2Coding(CV cv);
	
	AttachmentDt tED2Attachment(ED ed);
	
	HumanNameDt tEN2HumanName(EN en); 
	
	IdentifierDt tII2Identifier(II ii); 
    
	IntegerDt tINT2Integer(INT myInt);
    
	PeriodDt tIVL_TS2Period(IVL_TS ivlts);
    
	RangeDt tIVL_PQ2Range(IVL_PQ ivlpq);

	TimingDt tPIVL_TS2Timing(PIVL_TS pivlts);
	
	SimpleQuantityDt tPQ2SimpleQuantityDt( PQ pq );
	
	QuantityDt tPQ2Quantity(PQ pq);
    
	DecimalDt tREAL2Decimal(REAL real);
    
	RatioDt tRTO2Ratio(RTO rto);
	
	StringDt tST2String(ST st);

	DateTimeDt tString2DateTime(String date);

	NarrativeDt tStrucDocText2Narrative(StrucDocText sdt);
	
	ContactPointDt tTEL2ContactPoint(TEL tel);
	
	DateDt tTS2Date(TS ts);
	
	DateTimeDt tTS2DateTime(TS ts);
	
	InstantDt tTS2Instant(TS ts);
	
	UriDt tURL2Uri(URL url);
    
}
