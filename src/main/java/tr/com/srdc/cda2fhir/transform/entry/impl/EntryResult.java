package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EntryResult implements IEntryResult {
	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;

	//public EntryResult(Bundle bundle) {
	//	this.bundle = bundle;
	//}

	//public EntryResult() {}
	
	@Override
	public Bundle getBundle() {
		return bundle;
	}

	@Override
	public void copyTo(Bundle bundle) {
		if (bundle != null) {
			FHIRUtil.mergeBundle(this.bundle, bundle);
		}
	}

	public void addResource(Resource resource) {
		if (bundle == null) {
			bundle = new Bundle();
		}
		bundle.addEntry(new BundleEntryComponent().setResource(resource));		
	}
	
	public void updateFrom(IEntityResult entityResult) {
		if (bundle == null) {
			bundle = new Bundle();
		}
		entityResult.copyTo(bundle);
	}
	
	public void addDeferredReference(IDeferredReference deferredReference) {
		if (deferredReferences == null) {
			deferredReferences = new ArrayList<IDeferredReference>();
		}
		deferredReferences.add(deferredReference);
	}

	@Override
	public boolean hasDeferredReferences() {
		return deferredReferences != null && !deferredReferences.isEmpty();
	}

	@Override
	public List<IDeferredReference> getDeferredReferences() {
		return deferredReferences;
	}
}
