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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ValidatorImpl implements IValidator {

	private String tServerURL = null;
	private static final String definitionsPath = "src/main/resources/validation-min.xml.zip";
	private final org.hl7.fhir.dstu2.validation.ValidationEngine validationEngine = new org.hl7.fhir.dstu2.validation.ValidationEngine();
	private final Logger logger = LoggerFactory.getLogger(ValidatorImpl.class);
	
	/**
	 * Constructs a validator using the default configuration.
	 */
	public ValidatorImpl() {
		tServerURL = Config.DEFAULT_VALIDATOR_TERMINOLOGY_SERVER_URL;
		try {
			validationEngine.readDefinitions(definitionsPath);
		} catch (IOException e) {
			logger.error("IOException occurred while trying to read the definitions for the validator",e);
		} catch (SAXException e) {
			logger.error("Improper definition for the validator",e);
		} catch (FHIRException e) {
			logger.error("FHIRException occurred while trying to read the definitions for the validator",e);
		}
		try {
			validationEngine.connectToTSServer(tServerURL);
		} catch (URISyntaxException ex) {
			logger.error("Terminology server URL string could not be parsed as a URI reference", ex);
		}
	}
	
	public void setTerminologyServer(String paramTServerURL) {
		this.tServerURL = paramTServerURL;
		try {
			validationEngine.connectToTSServer(tServerURL);
		} catch (URISyntaxException ex) {
			logger.error("Terminology server URL string could not be parsed as a URI reference", ex);
		}
	}

	/**
	 * Transforms a FHIR IResource instance to a byte array. 
	 * @param paramResource a FHIR IResource instance
	 * @return A byte array
	 */
	private byte[] tIResource2ByteArray(IResource paramResource) {
		return FHIRUtil.encodeToXML(paramResource).getBytes();
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
			outputStream.write("<html>\n\t<body>".getBytes());
		} catch (IOException e) {
			logger.error("Could not write to the output stream.", e);
		}

		// traverse the entries of the bundle
		for(Bundle.Entry entry : bundle.getEntry()) {
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
			outputStream.write("\n\t</body>\n</html>".getBytes());
		} catch (IOException e) {
			logger.error("Could not write to the output stream.", e);
		}
		
		return outputStream;
	}
	
	public OutputStream validateResource(IResource resource) {
		if(resource == null) {
			logger.warn("The resource validator was running on was found null. Returning null");
			return null;
		}
		
		if(resource instanceof Bundle) {
			logger.error("Bundle is not a proper parameter for the method Validator.validateResource. Use Validator.validateBundle instead.");
			return null;
		}
		
		logger.info("Validating resource "+resource.getId());
		
		// set resource
		this.validationEngine.setSource(this.tIResource2ByteArray(resource));
		
		// validate!
		try {
			this.validationEngine.process();
		} catch (FHIRException | ParserConfigurationException | TransformerException | SAXException | IOException e) {
			logger.error("Exception occurred while trying to validate the FHIR resource. Returning exception message",e);
			String exceptionAsHtml = "<h3>" + resource.getId() + "</h3>" + "Exception occurred while validating this resource:<br>"
					+ e.getMessage()+"<hr>";
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(exceptionAsHtml.getBytes());
				return outputStream;
			} catch (IOException e1) {
				logger.error("Exception occurred while trying to write the exception outcome to the output stream. Ignoring ");
			}
		}
		
		// direct outcome string to an output stream
		java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
		
		// notice that html tag is not included in the outcome string
		try {
			String outcomeText = this.validationEngine.getOutcome().getText().getDivAsString();
			outcomeText = "<h3>" + resource.getId() + "</h3>" + outcomeText + "<hr>";
			outputStream.write(outcomeText.getBytes());
		} catch (IOException e) {
			logger.error("Exception occurred while trying to write the validation outcome to the output stream. Returning null", e);
			return null;
		}
		
		return outputStream;
	}
}
