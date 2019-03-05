package tr.com.srdc.cda2fhir.transform.util;

import java.util.Map;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;

public interface IBundleInfo {
	IResourceTransformer getResourceTransformer();
	
	public Map<String, String> getIdedAnnotations();
}
