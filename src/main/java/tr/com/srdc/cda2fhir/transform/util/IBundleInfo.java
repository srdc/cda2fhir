package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;

public interface IBundleInfo {
	IResourceTransformer getResourceTransformer();

	Map<String, String> getIdedAnnotations();

	Reference getReferenceByIdentifier(String fhirType, Identifier identifier);
	
	IEntityInfo findEntityResult(II ii);

	IEntityInfo findEntityResult(List<II> iis);
}
