package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

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
		id.put("extention", Integer.toString(counter));
		return id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> mapTop = (Map<String, Object>) input;
		Map<String, Object> map = (Map<String, Object>) mapTop.get(path);
		if (map == null) {
			return input;
		}		
		if (map.containsKey("id")) {
			return input;
		}
		map.put("id", nextId());
		if (!map.containsKey("representedOrganization")) {
			return map;
		}
		Map<String, Object> org = (Map<String, Object>) map.get("representedOrganization");
		if (org.containsKey("id")) {
			return map;
		}
		org.put("id", nextId());
		return map;
	}
}
