package tr.com.srdc.cda2fhir;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class AllergiesSectionJoltTest {
    @BeforeClass
    public static void init() {
        CDAUtil.loadPackages();
    }
    
    private static JSONObject getSection(JSONArray component, String code) throws JSONException {
    	for (int idx = 0; idx < component.length(); ++idx) {
    		JSONObject section = component.getJSONObject(idx).getJSONObject("section");
    		String sectionCode = section.getJSONObject("code").getString("code");
    		if (code.equals(sectionCode)) {
    			return section;
    		}
    	}
    	return null;
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

    @Test
    public void testSample1() throws Exception {
    	String sourceName = "C-CDA_R2-1_CCD.xml";
    	File file = new File("src/test/resources/" + sourceName);
    	String content = FileUtils.readFileToString(file, Charset.defaultCharset());    	
    	JSONArray component = XML.toJSONObject(content)
							.getJSONObject("ClinicalDocument")
							.getJSONObject("component")
							.getJSONObject("structuredBody")
    						.getJSONArray("component");
    	JSONObject allergiesSection = getSection(component, "48765-2");
    	JSONObject entry = allergiesSection.getJSONArray("entry").getJSONObject(0);
    	//convertNamedObjectToArray(entry, "entryRelationship");
    	
    	String outputFile = "src/test/resources/output/" + "C-CDA_R2-1_CCD allergy entry - jolt.json";
    	FileUtils.writeStringToFile(new File(outputFile), entry.toString(4), Charset.defaultCharset());    	
    	
        List<Object> chainrSpecJSON = JsonUtils.filepathToList("src/test/resources/jolt/entry/AllergyConcernAct.json");
        
        Chainr chainr = Chainr.fromSpec( chainrSpecJSON );
        Object inputJSON = JsonUtils.filepathToObject(outputFile);
        Object transformedOutput = chainr.transform(inputJSON);
        String prettyJson = JsonUtils.toPrettyJsonString(transformedOutput);
    	String resultFile = "src/test/resources/output/jolt/" + "C-CDA_R2-1_CCD allergy entry result - jolt.json";
        FileUtils.writeStringToFile(new File(resultFile), prettyJson, Charset.defaultCharset());
    }
}
