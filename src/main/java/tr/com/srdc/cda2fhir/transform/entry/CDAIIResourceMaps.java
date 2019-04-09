package tr.com.srdc.cda2fhir.transform.entry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.util.ICDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIResourceMapsSource;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class CDAIIResourceMaps<T> implements ICDAIIResourceMaps<T>, ICDAIIResourceMapsSource<T> {
	Map<Class<? extends T>, CDAIIMap<T>> iiMaps = new HashMap<Class<? extends T>, CDAIIMap<T>>();

	@Override
	public boolean hasMap(Class<? extends T> clazz) {
		return iiMaps.containsKey(clazz);
	}

	@Override
	public CDAIIMap<T> getMap(Class<? extends T> clazz) {
		return iiMaps.get(clazz);
	}

	@Override
	public void putMap(Class<? extends T> clazz, CDAIIMap<T> map) {
		iiMaps.put(clazz, map);
	}

	@Override
	public boolean hasMapValues() {
		return iiMaps.values().parallelStream().filter(e -> e.hasIIMapValues()).findAny().isPresent();
	}

	@Override
	public T get(II ii, Class<? extends T> clazz) {
		if (hasMap(clazz)) {
			CDAIIMap<T> iiMap = this.getMap(clazz);

			if (iiMap.hasIIMapValues() && iiMap.get(ii) != null) {
				return iiMap.get(ii);
			}
		}
		return null;
	}

	@Override
	public T get(List<II> iis, Class<? extends T> clazz) {
		if (hasMap(clazz)) {
			CDAIIMap<T> iiMap = getMap(clazz);
			if (iiMap.hasIIMapValues() && iiMap.get(iis) != null) {
				return iiMap.get(iis);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putRootValuesTo(Class<? extends T> clazz, Map<String, T> target) {
		if (iiMaps.containsKey(clazz)) {
			CDAIIMap<T> iiMap = iiMaps.get(clazz);
			iiMap.putRootValuesTo(target);
		}

	}

	@Override
	public void putExtensionValuesTo(Class<? extends T> clazz, Map<String, Map<String, T>> target) {
		if (iiMaps.containsKey(clazz)) {
			CDAIIMap<T> iiMap = iiMaps.get(clazz);
			iiMap.putExtensionValuesTo(target);
		}

	}

	@Override
	public Collection<Class<? extends T>> keySet() {
		return iiMaps.keySet();
	}

	@Override
	public Collection<CDAIIMap<T>> values() {
		return iiMaps.values();
	}

	@Override
	public void put(ICDAIIResourceMapsSource<T> sourceMaps) {
		for (Class<? extends T> clazz : iiMaps.keySet()) {
			CDAIIMap<T> iiMap = this.getMap(clazz);
			CDAIIMap<T> iiMapSource = sourceMaps.getMap(clazz);
			if (iiMap != null) {
				if (iiMapSource != null) {
					iiMapSource.put(iiMap);
				} else {
					CDAIIMap<T> iiMapSourceNew = new CDAIIMap<T>();
					sourceMaps.putMap(clazz, iiMapSourceNew);
					iiMapSourceNew.put(iiMap);
				}

			}

		}
	}

}
