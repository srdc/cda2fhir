package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class OrgJsonUtil {
	final private static String[] SECTION_PATH = { "ClinicalDocument", "component", "structuredBody" };

	private JSONObject root;

	public OrgJsonUtil(JSONObject root) {
		this.root = root;
	}

	public JSONArray getSections() throws JSONException {
		JSONArray component = get(this.root, SECTION_PATH).getJSONArray("component");
		JSONArray result = new JSONArray();
		for (int idx = 0; idx < component.length(); ++idx) {
			JSONObject section = component.getJSONObject(idx).getJSONObject("section");
			result.put(section);
		}
		return result;
	}
	
	public JSONObject getSection(String code) throws JSONException {
		JSONArray sections = getSections();
		for (int idx = 0; idx < sections.length(); ++idx) {
			JSONObject section = sections.getJSONObject(idx);
			String sectionCode = section.getJSONObject("code").getString("code");
			if (code.equals(sectionCode)) {
				return section;
			}
		}
		return null;
	}

	public JSONArray getAllergiesSectionEntries() throws JSONException {
		JSONObject section = getSection("48765-2");
		return section.getJSONArray("entry");		
	}
	
	public static OrgJsonUtil readXML(String filepath) throws JSONException, IOException {
		File file = new File(filepath);
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		JSONObject root = XML.toJSONObject(content);
		convertNamedObjectToArray(root, "templateId");
		convertNamedObjectToArray(root, "entryRelationship");
		convertNamedObjectToArray(root, "entry");
		convertNamedObjectToArray(root, "translation");
		return new OrgJsonUtil(root);
	}

	public static JSONObject get(JSONObject object, String[] paths) throws JSONException {
		JSONObject result = object;
		for (String path: paths) {
			result = result.getJSONObject(path);
		}
		return result;
	}
	
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
