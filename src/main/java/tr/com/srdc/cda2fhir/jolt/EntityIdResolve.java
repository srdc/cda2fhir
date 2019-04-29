package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class EntityIdResolve implements ContextualTransform, SpecDriven {
	private String resourceType;

	@SuppressWarnings("unchecked")
	@Inject
	public EntityIdResolve(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		resourceType = (String) specAsMap.get("resourceType");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		List<Object> ids = (List<Object>) context.get("CurrentEntityIds");
		context.remove("CurrentEntityIds");
		CDAIIMap<Map<String, Object>> entityMap = (CDAIIMap<Map<String, Object>>) context.get("EntityMap");
		if (input == null) {
			if (ids == null) {
				return null;
			}
			if (entityMap != null) {
				Map<String, Object> existing = entityMap.jget(ids);
				if (existing != null) {
					if (existing.containsKey(resourceType)) {
						return existing.get(resourceType);
					}
				}
			}
		}
		if (ids != null) {
			if (entityMap == null) {
				entityMap = new CDAIIMap<Map<String, Object>>();
				context.put("EntityMap", entityMap);
			}
			Map<String, Object> resources = entityMap.jget(ids);
			if (resources == null) {
				resources = new LinkedHashMap<String, Object>();
				entityMap.jput(ids, resources);
			}
			resources.put(resourceType, input);
		}
		return input;
	}
}
