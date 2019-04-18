package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.Collection;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public class MultiAndCondition extends MultiCondition {
	public MultiAndCondition() {
		super();
	}

	public MultiAndCondition(Collection<ICondition> conditions) {
		super(conditions);
	}

	@Override
	public MultiAndCondition clone() {
		MultiAndCondition result = new MultiAndCondition();
		copyConditions(result);
		return result;
	}

	@Override
	public String toString() {
		return toString(" and ");
	}

	@Override
	public boolean equals(Object rhs) {
		if (!(rhs instanceof MultiAndCondition)) {
			return false;
		}
		return commonEquals((MultiAndCondition) rhs);
	}
}
