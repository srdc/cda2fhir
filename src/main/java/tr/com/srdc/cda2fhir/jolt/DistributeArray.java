package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import tr.com.srdc.cda2fhir.jolt.report.ReportException;

public class DistributeArray implements ContextualTransform, SpecDriven {
	private Map<String, Object> spec;

	@SuppressWarnings("unchecked")
	@Inject
	public DistributeArray(Object spec) {
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
			if (!(source instanceof List)) {
				throw new ReportException("Only list fields can be distributed.");
			}
			List<Object> sourceAsList = (List<Object>) source;
			Map<String, Object> result = new LinkedHashMap<>();
			sourceAsList.forEach(element -> {
				if (element == null) {
					return;
				}
				if (!(element instanceof Map)) {
					throw new ReportException("List can only be distributed to objects.");
				}
				Map<String, Object> elementAsMap = (Map<String, Object>) element;
				if (elementAsMap.size() == 0) {
					return;
				}
				elementAsMap.entrySet().forEach(sourceEntry -> {
					String sourceKey = sourceEntry.getKey();
					Object sourceValue = sourceEntry.getValue();
					if (sourceValue == null) {
						return;
					}
					List<Object> resultEntry = (List<Object>) result.get(sourceKey);
					if (resultEntry == null) {
						resultEntry = new ArrayList<Object>();
						result.put(sourceKey, resultEntry);
					}
					resultEntry.add(sourceValue);
				});
			});
			inputAsMap.remove(key);
			if (!result.isEmpty()) {
				inputAsMap.putAll(result);
			}
		});
		return input;
	}
}
