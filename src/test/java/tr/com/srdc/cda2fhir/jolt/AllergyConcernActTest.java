package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import com.bazaarvoice.jolt.JsonUtils;

public class AllergyConcernActTest {
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
    	
    	String outputFile = "src/test/resources/output/" + "C-CDA_R2-1_CCD allergy entry - jolt.json";
    	FileUtils.writeStringToFile(new File(outputFile), entry.toString(4), Charset.defaultCharset());    	
    	
    	Object transformedOutput = TransformManager.transformEntryInFile("AllergyConcernAct", outputFile);
    	
        String prettyJson = JsonUtils.toPrettyJsonString(transformedOutput);
    	String resultFile = "src/test/resources/output/jolt/" + "C-CDA_R2-1_CCD allergy entry result - jolt.json";
        FileUtils.writeStringToFile(new File(resultFile), prettyJson, Charset.defaultCharset());
    }
}
