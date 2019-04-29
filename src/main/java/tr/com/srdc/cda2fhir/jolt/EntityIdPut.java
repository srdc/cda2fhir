package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class EntityIdPut implements ContextualTransform, SpecDriven {
	private String resourceType;

	@SuppressWarnings("unchecked")
	@Inject
	public EntityIdPut(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		resourceType = (String) specAsMap.get("resourceType");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		context.remove("CurrentEntityIds");
		if (input == null) {
			return null;
		}

		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		List<Object> ids = (List<Object>) inputAsMap.get("id");
		if (ids != null) {
			context.put("CurrentEntityIds", ids);
			CDAIIMap<Map<String, Object>> entityMap = (CDAIIMap<Map<String, Object>>) context.get("EntityMap");
			if (entityMap != null) {
				Map<String, Object> resources = entityMap.jget(ids);
				if (resources != null) {
					if (resources.containsKey(resourceType)) {
						return null;
					}
				}
			}
		}
		return input;
	}
}
