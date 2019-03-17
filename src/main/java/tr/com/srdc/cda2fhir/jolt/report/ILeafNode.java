package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public interface ILeafNode extends INode {
	String getLink();
	
	String getTarget();

	void expandLinks(Map<String, RootNode> linkMap);
}
