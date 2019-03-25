package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.List;
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
				Map<String, T> extensionMap = extensionMaps.get(root);
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

	public void put(List<II> ids, T value) {
		for (II id : ids) {
			put(id, value);
		}
	}

	public void put(ICDAIIMapSource<T> source) {
		if (rootMap == null) {
			rootMap = new HashMap<String, T>();
		}
		source.putRootValuesTo(rootMap);
		if (extensionMaps == null) {
			extensionMaps = new HashMap<String, Map<String, T>>();
		}
		source.putExtensionValuesTo(extensionMaps);
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
	public T get(II ii) {
		if (ii == null) {
			return null;
		}
		return get(ii.getRoot(), ii.getExtension());
	}

	@Override
	public T get(List<II> iis) {
		if (iis != null) {
			for (II ii : iis) {
				T result = get(ii);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public void putRootValuesTo(Map<String, T> target) {
		if (rootMap != null) {
			target.putAll(rootMap);
		}
	}

	@Override
	public void putExtensionValuesTo(Map<String, Map<String, T>> target) {
		if (extensionMaps != null) {
			for (Map.Entry<String, Map<String, T>> entry : extensionMaps.entrySet()) {
				String root = entry.getKey();
				Map<String, T> extensionMap = entry.getValue();
				Map<String, T> targetExtensionMap = target.get(root);
				if (targetExtensionMap == null) {
					target.put(root, extensionMap);
				} else {
					targetExtensionMap.putAll(extensionMap);
				}
			}
		}
	}

	@Override
	public boolean hasIIMapValues() {
		return rootMap != null || extensionMaps != null;
	}
}
