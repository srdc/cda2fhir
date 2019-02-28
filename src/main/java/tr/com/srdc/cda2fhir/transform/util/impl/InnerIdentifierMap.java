package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;

public class InnerIdentifierMap<T> {
	private Map<String, T> genericMap; 
	private Map<String, Map<String, T>> systemMaps;
	
	public void put(Identifier identifier, T identifiedValue) {
		String value = identifier.getValue();
		if (value != null) {		
			String system = identifier.getSystem();
			if (system != null) {
				if (systemMaps == null) {
					systemMaps = new HashMap<String, Map<String, T>>();
				}
				Map<String, T> systemMap = systemMaps.get(system);
				if (systemMap == null) {
					systemMap = new HashMap<String, T>();
					systemMaps.put(system, systemMap);
				}
				systemMap.put(value, identifiedValue);
			} else {
				if (genericMap == null) {
					genericMap = new HashMap<String, T>();
				}
				genericMap.put(value, identifiedValue);
			}
		}
	}
	
	public T get(Identifier identifier) {
		String value = identifier.getValue();
		if (value != null) {		
			String system = identifier.getSystem();
			if (system != null) {
				if (systemMaps == null) {
					return null;
				}
				Map<String, T> systemMap = systemMaps.get(system);
				if (systemMap == null) {
					return null;
				}
				return systemMap.get(value);
			}
			if (genericMap != null) {
				return genericMap.get(value);
			}
		}
		return null;
	}
}
