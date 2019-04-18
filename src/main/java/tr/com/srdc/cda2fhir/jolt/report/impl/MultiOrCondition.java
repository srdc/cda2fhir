package tr.com.srdc.cda2fhir.jolt.report.impl;

import java.util.Collection;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public class MultiOrCondition extends MultiCondition {
	public MultiOrCondition() {
		super();
	}

	public MultiOrCondition(Collection<ICondition> conditions) {
		super(conditions);
	}

	@Override
	public MultiOrCondition clone() {
		MultiOrCondition result = new MultiOrCondition();
		copyConditions(result);
		return result;
	}

	@Override
	public String toString() {
		return toString(" or ");
	}

	@Override
	public boolean equals(Object rhs) {
		if (!(rhs instanceof MultiOrCondition)) {
			return false;
		}
		return commonEquals((MultiOrCondition) rhs);
	}
}
