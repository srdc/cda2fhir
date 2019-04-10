package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.transform.entry.CDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.impl.CDACDMap;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public abstract class SectionResult implements ISectionResult {

	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;
	private CDAIIMap<IEntityInfo> entities;
	private CDAIIResourceMaps<IBaseResource> resourceMaps;
	private CDACDMap<IBaseResource> cdMap;

	SectionResult() {
		bundle = new Bundle();
	}

	SectionResult(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public boolean hasDeferredReferences() {
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
			}
			if (resourceMaps == null) {
				resourceMaps = new CDAIIResourceMaps<IBaseResource>();
			}

			if (cdMap == null) {
				cdMap = new CDACDMap<IBaseResource>();
			}
			entities.put(entryResult);
			resourceMaps.put(entryResult);
			cdMap.put(entryResult);
		}
	}

	@Override
	public void putCDValuesTo(Map<String, Map<String, IBaseResource>> target) {
		if (cdMap != null) {
			cdMap.putCDValuesTo(target);
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
	public boolean hasEntities() {
		return entities != null;
	}

	@Override
	public boolean hasIIResourceMaps() {
		return resourceMaps != null;
	}

	@Override
	public boolean hasCDMap() {
		return cdMap != null;
	}

	@Override
	public boolean hasIIMapValues() {
		return entities != null || resourceMaps != null;
	}

	@Override
	public boolean hasResourceMaps() {
		return resourceMaps != null;
	}

	@Override
	public void putRootValuesTo(Class<? extends IBaseResource> clazz, Map<String, IBaseResource> target) {
		if (resourceMaps != null) {
			resourceMaps.putRootValuesTo(clazz, target);
		}
	}

	@Override
	public void putExtensionValuesTo(Class<? extends IBaseResource> clazz,
			Map<String, Map<String, IBaseResource>> target) {
		if (resourceMaps != null) {
			resourceMaps.putExtensionValuesTo(clazz, target);
		}
	}

	@Override
	public CDAIIMap<IBaseResource> getMap(Class<? extends IBaseResource> clazz) {
		return resourceMaps.getMap(clazz);
	}

	@Override
	public void putMap(Class<? extends IBaseResource> clazz, CDAIIMap<IBaseResource> map) {
		resourceMaps.putMap(clazz, map);
	}

	@Override
	public boolean hasCDMapValues() {
		return cdMap != null && cdMap.hasCDMapValues();
	}

	@Override
	public boolean hasMapValues() {
		return entities != null || resourceMaps != null || cdMap != null;
	}

	@Override
	public void putCDResource(CD cd, IBaseResource resource) {
		cdMap.put(cd, resource);
	}

}
