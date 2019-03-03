package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class LocalBundleInfo implements IBundleInfo {
	private IBundleInfo bundleInfo;
	private CDAIIMap<IEntityInfo> entities = new CDAIIMap<IEntityInfo>();
	
	public LocalBundleInfo(IBundleInfo bundleInfo) {
		this.bundleInfo = bundleInfo;
	}

	@Override
	public IResourceTransformer getResourceTransformer() {
		return bundleInfo.getResourceTransformer();
	}

	@Override
	public Map<String, String> getIdedAnnotations() {
		return bundleInfo.getIdedAnnotations();
	}

	@Override
	public Reference getReferenceByIdentifier(String fhirType, Identifier identifier) {
		return bundleInfo.getReferenceByIdentifier(fhirType, identifier);
	}
	
	@Override
	public IEntityInfo findEntityResult(II ii) {
		IEntityInfo result = bundleInfo.findEntityResult(ii);
		if (result == null) {
			return entities.get(ii);
		}
		return result;
	}

	@Override
	public IEntityInfo findEntityResult(List<II> iis) {
		IEntityInfo result = bundleInfo.findEntityResult(iis);
		if (result == null) {
			return entities.get(iis);
		}
		return result;
	}
}
