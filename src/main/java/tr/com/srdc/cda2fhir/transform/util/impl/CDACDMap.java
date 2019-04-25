package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMap;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMapSource;

public class CDACDMap<T> implements ICDACDMap<T>, ICDACDMapSource<T> {

	Map<String, T> cdMap;

	@Override
	public void put(CD cd, T value) {
		if (cdMap == null) {
			cdMap = new HashMap<String, T>();
		}

		String codeSetKey = getKeyString(cd);

		if (!codeSetKey.contentEquals("")) {
			put(codeSetKey, value);
		}
	}

	private String getKeyString(CD cd) {
		ArrayList<String> codeSetKeyArr = new ArrayList<String>();

		String codeSystem = cd.getCodeSystem();
		String code = cd.getCode();

		if (codeSystem != null && code != null) {
			checkSystemFlagAndAdd(codeSetKeyArr, codeSystem, code);
		}

		if (cd.getTranslations() != null && !cd.getTranslations().isEmpty()) {
			for (CD currCD : cd.getTranslations()) {
				if (currCD.getCodeSystem() != null && currCD.getCode() != null) {
					checkSystemFlagAndAdd(codeSetKeyArr, currCD.getCodeSystem(), currCD.getCode());
				}
			}
		}

		codeSetKeyArr.sort(Comparator.comparing(String::toString));

		String codeSetKey = codeSetKeyArr.parallelStream().collect(Collectors.joining());

		return codeSetKey;
	}

	private void checkSystemFlagAndAdd(ArrayList<String> arr, String system, String value) {
		if (Config.MEDICATION_CODE_SYSTEM != null && Config.MEDICATION_CODE_SYSTEM.contentEquals(system.trim())) {
			arr.add(system.trim());
			arr.add(value.trim());
		} else if (Config.MEDICATION_CODE_SYSTEM == null) {
			arr.add(system.trim());
			arr.add(value.trim());
		}
	}

	private void put(String key, T value) {
		if (key == null || value == null) {
			return;
		}

		if (cdMap == null) {
			cdMap = new HashMap<String, T>();
		}

		cdMap.put(key, value);
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
			String keyStr = getKeyString(cd);

			if (!keyStr.contentEquals("")) {
				return cdMap.get(keyStr);
			}

		}
		return null;
	}

	public Map<String, T> getCDMap() {
		return cdMap;
	}

}
