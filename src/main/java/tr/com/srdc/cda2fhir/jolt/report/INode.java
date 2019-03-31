package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;

public interface INode {
	void addCondition(JoltCondition condition);

	List<JoltCondition> getConditions();

	void addConditions(List<JoltCondition> conditions);

	void copyConditions(INode source);

	IParentNode getParent();

	void setParent(IParentNode parent);

	void fillLinkedNodes(List<ILinkedNode> result);

	List<ILinkedNode> getLinkedNodes();

	void fillConditionNodes(List<IConditionNode> result);

	List<IConditionNode> getConditionNodes();

	void fillWildcardNodes(List<IWildcardNode> result);

	List<IWildcardNode> getWildcardNodes();

	INode clone(IParentNode parent);

	List<TableRow> toTableRows(Templates templates);

	String getPath();

	void setPath(String path);

	void promoteTargets(String target);

	void fillNodes(List<INode> result, PathPredicate pathPredicate);

	List<INode> findNodes(PathPredicate pathPredicate);

	int originalNodeCount();

	boolean hasSibling();
}
