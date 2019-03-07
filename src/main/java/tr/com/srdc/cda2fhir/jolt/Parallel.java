package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

public class Parallel implements ContextualTransform, SpecDriven {
	private String path;
	private List<Object> branches;

	@Inject
	@SuppressWarnings("unchecked")
	public Parallel(Object spec) {
		Map<String, Object> map = (Map<String, Object>) spec;
		path = (String) map.get("path");
		branches = (List<Object>) map.get("branches");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> mapTop = (Map<String, Object>) input;
		Map<String, Object> map = (Map<String, Object>) mapTop.get(path);
		if (map == null) {
			return map;
		}
		EntryToResource etr = new EntryToResource();
		List<Object> results = branches.stream().map(r -> {
			Map<String, Object> result = new LinkedHashMap<String, Object>();
			String key = (String) r;
			result.put(key, map);
			return etr.transform(result, context);
		}).collect(Collectors.toList());
		return results.get(0);
	}
}
