package tr.com.srdc.cda2fhir.jolt;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class SquashEmpty implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public SquashEmpty(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		if (!(input instanceof Map)) {
			return input;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		if (this.spec != null && !this.spec.isEmpty()) {
			String key = inputAsMap.keySet().iterator().next();
			inputAsMap = (Map<String, Object>) inputAsMap.get(key);
			if (inputAsMap == null) {
				return input;
			}
		}
		Iterator<Map.Entry<String, Object>> itr = inputAsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, Object> entry = itr.next();
			Object value = entry.getValue();
			if (value instanceof Map) {
				Map<String, Object> valueAsMap = (Map<String, Object>) value;
				if (valueAsMap.isEmpty()) {
					itr.remove();
				}
				continue;
			}
			if (value instanceof String) {
				String valueAsString = (String) value;
				if (valueAsString.isEmpty()) {
					itr.remove();
				}
			}
		}
		return input;
	}
}
