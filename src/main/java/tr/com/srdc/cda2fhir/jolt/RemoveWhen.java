package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
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

	@SuppressWarnings("unchecked")
	public static Map<String, Object> findMatches(Map<String, Object> source, Map<String, Object> input, String path) {
		Map<String, Object> result = new HashMap<>();
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String key = entry.getKey();
			if (!input.containsKey(key)) {
				continue;
			}
			Object value = entry.getValue();
			String newPath = path.isEmpty() ? key : path + "." + key;
			if (!(value instanceof Map)) {
				result.put(newPath, value);
				continue;
			}
			Object inputBranch = input.get(key);
			if (!(inputBranch instanceof Map)) {
				continue;
			}
			Map<String, Object> newSource = (Map<String, Object>) value;
			Map<String, Object> newInput = (Map<String, Object>) inputBranch;
			Map<String, Object> newResult = findMatches(newSource, newInput, newPath);
			result.putAll(newResult);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> matches = findMatches(this.spec, inputAsMap, "");
		for (Map.Entry<String, Object> match : matches.entrySet()) {
			Object value = match.getValue();
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
