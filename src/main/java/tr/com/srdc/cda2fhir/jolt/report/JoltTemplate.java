package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoltTemplate {
	public List<Map<String, Object>> shifts = new ArrayList<Map<String, Object>>();
	public Map<String, Object> cardinality;
	public Map<String, Object> format;
	
	public boolean topTemplate = false;
	public boolean leafTemplate = false;
	
	@SuppressWarnings("unchecked")
	private static List<JoltPath> toPaths(Map<String, Object> map) {
		List<JoltPath> result = new ArrayList<JoltPath>();
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				JoltPath joltPath = JoltPath.getInstance(key, null);
				result.add(joltPath);
				continue;
			}			
			if (value instanceof Map) {
				List<JoltPath> children = toPaths((Map<String, Object>) value);
				JoltPath joltPath = JoltPath.getInstance(key, null);
				joltPath.addChildren(children);
				result.add(joltPath);
				continue;
			}
			if (value instanceof String) {
				JoltPath joltPath = JoltPath.getInstance(key, (String) value);
				result.add(joltPath);
				continue;
			}
			if (value instanceof List) {
				List<String> values = (List<String>) value;
				values.forEach(target -> {
					JoltPath joltPath = JoltPath.getInstance(key, target);
					result.add(joltPath);					
				});
				continue;
			}
		}
		return result;
	}
	
	public List<JoltPath> toJoltPaths() {
		Map<String, Object> shift = shifts.get(0);
		List<JoltPath> joltPaths = toPaths(shift);
		return joltPaths;
	}
	
	private static Map<String, List<JoltPath>> getExpandableLinks(Map<String, JoltTemplate> map) {
		return map.entrySet().stream().filter(entry -> {
			JoltTemplate value = entry.getValue();
			if (value.leafTemplate || value.topTemplate) {
				return false;
			}
			if (value.shifts.size() < 1) {
				return false;								
			}
			return true;
		}).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toJoltPaths()));
	}
	
	public Table createTable(Map<String, JoltTemplate> map) {
		Table result = new Table();
		List<JoltPath> joltPaths = toJoltPaths();
		Map<String, List<JoltPath>> expandable = getExpandableLinks(map);
		joltPaths.forEach(jp -> jp.expandLinks(expandable));
		joltPaths.forEach(jp -> jp.createConditions(null));
		
		joltPaths.forEach(jp -> {
			List<TableRow> rows = jp.toTableRows();
			rows.forEach(r -> System.out.println(r.toString()));			
		});		
		//appendRowsFromShift(map, result, "", shift);
		return result;
	}
}
