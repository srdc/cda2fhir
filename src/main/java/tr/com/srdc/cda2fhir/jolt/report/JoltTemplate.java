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

	private static Map<String, JoltTemplate> getIntermediateTemplates(Map<String, JoltTemplate> map) {
		return map.entrySet().stream().filter(entry -> {
			JoltTemplate value = entry.getValue();
			if (value.leafTemplate || value.topTemplate) {
				return false;
			}
			if (value.shifts.size() < 1) {
				return false;								
			}
			return true;
		}).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
	}
	
	private static Map<String, JoltPath> getExpandableLinks(Map<String, JoltTemplate> templates) {		
		return templates.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toJoltPath()));
	}
	
	public Table createTable(Map<String, JoltTemplate> map) {
		JoltPath rootPath = toJoltPath();
		Map<String, JoltTemplate> intermediateTemplates = getIntermediateTemplates(map);
		
		Map<String, JoltPath> expandable = getExpandableLinks(intermediateTemplates);		
		rootPath.expandLinks(expandable);
		rootPath.conditionalize();

		return rootPath.toTable();
	}

	@SuppressWarnings("unchecked")
	public static JoltTemplate getInstance(List<Object> content) {
		JoltTemplate result = new JoltTemplate();

		boolean beforeShift = true;
		int length = content.size();
		for (int index = 0; index < length; ++index) {
			Map<String, Object> transform = (Map<String, Object>) content.get(index);
			String operation = (String) transform.get("operation");
			if (operation.equals("cardinality")) {
				if (beforeShift) {
					result.cardinality = (Map<String, Object>) transform.get("spec");
				}
				continue;
			}
			if (operation.equals("shift")) {
				Map<String, Object> shift = (Map<String, Object>) transform.get("spec");
				result.shifts.add(shift);
				continue;
			}
			if (operation.endsWith("ResourceAccumulator")) {
				result.topTemplate = true;
				continue;
			}
			if (operation.endsWith("AdditionalModifier")) {
				result.format = (Map<String, Object>) transform.get("spec");
				continue;
			}
		}
		
		return result;
	}
}
