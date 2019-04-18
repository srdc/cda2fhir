package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class DefaultWhen implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public DefaultWhen(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		String target = (String) spec.get("target");
		if (inputAsMap.containsKey(target)) {
			return input;
		}
		String source = (String) spec.get("source");
		if (!inputAsMap.containsKey(source)) {
			return input;
		}
		Object value = spec.get("value");
		inputAsMap.put(target, value);
		return input;
	}
}
