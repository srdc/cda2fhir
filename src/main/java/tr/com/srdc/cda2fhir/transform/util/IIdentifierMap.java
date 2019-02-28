package tr.com.srdc.cda2fhir.transform.util;

import org.hl7.fhir.dstu3.model.Identifier;

public interface IIdentifierMap<T> {
	void put(String fhirType, Identifier identifier, T identifiedValue);

	T get(String fhirType, Identifier identifier);
}
