package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMap;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMapSource;

public class CDACDMap<T> implements ICDACDMap<T>, ICDACDMapSource<T> {

	Map<String, Map<String, T>> cdMap;

	@Override
	public void put(CD cd, T value) {
		if (cdMap == null) {
			cdMap = new HashMap<String, Map<String, T>>();
		}
		List<CD> cds = new ArrayList<CD>();
		put(cd.getCodeSystemName(), cd.getCode(), value);
		if (cd.getTranslations() != null && !cd.getTranslations().isEmpty()) {
			for (CD currCD : cd.getTranslations()) {
				put(currCD.getCodeSystemName(), currCD.getCode(), value);
			}
		}

	}

	private void put(String system, String code, T value) {
		if (system == null || code == null || value == null) {
			return;
		}
		if (Config.MEDICATION_CODE_SYSTEM != null && !Config.MEDICATION_CODE_SYSTEM.contentEquals(system)) {
			return;
		}
		if (cdMap.get(system) == null) {
			cdMap.put(system, new HashMap<String, T>());
		}

		cdMap.get(system).put(code, value);
	}

	public void put(ICDACDMapSource<T> source) {
		if (source == null)
			return;
		if (cdMap == null) {
			cdMap = new HashMap<String, Map<String, T>>();
		}

		source.putCDValuesTo(cdMap);
	}

	@Override
	public void putCDValuesTo(Map<String, Map<String, T>> target) {
		if (cdMap != null) {
			for (String system : cdMap.keySet()) {
				if (target.get(system) == null) {
					target.put(system, new HashMap<String, T>());
				}
				target.get(system).putAll(cdMap.get(system));
			}

		}
	}

	@Override
	public boolean hasCDMapValues() {
		return cdMap != null;
	}

	@Override
	public T get(CD cd) {
		if (cdMap != null) {

			Map<String, T> codeMap = cdMap.get(cd.getCodeSystemName());
			if (codeMap != null && codeMap.get(cd.getCode()) != null) {
				return codeMap.get(cd.getCode());
			}
			if (cd.getTranslations() != null && !cd.getTranslations().isEmpty()) {
				for (CD currCD : cd.getTranslations()) {
					codeMap = cdMap.get(currCD.getCodeSystemName());
					if (codeMap != null && codeMap.get(currCD.getCode()) != null) {
						return codeMap.get(currCD.getCode());
					}
				}
			}

		}
		return null;
	}

	public Map<String, Map<String, T>> getCDMap() {
		return cdMap;
	}

}
