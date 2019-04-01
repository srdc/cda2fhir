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
		id.put("extension", Integer.toString(counter));
		return id;
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
			return input;
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
