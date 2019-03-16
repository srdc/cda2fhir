package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.JoltPath;

public class ConditionNode extends JoltPath {
	public ConditionNode(String path) {
		super(path);
	}

	public ConditionNode(String path, String target, String link) {
		super(path, target, link);
	}
}
