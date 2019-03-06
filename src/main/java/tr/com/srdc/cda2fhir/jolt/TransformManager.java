package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class TransformManager {
	private static Map<String, Object> getInitialContext() {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("Resources", new ArrayList<Object>());
		return context;
	}

	private static List<Object> getTopEntryTemplate() {
        Map<String, Object> template = new LinkedHashMap<String, Object>();
        template.put("operation", "tr.com.srdc.cda2fhir.jolt.EntryToResource");
        List<Object> chainrSpec = new ArrayList<Object>();
		chainrSpec.add(template);
		return chainrSpec;
	}
	
	public static Object transformEntryInFile(String cdaName, String filepath) {
		//String specpath = String.format("src/test/resources/jolt/entry/%s.json", cdaName);		
		//List<Object> chainrSpec = JsonUtils.filepathToList(specpath);
		
		List<Object> chainrSpec = getTopEntryTemplate();
        Chainr chainr = Chainr.fromSpec(chainrSpec);
        Object input = JsonUtils.filepathToObject(filepath);
        Map<String, Object> extendedInput = new LinkedHashMap<String, Object>();
        extendedInput.put("--" + cdaName, input);
 
        Map<String, Object> context = getInitialContext();
        chainr.transform(extendedInput, context);		
        Object result = context.get("Resources");
        return result;
	}
}
