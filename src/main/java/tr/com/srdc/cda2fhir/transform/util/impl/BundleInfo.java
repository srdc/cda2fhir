package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;

public class BundleInfo implements IBundleInfo {
	private IResourceTransformer resourceTransformer;
	private Map<String, String> idedAnnotations = new HashMap<String, String>();
	private IIdentifierMap<Reference> identifiedReferences = new IdentifierMap<Reference>();

	private CDAIIMap<IEntityInfo> entities = new CDAIIMap<IEntityInfo>();

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

	@Override
	public Reference getReferenceByIdentifier(String fhirType, Identifier identifier) {
		return identifiedReferences.get(fhirType, identifier);
	}

	public void putReference(String fhirType, Identifier identifier, Reference reference) {
		identifiedReferences.put(fhirType, identifier, reference);
	}

	public void updateFrom(IEntityResult entityResult) {
		List<II> iis = entityResult.getNewIds();
		if (iis != null) {
			entities.put(iis, entityResult.getInfo());
		}
	}

	public void updateFrom(ICDAIIMapSource<IEntityInfo> source) {
		if (source.hasIIMapValues()) {
			entities.put(source);
		}
	}

	@Override
	public IEntityInfo findEntityResult(II ii) {
		return entities.get(ii);
	}

	@Override
	public IEntityInfo findEntityResult(List<II> iis) {
		return entities.get(iis);
	}
}
