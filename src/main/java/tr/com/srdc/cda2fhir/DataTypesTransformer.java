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

	AnnotationDt Act2Annotation(Act act);
	
	AddressDt AD2Address(AD ad);
	
	Base64BinaryDt BIN2Base64Binary(BIN bin);
	
	BooleanDt BL2Boolean(BL bl);
	
	CodeableConceptDt CD2CodeableConcept(CD cd);
	
	CodingDt CV2Coding(CV cv);
	
	AttachmentDt ED2Attachment(ED ed);
	
	HumanNameDt EN2HumanName(EN en); 
	
	IdentifierDt II2Identifier(II ii); 
    
	IntegerDt INT2Integer(INT myInt);
    
	PeriodDt IVL_TS2Period(IVL_TS ivlts);
    
	RangeDt IVL_PQ2Range(IVL_PQ ivlpq);

	TimingDt PIVL_TS2Timing(PIVL_TS pivlts);
	
	SimpleQuantityDt PQ2SimpleQuantityDt( PQ pq );
	
	QuantityDt PQ2Quantity(PQ pq);
    
	DecimalDt REAL2Decimal(REAL real);
    
	RatioDt RTO2Ratio(RTO rto);
	
	StringDt ST2String(ST st);

	DateTimeDt String2DateTime(String date);

	NarrativeDt StrucDocText2Narrative(StrucDocText sdt);
	
	ContactPointDt TEL2ContactPoint(TEL tel);
	
	DateDt TS2Date(TS ts);
	
	DateTimeDt TS2DateTime(TS ts);
	
	InstantDt TS2Instant(TS ts);
	
	UriDt URL2Uri(URL url);
    
}
