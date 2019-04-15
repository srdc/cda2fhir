package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class EntityIdResolve implements ContextualTransform {
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		List<Object> ids = (List<Object>) context.get("CurrentEntityIds");
		CDAIIMap<Map<String, Object>> entityMap = (CDAIIMap<Map<String, Object>>) context.get("EntityMap");
		if (input == null) {
			if (ids != null && entityMap != null) {
				Object existing = entityMap.jget(ids);
				if (existing != null) {
					context.remove("CurrentEntityIds");
					return existing;
				}
			}
			context.remove("CurrentEntityIds");
			return null;
		}
		if (ids != null) {
			if (entityMap == null) {
				entityMap = new CDAIIMap<Map<String, Object>>();
				context.put("EntityMap", entityMap);
			}
			entityMap.jput(ids, (Map<String, Object>) input);
		}
		context.remove("CurrentEntityIds");
		return input;
	}
}
