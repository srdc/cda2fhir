package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.List;
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
	public T get(II ii) {
		T result = existingIIMap.get(ii);
		if (result == null && newIIMap != null) {
			return newIIMap.get(ii);
		}
		return result;
	}

	@Override
	public T get(List<II> iis) {
		T result = existingIIMap.get(iis);
		if (result == null && newIIMap != null) {
			return newIIMap.get(iis);
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

	@Override
	public boolean hasIIMapValues() {
		return newIIMap != null;
	}
}
