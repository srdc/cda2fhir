package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
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
}
