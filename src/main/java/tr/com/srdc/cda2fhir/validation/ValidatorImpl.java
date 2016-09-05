package tr.com.srdc.cda2fhir.validation;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hl7.fhir.exceptions.FHIRException;
import org.xml.sax.SAXException;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;

public class ValidatorImpl implements IValidator {
	
	private String tsServer = null;
	private String definitions = null;
	private String resourcePath = null;
	private IResource resource = null;
	private Bundle resourceBundle = null;
	private org.hl7.fhir.dstu2.validation.ValidationEngine validationEngine = null;
	
	public ValidatorImpl() {
		this.tsServer = "http://fhir2.healthintersections.com.au/open";
		this.validationEngine = new org.hl7.fhir.dstu2.validation.ValidationEngine();
	}
	
	public void setTsServer(String tsServerUrl) {
		this.tsServer = tsServerUrl;
	}
	
	public String getTsServer() {
		return this.tsServer;
	}
	
	public void setDefinitions(String definitionPath) {
		this.definitions = definitionPath;
	}
	
	public String getDefinitions() {
		return this.definitions;
	}
	
	public void setResource(IResource paramResource) {
		this.resourcePath = null;
		this.resourceBundle = null;
		this.resource = paramResource;
	}
	
	public void setResource(Bundle paramResourceBundle) {
		this.resourcePath = null;
		this.resource = null;
		this.resourceBundle = paramResourceBundle;
	}
	
	public void setResource(String paramResourcePath) {
		this.resource = null;
		this.resourceBundle = null;
		this.resourcePath = paramResourcePath;
	}
	
	public String getResource() {
		return this.resourcePath;
	}

	private byte[] loadSourceFromFile(String paramResourcePath) throws IOException {
		byte[] arrayOfByte;
		// loading from file
		if(new java.io.File(paramResourcePath).exists()) {
			java.io.FileInputStream localFileInputStream = new java.io.FileInputStream(this.resourcePath);
		    arrayOfByte = new byte[localFileInputStream.available()];
		    localFileInputStream.read(arrayOfByte);
		    localFileInputStream.close();
		} else {
			arrayOfByte = paramResourcePath.getBytes();
		}
		return arrayOfByte;
	}
	
	private byte[] loadSourceFromIResource(IResource resource) throws IOException {
		// setting source 
		java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
		java.io.ObjectOutput objectOutput = new java.io.ObjectOutputStream(byteArrayOutputStream);
		objectOutput.writeObject(this.resource);
		return byteArrayOutputStream.toByteArray();

	}
	
	public String getOutcome() throws IOException {
		java.io.ByteArrayOutputStream localByteArrayOutputStream = new java.io.ByteArrayOutputStream();
	    new org.hl7.fhir.dstu2.formats.XmlParser().compose(localByteArrayOutputStream, this.validationEngine.getOutcome(), true);
	    localByteArrayOutputStream.close();
	    return localByteArrayOutputStream.toString();
	}
	
	/*
	 * recursive method
	 */
	public void process() throws IOException, SAXException, FHIRException, URISyntaxException, ParserConfigurationException, TransformerException {
		// commons: setting definitions and tsServer
		this.validationEngine.readDefinitions(this.definitions);
		this.validationEngine.connectToTSServer(this.tsServer == null ? "http://fhir2.healthintersections.com.au/open": this.tsServer);
		
		if(this.resourcePath != null) {
			this.validationEngine.setSource(loadSourceFromFile(this.resourcePath));
			this.validationEngine.process();
		} else if(this.resource != null) {
			this.validationEngine.setSource(loadSourceFromIResource(resource));
			// load profile if present
			if(this.resource.getMeta() != null && this.resource.getMeta().getProfile() != null && !this.resource.getMeta().getProfile().isEmpty()) {
				this.validationEngine.setProfileURI(this.resource.getMeta().getProfile().get(0).getValue());
			}
			this.validationEngine.process();
		} else if(this.resourceBundle != null) {
			// validating the bundle
			this.validationEngine.setSource(loadSourceFromIResource(resourceBundle));
			this.validationEngine.process();
			// validating the entries of the bundle
			// since we will assign this.resourceBundle to null, let's keep the bundle information in another variable
			Bundle tempBundle = this.resourceBundle;
			for(Bundle.Entry entry : tempBundle.getEntry()) {
				if(entry != null && entry.getResource() != null) {
					setResource(entry.getResource());
					this.validationEngine.process();
				}
			}
		}
		
	}
}
