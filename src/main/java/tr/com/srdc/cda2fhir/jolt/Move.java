package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class Move implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public Move(Object spec) {
		this.spec = (Map<String, Object>) spec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		this.spec.entrySet().forEach(entry -> {
			String key = entry.getKey();
			Object source = inputAsMap.get(key);
			if (source == null) {
				return;
			}
			Object value = entry.getValue();
			if (!(value instanceof String)) {
				throw new NotImplementedException("Only top level Move's are implemented.");
			}
			String valueAsString = (String) value;
			Object target = inputAsMap.get(valueAsString);
			if (target == null) {
				return;
			}
			if (target instanceof List) {
				List<Object> targetElements = (List<Object>) target;
				targetElements.forEach(targetElement -> {
					if (!(targetElement instanceof Map)) {
						throw new NotImplementedException("Move target elements can only be a map.");
					}
					Map<String, Object> targetElementAsMap = (Map<String, Object>) targetElement;
					targetElementAsMap.put(key, source);
				});
				return;
			}
			throw new NotImplementedException("Move target can currently only be a list.");
		});
		return input;
	}
}
