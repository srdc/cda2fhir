package tr.com.srdc.cda2fhir.jolt.report;

public class JoltCondition {
	public String path;
	public String relation;
	public String value;

	public JoltCondition(String path, String relation) {
		this.path = path;
		this.relation = relation;		
	}

	@Override
	public String toString() {
		String result = "* " + path + " " + relation;
		if (value != null) {
			result += value;
		}
		return result;
	}
}
