package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class MedicationMap {

	Map<String, MedicationContainer> ccMap = new HashMap<String, MedicationContainer>();

	private class MedicationContainer {

		private Map<String, Object> medicationResource = null;

		private IdentifierMap<Map<String, Object>> medByOrgId = null;

		public MedicationContainer() {
			this.medByOrgId = new IdentifierMap<Map<String, Object>>();
		}

		public MedicationContainer(Map<String, Object> medicationResource) {
			this.medicationResource = medicationResource;
		}

		public void putMedicationByOrg(String orgSystem, String orgIdValue, Map<String, Object> medication) {
			if (this.medByOrgId == null) {
				this.medByOrgId = new IdentifierMap<Map<String, Object>>();
			}
			this.medByOrgId.put("Medication", orgSystem, orgIdValue, medication);
		}

		public boolean isMedication(Map<String, Object> othercc, String orgIdSystem, String orgIdValue) {
			if (orgIdSystem == null && orgIdValue == null && this.medicationResource != null) {
				return true;
			} else {
				return this.medByOrgId.get("Medication", orgIdSystem, orgIdValue) != null;
			}
		}

		public Map<String, Object> getMedication(String orgIdSystem, String orgIdValue) {
			if (orgIdSystem == null && orgIdValue == null && this.medicationResource != null) {
				return this.medicationResource;
			} else {

				if (orgIdSystem == null || orgIdValue == null)
					return null;

				return this.medByOrgId.get("Medication", orgIdSystem, orgIdValue);
			}
		}

		public Map<String, Object> getMedication() {
			return this.medicationResource;
		}

	}

	public void put(Map<String, Object> medResource, List<Object> orgIdentifiers) {
		Map<String, Object> cc = (Map<String, Object>) medResource.get("code");

		if (cc != null && medResource != null) {
			String keyString = getKeyString(cc);
			if (orgIdentifiers == null) {
				MedicationContainer medContainer = new MedicationContainer(medResource);
				ccMap.put(keyString, medContainer);
			} else {
				MedicationContainer medContainer = new MedicationContainer();

				if (!(orgIdentifiers instanceof List)) {
					return;
				}

				Iterator<Object> itr = orgIdentifiers.iterator();
				while (itr.hasNext()) {
					Object identifier = itr.next();
					Map<String, Object> map = (Map<String, Object>) identifier;
					String system = (String) map.get("system");
					Object valueObject = map.get("value");
					String value = valueObject == null ? null : valueObject.toString();
					medContainer.putMedicationByOrg(system, value, medResource);
				}

			}
		}

	}

	@SuppressWarnings("unchecked")
	private String getKeyString(Object cc) {

		if (cc == null || !(cc instanceof Map)) {
			return null;
		}

		Map<String, Object> mapcc = (Map<String, Object>) cc;

		Object codings = mapcc.get("coding");

		if (codings == null) {
			return null;
		}
		List<Object> codingsList = (List<Object>) codings;

		ArrayList<String> codeSetKeyArr = new ArrayList<String>();

		codingsList.forEach((coding) -> {
			if (coding != null && (coding instanceof Map)) {

				Map<String, Object> mapCoding = (Map<String, Object>) coding;

				String system = (String) mapCoding.get("system");
				String code = (String) mapCoding.get("code");

				if (system != null && code != null) {
					checkSystemFlagAndAdd(codeSetKeyArr, system, code);
				}
			}
		});

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

	public Map<String, Object> get(Map<String, Object> medReasource, List<Object> orgIdentifiers) {
		if (medReasource == null || ccMap == null) {
			return null;
		}
		Map<String, Object> cc = (Map<String, Object>) medReasource.get("code");

		if (cc == null) {
			return null;
		}
		String keyString = getKeyString(cc);

		if (!keyString.contentEquals("")) {
			MedicationContainer medContainer = ccMap.get(keyString);
			if (medContainer != null) {
				if (orgIdentifiers == null) {
					return medContainer.getMedication();
				} else {
					if (orgIdentifiers instanceof List) {
						Iterator<Object> itr = orgIdentifiers.iterator();
						while (itr.hasNext()) {
							Object identifier = itr.next();
							Map<String, Object> map = (Map<String, Object>) identifier;
							String system = (String) map.get("system");
							Object valueObject = map.get("value");
							String value = valueObject == null ? null : valueObject.toString();
							Map<String, Object> medResource = medContainer.getMedication(system, value);
							if (medResource != null) {
								return medResource;
							}
						}
					}
				}
			}

		}

		return null;
	};

}
