package tr.com.srdc.cda2fhir.transform.util;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;

public interface IDeferredReference {
	public String getFhirType();

	public Identifier getIdentifier();

	public void resolve(Reference reference);
}
