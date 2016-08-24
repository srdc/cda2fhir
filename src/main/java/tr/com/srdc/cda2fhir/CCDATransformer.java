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
     * Sets the resource id generator format, which is either a COUNTER or UUID
     * @param idGen The id generator enumeration to be set
     */
    void setIdGenerator(IdGeneratorEnum idGen);

    /**
     * A consistent unique resource id generator
     * @return a unique resource id
     */
    String getUniqueId();

    ResourceReferenceDt getPatientRef();

    Bundle transformCCD(ContinuityOfCareDocument ccd);
}
