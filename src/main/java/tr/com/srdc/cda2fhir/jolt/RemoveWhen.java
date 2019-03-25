package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class RemoveWhen implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public RemoveWhen(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		for (Map.Entry<String, Object> entry : this.spec.entrySet()) {
			String key = entry.getKey();
			if (!inputAsMap.containsKey(key)) {
				continue;
			}
			Object value = entry.getValue();
			if ("*".equals(value)) {
				return null;
			}
			if (value instanceof String) {
				inputAsMap.remove(value);
				continue;
			}
			if (value instanceof List) {
				List<String> valueAsList = (List<String>) value;
				valueAsList.forEach(r -> inputAsMap.remove(r));
				continue;
			}
		}
		return input;
	}
}
