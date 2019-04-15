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

	public void put(String root, String extension, T value) {
		if (root != null) {
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

	@Override
	public void put(II id, T value) {
		put(id.getRoot(), id.getExtension(), value);
	}

	public void put(List<II> ids, T value) {
		for (II id : ids) {
			put(id, value);
		}
	}

	public void put(ICDAIIMapSource<T> source) {
		if (source == null)
			return;
		if (rootMap == null) {
			rootMap = new HashMap<String, T>();
		}
		source.putRootValuesTo(rootMap);
		if (extensionMaps == null) {
			extensionMaps = new HashMap<String, Map<String, T>>();
		}
		source.putExtensionValuesTo(extensionMaps);
	}

	public void jput(Map<String, Object> identifier, T value) {
		String root = (String) identifier.get("root");
		Object extensionObject = identifier.get("extension");
		String extension = extensionObject == null ? null : extensionObject.toString();
		put(root, extension, value);
	}

	@SuppressWarnings("unchecked")
	public void jput(Object identifier, T value) {
		jput((Map<String, Object>) identifier, value);
	}

	public void jput(List<Object> identifiers, T value) {
		identifiers.forEach(identifier -> jput(identifier, value));
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

	public T jget(Map<String, Object> identifier) {
		String root = (String) identifier.get("root");
		Object extensionObject = identifier.get("extension");
		String extension = extensionObject == null ? null : extensionObject.toString();
		return get(root, extension);
	}

	@SuppressWarnings("unchecked")
	public T jget(Object identifier) {
		return jget((Map<String, Object>) identifier);
	}

	public T jget(List<Object> identifiers) {
		for (Object identifier : identifiers) {
			T value = jget(identifier);
			if (value != null) {
				return value;
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

	public Map<String, T> getRootMap() {
		return rootMap;
	}

	public Map<String, Map<String, T>> getExtensionMap() {
		return extensionMaps;
	}

}
