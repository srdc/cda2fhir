package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;

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

	@SuppressWarnings("unchecked")
	private static void fillNode(INode node, Map<String, Object> map) {
		map.forEach((key, value) -> {
			if (value == null) {
				JoltPath joltPath = new JoltPath(key);
				node.addChild(joltPath);
				return;
			}
			if (value instanceof Map) {
				JoltPath childNode = new JoltPath(key);
				fillNode(childNode, (Map<String, Object>) value);
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
		return node;
	}
}
