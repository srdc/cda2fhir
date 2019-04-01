package tr.com.srdc.cda2fhir.jolt.report.impl;

public class EqualCondition extends Condition {
	private String value;

	public EqualCondition(String path, String value) {
		super(path);
		this.value = value;
	}

	@Override
	public EqualCondition clone() {
		return new EqualCondition(getPath(), value);
	}

	@Override
	public Condition not() {
		return new NotEqualCondition(getPath(), value);
	}

	@Override
	public String toString() {
		String path = getPath();
		return path + " equal " + value;
	}
}
