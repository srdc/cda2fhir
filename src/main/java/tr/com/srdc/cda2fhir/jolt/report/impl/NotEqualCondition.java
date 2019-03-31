package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NotEqualCondition extends Condition {
	private String value;

	public NotEqualCondition(String path, String value) {
		super(path);
		this.value = value;
	}

	@Override
	public NotEqualCondition clone() {
		return new NotEqualCondition(getPath(), value);
	}

	@Override
	public Condition not() {
		return new EqualCondition(getPath(), value);
	}

	@Override
	public String toString() {
		return getPath() + " notequal " + value;
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = getConditionPath(ownerPath);
		return conditionPath + " notequal " + value;
	}

}
