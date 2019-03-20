package tr.com.srdc.cda2fhir.jolt.report;

public class JoltCondition {
	private String path;
	private String relation;
	private String value;

	public JoltCondition(String path, String relation) {
		this.path = path;
		this.relation = relation;
	}

	public JoltCondition(String path, String relation, String value) {
		this.path = path;
		this.relation = relation;
		this.value = value;
	}

	@Override
	public JoltCondition clone() {
		return new JoltCondition(path, relation, value);
	}

	public void prependPath(String path) {
		if (this.path == null || this.path.length() == 0) {
			this.path = path;
		} else {
			this.path = path + '.' + this.path;
		}
	}

	public JoltCondition not() {
		if (relation.equals("isnotnull")) {
			return new JoltCondition(path, "isnull");
		}
		if (relation.equals("isnull")) {
			return new JoltCondition(path, "isnotnull");
		}
		if (relation.equals("equal")) {
			return new JoltCondition(path, "notequal", value);
		}
		if (relation.equals("notequal")) {
			return new JoltCondition(path, "equal", value);
		}
		return new JoltCondition(path, "not" + relation, value);
	}

	@Override
	public String toString() {
		String result = path + " " + relation;
		if (value != null) {
			result += " " + value;
		}
		return result;
	}

	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + relation;
		if (value != null) {
			result += " " + value;
		}
		return result;
	}
}
