package tr.com.srdc.cda2fhir.jolt.report.impl;

public class NullCondition extends Condition {
	public NullCondition(String path) {
		super(path);
	}

	@Override
	public Condition clone() {
		return new NullCondition(path);
	}

	@Override
	public NotNullCondition not() {
		return new NotNullCondition(path);
	}

	@Override
	public String toString() {
		String result = path + " " + "isnull";
		return result;
	}

	@Override
	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + "isnull";
		return result;
	}

}
