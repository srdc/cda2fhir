package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class JoltTemplate {
	public List<JSONObject> shifts = new ArrayList<JSONObject>();
	public JSONObject cardinality;
	public JSONObject format;
	
	public boolean topTemplate = false;
	public boolean leafTemplate = false;
	
	
	public Table createTable(Map<String, JoltTemplate> map) {
		Table result = new Table();
		return result;
	}
}
