package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.JoltFormat;
import tr.com.srdc.cda2fhir.jolt.report.Table;

public class EntryNode extends RootNode {
	private String resourceType;
	
	public EntryNode(String resourceType) {
		super();
		this.resourceType = resourceType;
	}

	@Override
	public Table toTable(JoltFormat resolvedFormat) {
		Table result = super.toTable(resolvedFormat);
		result.promoteTargets(resourceType);
		return result;
	}
}
