package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public class JoltTemplate {
	private static final class RawTemplate {
		public Map<String, Object> shift;
		public Map<String, Object> assign;
		public Map<String, Object> accumulator;
		public Map<String, Object> modifier;

		@SuppressWarnings("unchecked")
		public static RawTemplate getInstance(List<Object> content) {
			RawTemplate result = new RawTemplate();

			content.forEach(rawTransform -> {
				Map<String, Object> transform = (Map<String, Object>) rawTransform;
				
				String operation = (String) transform.get("operation");
				Map<String, Object> spec = (Map<String, Object>) transform.get("spec");
				
				if (operation.equals("shift")) {
					if (result.shift == null) result.shift = spec;
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
					result.modifier = spec;
					return;
				}						
			});

			return result;
		}		
	}
	
	private String name;
	public RootNode rootNode;
	public JoltFormat format;

	public boolean leafTemplate = false;
	private String resourceType;

	private Table assignTable;
	
	private JoltTemplate(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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

	private static Map<String, RootNode> getPathLinks(Map<String, JoltTemplate> templates) {
		return templates.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().rootNode));
	}

	private JoltFormat getResolvedFormat(Map<String, JoltTemplate> templateMap) {
		if (format == null) {
			return null;
		}
		JoltFormat result = format.clone();
		List<ILinkedNode> linkedNodes = rootNode.getLinks();
		linkedNodes.forEach(linkedNode -> {
			String link = linkedNode.getLink();
			String target = linkedNode.getTarget();

			JoltTemplate linkedTemplate = templateMap.get(link);
			if (linkedTemplate == null) return;
			
			JoltFormat resolvedLinkedFormat = linkedTemplate.getResolvedFormat(templateMap);
			if (resolvedLinkedFormat != null) {
				result.putAllAsPromoted(resolvedLinkedFormat, target);
			}
		});
		return result;
	}

	public Table createTable(Map<String, JoltTemplate> map) {
		Map<String, JoltTemplate> intermediateTemplates = getIntermediateTemplates(map);

		Map<String, RootNode> pathLinks = getPathLinks(intermediateTemplates);

		JoltFormat resolvedFormat = getResolvedFormat(map);

		rootNode.expandLinks(pathLinks);

		Templates templates = new Templates(resourceType, map, resolvedFormat);
		Table table = rootNode.toTable(templates);
		return table;
	}

	public static JoltTemplate getInstance(String name, List<Object> content) {
		JoltTemplate result = new JoltTemplate(name);

		result.leafTemplate = name.equals("ID") || name.contentEquals("CD");
				
		RawTemplate rawTemplate = RawTemplate.getInstance(content);
		
		if (rawTemplate.modifier != null) {
			result.format = JoltFormat.getInstance(rawTemplate.modifier);			
		}
		result.rootNode = NodeFactory.getInstance(rawTemplate.shift);
		if (rawTemplate.accumulator != null) {
			result.resourceType = (String) rawTemplate.accumulator.get("resourceType");
		}
		if (rawTemplate.assign != null) {
			RootNode assignRootNode = NodeFactory.getInstance(rawTemplate.assign);
			Templates templates = new Templates(result.format);
			result.assignTable = assignRootNode.toTable(templates);
		}

		return result;
	}
}
