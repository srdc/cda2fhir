package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.IRootNodeUpdater;
import tr.com.srdc.cda2fhir.jolt.KeepWhen;
import tr.com.srdc.cda2fhir.jolt.RemoveWhen;
import tr.com.srdc.cda2fhir.jolt.RemoveWhenNull;
import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltTemplate {
	private static final class RawTemplate {
		public List<Map<String, Object>> shifts = new ArrayList<>();
		public Map<String, Object> assign;
		public Map<String, Object> accumulator;
		public List<Map<String, Object>> modifiers = new ArrayList<>();
		public Map<String, Object> move;
		public Map<String, Object> flatten;
		public Map<String, Object> distributeArray;
		public Map<String, Object> linkSettings;
		public List<IRootNodeUpdater> rootNodeUpdater = new ArrayList<>();
		public Map<String, Object> defaults;

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
				if (operation.equals("default") && (result.shifts.size() > 0)) {
					result.defaults = spec;
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
					IRootNodeUpdater rootNodeUpdater = new RemoveWhen(spec);
					result.rootNodeUpdater.add(rootNodeUpdater);
					return;
				}
				if (operation.endsWith("RemoveWhenNull")) {
					IRootNodeUpdater rootNodeUpdater = new RemoveWhenNull(spec);
					result.rootNodeUpdater.add(rootNodeUpdater);
					return;
				}
				if (operation.endsWith("KeepWhen")) {
					IRootNodeUpdater rootNodeUpdater = new KeepWhen(spec);
					result.rootNodeUpdater.add(rootNodeUpdater);
					return;
				}
				if (operation.endsWith("Move")) {
					result.move = spec;
					return;
				}
				if (operation.endsWith("Flatten")) {
					result.flatten = spec;
					return;
				}
				if (operation.endsWith("DistributeArray")) {
					result.distributeArray = spec;
					return;
				}
				if (operation.endsWith("Substitute")) {
					result.linkSettings = spec;
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

	private static Map<String, String> convertRawDefaults(String resourceType, Map<String, Object> input) {
		if (input == null) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		input.entrySet().forEach(entry -> {
			String key = resourceType + "." + entry.getKey();
			result.put(key, entry.getValue().toString());
		});
		return result;
	}

	private String name;
	private RootNode rootNode;
	private RootNode supportRootNode;
	private JoltFormat format;
	private Map<String, String> moveMap;
	private String flattened;

	private boolean leafTemplate = false;
	private String resourceType;

	private Table assignTable;

	private Set<String> distributeArrays;
	private Map<String, String> alias;
	private Map<String, String> defaults;

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

	public Map<String, String> getAlias() {
		if (alias == null) {
			return alias;
		}
		return Collections.unmodifiableMap(alias);
	}

	private static Map<String, JoltTemplate> getLeafTemplates(Map<String, JoltTemplate> map) {
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
		if (alias != null) {
			result.renameSources(alias);
		}
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

	public boolean isDistributed(String target) {
		if (distributeArrays != null && target.length() > 0) {
			int lastIndex = target.lastIndexOf('.');
			String lastPathRaw = target.substring(lastIndex + 1);
			String lastPath = lastPathRaw.split("\\[")[0];
			return distributeArrays.contains(lastPath);
		}
		return false;
	}

	private Table createTable(Map<String, JoltTemplate> map, Map<String, JoltTemplate> leafTemplates) {
		JoltFormat resolvedFormat = getResolvedFormat(leafTemplates);
		Table assignTable = getAssignTable(leafTemplates);
		if (assignTable != null) {
			assignTable.correctArrayOnFormat();
		}

		rootNode.expandLinks(this, leafTemplates);

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

		if (flattened != null) {
			table.flattenTarget(flattened);
		}

		if (defaults != null) { // assume top level status fields
			table.addDefaultValues(defaults);
		}

		return table;
	}

	public Table createTable(Map<String, JoltTemplate> map, boolean fullyExpand) {
		if (fullyExpand) {
			return createTable(map, map);
		} else {
			Map<String, JoltTemplate> leafTemplates = getLeafTemplates(map);
			return createTable(map, leafTemplates);
		}
	}

	@SuppressWarnings("unchecked")
	public static JoltTemplate getInstance(String name, List<Object> content) {
		JoltTemplate result = new JoltTemplate(name);
		result.leafTemplate = name.equals(name.toUpperCase()) || name.equals("IVL_TSPeriod")
				|| name.equals("PIVL_TSTiming") || name.startsWith("IVL_PQ");

		RawTemplate rawTemplate = RawTemplate.getInstance(content);

		if (rawTemplate.modifiers != null) {
			result.format = JoltFormat.getInstance(rawTemplate.modifiers);
		}

		result.rootNode = NodeFactory.getInstance(rawTemplate.shifts.get(0));

		rawTemplate.rootNodeUpdater.forEach(rnu -> {
			rnu.update(result.rootNode);
		});

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

		if (rawTemplate.flatten != null) {
			result.flattened = (String) rawTemplate.flatten.entrySet().iterator().next().getValue();
		}

		if (rawTemplate.distributeArray != null) {
			result.distributeArrays = rawTemplate.distributeArray.keySet();
		}

		if (rawTemplate.linkSettings != null && !rawTemplate.linkSettings.isEmpty()) {
			Map<String, Object> aliasObject = (Map<String, Object>) rawTemplate.linkSettings.get("alias");
			if (aliasObject != null) {
				Map<String, String> alias = new HashMap<String, String>();
				aliasObject.entrySet().forEach(entry -> {
					alias.put(entry.getKey(), (String) entry.getValue());
				});
				result.alias = alias;
			}
		}

		if (result.resourceType != null) {
			result.defaults = convertRawDefaults(result.resourceType, rawTemplate.defaults);
		}

		return result;
	}
}
