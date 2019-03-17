package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public interface INode {
	void addCondition(JoltCondition condition);
	
	List<JoltCondition> getConditions();
	
	void addConditions(List<JoltCondition> conditions);

	
	void fillLinks(List<ILeafNode> result);

	List<ILeafNode> getLinks();


	void fillConditionNodes(List<IConditionNode> result);

	List<IConditionNode> getConditionNodes();


	void expandLinks(Map<String, RootNode> linkMap);
	
	INode clone();

	List<TableRow> toTableRows();
	
	boolean isLeaf();
	
	
	String getPath();
	
	void promoteTargets(String target);
	
	void setPath(String path);
}
