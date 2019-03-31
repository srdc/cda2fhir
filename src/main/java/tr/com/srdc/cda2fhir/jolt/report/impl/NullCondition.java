package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NullCondition extends Condition {
	public NullCondition(String path) {
		super(path);
	}

	@Override
	public Condition clone() {
		return new NullCondition(getPath());
	}

	@Override
	public NotNullCondition not() {
		return new NotNullCondition(getPath());
	}

	@Override
	public String toString() {
		return getPath() + " " + "isnull";
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = getConditionPath(ownerPath);
		return conditionPath + " " + "isnull";
	}

}
