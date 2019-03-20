package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

public interface ILinkedNode {
	String getLink();

	String getTarget();

	void expandLinks(Map<String, JoltTemplate> templateMap);
}
