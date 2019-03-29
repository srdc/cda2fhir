package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltTemplate {
	private static final class RawTemplate {
		public List<Map<String, Object>> shifts = new ArrayList<>();
		public Map<String, Object> assign;
		public Map<String, Object> accumulator;
		public List<Map<String, Object>> modifiers = new ArrayList<>();
		public Map<String, Object> removeWhen;
		public Map<String, Object> move;
		public Map<String, Object> distributeArray;

		@SuppressWarnings("unchecked")
		public static RawTemplate getInstance(List<Object> content) {
			RawTemplate result = new RawTemplate();

			content.forEach(rawTransform -> {
				Map<String, Object> transform = (Map<String, Object>) rawTransform;

				String operation = (String) transform.get("operation");
				Map<String, Object> spec = (Map<String, Object>) transform.get("spec");

				if (operation.equals("shift")) {
					result.shifts.add(spec);
					return;
				}
				if (operation.endsWith("Assign")) {
					result.assign = spec;
					return;
				}
				if (operation.endsWith("ResourceAccumulator")) {
					result.accumulator = spec;
					return;
				}
				if (operation.endsWith("AdditionalModifier")) {
					result.modifiers.add(spec);
					return;
				}
				if (operation.endsWith("RemoveWhen")) {
					result.removeWhen = spec;
					return;
				}
				if (operation.endsWith("Move")) {
					result.move = spec;
					return;
				}
				if (operation.endsWith("DistributeArray")) {
					result.distributeArray = spec;
					return;
				}
			});

			if (result.shifts.isEmpty()) {
				throw new ReportException("Templates should have at least on 'shift' transform.");
			}
			if (result.shifts.size() > 2) {
				throw new ReportException("Templates can have at most two 'shift' transforms.");
			}

			return result;
		}
	}

	private String name;
	private RootNode rootNode;
	private RootNode supportRootNode;
	private JoltFormat format;
	private Map<String, String> moveMap;

	private boolean leafTemplate = false;
	private String resourceType;

	private Table assignTable;

	private Set<String> distributeArrays;

	private JoltTemplate(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public RootNode getRootNode() {
		return rootNode;
	}

	public boolean doesGenerateResource() {
		return this.resourceType != null;
	}

	private static Map<String, JoltTemplate> getIntermediateTemplates(Map<String, JoltTemplate> map) {
		return map.entrySet().stream().filter(entry -> {
			JoltTemplate value = entry.getValue();
			if (value.leafTemplate || value.resourceType != null) {
				return false;
			}
			if (value.rootNode == null) {
				return false;
			}
			return true;
		}).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
	}

	private JoltFormat getResolvedFormat(Map<String, JoltTemplate> templateMap) {
		if (format == null) {
			return null;
		}
		JoltFormat result = format.clone();
		List<ILinkedNode> linkedNodes = rootNode.getLinkedNodes();
		linkedNodes.forEach(linkedNode -> {
			String link = linkedNode.getLink();
			String target = linkedNode.getTarget();

			JoltTemplate linkedTemplate = templateMap.get(link);
			if (linkedTemplate == null)
				return;

			JoltFormat resolvedLinkedFormat = linkedTemplate.getResolvedFormat(templateMap);
			if (resolvedLinkedFormat != null) {
				result.putAllAsPromoted(resolvedLinkedFormat, target);
			}
		});
		return result;
	}

	private Table getAssignTable(Map<String, JoltTemplate> templateMap) {
		if (assignTable == null) {
			return null;
		}
		Table result = assignTable.clone();
		List<ILinkedNode> linkedNodes = rootNode.getLinkedNodes();
		linkedNodes.forEach(linkedNode -> {
			String link = linkedNode.getLink();
			String target = linkedNode.getTarget();

			JoltTemplate linkedTemplate = templateMap.get(link);
			if (linkedTemplate == null)
				return;

			Table linkedTable = linkedTemplate.getAssignTable(templateMap);
			if (linkedTable != null) {
				linkedTable.promoteTargets(target);
				result.addTable(linkedTable);
			}
		});
		return result;
	}

	public Table createTable(Map<String, JoltTemplate> map) {
		Map<String, JoltTemplate> intermediateTemplates = getIntermediateTemplates(map);

		JoltFormat resolvedFormat = getResolvedFormat(map);
		Table assignTable = getAssignTable(map);
		if (assignTable != null) {
			assignTable.correctArrayOnFormat();
		}

		rootNode.expandLinks(intermediateTemplates);

		if (distributeArrays != null) {
			rootNode.distributeArrays(distributeArrays);
		}

		Templates templates = new Templates(resourceType, map, resolvedFormat);
		Table table = rootNode.toTable(templates);

		if (supportRootNode != null) {
			Table supportTable = supportRootNode.toTable(new Templates());
			Map<String, TableRow> pathMap = supportTable.getPathMap();
			table = table.getUpdatedFromPathMap(pathMap);
		}

		if (assignTable != null) {
			if (resourceType != null) {
				Set<String> otherTargets = table.getRows().stream().map(r -> r.getTarget())
						.filter(r -> !r.startsWith(resourceType)).collect(Collectors.toSet());
				assignTable.updateResourceType(resourceType, otherTargets);
			}
			table.addTable(assignTable);
		}
		if (moveMap != null) {
			table.moveTargets(moveMap);
		}

		return table;
	}

	public static JoltTemplate getInstance(String name, List<Object> content) {
		JoltTemplate result = new JoltTemplate(name);
		result.leafTemplate = name.equals(name.toUpperCase()) || name.equals("IVL_TSPeriod");

		RawTemplate rawTemplate = RawTemplate.getInstance(content);

		if (rawTemplate.modifiers != null) {
			result.format = JoltFormat.getInstance(rawTemplate.modifiers);
		}
		result.rootNode = NodeFactory.getInstance(rawTemplate.shifts.get(0));
		if (rawTemplate.removeWhen != null) {
			result.rootNode.updateFromRemoveWhen(rawTemplate.removeWhen);
		}
		if (rawTemplate.shifts.size() > 1) {
			result.supportRootNode = NodeFactory.getInstance(rawTemplate.shifts.get(1));
		}
		if (rawTemplate.accumulator != null) {
			result.resourceType = (String) rawTemplate.accumulator.get("resourceType");
		}
		if (rawTemplate.assign != null) {
			RootNode assignRootNode = NodeFactory.getInstance(rawTemplate.assign);
			Templates templates = new Templates(result.format);
			result.assignTable = assignRootNode.toTable(templates);
		}
		if (rawTemplate.move != null) {
			result.moveMap = new HashMap<String, String>();
			rawTemplate.move.entrySet().forEach(entry -> {
				String key = entry.getKey();
				String value = (String) entry.getValue();
				result.moveMap.put(key, value);
			});
		}
		if (rawTemplate.distributeArray != null) {
			result.distributeArrays = rawTemplate.distributeArray.keySet();
		}

		return result;
	}
}
