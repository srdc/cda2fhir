package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;

/**
 * Created by mustafa on 7/21/2016.
 */
public interface DataTypesTransformer {

    CodingDt CV2Coding(CV cv);

    CodeableConceptDt CD2CodeableConcept(CD cd);

}
