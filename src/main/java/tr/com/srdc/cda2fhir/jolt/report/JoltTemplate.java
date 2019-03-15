package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltTemplate {
	public List<Map<String, Object>> shifts = new ArrayList<Map<String, Object>>();
	public Map<String, Object> cardinality;
	public JoltFormat format;

	public boolean leafTemplate = false;
	private String resourceType;

	public RootNode toJoltPath() {
		Map<String, Object> shift = shifts.get(0);
		return NodeFactory.getInstance(shift, resourceType);
	}

	private static Map<String, JoltTemplate> getIntermediateTemplates(Map<String, JoltTemplate> map) {
		return map.entrySet().stream().filter(entry -> {
			JoltTemplate value = entry.getValue();
			if (value.leafTemplate || value.resourceType != null) {
				return false;
			}
			if (value.shifts.size() < 1) {
				return false;
			}
			return true;
		}).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
	}

	private static Map<String, RootNode> getPathLinks(Map<String, JoltTemplate> templates) {
		return templates.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toJoltPath()));
	}

	private static Map<String, JoltFormat> getFormatLinks(Map<String, JoltTemplate> templates) {
		return templates.entrySet().stream().filter(e -> e.getValue().format != null)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().format));
	}

	private static JoltFormat getResolvedFormat(JoltFormat format, RootNode rootPath,
			Map<String, JoltFormat> formatLinks, Map<String, RootNode> pathLinks) {
		if (format == null) {
			return null;
		}
		JoltFormat result = format.clone();
		List<JoltPath> linkPaths = rootPath.getLinks();
		linkPaths.forEach(joltPath -> {
			String link = joltPath.getLink();
			String target = joltPath.getTarget();

			JoltFormat linkedFormat = formatLinks.get(link);
			if (linkedFormat == null) {
				return;
			}
			RootNode linkedRootPath = pathLinks.get(link);
			JoltFormat resolvedLinkedFormat = getResolvedFormat(linkedFormat, linkedRootPath, formatLinks, pathLinks);
			result.putAllAsPromoted(resolvedLinkedFormat, target);
		});
		return result;
	}

	private JoltFormat getResolvedFormat(RootNode rootPath, Map<String, JoltFormat> formatLinks,
			Map<String, RootNode> pathLinks) {
		return getResolvedFormat(format, rootPath, formatLinks, pathLinks);
	}

	public Table createTable(Map<String, JoltTemplate> map) {
		Map<String, JoltTemplate> intermediateTemplates = getIntermediateTemplates(map);

		Map<String, JoltFormat> formatLinks = getFormatLinks(intermediateTemplates);
		Map<String, RootNode> pathLinks = getPathLinks(intermediateTemplates);

		RootNode rootPath = toJoltPath();

		JoltFormat resolvedFormat = getResolvedFormat(rootPath, formatLinks, pathLinks);

		rootPath.expandLinks(pathLinks);
		rootPath.conditionalize();

		Table table = rootPath.toTable(resolvedFormat);
		return table;
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
				Map<String, Object> resource = (Map<String, Object>) transform.get("spec");
				result.resourceType = (String) resource.get("resourceType");
				continue;
			}
			if (operation.endsWith("AdditionalModifier")) {
				result.format = JoltFormat.getInstance((Map<String, Object>) transform.get("spec"));
				continue;
			}
		}

		return result;
	}
}
