package tr.com.srdc.cda2fhir.jolt;

import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class ResolveDeferred implements ContextualTransform {
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		List<DeferredUpdate> duList = (List<DeferredUpdate>) context.get("DEFERRED_UPDATE");
		if (duList == null) {
			return input;
		}
		IdentifierMap<String> refsByIdentifier = (IdentifierMap<String>) context.get("RefsByIdentifier");
		IdentifierMap<String> refDisplaysByIdentifier = (IdentifierMap<String>) context.get("RefDisplaysByIdentifier");
		if (refsByIdentifier == null && refDisplaysByIdentifier == null) {
			return input;
		}
		duList.forEach(du -> {
			du.update(refsByIdentifier, refDisplaysByIdentifier);
		});
		return input;
	}
}
