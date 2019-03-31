package tr.com.srdc.cda2fhir.jolt.report.impl;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public abstract class Condition implements ICondition {
	protected String path;

	public Condition(String path) {
		this.path = path;
	}

	@Override
	abstract public Condition clone();

	@Override
	public void prependPath(String path) {
		if (this.path == null || this.path.length() == 0) {
			this.path = path;
		} else {
			this.path = path + '.' + this.path;
		}
	}
}
