package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.hl7.fhir.dstu3.model.Identifier;

public interface IIdentifierMap<T> {
	void put(String fhirType, Identifier identifier, T identifiedValue);

	void put(String fhirType, String system, String value, T identifiedValue);

	void put(String fhirType, List<Identifier> identifiers, T identifiedValue);

	T get(String fhirType, Identifier identifier);

	T get(String fhirType, String value);

	T get(String fhirType, String system, String value);

	T getFromJSONArray(String fhirType, List<Object> identifiers);
}
