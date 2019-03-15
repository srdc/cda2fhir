package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.EntryNode;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class NodeFactory {
	public static RootNode getInstance(Map<String, Object> map, String resourceType) {
		RootNode node = resourceType == null ? new RootNode() : new EntryNode(resourceType);
		List<JoltPath> list = JoltPath.toJoltPaths(map);
		list.forEach(joltPath -> node.addChild(joltPath));
		return node;
	}
}
