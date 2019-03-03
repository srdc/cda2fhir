package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.util.ICDAIIMap;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;

public class LocalCDAIIMap<T> implements ICDAIIMap<T>, ICDAIIMapSource<T> {
	private CDAIIMap<T> newIIMap;
	private ICDAIIMap<T> existingIIMap;
	
	public LocalCDAIIMap(ICDAIIMap<T> iiMap) {
		existingIIMap = iiMap;
	}
	
	public void put(II id, T value) {
		if (newIIMap == null) {
			 newIIMap = new CDAIIMap<T>();
		}
		newIIMap.put(id, value);
	}	

	@Override
	public T get(II id) {
		T result = existingIIMap.get(id);
		if (result == null && newIIMap != null) {
			return newIIMap.get(id);
		}
		return result;
	}

	@Override
	public void putRootValuesTo(Map<String, T> target) {
		if (newIIMap != null) {
			newIIMap.putRootValuesTo(target);
		}
	}

	@Override
	public void putExtensionValuesTo(Map<String, Map<String, T>> target) {
		if (newIIMap != null) {
			newIIMap.putExtensionValuesTo(target);		
		}
	}
}
