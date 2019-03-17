package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public interface INode {
	void addCondition(JoltCondition condition);
	
	void addChild(INode node);
	
	void removeChild(INode node);
	
	List<INode> getChildren();
	
	List<JoltCondition> getConditions();
	
	void addConditions(List<JoltCondition> conditions);

	List<ILeafNode> getLinks();

	void expandLinks(Map<String, RootNode> linkMap);
	
	void conditionalize();

	INode clone();

	List<TableRow> toTableRows();
	
	boolean isLeaf();
	
	void fillLinks(List<ILeafNode> result);
	
	boolean isCondition();
	
	INode mergeToParent(INode node);

	String getPath();
	
	void promoteTargets(String target);
	
	void setPath(String path);
}
