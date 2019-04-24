package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Set;

public interface INode {
	void addCondition(ICondition condition);

	Set<ICondition> getConditions();

	void addConditions(Set<ICondition> conditions);

	void copyConditions(INode source);

	void copyConditionsOred(INode source);

	void copyConditionsNot(INode source);

	ICondition notCondition();

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

	void promoteTargets(String target, boolean isDistributed);

	void fillNodes(List<INode> result, PathPredicate pathPredicate);

	List<INode> findNodes(PathPredicate pathPredicate);

	int originalNodeCount();

	boolean hasSibling();
}
