package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class EntryResult implements IEntryResult {
	private Bundle bundle;
	private List<IDeferredReference> deferredReferences;

	private CDAIIMap<IEntityInfo> entities;

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
		if (entryResult.hasIIMapValues()) {
			if (entities == null) {
				entities = new CDAIIMap<IEntityInfo>();
			}
			entities.put(entryResult);
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
	public boolean hasIIMapValues() {
		return entities != null;
	}
}
