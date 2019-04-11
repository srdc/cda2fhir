package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;

import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;

public class IdentifierMap<T> implements IIdentifierMap<T> {
	private Map<String, InnerIdentifierMap<T>> map = new HashMap<String, InnerIdentifierMap<T>>();

	@Override
	public void put(String fhirType, Identifier identifier, T identifiedValue) {
		InnerIdentifierMap<T> innerMap = map.get(fhirType);
		if (innerMap == null) {
			innerMap = new InnerIdentifierMap<T>();
			map.put(fhirType, innerMap);
		}
		innerMap.put(identifier, identifiedValue);
	}

	public void put(String fhirType, List<Identifier> identifiers, T identifiedValue) {
		identifiers.forEach(identifier -> put(fhirType, identifier, identifiedValue));
	}

	@Override
	public void put(String fhirType, String system, String value, T identifiedValue) {
		InnerIdentifierMap<T> innerMap = map.get(fhirType);
		if (innerMap == null) {
			innerMap = new InnerIdentifierMap<T>();
			map.put(fhirType, innerMap);
		}
		innerMap.put(system, value, identifiedValue);
	}

	@Override
	public T get(String fhirType, Identifier identifier) {
		InnerIdentifierMap<T> innerMap = map.get(fhirType);
		if (innerMap != null) {
			return innerMap.get(identifier);
		}
		return null;
	}

	@Override
	public T get(String fhirType, String value) {
		InnerIdentifierMap<T> innerMap = map.get(fhirType);
		if (innerMap != null) {
			return innerMap.get(value);
		}
		return null;
	}

	@Override
	public T get(String fhirType, String system, String value) {
		InnerIdentifierMap<T> innerMap = map.get(fhirType);
		if (innerMap != null) {
			return innerMap.get(system, value);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getFromJSONArray(String fhirType, List<Object> identifiers) {
		for (Object identifier : identifiers) {
			Map<String, Object> idAsMap = (Map<String, Object>) identifier;
			String system = (String) idAsMap.get("system");
			String value = (String) idAsMap.get("value");

			return get(fhirType, system, value);
		}
		return null;
	}
}
