package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.SpecDriven;

public class Assign implements ContextualTransform, SpecDriven {
	private Shiftr shiftr;

	@SuppressWarnings("unchecked")
	@Inject
	public Assign(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		if (!specAsMap.isEmpty()) { // empty is allowed not for an issue in reporting
			shiftr = new Shiftr(spec);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		if (shiftr == null) {
			return input;
		}
		Object output = shiftr.transform(input);
		if (output == null) {
			return input;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> outputAsMap = (Map<String, Object>) output;
		outputAsMap.entrySet().forEach(entry -> { // only top level merge
			String key = entry.getKey();
			Object value = entry.getValue();
			if (!inputAsMap.containsKey(key)) {
				inputAsMap.put(key, value);
				return;
			}
			if (!(value instanceof Map)) {
				inputAsMap.put(key, value);
				return;
			}
			Object currentValue = inputAsMap.get(key);
			if (currentValue == null || !(currentValue instanceof Map)) {
				inputAsMap.put(key, value);
				return;
			}
			Map<String, Object> currentValueAsMap = (Map<String, Object>) currentValue;
			Map<String, Object> valueAsMap = (Map<String, Object>) value;
			currentValueAsMap.putAll(valueAsMap);
		});

		return inputAsMap;
	}
}
