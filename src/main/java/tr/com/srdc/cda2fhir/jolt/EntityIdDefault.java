package tr.com.srdc.cda2fhir.jolt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class EntityIdDefault implements ContextualTransform, SpecDriven {
	private static int counter = 0;

	private String path;

	@Inject
	@SuppressWarnings("unchecked")
	public EntityIdDefault(Object spec) {
		Map<String, Object> map = (Map<String, Object>) spec;
		path = (String) map.get("path");
	}

	private static Object nextId() {
		++counter;
		Map<String, Object> id = new LinkedHashMap<String, Object>();
		id.put("root", "0.0.0.0.0.0");
		id.put("extension", Integer.toString(counter));
		return Collections.singletonList((Object) id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> content = (Map<String, Object>) inputAsMap.get(path);

		if (content == null) {
			return null;
		}

		if (content.containsKey("id")) {
			List<Object> ids = (List<Object>) content.get("id");
			context.put("CurrentEntityIds", ids);
			CDAIIMap<Map<String, Object>> entityMap = (CDAIIMap<Map<String, Object>>) context.get("EntityMap");
			if (entityMap != null && entityMap.jget(ids) != null) {
				return null;
			}
		}

		if (!content.containsKey("id")) {
			content.put("id", nextId());
		}
		if (!content.containsKey("representedOrganization")) {
			return input;
		}
		Map<String, Object> org = (Map<String, Object>) content.get("representedOrganization");
		if (org.containsKey("id")) {
			return input;
		}
		org.put("id", nextId());
		return input;
	}
}
