package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NotNullCondition extends Condition {
	public NotNullCondition(String path) {
		super(path);
	}

	@Override
	public NotNullCondition clone() {
		return new NotNullCondition(path);
	}

	@Override
	public NullCondition not() {
		return new NullCondition(path);
	}

	@Override
	public String toString() {
		String result = path + " " + "isnotnull";
		return result;
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + "isnotnull";
		return result;
	}

}
