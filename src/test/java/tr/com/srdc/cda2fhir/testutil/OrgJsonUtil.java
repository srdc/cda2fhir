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

	public JSONObject getJSONObject() {
		return root;
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
		return section.optJSONArray("entry");
	}

	public JSONArray getProblemSectionEntries() throws JSONException {
		JSONObject section = getSection("11450-4");
		return section.optJSONArray("entry");
	}

	public static OrgJsonUtil readXML(String filepath) throws JSONException, IOException {
		File file = new File(filepath);
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		JSONObject root = XML.toJSONObject(content);
		convertNamedObjectToArray(root, "templateId");
		convertNamedObjectToArray(root, "entryRelationship");
		convertNamedObjectToArray(root, "entry");
		convertNamedObjectToArray(root, "translation");
		renameProperty(root, "participantrole", "participantRole"); // due to an apparent bug in CDAUtil
		renameProperty(root, "manufacturedproduct", "manufacturedProduct"); // due to an apparent bug in CDAUtil
		renameProperty(root, "substanceadministration", "substanceAdministration"); // due to an apparent bug in CDAUtil
		renameProperty(root, "patientrole", "patientRole");
		renameProperty(root, "patientrole", "patientRole");
		renameProperty(root, "clinicaldocument", "clinicalDocument");
		renameProperty(root, "assignedentity", "assignedEntity");
		return new OrgJsonUtil(root);
	}

	private static JSONObject get(JSONObject object, String[] paths) throws JSONException {
		JSONObject result = object;
		for (String path : paths) {
			result = result.getJSONObject(path);
		}
		return result;
	}

	private static void convertNamedObjectToArray(JSONObject input, String key) throws JSONException {
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

	private static void convertNamedObjectToArray(JSONArray input, String key) throws JSONException {
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

	private static void renameProperty(JSONObject input, String key, String newKey) throws JSONException {
		JSONArray names = input.names();
		boolean found = false;
		for (int index = 0; index < names.length(); ++index) {
			String name = names.optString(index);
			if (key.equals(name)) {
				found = true;
			}
			JSONArray asArray = input.optJSONArray(name);
			if (asArray != null) {
				renameProperty(asArray, key, newKey);
				continue;
			}
			JSONObject asObject = input.optJSONObject(name);
			if (asObject != null) {
				renameProperty(asObject, key, newKey);
				continue;
			}
		}
		if (found) {
			Object object = input.get(key);
			input.put(newKey, object);
			input.remove(key);
		}
	}

	private static void renameProperty(JSONArray input, String key, String newKey) throws JSONException {
		int length = input.length();
		for (int index = 0; index < length; ++index) {
			JSONArray asArray = input.optJSONArray(index);
			if (asArray != null) {
				renameProperty(asArray, key, newKey);
				continue;
			}
			JSONObject asObject = input.optJSONObject(index);
			if (asObject != null) {
				renameProperty(asObject, key, newKey);
			}
		}
	}
}
