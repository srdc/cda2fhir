package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public class EntryResult implements IEntryResult {
	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;

	public EntryResult(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}

	public boolean hasDeferredReference() {
		return !deferredReferences.isEmpty();
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
