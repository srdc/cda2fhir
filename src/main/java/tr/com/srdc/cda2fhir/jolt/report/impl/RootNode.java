package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;
import tr.com.srdc.cda2fhir.jolt.report.IConditionNode;
import tr.com.srdc.cda2fhir.jolt.report.ILeafNode;
import tr.com.srdc.cda2fhir.jolt.report.ILinkedNode;
import tr.com.srdc.cda2fhir.jolt.report.INode;
import tr.com.srdc.cda2fhir.jolt.report.IParentNode;
import tr.com.srdc.cda2fhir.jolt.report.IWildcardNode;
import tr.com.srdc.cda2fhir.jolt.report.JoltTemplate;
import tr.com.srdc.cda2fhir.jolt.report.Table;
import tr.com.srdc.cda2fhir.jolt.report.TableRow;
import tr.com.srdc.cda2fhir.jolt.report.Templates;

public class RootNode {
	private ParentNode root;
	private ParentNode base;

	public RootNode() {
		root = new ParentNode(null, "root");
		base = new ParentNode(root, "base");
		root.addChild(base);
	}

	public void addChild(ParentNode node) {
		base.addChild(node);
	}

	public List<INode> getChildren() {
		return null;
	}

	public ParentNode getBase() {
		return base;
	}

	public void addCondition(Condition condition) {
		root.children.get(0).addCondition(condition);
	}

	public List<Condition> getConditions() {
		return null;
	}

	public List<ILinkedNode> getLinkedNodes() {
		return root.getLinkedNodes();
	}

	public void expandLinks(Map<String, JoltTemplate> templateMap) {
		List<ILinkedNode> linkedNodes = root.getLinkedNodes();
		linkedNodes.forEach(linkedNode -> linkedNode.expandLinks(templateMap));
	}

	public void eliminateWildcardNodes() {
		List<IWildcardNode> wildcardNodes = root.getWildcardNodes();
		wildcardNodes.forEach(wildcardNode -> wildcardNode.mergeToParent());
	}

	public void conditionalize() {
		List<IConditionNode> conditionNodes = root.getConditionNodes();
		conditionNodes.forEach(conditionNode -> conditionNode.mergeToParent());
	}

	public Table toTable(Templates templates) {
		Table result = new Table();
		root.children.forEach(child -> {
			((IParentNode) child).getChildren().forEach(grandChild -> {
				List<TableRow> grandChildRows = grandChild.toTableRows(templates);
				grandChildRows.forEach(row -> {
					child.getConditions().forEach(condition -> {
						String conditionAsString = condition.toString();
						row.addCondition(conditionAsString);
					});
				});
				result.addRows(grandChildRows);
			});
		});
		return result;
	}

	public List<INode> getAsLinkReplacement(LinkedNode linkedNode) {
		IParentNode parent = linkedNode.getParent();
		String path = linkedNode.getPath();
		String target = linkedNode.getTarget();
		List<INode> result = new ArrayList<INode>();
		root.children.forEach(base -> {
			INode node = base.clone(parent);
			if (target.length() > 0) {
				node.promoteTargets(target);
			}
			node.setPath(path);
			result.add(node);
		});
		return result;
	}

	private void updateBase(Consumer<IParentNode> consumer) {
		List<IParentNode> children = root.children.stream().map(c -> (IParentNode) c).collect(Collectors.toList());
		children.forEach(base -> {
			consumer.accept(base);
		});
	}

	private static final class RemoveWhenResolution {
		public String target;
		public String path;

		RemoveWhenResolution(String target, String path) {
			this.target = target;
			this.path = path;
		}
	}

	@SuppressWarnings("unchecked")
	private List<RemoveWhenResolution> resolveRemoveWhen(Object updateInfo, String parentPath) {
		Map<String, Object> updateInfoAsMap = (Map<String, Object>) updateInfo;
		return resolveRemoveWhen(updateInfoAsMap, parentPath);
	}

	@SuppressWarnings("unchecked")
	private List<RemoveWhenResolution> resolveRemoveWhen(Map<String, Object> updateInfo, String parentPath) {
		List<RemoveWhenResolution> result = new ArrayList<>();
		for (Map.Entry<String, Object> entry : updateInfo.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			String path = parentPath.isEmpty() ? key : parentPath + "." + key;
			if (value instanceof String) {
				result.add(new RemoveWhenResolution((String) value, path));
				continue;
			}
			if (value instanceof List) {
				List<Object> valueAsList = (List<Object>) value;
				for (Object valueElement : valueAsList) {
					if (valueElement instanceof String) {
						result.add(new RemoveWhenResolution((String) valueElement, path));
						continue;
					}
					List<RemoveWhenResolution> elementResult = resolveRemoveWhen(valueElement, path);
					result.addAll(elementResult);
				}
				continue;
			}
			List<RemoveWhenResolution> elementResult = resolveRemoveWhen(value, path);
			result.addAll(elementResult);
		}
		return result;
	}

	public void updateFromRemoveWhen(Map<String, Object> updateInfo) {
		List<RemoveWhenResolution> rwrs = resolveRemoveWhen(updateInfo, "");
		final Map<String, ICondition> alreadySeen = new HashMap<>();
		rwrs.forEach(rwr -> {
			final String target = rwr.target;
			final String path = rwr.path;
			if ("*".equals(target)) {
				updateBase(base -> {
					Condition condition = new NullCondition(path);
					base.addCondition(condition);
				});
				return;
			}
			updateBase(base -> {
				List<IParentNode> newBases = base.separateChildLines(target);
				newBases.forEach(newBase -> {
					ICondition condition = new NullCondition(path);
					String rootPath = path.split("\\.")[0];
					ICondition prevCondition = alreadySeen.get(rootPath);
					if (prevCondition != null) {
						condition = new OrCondition(prevCondition.not(), condition);
					}
					newBase.addCondition(condition);
					alreadySeen.put(target, condition);
					if (base != newBase) {
						root.addChild(newBase);
					}
				});
			});
		});
	}

	public void distributeArrays(Set<String> topPaths) {
		List<ILinkedNode> linkedNodes = root.getLinkedNodes();
		linkedNodes.forEach(linkedNode -> {
			String target = linkedNode.getTarget();
			String[] targetArrayPieces = target.split("\\[");
			if (targetArrayPieces.length < 2) {
				return;
			}
			String targetArrayName = targetArrayPieces[0];
			if (!topPaths.contains(targetArrayName)) {
				return;
			}
			String[] targetPieces = target.split("\\.");
			if (targetPieces.length != 2) {
				return;
			}
			String newTarget = targetPieces[1] + "[]";
			ILeafNode nodeAsLeaf = (ILeafNode) linkedNode;
			nodeAsLeaf.setTarget(newTarget);
		});
	}
}
