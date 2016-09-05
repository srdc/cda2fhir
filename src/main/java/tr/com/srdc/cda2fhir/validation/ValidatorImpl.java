package tr.com.srdc.cda2fhir.validation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.Constants;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ValidatorImpl implements IValidator {

	private String tServerURL = null;
	private static final String definitionsPath = "src/main/resources/validation-min.xml.zip";
	private final org.hl7.fhir.dstu2.validation.ValidationEngine validationEngine = new org.hl7.fhir.dstu2.validation.ValidationEngine();
	private final Logger logger = LoggerFactory.getLogger(CCDTransformerImpl.class);
	
	public ValidatorImpl() {
		tServerURL = Constants.DEFAULT_VALIDATOR_TERMINOLOGY_SERVER_URL;
		try {
			validationEngine.readDefinitions(definitionsPath);
		} catch (IOException e) {
			logger.error("IOException occured while trying to read the definitions for the validatior",e);
		} catch (SAXException e) {
			logger.error("Improper definition for the validator",e);
		} catch (FHIRException e) {
			logger.error("FHIRException occured while trying to read the definitions for the validator",e);
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

	private byte[] tIResource2ByteArray(IResource paramResource) {
		return FHIRUtil.getXML(paramResource).getBytes();
	}
	
	public OutputStream validateResource(IResource resource, boolean validateProfile) {
		if(resource == null) {
			logger.warn("The resource validator was running on was found null. Returning null");
			return null;
		}
		
		if(resource instanceof Bundle) {
			logger.error("Bundle is not a proper parameter for the method Validator.validateResource. Use Validator.validateBundle instead.");
			return null;
		}
		
		// if validateProfile == true, profile <- resource.meta.profile[0]
		if(validateProfile) {
			if(resource.getMeta() != null && resource.getMeta().getProfile() != null && !resource.getMeta().getProfile().isEmpty()) {
				if(resource.getMeta().getProfile().get(0) != null && resource.getMeta().getProfile().get(0).getValue() != null) {
					try {
						this.validationEngine.loadProfile(resource.getMeta().getProfile().get(0).getValue());
					} catch (DefinitionException e) {
						logger.error("DefinitionException occured while trying to load the profile of a FHIR resource altough the profile was defined."
								+ "Validation will continue without using any profile for the resource.",e);
					} catch (Exception e) {
						logger.error("Exception occured while trying to load the profile of a FHIR resource altough the profile was defined."
								+ "Validation will continue without using any profile for the resource.",e);
					}
				}
			}
		}
		// set resource
		this.validationEngine.setSource(this.tIResource2ByteArray(resource));
		// validate!
		try {
			this.validationEngine.process();
		} catch (FHIRException | ParserConfigurationException | TransformerException | SAXException | IOException e) {
			logger.error("Exception occured while trying to validate the FHIR resource. Returning null",e);
			return null;
		}
		
		// direct outcome string to an output stream
		java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
		// notice that html tag is not included in the outcome string
		try {
			String outcomeText = this.validationEngine.getOutcome().getText().getDivAsString();
			outcomeText = "<h3>"+resource.getId() +"</h3>"+outcomeText + "<hr>";
			outputStream.write(outcomeText.getBytes());
		} catch (IOException e) {
			logger.error("Exception occured while trying to write the validation outcome to the output stream. Returning null", e);
			return null;
		}
		// TODO: Notice that the output stream is not closed. Determine if it should be closed
		return outputStream;
	}
	
	public OutputStream validateBundle(Bundle bundle, boolean validateProfile) {
		if(bundle == null) {
			logger.warn("The bundle validator was running on was found null. Returning null");
			return null;
		}
		java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
		for(Bundle.Entry entry : bundle.getEntry()) {
			if(entry != null && entry.getResource() != null) {	
				try {
					java.io.ByteArrayOutputStream byteArrayOutputStream = (java.io.ByteArrayOutputStream)this.validateResource(entry.getResource(), validateProfile);
					if(byteArrayOutputStream != null) {
						byte[] byteArray = byteArrayOutputStream.toByteArray();
						if(byteArray != null)
							outputStream.write(byteArray);
					}
				} catch (IOException e) {
					// TODO: This exception handler will be modified later.
					// Because of the exception caused by condition.onsetDateTime, validation doesn't continue
					logger.error("Exception occured while trying to write the validation outcome to the output stream. Ignoring the outcome",e);
				}	
			} else {
				logger.warn("An entry of the bundle validator was running on was found null. Ignoring the entry");
			}
		}
		
		return outputStream;
	}
}
