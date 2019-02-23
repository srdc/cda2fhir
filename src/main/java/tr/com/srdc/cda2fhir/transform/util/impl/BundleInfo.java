package tr.com.srdc.cda2fhir.transform.util.impl;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class BundleInfo implements IBundleInfo {
	private IResourceTransformer resourceTransformer;
	
	public BundleInfo(IResourceTransformer resourceTransformer) {
		this.resourceTransformer = resourceTransformer;
	}

	@Override
	public IResourceTransformer getResourceTransformer() {
		return resourceTransformer;
	}
}
