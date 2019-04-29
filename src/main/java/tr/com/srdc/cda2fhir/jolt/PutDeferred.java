package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

public class PutDeferred implements ContextualTransform {
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> identifier = (Map<String, Object>) inputAsMap.get("context");
		if (identifier == null) {
			return input;
		}
		List<DeferredUpdate> duList = (List<DeferredUpdate>) context.get("DEFERRED_UPDATE");
		if (duList == null) {
			duList = new ArrayList<DeferredUpdate>();
			context.put("DEFERRED_UPDATE", duList);
		}
		DeferredUpdate du = new DeferredUpdate(inputAsMap, identifier);
		duList.add(du);
		inputAsMap.remove("context");

		return input;
	}
}
