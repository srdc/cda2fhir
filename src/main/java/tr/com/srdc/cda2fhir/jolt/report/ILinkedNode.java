package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

import tr.com.srdc.cda2fhir.jolt.report.impl.RootNode;

public interface ILinkedNode {
	String getLink();
	
	String getTarget();

	void expandLinks(Map<String, RootNode> linkMap);
}
