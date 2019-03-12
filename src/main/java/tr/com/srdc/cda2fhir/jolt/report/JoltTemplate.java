package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoltTemplate {
    private final Logger logger = LoggerFactory.getLogger(JoltTemplate.class);

	public List<Map<String, Object>> shifts = new ArrayList<Map<String, Object>>();
	public Map<String, Object> cardinality;
	public Map<String, Object> format;
	
	public boolean topTemplate = false;
	public boolean leafTemplate = false;
	
	@SuppressWarnings("unchecked")
	private void appendRowsFromShift(Map<String, JoltTemplate> map, Table table, String parentPath, Map<String, Object> shift) {
		Set<Map.Entry<String, Object>> entrySet = shift.entrySet();
		for (Map.Entry<String, Object> entry: entrySet) {
			String key = entry.getKey();
			String path = parentPath.length() > 0 ? parentPath + "." + key : key;
			Object value = entry.getValue();
			if (value instanceof Map) {
				Map<String, Object> nextShift = (Map<String, Object>) value;
				appendRowsFromShift(map, table, path, nextShift);
				continue;
			}
			if (value instanceof String) {
				TableRow row = new TableRow();
				String target = (String) value;
				if (key.charAt(0) == '#') {
					row.path = String.format("%s%s%s", "\"", key.substring(1), "\"");
					row.target = target;
					table.rows.add(row);
					continue;
				}
				String[] targetPieces = target.split("\\.");
				if (targetPieces.length > 0) {
					String lastTarget = targetPieces[targetPieces.length -1];
					if (lastTarget.startsWith("->")) {
						String targetName = lastTarget.substring(2);
						JoltTemplate targetTemplate = map.get(targetName);
						if (targetTemplate == null) {
							logger.error("Unknown template: " + targetName);
						} else {
							if (!targetTemplate.leafTemplate && !targetTemplate.topTemplate) {
								Table targetTable = targetTemplate.createTable(map);
								String targetCSV = targetTable.writeCsv();
								System.out.println(targetCSV);
							}
						}
					}	
				}
				row.path = path;
				row.target = (String) value;
				table.rows.add(row);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<JoltPath> toPaths(Map<String, Object> map, Map<String, JoltTemplate> templateMap) {
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
				List<JoltPath> subResult = toPaths((Map<String, Object>) value, templateMap);
				subResult.forEach(r -> r.prependPath(key));
				result.addAll(subResult);
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
		List<JoltPath> expandedResult = expandPathLinks(result, templateMap);
		return expandedResult;
	}
	
	private static List<JoltPath> expandPathLinks(List<JoltPath> joltPaths, Map<String, JoltTemplate> map) {
		List<JoltPath> result = new ArrayList<JoltPath>();
		joltPaths.forEach(joltPath -> {
			String link = joltPath.getLink();
			if (link == null) {
				result.add(joltPath);
				return;
			}			
			JoltTemplate linkedTemplate = map.get(link);
			if (linkedTemplate.leafTemplate || linkedTemplate.topTemplate) {
				result.add(joltPath);
				return;				
			}
			if (linkedTemplate.shifts.size() < 1) {
				result.add(joltPath);
				return;								
			}
			Map<String, Object> shift = linkedTemplate.shifts.get(0);
			List<JoltPath> linkedPaths = toPaths(shift, map);
			linkedPaths.forEach(linkedPath -> {
				linkedPath.prependFrom(joltPath);
			});
			result.addAll(linkedPaths);
		});
		return result;
	}

	public Table createTable(Map<String, JoltTemplate> map) {
		Table result = new Table();
		Map<String, Object> shift = shifts.get(0);
		List<JoltPath> joltPaths = toPaths(shift, map);
		
		joltPaths.forEach(jp -> System.out.println(jp.toString()));		
		//appendRowsFromShift(map, result, "", shift);
		return result;
	}
}
