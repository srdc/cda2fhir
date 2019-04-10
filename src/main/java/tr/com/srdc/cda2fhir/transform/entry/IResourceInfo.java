package tr.com.srdc.cda2fhir.transform.entry;

import org.hl7.fhir.instance.model.api.IBaseResource;

public interface IResourceInfo {

	IBaseResource getResource();

	boolean isNewResource();

	void setNewResource(IBaseResource resource);

	void setExistingResource(IBaseResource resource);
}
