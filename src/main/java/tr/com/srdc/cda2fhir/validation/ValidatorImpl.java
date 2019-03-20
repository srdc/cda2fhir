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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;

public class ValidatorImpl implements IValidator {

	private final Logger logger = LoggerFactory.getLogger(ValidatorImpl.class);

	private FhirContext ctx = FhirContext.forDstu3();

	public ValidatorImpl(FhirContext ctx) {
		this.setCtx(ctx);
	}

	public ValidatorImpl() {
		this.setCtx(FhirContext.forDstu3());
	}

	@Override
	public OutputStream validateBundle(Bundle bundle) {
		if (bundle == null) {
			logger.warn("The bundle to be validated is null. Returning null.");
			return null;
		}
		if (bundle.getEntry().isEmpty()) {
			logger.warn("The bundle to be validated is empty. Returning null");
			return null;
		}

		logger.info("Validating the bundle containing " + bundle.getEntry().size() + " entries");

		// create an output stream to return
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// init the html output
		try {
			outputStream.write("<html>\n\t<body>".getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error("Could not write to the output stream.", e);
		}

		// traverse the entries of the bundle
		for (BundleEntryComponent entry : bundle.getEntry()) {
			if (entry != null && entry.getResource() != null) {
				try {
					// validate the resource contained in the entry
					ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) validateResource(
							entry.getResource());

					if (byteArrayOutputStream != null) {
						byte[] byteArray = byteArrayOutputStream.toByteArray();
						if (byteArray != null)
							outputStream.write(byteArray);
					}
				} catch (IOException e) {
					logger.error(
							"Exception occurred while trying to write the validation outcome to the output stream. Ignoring",
							e);
				}
			} else {
				logger.warn("Null bundle entry found. Ignoring the entry.");
			}
		}

		// last touch to the html output
		try {
			outputStream.write("\n\t</body>\n</html>".getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error("Could not write to the output stream.", e);
		}

		return outputStream;
	}

	public void logValidationResult(ValidationResult result) {
		if (logger.isDebugEnabled()) {
			if (result.isSuccessful()) {
				logger.info("Validation of resource passed.");
			} else {
				logger.info("Validation of resource failed.");
			}
			List<SingleValidationMessage> messages = result.getMessages();
			for (SingleValidationMessage message : messages) {
				logger.debug("Validation Message:");
				logger.debug(" * Location: " + message.getLocationString());
				logger.debug(" * Severity: " + message.getSeverity());
				logger.debug(" * Message : " + message.getMessage());
			}
		}
	}

	private String getOutcomeMessagesString(ValidationResult result) {
		String messagesStr = "";
		if (result.isSuccessful()) {
			messagesStr += "Validation successful\n";
		}
		if (result.getMessages().size() > 0) {
			for (SingleValidationMessage message : result.getMessages()) {
				messagesStr += message.getLocationString() + "\n";
				messagesStr += message.getSeverity() + "\n";
				messagesStr += message.getMessage();
			}
		}
		return messagesStr;

	}

	@Override
	public OutputStream validateResource(IBaseResource resource) {
		if (resource == null) {
			logger.warn("The resource to be validated is null. Returning null");
			return null;
		}

		if (resource instanceof Bundle) {
			logger.error(
					"Bundle is not a proper parameter for the method Validator.validateResource. Use Validator.validateBundle instead.");
			return null;
		}

		logger.info("Validating resource " + resource.getIdElement());

		FhirValidator validator = ctx.newValidator();

		IValidatorModule schemaModule = new SchemaBaseValidator(ctx);
		IValidatorModule schematronModule = new SchematronBaseValidator(ctx);
		validator.registerValidatorModule(schemaModule);
		validator.registerValidatorModule(schematronModule);

		ValidationResult result = validator.validateWithResult(resource);
		logValidationResult(result);

		// direct outcome string to an output stream
		ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

		// notice that html tag is not included in the outcome string
		try {
			String outcomeText = getOutcomeMessagesString(result);
			outcomeText = "<h3>" + resource.getIdElement() + "</h3>" + outcomeText + "<hr>";
			outputStream.write(outcomeText.getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error(
					"Exception occurred while trying to write the validation outcome to the output stream. Returning null",
					e);
			return null;
		}

		return outputStream;
	}

	@Override
	public ValidationResult validateResourceResultOnly(IBaseResource resource) {
		if (resource == null) {
			logger.warn("The resource to be validated is null. Returning null");
			return null;
		}

		logger.info("Validating resource " + resource.getIdElement());

		FhirContext ctx = FhirContext.forDstu3();
		FhirValidator validator = ctx.newValidator();

		IValidatorModule schemaModule = new SchemaBaseValidator(ctx);
		IValidatorModule schematronModule = new SchematronBaseValidator(ctx);
		validator.registerValidatorModule(schemaModule);
		validator.registerValidatorModule(schematronModule);

		return validator.validateWithResult(resource);

	}

	@Override
	public ValidationResult validateFile(String filepath) throws IOException, FileNotFoundException {

		FhirValidator validator = ctx.newValidator();
		validator.setValidateAgainstStandardSchema(true);
		validator.setValidateAgainstStandardSchematron(true);

		String content = IOUtils.toString(new FileReader(filepath));
		IBaseResource resource = ctx.newXmlParser().parseResource(content);
		ValidationResult result = validator.validateWithResult(resource);
		logValidationResult(result);
		return result;
	}

	public FhirContext getCtx() {
		return ctx;
	}

	public void setCtx(FhirContext ctx) {
		this.ctx = ctx;
	}

}
