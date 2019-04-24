package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class Flatten implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public Flatten(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		String key = (String) this.spec.get("key");
		List<Object> currents = (List<Object>) inputAsMap.get(key);
		if (currents == null) {
			return input;
		}
		List<Object> result = new ArrayList<>();
		currents.forEach(current -> {
			if (current != null) {
				Map<String, Object> currentAsMap = (Map<String, Object>) current;
				if (currentAsMap != null) {
					List<Object> list = (List<Object>) currentAsMap.get(key);
					list.forEach(e -> result.add(e));
				}
			}
		});
		if (result.size() == 0) {
			inputAsMap.remove(key);
		} else {
			inputAsMap.put(key, result);
		}
		return input;
	}
}
