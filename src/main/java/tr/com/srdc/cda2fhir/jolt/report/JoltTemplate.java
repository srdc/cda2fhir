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
	private static JoltPath toRootPath(Map<String, Object> map) {
		JoltPath result = new JoltPath("root");
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				JoltPath joltPath = JoltPath.getInstance(key, null);
				result.addChild(joltPath);
				continue;
			}			
			if (value instanceof Map) {
				JoltPath rootChild = toRootPath((Map<String, Object>) value);
				JoltPath joltPath = JoltPath.getInstance(key, null);
				joltPath.addChildrenOf(rootChild);
				result.addChild(joltPath);
				continue;
			}
			if (value instanceof String) {
				JoltPath joltPath = JoltPath.getInstance(key, (String) value);
				result.addChild(joltPath);
				continue;
			}
			if (value instanceof List) {
				List<String> values = (List<String>) value;
				values.forEach(target -> {
					JoltPath joltPath = JoltPath.getInstance(key, target);
					result.addChild(joltPath);					
				});
				continue;
			}
		}
		return result;
	}
	
	public JoltPath toJoltPath() {
		Map<String, Object> shift = shifts.get(0);
		return toRootPath(shift);
	}
	
	private static Map<String, JoltPath> getExpandableLinks(Map<String, JoltTemplate> map) {
		return map.entrySet().stream().filter(entry -> {
			JoltTemplate value = entry.getValue();
			if (value.leafTemplate || value.topTemplate) {
				return false;
			}
			if (value.shifts.size() < 1) {
				return false;								
			}
			return true;
		}).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toJoltPath()));
	}
	
	public Table createTable(Map<String, JoltTemplate> map) {
		Table result = new Table();
		JoltPath rootPath = toJoltPath();
		Map<String, JoltPath> expandable = getExpandableLinks(map);
		
		rootPath.expandLinks(expandable);
		rootPath.createConditions();
		rootPath.mergeSpecialDescendants();

		rootPath.children.forEach(jp -> {
			List<TableRow> rows = jp.toTableRows();
			rows.forEach(r -> System.out.println(r.toString()));			
		});		
		//appendRowsFromShift(map, result, "", shift);
		return result;
	}
}
