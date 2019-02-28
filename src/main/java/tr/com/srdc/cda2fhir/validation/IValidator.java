package tr.com.srdc.cda2fhir.validation;

import java.io.FileNotFoundException;
import java.io.IOException;

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

import java.io.OutputStream;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.validation.ValidationResult;

public interface IValidator {

    /**
     * Validates the FHIR resource(s) contained in the FHIR Bundle by using the validation engine supplied by hl7.org
     * @param bundle A FHIR Bundle instance containing the FHIR resource(s) to be validated. If the (DAF) profile is supplied in meta.profile attribute of contained resources, then (DAF) profile validation is enable automatically.
     * @return An output stream containing the validation result(s). The validation results are contained in separate div elements.
     */
    OutputStream validateBundle(Bundle bundle);
    
    /**
     * Validates a FHIR IBaseResource instance by using the validation engine supplied by hl7.org
     * @param resource A FHIR IBaseResource instance. If the (DAF) profile is supplied in meta.profile attribute, then (DAF) profile validation is enable automatically.
     * @return An output stream containing the validation result. The validation result is contained in div element.
     */
    OutputStream validateResource(IBaseResource resource);

    /**
     * Validates a FHIR File
     * @param filepath The path of the file.
     * @return An output object that contains validation results.
     */
    ValidationResult validateFile(String filepath) throws IOException, FileNotFoundException;
}