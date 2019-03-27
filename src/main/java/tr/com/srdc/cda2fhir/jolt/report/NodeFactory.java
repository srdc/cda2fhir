package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.ConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LeafConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LeafNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LeafRawConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LeafWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LinkedConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LinkedRawConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.LinkedWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.ParentNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RawConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.WildcardNode;

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

	private static LeafNode getInstance(IParentNode parent, String path, String target) {
		ParsedTarget parsedTarget = ParsedTarget.getInstance(target);
		if (parsedTarget.link == null) {
			if (path.equals("*")) {
				return new LeafWildcardNode(parent, path, parsedTarget.target);
			} else if (path.equals("@0")) {
				return new LeafRawConditionNode(parent, 0, parsedTarget.target);
			} else {
				return new LeafNode(parent, path, parsedTarget.target);
			}
		} else {
			if (path.equals("*")) {
				return new LinkedWildcardNode(parent, path, parsedTarget.target, parsedTarget.link);
			} else if (path.equals("@0")) {
				return new LinkedRawConditionNode(parent, 0, parsedTarget.target, parsedTarget.link);
			} else {
				return new LinkedNode(parent, path, parsedTarget.target, parsedTarget.link);
			}
		}
	}

	private static List<JoltCondition> childToCondition(String value, IParentNode parent) {
		if ("*".equals(value)) {
			return parent.getChildren().stream().map(c -> c.getConditions().get(0)).map(c -> c.not())
					.collect(Collectors.toList());
		}
		if (value.isEmpty()) {
			return Collections.singletonList(new JoltCondition("", "isnull"));
		}
		return Collections.singletonList(new JoltCondition("", "equal", value));
	}

	@SuppressWarnings("unchecked")
	private static boolean isValueBranching(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value != null && value instanceof Map) {
				Map<String, Object> valueAsMap = (Map<String, Object>) value;
				for (String key : valueAsMap.keySet()) {
					if (!key.isEmpty() && key.charAt(0) == '@' && !key.contentEquals("@0")) {
						return true;
					}
				}

			}
		}
		return false;
	};

	@SuppressWarnings("unchecked")
	private static void fillConditionNode(IParentNode parent, Map<String, Object> map) {
		map.forEach((nodeValue, conditionSpec) -> {
			if (conditionSpec == null)
				return;

			if (!(conditionSpec instanceof Map)) {
				throw new ReportException("Value based branch can only be an object or null.");
			}

			Map<String, Object> conditionSpecAsMap = (Map<String, Object>) conditionSpec;

			Set<Map.Entry<String, Object>> conditionSpecs = conditionSpecAsMap.entrySet();

			if (conditionSpecs.size() > 1) {
				throw new ReportException("'@' nodes cannot have siblings.");
			}

			Map.Entry<String, Object> entry = conditionSpecs.iterator().next();

			List<JoltCondition> conditions = childToCondition(nodeValue, parent);

			int rank = Integer.valueOf(entry.getKey().substring(1));

			Object conditionChilren = entry.getValue();
			if (conditionChilren instanceof String) {
				ParsedTarget pt = ParsedTarget.getInstance((String) conditionChilren);
				if (pt.link == null) {
					LeafConditionNode conditionNode = new LeafConditionNode(parent, rank - 1, pt.target);
					conditionNode.addConditions(conditions);
					parent.addChild(conditionNode);
				} else {
					LinkedConditionNode conditionNode = new LinkedConditionNode(parent, rank - 1, pt.target, pt.link);
					conditionNode.addConditions(conditions);
					parent.addChild(conditionNode);
				}
				return;
			}
			ParentNode conditionNode = new ConditionNode(parent, rank - 1);
			conditionNode.addConditions(conditions);
			fillNode(conditionNode, (Map<String, Object>) conditionChilren);
			parent.addChild(conditionNode);
		});
	}

	@SuppressWarnings("unchecked")
	private static void fillNode(IParentNode node, Map<String, Object> map) {
		if (isValueBranching(map)) {
			fillConditionNode(node, map);
			return;
		}

		map.forEach((key, value) -> {
			if (value == null) {
				JoltCondition condition = new JoltCondition(key, "isnull");
				node.addCondition(condition);
				return;
			}
			if (value instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) value;
				ParentNode parentNode;
				if (key.equals("*")) {
					parentNode = new WildcardNode(node, key);
				} else if (key.equals("@0")) {
					parentNode = new RawConditionNode(node, 1);
				} else {
					parentNode = new ParentNode(node, key);
				}
				fillNode(parentNode, valueMap);
				node.addChild(parentNode);
				return;
			}
			if (value instanceof String) {
				LeafNode joltPath = getInstance(node, key, (String) value);
				node.addChild(joltPath);
				return;
			}
			if (value instanceof List) {
				List<String> values = (List<String>) value;
				values.forEach(target -> {
					LeafNode joltPath = getInstance(node, key, target);
					node.addChild(joltPath);
				});
				return;
			}
		});
	}

	public static RootNode getInstance(Map<String, Object> map) {
		RootNode node = new RootNode();
		IParentNode base = node.getBase();
		fillNode(base, map);
		node.eliminateWildcardNodes();
		node.conditionalize();
		return node;
	}
}
