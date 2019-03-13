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
	
	public JoltPath toJoltPath() {
		Map<String, Object> shift = shifts.get(0);
		return JoltPath.getInstance(shift);
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
		JoltPath rootPath = toJoltPath();
		Map<String, JoltPath> expandable = getExpandableLinks(map);
		
		rootPath.expandLinks(expandable);
		rootPath.conditionalize();

		return rootPath.toTable();
	}
}
