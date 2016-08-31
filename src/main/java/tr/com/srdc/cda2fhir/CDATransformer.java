package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

/**
 * Created by mustafa on 8/3/2016.
 */
public interface CDATransformer {

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
     * Returns a ResourceReferenceDt for the patient of the CDA document
     * @return A ResourceReferenceDt that references the patient (i.e. recordTarget/patientRole) of the document
     */
    ResourceReferenceDt getPatientRef();

    /**
     * Transforms a Clinical Document Architecture (CDA) instance to a Bundle of corresponding FHIR resources
     * @param cda A ClinicalDocument (CDA) instance to be transformed
     * @return A FHIR Bundle that contains a Composition corresponding to the CDA document and all other resources that are referenced within the Composition.
     */
    Bundle transformDocument(ClinicalDocument cda);
}
