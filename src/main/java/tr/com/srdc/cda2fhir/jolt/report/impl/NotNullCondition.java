package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NotNullCondition extends Condition {
	public NotNullCondition(String path) {
		super(path);
	}

	@Override
	public NotNullCondition clone() {
		return new NotNullCondition(getPath());
	}

	@Override
	public NullCondition not() {
		return new NullCondition(getPath());
	}

	@Override
	public String toString() {
		return getPath() + " " + "isnotnull";
	}
}
