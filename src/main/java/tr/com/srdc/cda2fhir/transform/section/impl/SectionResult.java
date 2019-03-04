package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public abstract class SectionResult implements ISectionResult {
	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;

	private CDAIIMap<IEntityInfo> entities;
		
	SectionResult() {
		bundle = new Bundle();
	}
	
	SectionResult(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public boolean hasDefferredReferences() {
		return deferredReferences != null && !deferredReferences.isEmpty();
	}
	
	public void addDeferredReferences(List<IDeferredReference> references) {
		if (deferredReferences == null) {
			deferredReferences = new ArrayList<IDeferredReference>();
		}
		deferredReferences.addAll(references);
	}
	
	@Override
	public List<IDeferredReference> getDeferredReferences() {
		return deferredReferences;
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}
	
	@Override
	public void updateFrom(IEntryResult entryResult) {
		entryResult.copyTo(bundle);
   		if (entryResult.hasDeferredReferences()) {
   			addDeferredReferences(entryResult.getDeferredReferences());
   		}
   		if (entryResult.hasIIMapValues()) {
   			if (entities == null) {
   				entities = new CDAIIMap<IEntityInfo>();
   				entities.put(entryResult);
   			}
   		}
	}

	@Override
	public void putRootValuesTo(Map<String, IEntityInfo> target) {
		if (entities != null) {
			entities.putRootValuesTo(target);
		}
	}

	@Override
	public void putExtensionValuesTo(Map<String, Map<String, IEntityInfo>> target) {
		if (entities != null) {
			entities.putExtensionValuesTo(target);
		}		
	}
	
	@Override
	public boolean hasIIMapValues() {
		return entities != null;
	}
}
