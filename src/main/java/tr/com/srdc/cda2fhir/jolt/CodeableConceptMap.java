package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeableConceptMap {

	Map<String, Map<String, Map<String, Object>>> ccMap;

	@SuppressWarnings("unchecked")
	public void put(Object cc, Map<String, Object> value) {
		if (cc == null || value == null || !(cc instanceof Map)) {
			return;
		}

		Map<String, Object> mapCc = (Map<String, Object>) cc;

		Object codings = mapCc.get("coding");
		if (codings == null || !(codings instanceof List)) {
			return;
		}

		List<Object> codingsList = (List<Object>) codings;

		codingsList.forEach((coding) -> {
			if (coding == null || !(coding instanceof Map)) {
				return;
			}
			Map<String, Object> mapCoding = (Map<String, Object>) coding;

			String system = (String) mapCoding.get("system");
			String code = (String) mapCoding.get("code");
			put(system, code, value);
		});
	}

	private void put(String system, String code, Map<String, Object> value) {
		if (system == null || code == null || value == null) {
			return;
		}
		if (ccMap == null) {
			ccMap = new HashMap<String, Map<String, Map<String, Object>>>();
		}

		Map<String, Map<String, Object>> systemMap = ccMap.get(system);
		if (systemMap == null) {
			systemMap = new HashMap<String, Map<String, Object>>();
			ccMap.put(system, systemMap);
		}

		systemMap.put(code, value);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> get(Map<String, Object> cc) {
		if (cc == null || ccMap == null) {
			return null;
		}

		Map<String, Object> mapCc = cc;

		Object codings = mapCc.get("coding");
		if (codings == null || !(codings instanceof List)) {
			return null;
		}

		List<Object> codingsList = (List<Object>) codings;

		for (Object coding : codingsList) {
			if (coding == null || !(coding instanceof Map)) {
				continue;
			}

			Map<String, Object> mapCoding = (Map<String, Object>) coding;

			String system = (String) mapCoding.get("system");
			String code = (String) mapCoding.get("code");

			if (system == null || code == null) {
				continue;
			}

			Map<String, Map<String, Object>> systemMap = ccMap.get(system);
			if (systemMap == null) {
				continue;
			}

			Map<String, Object> value = systemMap.get(code);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> get(Object cc) {
		if (cc == null || ccMap == null || !(cc instanceof Map)) {
			return null;
		}
		return get((Map<String, Object>) cc);
	}
}
