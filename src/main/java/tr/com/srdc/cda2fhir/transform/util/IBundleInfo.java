package tr.com.srdc.cda2fhir.transform.util;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;

public interface IBundleInfo {
	IResourceTransformer getResourceTransformer();

	public Map<String, String> getIdedAnnotations();

	public Reference getReferenceByIdentifier(String fhirType, Identifier identifier);
}
