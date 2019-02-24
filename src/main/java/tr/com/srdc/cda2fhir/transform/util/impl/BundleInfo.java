package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.Map;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class BundleInfo implements IBundleInfo {
	private IResourceTransformer resourceTransformer;
	private Map<String, String> idedAnnotations = new HashMap<String, String>();
	
	public BundleInfo(IResourceTransformer resourceTransformer) {
		this.resourceTransformer = resourceTransformer;
	}

	@Override
	public IResourceTransformer getResourceTransformer() {
		return resourceTransformer;
	}
	
	@Override
	public Map<String, String> getIdedAnnotations() {
		return idedAnnotations;		
	}
	
	public void mergeIdedAnnotations(Map<String, String> newAnnotations) {
		idedAnnotations.putAll(newAnnotations);
	}
}
