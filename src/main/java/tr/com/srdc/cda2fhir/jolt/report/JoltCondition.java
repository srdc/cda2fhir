package tr.com.srdc.cda2fhir.jolt.report;

public class JoltCondition {
	public String path;
	public String relation;
	public String value;

	public JoltCondition(String path, String relation) {
		this.path = path;
		this.relation = relation;		
	}

	public JoltCondition(String path, String relation, String value) {
		this.path = path;
		this.relation = relation;
		this.value = value;
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
	
	public String toString(String ownerPath) {
		String conditionPath = path.length() == 0 ? ownerPath : ownerPath + "." + path;
		String result = conditionPath + " " + relation;
		if (value != null) {
			result += " " + value;
		}
		return result;
	}
}
