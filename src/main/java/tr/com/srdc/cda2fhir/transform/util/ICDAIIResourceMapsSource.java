package tr.com.srdc.cda2fhir.transform.util;

import java.util.Collection;
import java.util.Map;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public interface ICDAIIResourceMapsSource<T> {
	void putRootValuesTo(Class<? extends T> clazz, Map<String, T> target);

	void putExtensionValuesTo(Class<? extends T> clazz, Map<String, Map<String, T>> target);

	boolean hasMapValues();

	public CDAIIMap<T> getMap(Class<? extends T> clazz);

	void putMap(Class<? extends T> clazz, CDAIIMap<T> map);

	Collection<Class<? extends T>> keySet();

}
