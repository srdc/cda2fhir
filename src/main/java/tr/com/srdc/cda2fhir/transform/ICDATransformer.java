package tr.com.srdc.cda2fhir.transform;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public interface ICDATransformer {

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
