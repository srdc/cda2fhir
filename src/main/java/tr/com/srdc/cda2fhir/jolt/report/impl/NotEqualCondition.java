package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NotEqualCondition extends Condition {
	private String value;

	public NotEqualCondition(String path, String value) {
		super(path);
		this.value = value;
	}

	@Override
	public NotEqualCondition clone() {
		return new NotEqualCondition(path, value);
	}

	@Override
	public Condition not() {
		return new EqualCondition(path, value);
	}

	@Override
	public String toString() {
		String result = path + " " + "notequal";
		if (value != null) {
			result += " " + value;
		}
		return result;
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + "notequal";
		if (value != null) {
			result += " " + value;
		}
		return result;
	}

}
