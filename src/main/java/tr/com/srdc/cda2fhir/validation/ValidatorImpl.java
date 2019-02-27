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

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;

import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

import tr.com.srdc.cda2fhir.conf.Config;

public class ValidatorImpl implements IValidator {

	private String tServerURL = null;
	private final org.hl7.fhir.dstu2.validation.ValidationEngine validationEngine = new org.hl7.fhir.dstu2.validation.ValidationEngine();
	private final Logger logger = LoggerFactory.getLogger(ValidatorImpl.class);
	
	/**
	 * Constructs a validator using the default configuration.
	 */
	public ValidatorImpl() {
		// searching for an available terminology server
		for(String tServerURLString : Config.VALIDATOR_TERMINOLOGY_SERVER_URLS) {
			/*
			 *  if the request is successful,  set tServerURL with that available terminology server and break the loop.
			 *   .. otherwise, catch the exception and continue with the next terminology server URL string contained in the config.
			 */
			boolean checkResult = checkServer(tServerURLString);
			if(checkResult) {
				this.tServerURL = tServerURLString;
				logger.info("Terminology server is successfully set as: {}", tServerURLString);
				break;
			} else {
				logger.warn("Could not reach terminology server at {} . Trying the next alternative ...", tServerURLString);
				continue;
			}
		}

		// if the set of terminology servers did not work, proceed with the fallback option even if it is not reachable
		// because the HL7 validation engine mandates setting a terminology server
		if(this.tServerURL == null) {
			this.tServerURL = Config.DEFAULT_VALIDATOR_TERMINOLOGY_SERVER_URL;
			logger.warn("None of the terminology server alternatives was reachable. Proceeding with the fallback option {}", this.tServerURL);
		}
		
		// reading definitions
		try {
			validationEngine.readDefinitions(Config.VALIDATION_DEFINITION_PATH);
		} catch (IOException e) {
			logger.error("IOException occurred while trying to read the definitions for the validator", e);
		} catch (SAXException e) {
			logger.error("Improper definition for the validator", e);
		} catch (FHIRException e) {
			logger.error("FHIRException occurred while trying to read the definitions for the validator", e);
		}
		
		// calling the validation engine's connect method for terminology service
		setTerminologyServer(this.tServerURL);
	}
	
	public void setTerminologyServer(String paramTServerURL) {
		this.tServerURL = paramTServerURL;
		try {
			validationEngine.connectToTSServer(tServerURL);
		} catch (URISyntaxException ex) {
			logger.error("Terminology server URL string could not be parsed as a URI reference", ex);
		}
	}

	public OutputStream validateBundle(Bundle bundle) {
		if(bundle == null) {
			logger.warn("The bundle to be validated is null. Returning null.");
			return null;
		}
		if(bundle.getEntry().isEmpty()) {
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
		for(BundleEntryComponent entry : bundle.getEntry()) {
			if(entry != null && entry.getResource() != null) {	
				try {
					// validate the resource contained in the entry
					ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) validateResource(entry.getResource());
					
					if(byteArrayOutputStream != null) {
						byte[] byteArray = byteArrayOutputStream.toByteArray();
						if(byteArray != null)
							outputStream.write(byteArray);
					}
				} catch (IOException e) {
					logger.error("Exception occurred while trying to write the validation outcome to the output stream. Ignoring", e);
				}	
			} else {
				logger.warn("An entry of the bundle validator was running on was found null. Ignoring the entry.");
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
	
	private void logValidationResult(ValidationResult result) {
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
		if(result.isSuccessful()) {
			messagesStr += "Validation successful\n";
		}
		if(result.getMessages().size() > 0) {
			for (SingleValidationMessage message : result.getMessages()) {
				messagesStr += message.getLocationString() + "\n";
				messagesStr += message.getSeverity() + "\n";
				messagesStr += message.getMessage();
		 	}    	
		}
		return messagesStr;
			
	}
	
	public OutputStream validateResource(IBaseResource resource) {
		if(resource == null) {
			logger.warn("The resource to be validated is null. Returning null");
			return null;
		}
		
		if(resource instanceof Bundle) {
			logger.error("Bundle is not a proper parameter for the method Validator.validateResource. Use Validator.validateBundle instead.");
			return null;
		}
		
		logger.info("Validating resource " + resource.getIdElement());
		
		FhirContext ctx = FhirContext.forDstu3();
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
			String outcomeText =  getOutcomeMessagesString(result);
			outcomeText = "<h3>" + resource.getIdElement() + "</h3>" + outcomeText + "<hr>";
			outputStream.write(outcomeText.getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error("Exception occurred while trying to write the validation outcome to the output stream. Returning null", e);
			return null;
		}
		
		return outputStream;
	}
	
	/**
	 * Sends an HTTP GET request to a server to check if the server is available
	 * @param serverURLString A string that contains the URL of the server
	 * @return A boolean indicating if the server is available
	 */
	private boolean checkServer(String serverURLString) {
		try {
			URL url = new URL(serverURLString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(Config.DEFAULT_VALIDATOR_TERMINOLOGY_SERVER_CHECK_TIMEOUT);
			con.connect();
			if (con.getResponseCode() < 300)
				return true;
			else
				return false;
		} catch(Exception e) {
			logger.error("Exception occurred while trying to reach the server at {}", serverURLString, e);
			return false;
		}
	}

    public ValidationResult validateFile(String filepath) throws IOException, FileNotFoundException {
		FhirContext ctx = FhirContext.forDstu3();
		FhirValidator validator = ctx.newValidator();
		validator.setValidateAgainstStandardSchema(true);
		validator.setValidateAgainstStandardSchematron(true);
		
		String content = IOUtils.toString(new FileReader(filepath));
    	IBaseResource resource = ctx.newXmlParser().parseResource(content);
    	ValidationResult result = validator.validateWithResult(resource);
    	logValidationResult(result);
    	return result;
    }
    
    public void logValidationErrors(ValidationResult result) {	
		if (!result.isSuccessful()) {
			List<SingleValidationMessage> messages = result.getMessages();
			if(messages.size() > 0) {
				for (SingleValidationMessage message : messages) {
	    		   logger.debug("Validation Message:");
	    		   logger.debug(" * Location: " + message.getLocationString());
	    		   logger.debug(" * Severity: " + message.getSeverity());
	    		   logger.debug(" * Message : " + message.getMessage());
		    	}    
			}	
		} else {
			logger.debug("No validation errors.");
		}
	}
}
