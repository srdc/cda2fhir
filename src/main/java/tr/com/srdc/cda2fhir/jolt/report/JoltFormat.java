package tr.com.srdc.cda2fhir.jolt.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoltFormat {
	private Map<String, String> map = new HashMap<String, String>();

	public String get(String target) {
		return map.get(target);
	}

	@Override
	public JoltFormat clone() {
		JoltFormat formatClone = new JoltFormat();
		formatClone.map.putAll(map);
		return formatClone;
	}

	public void putAllAsPromoted(JoltFormat source, String target) {
		source.map.forEach((key, format) -> {
			if (target.isEmpty()) {
				map.put(key, format);
			} else {
				String promotedKey = String.format("%s.%s", target, key);
				map.put(promotedKey, format);
			}
		});
	}

	private static String formatValue(String value, String previousValue) {
		String result = value.replace(",@0", "").replace("(@0)", "");
		String currentValue = result.substring(1);
		return previousValue == null ? currentValue : previousValue + " " + currentValue;
	}

	@SuppressWarnings("unchecked")
	private static void fillResult(JoltFormat result, Map<String, Object> map, String parentPath) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				throw new ReportException("There must be a function for each entry in format specification");
			}
			if (value instanceof Map) {
				Map<String, Object> valueAsMap = (Map<String, Object>) value;
				for (Map.Entry<String, Object> valueEntry : valueAsMap.entrySet()) {
					String newPath = key;
					String valueKey = valueEntry.getKey();
					if (valueKey.equals("*")) {
						newPath += "[]";
					} else {
						newPath += "." + valueKey;
					}
					if (parentPath.length() > 0) {
						newPath = parentPath + "." + newPath;
					}
					Object valueEntryValue = valueEntry.getValue();
					if (valueEntryValue instanceof String) {
						String previousValue = result.map.get(newPath);
						result.map.put(newPath, formatValue((String) valueEntryValue, previousValue));
						continue;
					}
					fillResult(result, (Map<String, Object>) valueEntryValue, newPath);
				}
				continue;
			}
			if (value instanceof String) {
				String path = key;
				if (parentPath.length() > 0) {
					if (key.equals("*")) {
						path = parentPath + "[]";
					} else {
						path = String.format("%s.%s", parentPath, path);
					}
				}
				String previousValue = result.map.get(path);
				result.map.put(path, formatValue((String) value, previousValue));
				continue;
			}
			if (value instanceof List) {
				throw new ReportException("Lists are not supported in format specification");
			}
		}
	}

	public static JoltFormat getInstance(List<Map<String, Object>> maps) {
		JoltFormat result = new JoltFormat();
		maps.forEach(map -> fillResult(result, map, ""));
		return result;
	}
}
