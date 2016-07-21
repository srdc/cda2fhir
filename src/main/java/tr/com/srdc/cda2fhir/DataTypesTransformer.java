package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

/**
 * Created by mustafa on 7/21/2016.
 */
public interface DataTypesTransformer {

    CodingDt CV2Coding(CV cv);

    CodeableConceptDt CD2CodeableConcept(CD cd);
    
    PeriodDt IVL_TS2Period(IVL_TS ivlts);
    
    DateTimeDt TS2DateTime(TS ts);

}
