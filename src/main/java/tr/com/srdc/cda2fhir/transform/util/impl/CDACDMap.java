package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.transform.util.ICDACDMap;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMapSource;

public class CDACDMap<T> implements ICDACDMap<T>, ICDACDMapSource<T> {

	Map<String, T> cdMap;

	@Override
	public void put(CD cd, T value) {
		if (cdMap == null) {
			cdMap = new HashMap<String, T>();
		}
		cdMap.put(cd.getCode(), value);
	}

	public void put(ICDACDMapSource<T> source) {
		if (source == null)
			return;
		if (cdMap == null) {
			cdMap = new HashMap<String, T>();
		}

		source.putCDValuesTo(cdMap);
	}

	@Override
	public void putCDValuesTo(Map<String, T> target) {
		if (cdMap != null) {
			target.putAll(cdMap);
		}
	}

	@Override
	public boolean hasCDMapValues() {
		return cdMap != null;
	}

	@Override
	public T get(CD cd) {
		if (cdMap != null) {
			cdMap.get(cd.getCode());
		}
		return null;
	}

	public Map<String, T> getCDMap() {
		return cdMap;
	}

}
