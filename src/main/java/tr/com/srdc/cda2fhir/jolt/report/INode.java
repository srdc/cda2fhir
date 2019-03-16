package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public interface INode {
	void addChild(JoltPath node);
	
	void addCondition(JoltCondition condition);
	
	List<JoltPath> getChildren();
	
	List<JoltCondition> getConditions();
	
	List<JoltPath> getLinks();

	void expandLinks(Map<String, RootNode> linkMap);
	
	void conditionalize();
}
