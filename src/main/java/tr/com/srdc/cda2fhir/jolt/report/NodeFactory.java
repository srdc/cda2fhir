package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.ConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.EntryNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LeafNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.ParentNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class NodeFactory {
	private final static class ParsedTarget {
		public String target;
		public String link;
		
		private ParsedTarget(String target) {
			this.target = target;
		}
		
		private ParsedTarget(String target, String link) {
			this.target = target;
			this.link = link;
		}
		
		public static ParsedTarget getInstance(String target) {
			String[] pieces = target.split("\\.");
			int length = pieces.length;
			String lastPiece = pieces[length - 1];
			if (!lastPiece.startsWith("->")) {
				return new ParsedTarget(target);
			}
			String link = lastPiece.substring(2);
			if (length == 1) {
				return new ParsedTarget("", link);
			}
			String reducedTarget = pieces[0];
			for (int index = 1; index < length - 1; ++index) {
				reducedTarget += "." + pieces[index];
			}
			return new ParsedTarget(reducedTarget, link);			
		}
	}
	
	private static LeafNode getInstance(String path, String target) {
		ParsedTarget parsedTarget = ParsedTarget.getInstance(target);
		return new LeafNode(path, parsedTarget.target, parsedTarget.link);
	}

	private static List<JoltCondition> childToCondition(String value, INode parent) {
		if ("*".equals(value)) {
			return parent.getChildren().stream().map(c -> c.getConditions().get(0)).map(c -> c.not())
					.collect(Collectors.toList());
		}
		if (value.isEmpty()) {
			return Collections.singletonList(new JoltCondition("", "isnull"));
		}
		return Collections.singletonList(new JoltCondition("", "equal", value));
	}

	private static String decrementSpecialPath(String path) {
		int value = Integer.valueOf(path.substring(1));
		value -= 1;
		return "!" + value;
	}

	@SuppressWarnings("unchecked")
	private static ParentNode toConditionNode(String value, Map<String, Object> map, INode parent) {
		String key = map.keySet().iterator().next();
		if (key.isEmpty() || key.charAt(0) != '@') {
			return null;
		}
		String newPath = decrementSpecialPath(key);

		List<JoltCondition> conditions = childToCondition(value, parent);
		Object conditionChilren = map.get(key);
		if (conditionChilren instanceof String) {
			ParsedTarget pt = ParsedTarget.getInstance((String) conditionChilren);
			ParentNode conditionNode = new ConditionNode(newPath, pt.target, pt.link);
			conditionNode.conditions.addAll(conditions);
			return conditionNode;
		}
		ParentNode conditionNode = new ConditionNode(newPath);			
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
				ParentNode childNode = toConditionNode(key, valueMap, node);
				if (childNode == null) {
					childNode = new ParentNode(key);
					fillNode(childNode, valueMap);
				}
				node.addChild(childNode);
				return;
			}
			if (value instanceof String) {
				LeafNode joltPath = getInstance(key, (String) value);
				node.addChild(joltPath);
				return;
			}
			if (value instanceof List) {
				List<String> values = (List<String>) value;
				values.forEach(target -> {
					LeafNode joltPath = getInstance(key, target);
					node.addChild(joltPath);
				});
				return;
			}
		});
	}

	public static RootNode getInstance(Map<String, Object> map, String resourceType) {
		RootNode node = resourceType == null ? new RootNode() : new EntryNode(resourceType);
		INode base = node.getBase();
		fillNode(base, map);
		node.conditionalize();
		return node;
	}
}
