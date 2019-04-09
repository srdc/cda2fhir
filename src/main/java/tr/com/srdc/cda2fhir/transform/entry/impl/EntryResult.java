package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.entry.CDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.impl.CDACDMap;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EntryResult implements IEntryResult {
	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;

	private CDAIIMap<IEntityInfo> entities;
	private CDAIIResourceMaps<IBaseResource> resourceMaps;
	private CDACDMap<IBaseResource> cdMap;

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

	@Override
	public void updateFrom(IEntryResult entryResult) {
		entryResult.copyTo(bundle);
		if (entryResult.hasDeferredReferences()) {
			addDeferredReferences(entryResult.getDeferredReferences());
		}
		updateEntitiesFrom(entryResult);
		updateIIResourcesFrom(entryResult);
		updateCDResourcesFrom(entryResult);
	}

	public void addDeferredReferences(List<IDeferredReference> references) {
		if (deferredReferences == null) {
			deferredReferences = new ArrayList<IDeferredReference>();
		}
		deferredReferences.addAll(references);
	}

	public void updateFrom(IEntityResult entityResult) {
		List<II> iis = entityResult.getNewIds();
		if (iis != null) {
			if (bundle == null) {
				bundle = new Bundle();
			}
			entityResult.copyTo(bundle);
			if (entities == null) {
				entities = new CDAIIMap<IEntityInfo>();
			}
			entities.put(iis, entityResult.getInfo());
		} else if (!entityResult.isFromExisting()) {
			if (bundle == null) {
				bundle = new Bundle();
			}
			entityResult.copyTo(bundle);
		}
	}

	public void updateEntitiesFrom(IEntryResult entryResult) {
		if (entryResult.hasEntities()) {
			if (entities == null) {
				entities = new CDAIIMap<IEntityInfo>();
			}
			entities.put(entryResult);

		}
	}

	public void updateIIResourcesFrom(IEntryResult entryResult) {
		if (entryResult.hasIIResourceMaps()) {
			if (resourceMaps == null) {
				resourceMaps = new CDAIIResourceMaps<IBaseResource>();
			}
			resourceMaps.put(entryResult);
		}
	}

	public void updateCDResourcesFrom(IEntryResult entryResult) {
		if (entryResult.hasCDMap()) {
			if (cdMap == null) {
				cdMap = new CDACDMap<IBaseResource>();
			}
			cdMap.put(entryResult);
		}
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
	public boolean hasMapValues() {
		return entities != null || resourceMaps != null || cdMap != null;
	}

	@Override
	public boolean hasResult() {
		return this.bundle != null && this.bundle.hasEntry();
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
	public void putCDValuesTo(Map<String, IBaseResource> target) {
		if (cdMap != null) {
			cdMap.putCDValuesTo(target);
		}
	}

	@Override
	public boolean hasCDMapValues() {
		return cdMap != null && cdMap.hasCDMapValues();
	}

}
