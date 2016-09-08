package tr.com.srdc.cda2fhir.validation;

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

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;

import java.io.OutputStream;

public interface IValidator {

	/**
	 * Sets the terminology server of the validator object and make the connection to the given server.
	 * @param tServerURL A terminology server URL String
	 */
    void setTerminologyServer(String tServerURL);

    /**
     * Validates a FHIR IResource instance by using the validation engine supplied by hl7.org
     * @param resource A FHIR IResource instance. If the (DAF) profile is supplied in meta.profile attribute, then (DAF) profile validation is enable automatically.
     * @return An output stream containing the validation result. The validation result is contained in div element.
     */
    OutputStream validateResource(IResource resource);

    /**
     * Validates the FHIR resource(s) contained in the FHIR Bundle by using the validation engine supplied by hl7.org
     * @param bundle A FHIR Bundle instance containing the FHIR resource(s) to be validated. If the (DAF) profile is supplied in meta.profile attribute of contained resources, then (DAF) profile validation is enable automatically.
     * @return An output stream containing the validation result(s). The validation results are contained in separate div elements.
     */
    OutputStream validateBundle(Bundle bundle);
}