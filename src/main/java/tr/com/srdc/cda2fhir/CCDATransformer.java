package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

/**
 * Created by mustafa on 8/3/2016.
 */
public interface CCDATransformer {

    /**
     * Sets the resource id generator format, which is either an incremental COUNTER or UUID
     * @param idGen The id generator enumeration to be set
     */
    void setIdGenerator(IdGeneratorEnum idGen);

    /**
     * A consistent unique resource id generator
     * @return a unique resource id
     */
    String getUniqueId();

    /**
     * Returns a ResourceReferenceDt for the patient of the CCD document
     * @return A ResourceReferenceDt that references the patient (i.e. recordTarget/patientRole) of the document
     */
    ResourceReferenceDt getPatientRef();

    /**
     * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to a Bundle of corresponding FHIR resources
     * @param ccd A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to be transformed
     * @return A FHIR Bundle that contains a Composition corresponding to the CCD document and all other resources that are referenced within the Composition.
     */
    Bundle transformCCD(ContinuityOfCareDocument ccd);
}
