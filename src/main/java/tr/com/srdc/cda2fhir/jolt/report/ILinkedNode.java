package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

public interface ILinkedNode {
	String getLink();

	String getTarget();

	void addAlias(Map<String, String> alias);

	void expandLinks(JoltTemplate ownerTemplate, Map<String, JoltTemplate> templateMap);
}
