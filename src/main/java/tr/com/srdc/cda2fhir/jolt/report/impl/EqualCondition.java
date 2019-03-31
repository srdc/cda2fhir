package tr.com.srdc.cda2fhir.jolt.report.impl;

public class EqualCondition extends Condition {
	private String value;

	public EqualCondition(String path, String value) {
		super(path);
		this.value = value;
	}

	@Override
	public EqualCondition clone() {
		return new EqualCondition(path, value);
	}

	@Override
	public Condition not() {
		return new NotEqualCondition(path, value);
	}

	@Override
	public String toString() {
		String result = path + " " + "equal";
		if (value != null) {
			result += " " + value;
		}
		return result;
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + "equal";
		if (value != null) {
			result += " " + value;
		}
		return result;
	}
}
