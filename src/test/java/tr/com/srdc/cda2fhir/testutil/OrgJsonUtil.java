package tr.com.srdc.cda2fhir.testutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrgJsonUtil {
    public static void convertNamedObjectToArray(JSONObject input, String key) throws JSONException {
    	JSONArray names = input.names();
    	for (int index = 0; index < names.length(); ++index) {
    		String name = names.optString(index);
    		if (!key.equals(name)) {
        		JSONArray asArray = input.optJSONArray(name);
        		if (asArray != null) {
        			convertNamedObjectToArray(asArray, key);
        			continue;
        		}
        		JSONObject asObject = input.optJSONObject(name);
        		if (asObject != null) {
        			convertNamedObjectToArray(asObject, key);
        			continue;
        		}
    		}
    		JSONArray asArray = input.optJSONArray(name);
    		if (asArray != null) {
    			convertNamedObjectToArray(asArray, key);
    			continue;
    		}
    		JSONObject asObject = input.optJSONObject(name);
    		if (asObject != null) {
    			convertNamedObjectToArray(asObject, key);
    			JSONArray replacement = new JSONArray();
    			replacement.put(asObject);
    			input.remove(name);
    			input.put(name, replacement);
    		}
    	}
    }

    public static void convertNamedObjectToArray(JSONArray input, String key) throws JSONException {
    	int length = input.length();
    	for (int index = 0; index < length; ++index) {
    		JSONArray asArray = input.optJSONArray(index);
    		if (asArray != null) {
    			convertNamedObjectToArray(asArray, key);
    			continue;
    		}
    		JSONObject asObject = input.optJSONObject(index);
    		if (asObject != null) {
    			convertNamedObjectToArray(asObject, key);
    		}
    	}
    }
}
