package tr.com.srdc.cda2fhir.jolt.report.impl;

import org.apache.commons.lang3.NotImplementedException;

import tr.com.srdc.cda2fhir.jolt.report.ICondition;

public class OrCondition implements ICondition {
	private ICondition left;
	private ICondition right;

	public OrCondition(ICondition left, ICondition right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public OrCondition clone() {
		ICondition left = this.left.clone();
		ICondition right = this.right.clone();
		return new OrCondition(left, right);
	}

	@Override
	public void prependPath(String path) {
		this.left.prependPath(path);
		this.right.prependPath(path);
	}

	@Override
	public int compareTo(ICondition rhs) {
		return toString().compareTo(rhs.toString());
	}

	@Override
	public ICondition not() {
		throw new NotImplementedException("Not of or condition is not yet implemented.");
	}

	@Override
	public String toString() {
		return left.toString() + " or " + right.toString();
	}
}
