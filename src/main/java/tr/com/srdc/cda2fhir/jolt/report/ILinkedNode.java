package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

public interface ILinkedNode {
	String getLink();

	String getTarget();

	void expandLinks(JoltTemplate ownerTemplate, Map<String, JoltTemplate> templateMap);
}
