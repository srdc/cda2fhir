package tr.com.srdc.cda2fhir.transform.util;

import java.util.Collection;
import java.util.List;

import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public interface ICDAIIResourceMaps<T> {

	Collection<Class<? extends T>> keySet();

	Collection<CDAIIMap<T>> values();

	CDAIIMap<T> getMap(Class<? extends T> clazz);

	void put(ICDAIIResourceMapsSource<T> sourceMaps);

	T get(List<II> iis, Class<? extends T> clazz);

	T get(II ii, Class<? extends T> clazz);

	boolean hasMap(Class<? extends T> clazz);

	void put(II ii, Class<? extends T> clazz, T t);

	void put(List<II> iis, Class<? extends T> clazz, T t);

}
