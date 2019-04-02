package tr.com.srdc.cda2fhir.jolt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class SquashNulls implements ContextualTransform, SpecDriven {
	private boolean recursive;

	@SuppressWarnings("unchecked")
	@Inject
	public SquashNulls(Object spec) {
		Map<String, Object> specAsMap = (Map<String, Object>) spec;
		Object recursiveValue = specAsMap.get("recursive");
		if (recursiveValue != null) {
			recursive = (Boolean) recursiveValue;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean squashNullsInMap(Object input) {
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Iterator<Map.Entry<String, Object>> itr = inputAsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, Object> entry = itr.next();
			Object value = entry.getValue();
			if (value == null) {
				itr.remove();
				continue;
			}
			if (!recursive) {
				continue;
			}
			if (value instanceof Map) {
				boolean isEmpty = squashNullsInMap(value);
				if (isEmpty) {
					itr.remove();
				}
				continue;
			}
			if (value instanceof List) {
				boolean isEmpty = squashNullsInList(value);
				if (isEmpty) {
					itr.remove();
				}
				continue;
			}
		}
		return inputAsMap.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private boolean squashNullsInList(Object input) {
		List<Object> inputAsList = (List<Object>) input;
		Iterator<Object> itr = inputAsList.iterator();
		while (itr.hasNext()) {
			Object object = itr.next();
			if (object == null) {
				itr.remove();
				continue;
			}
			if (!recursive) {
				continue;
			}
			if (object instanceof Map) {
				boolean isEmpty = squashNullsInMap(object);
				if (isEmpty) {
					itr.remove();
				}
				continue;
			}
			if (object instanceof List) {
				boolean isEmpty = squashNullsInList(object);
				if (isEmpty) {
					itr.remove();
				}
				continue;
			}
		}
		return inputAsList.isEmpty();
	}

	@Override
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		if (input instanceof Map) {
			squashNullsInMap(input);
			return input;
		}
		if (input instanceof List) {
			squashNullsInList(input);
			return input;
		}
		return input;
	}
}
