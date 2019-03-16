package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.EntryNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class NodeFactory {
	private static JoltPath getInstance(String path, String target) {
		String[] pieces = target.split("\\.");
		int length = pieces.length;
		String lastPiece = pieces[length - 1];
		if (!lastPiece.startsWith("->")) {
			return new JoltPath(path, target);
		}
		String link = lastPiece.substring(2);
		if (length == 1) {
			return new JoltPath(path, "", link);
		}
		String reducedTarget = pieces[0];
		for (int index = 1; index < length - 1; ++index) {
			reducedTarget += "." + pieces[index];
		}
		return new JoltPath(path, reducedTarget, link);
	}

	private static List<JoltCondition> conditionToList(JoltCondition condition) {
		List<JoltCondition> conditions = new ArrayList<JoltCondition>();
		conditions.add(condition);
		return conditions;
	}

	private static List<JoltCondition> childToCondition(String value, INode parent) {
		if ("*".equals(value)) {
			return parent.getChildren().stream().map(c -> c.getConditions().get(0)).map(c -> c.not())
					.collect(Collectors.toList());
		}
		if (value.length() == 0) {
			return conditionToList(new JoltCondition("", "isnull"));
		}
		return conditionToList(new JoltCondition("", "equal", value));
	}

	private static String decrementSpecialPath(String path) {
		int value = Integer.valueOf(path.substring(1));
		value -= 1;
		return "!" + value;
	}

	@SuppressWarnings("unchecked")
	private static JoltPath toConditionNode(String value, Map<String, Object> map, INode parent) {
		String key = map.keySet().iterator().next();
		if (key.isEmpty() || key.charAt(0) != '@') {
			return null;
		}
		String newPath = decrementSpecialPath(key);

		List<JoltCondition> conditions = childToCondition(value, parent);
		Object conditionChilren = map.get(key);
		if (conditionChilren instanceof String) {	
			JoltPath conditionNode = getInstance(newPath, (String) conditionChilren);
			conditionNode.conditions.addAll(conditions);
			return conditionNode;
		}
		JoltPath conditionNode = new JoltPath(newPath);			
		conditionNode.conditions.addAll(conditions);
		fillNode(conditionNode, (Map<String, Object>) map.get(key));
		return conditionNode;
	}

	@SuppressWarnings("unchecked")
	private static void fillNode(INode node, Map<String, Object> map) {
		map.forEach((key, value) -> {
			if (value == null) {
				JoltCondition condition = new JoltCondition(key, "isnull");
				node.addCondition(condition);
				return;
			}
			if (value instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) value;
				JoltPath childNode = toConditionNode(key, valueMap, node);
				if (childNode == null) {
					childNode = new JoltPath(key);
					fillNode(childNode, valueMap);
				}
				node.addChild(childNode);
				return;
			}
			if (value instanceof String) {
				JoltPath joltPath = getInstance(key, (String) value);
				node.addChild(joltPath);
				return;
			}
			if (value instanceof List) {
				List<String> values = (List<String>) value;
				values.forEach(target -> {
					JoltPath joltPath = getInstance(key, target);
					node.addChild(joltPath);
				});
				return;
			}
		});
	}

	public static RootNode getInstance(Map<String, Object> map, String resourceType) {
		RootNode node = resourceType == null ? new RootNode() : new EntryNode(resourceType);
		fillNode(node, map);
		node.conditionalize();
		return node;
	}
}
