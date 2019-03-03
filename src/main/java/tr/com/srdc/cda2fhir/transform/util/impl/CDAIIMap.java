package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.util.ICDAIIMap;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;

public class CDAIIMap<T> implements ICDAIIMap<T>, ICDAIIMapSource<T> {
	private Map<String, T> rootMap; 
	private Map<String, Map<String, T>> extensionMaps;
	
	public void put(II id, T value) {
		String root = id.getRoot();
		if (root != null) {		
			String extension = id.getExtension();
			if (extension != null) {
				if (extensionMaps == null) {
					extensionMaps = new HashMap<String, Map<String, T>>();
				}
				Map<String, T> extensionMap = extensionMaps.get(extension);
				if (extensionMap == null) {
					extensionMap = new HashMap<String, T>();
					extensionMaps.put(root, extensionMap);
				}
				extensionMap.put(extension, value);
			} else {
				if (rootMap == null) {
					rootMap = new HashMap<String, T>();
				}
				rootMap.put(root, value);
			}
		}
	}

	private T get(String root, String extension) {
		if (root != null) {		
			if (extension != null) {
				if (extensionMaps == null) {
					return null;
				}
				Map<String, T> extensionMap = extensionMaps.get(root);
				if (extensionMap == null) {
					return null;
				}
				return extensionMap.get(extension);
			}
			if (rootMap != null) {
				return rootMap.get(root);
			}
		}
		return null;
	}
	
	@Override
	public T get(II id) {
		return get(id.getRoot(), id.getExtension());
	}

	@Override
	public void putRootValuesTo(Map<String, T> target) {
		target.putAll(rootMap);
	}

	@Override
	public void putExtensionValuesTo(Map<String, Map<String, T>> target) {
		target.putAll(extensionMaps);		
	}
}
