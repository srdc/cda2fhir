package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	public static Object transformEntryInFile(String cdaName, String filepath) {
		String specpath = String.format("src/test/resources/jolt/entry/%s.json", cdaName);		
		List<Object> chainrSpec = JsonUtils.filepathToList(specpath);
        Chainr chainr = Chainr.fromSpec(chainrSpec);
        Object input = JsonUtils.filepathToObject(filepath);
        Map<String, Object> context = getInitialContext();
        Object output = chainr.transform(input, context);		
        return output;
	}
}
