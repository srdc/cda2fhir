package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;

public interface INode {
	void addCondition(JoltCondition condition);
	
	List<JoltCondition> getConditions();
	
	void addConditions(List<JoltCondition> conditions);

	
	IParentNode getParent();

	void setParent(IParentNode parent);
	
	
	void fillLinkedNodes(List<ILeafNode> result);

	List<ILeafNode> getLinkedNodes();


	void fillConditionNodes(List<IConditionNode> result);

	List<IConditionNode> getConditionNodes();


	INode clone(IParentNode parent);


	List<TableRow> toTableRows();
	
	boolean isLeaf();
	
	
	String getPath();
	
	void promoteTargets(String target);
	
	void setPath(String path);
}
