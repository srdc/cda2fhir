package tr.com.srdc.cda2fhir.transform.util;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;

public interface IDeferredReference {
	public String getFhirType();

	public Identifier getIdentifier();

	public Resource getResource();

	public void resolve(Reference reference);
}
