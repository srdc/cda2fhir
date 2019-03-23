package tr.com.srdc.cda2fhir.jolt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class SquashNulls implements ContextualTransform, SpecDriven {
	@Inject
	public SquashNulls(Object spec) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		if (input instanceof Map) {
			Map<String, Object> inputAsMap = (Map<String, Object>) input;
			Iterator<Map.Entry<String, Object>> itr = inputAsMap.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, Object> entry = itr.next();
				if (entry.getValue() == null) {
					itr.remove();
				}
			}
		}
		if (input instanceof List) {
			List<Object> inputAsList = (List<Object>) input;
			Iterator<Object> itr = inputAsList.iterator();
			while (itr.hasNext()) {
				Object object = itr.next();
				if (object == null) {
					itr.remove();
				}
			}
		}
		return input;
	}
}
