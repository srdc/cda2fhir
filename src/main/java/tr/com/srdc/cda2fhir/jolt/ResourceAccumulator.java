package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class ResourceAccumulator implements SpecDriven, ContextualTransform {
	private String resourceType;
	private boolean keepNull = false;

	@Inject
	@SuppressWarnings("unchecked")
	public ResourceAccumulator(Object spec) {
		Map<String, Object> map = (Map<String, Object>) spec;
		resourceType = (String) map.get("resourceType");
		Boolean keepNullObject = (Boolean) map.get("keepNull");
		if (keepNullObject != null) {
			keepNull = keepNullObject.booleanValue();
		}
	}

	private Map<String, Object> getEncounterDiagnosisConditionCategory() {
		Map<String, Object> conditionCoding = new LinkedHashMap<String, Object>();

		conditionCoding.put("system", "http://hl7.org/fhir/condition-category");
		conditionCoding.put("code", "encounter-diagnosis");
		conditionCoding.put("display", "Encounter Diagnosis");

		return conditionCoding;
	}

	private Map<String, Object> getProblemListConditionCategory() {
		Map<String, Object> conditionCoding = new LinkedHashMap<String, Object>();

		conditionCoding.put("system", "http://hl7.org/fhir/condition-category");
		conditionCoding.put("code", "problem-list-item");
		conditionCoding.put("display", "Problem List Item");

		return conditionCoding;
	}

	@SuppressWarnings("unchecked")
	private Boolean conditionhasCategory(Map<String, Object> resource, Map<String, Object> otherConditionCoding) {
		if (resource == null || otherConditionCoding == null) {
			return false;
		} else {
			Map<String, Object> categoryCodeableConcept = (Map<String, Object>) resource.get("category");

			if (categoryCodeableConcept != null) {
				List<Map<String, String>> categoryCodings = (List<Map<String, String>>) categoryCodeableConcept
						.get("coding");

				if (categoryCodings != null) {
					for (Map<String, String> coding : categoryCodings) {
						if (coding != null) {
							String code = coding.get("code");

							if (code != null) {

								if (code.equals(otherConditionCoding.get("code"))) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void addConditionCoding(Map<String, Object> resource, Map<String, Object> conditionCoding) {
		List<Map<String, Object>> category = (List<Map<String, Object>>) resource.get("category");

		if (category == null) {
			category = new ArrayList<Map<String, Object>>();
			resource.put("category", category);
		}

		Map<String, Object> newCategory = new LinkedHashMap<String, Object>();

		List<Map<String, Object>> coding = new ArrayList<Map<String, Object>>();

		coding.add(conditionCoding);

		newCategory.put("coding", coding);

		category.add(newCategory);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null && !keepNull) {
			return null;
		}
		Map<String, Object> resource = (Map<String, Object>) input;

		if (resource == null) {
			resource = new LinkedHashMap<String, Object>();
		}

		List<Object> resources = (List<Object>) context.get("Resources");
		if (resources == null) {
			resources = new ArrayList<Object>();
			context.put("Resources", resources);
		}

		int id = resources.size() + 1;

		String reference = String.format("%s/%s", resourceType, id);
		String display = AdditionalModifier.getDisplay(resource);

		Map<String, Object> resourceMap = (Map<String, Object>) context.get("RESOURCE_MAP");

		if (resourceMap == null) {
			resourceMap = new HashMap<String, Object>();
			context.put("RESOURCE_MAP", resourceMap);
		}

		List<Object> identifiers;
		if (resource.get("identifier") instanceof LinkedHashMap) {
			identifiers = new ArrayList<Object>();
			identifiers.add(resource.get("identifier"));
		} else {
			identifiers = (List<Object>) resource.get("identifier");

		}
		if (input != null && "Condition".equals(resourceType)) {
			IdentifierMap<Map<String, Object>> conditionMap = (IdentifierMap<Map<String, Object>>) context
					.get("CONDITION_MAP");

			if (conditionMap == null) {
				conditionMap = new IdentifierMap<Map<String, Object>>();
				context.put("CONDITION_MAP", conditionMap);
			}

			if (identifiers != null) {

				if (conditionMap != null && identifiers != null) {

					for (Object identifierObj : identifiers) {

						if (identifierObj != null) {

							Map<String, String> identifier = (Map<String, String>) identifierObj;
							String system = identifier.get("system");
							String value = identifier.get("value");

							Map<String, Object> existing = conditionMap.get(system, value);

							if (existing != null) {
								Map<String, Object> problemListItemCoding = getProblemListConditionCategory();
								Map<String, Object> encounterDiagnosisCoding = getEncounterDiagnosisConditionCategory();
								if (conditionhasCategory(resource, problemListItemCoding)) {
									if (!conditionhasCategory(existing, problemListItemCoding)) {
										addConditionCoding(existing, problemListItemCoding);
									}
								}

								if (conditionhasCategory(resource, encounterDiagnosisCoding)) {
									if (!conditionhasCategory(existing, encounterDiagnosisCoding)) {
										addConditionCoding(existing, encounterDiagnosisCoding);
									}
								}
								return existing;
							} else {
								conditionMap.put(resourceType, system, value, resource);
							}

						}
					}
				}
			}

		}
		if (input != null && "Medication".equals(resourceType)) {
			MedicationMap medicationMap = (MedicationMap) context.get("MEDICATION_MAP");

			if (medicationMap == null) {
				medicationMap = new MedicationMap();
				context.put("MEDICATION_MAP", medicationMap);
			}

			Map<String, String> manufacturer = (Map<String, String>) resource.get("manufacturer");
			List<Object> orgIdentifiers = null;

			if (manufacturer != null) {
				String manuRef = manufacturer.get("reference");
				Map<String, Object> organization = (Map<String, Object>) resourceMap.get(manuRef);
				if (organization != null) {
					orgIdentifiers = (List<Object>) resource.get("identifier");
				}
			}
			Map<String, Object> existing = medicationMap.get(resource, orgIdentifiers);
			if (existing != null) {
				return existing;
			} else {
				medicationMap.put(resource, orgIdentifiers);
			}
		}

		if (input != null && "Organization".equals(resourceType))

		{

			IdentifierMap<Map<String, Object>> organizationMap = (IdentifierMap<Map<String, Object>>) context
					.get("ORGANIZATION_MAP");

			if (organizationMap == null) {
				organizationMap = new IdentifierMap<Map<String, Object>>();
				context.put("ORGANIZATION_MAP", organizationMap);
			}

			if (identifiers != null) {

				if (organizationMap != null && identifiers != null) {

					for (Object identifierObj : identifiers) {

						if (identifierObj != null) {

							Map<String, String> identifier = (Map<String, String>) identifierObj;
							String system = identifier.get("system");
							String value = identifier.get("value");

							Map<String, Object> existing = organizationMap.get(system, value);

							if (existing != null) {
								return existing;
							} else {
								organizationMap.put(resourceType, system, value, resource);
							}

						}
					}
				}
			}

		}

		resource.put("resourceType", resourceType);
		resource.put("id", id);
		resources.add(resource);

		resourceMap.put(reference, resource);

		if (identifiers == null) {
			return resource;
		}
		IdentifierMap<String> refsByIdentifier = (IdentifierMap<String>) context.get("RefsByIdentifier");
		if (refsByIdentifier == null) {
			refsByIdentifier = new IdentifierMap<String>();
			context.put("RefsByIdentifier", refsByIdentifier);
		}
		IdentifierMap<String> refDisplaysByIdentifier = (IdentifierMap<String>) context.get("RefDisplaysByIdentifier");
		if (refDisplaysByIdentifier == null) {
			refDisplaysByIdentifier = new IdentifierMap<String>();
			context.put("RefDisplaysByIdentifier", refDisplaysByIdentifier);
		}
		Iterator<Object> itr = identifiers.iterator();
		while (itr.hasNext()) {
			Object identifier = itr.next();
			Map<String, Object> map = (Map<String, Object>) identifier;
			String system = (String) map.get("system");
			Object valueObject = map.get("value");
			String value = valueObject == null ? null : valueObject.toString();
			refsByIdentifier.put(resourceType, system, value, reference);
			if (display != null) {
				refDisplaysByIdentifier.put(resourceType, system, value, display);
			}
			if (system != null && system.endsWith("0.0.0.0.0.0")) {
				itr.remove();
			}
		}
		if (identifiers.size() == 0) {
			resource.remove("identifier");
		}
		return resource;
	}
}
