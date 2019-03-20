package tr.com.srdc.cda2fhir.jolt;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class SquashEmpty implements ContextualTransform, SpecDriven {
	@Inject
	public SquashEmpty(Object spec) {
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
		Iterator<Map.Entry<String, Object>> itr = inputAsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, Object> entry = itr.next();
			Object value = entry.getValue();
			if (!(value instanceof Map))
				continue;
			Map<String, Object> valueAsMap = (Map<String, Object>) value;
			if (valueAsMap.isEmpty()) {
				itr.remove();
			}
		}
		return input;
	}
}
