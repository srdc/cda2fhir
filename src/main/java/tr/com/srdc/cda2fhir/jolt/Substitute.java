package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JsonUtils;

public class Substitute implements ContextualTransform {
    private static Chainr generateChainr(String filepath) {
        List<Object> spec = JsonUtils.filepathToList("src/test/resources/jolt/" + filepath);
        Chainr chainr = Chainr.fromSpec(spec);
        return chainr;
    }
    
    static Map<String, Chainr> templates = new HashMap<String, Chainr>();
    static {
    	templates.put("->ID", generateChainr("data-type/ID.json"));
    	templates.put("->CD", generateChainr("data-type/CD.json"));
    	templates.put("->AuthorParticipation", generateChainr("entry/AuthorParticipation.json"));
    	templates.put("->AllergyIntoleranceObservation", generateChainr("entry/AllergyIntoleranceObservation.json"));
    	templates.put("->ReactionObservation", generateChainr("entry/ReactionObservation.json"));
    	//templates.put("->EffectiveTimeLowOrValue", generateChainr("data-type/EffectiveTimeLowOrValue.json"));
    }
    
	private Object findTemplateValue(Map<String, Object> map, Map<String, Object> context) {
		Set<String> keys = map.keySet();
		if (keys.size() != 1) {
			return null;
		}
		String key = keys.stream().findFirst().get();
		Chainr chainr = templates.get(key);
	    if (chainr == null) {
			return null;
		}
		Object input = map.get(key);
        Object replacement = chainr.transform(input, context);
        return replacement;
	}
	
	@SuppressWarnings("unchecked")
	private void substitute(Map<String, Object> object, Map<String, Object> context) {
		if (object == null) {
			return;
		}
		
		List<String> topSubstitutes = new ArrayList<String>();			
		for (Map.Entry<String, Object> entry: object.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Chainr chainr = templates.get(key);
		    if (chainr != null) {
		    	topSubstitutes.add(key);
			}			
			if (value instanceof List) {
				List<Object> elements = (List<Object>) value;
				substitute(elements, context);
				continue;
			}
			if (value instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) value;
				Object replacement = findTemplateValue(map, context);
				if (replacement == null) {
					substitute(map, context);
					continue;
				}
				entry.setValue(replacement);
			}
		}

		for (String key: topSubstitutes) {
			Object value = object.get(key);
			Chainr chainr = templates.get(key);
			Map<String, Object> additionalKeys = (Map<String, Object>) chainr.transform(value, context);
			if (additionalKeys != null) {
				object.putAll(additionalKeys);
			}
			object.remove(key);			
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void substitute(List<Object> list, Map<String, Object> context) {
		int size = list.size();
		for (int index = 0; index < size; ++index) {
			Object element = list.get(index);
			if (element instanceof List) {
				substitute((List<Object>) element, context);
				continue;
			}
			if (element instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) element;
				Object replacement = findTemplateValue(map, context);
				if (replacement == null) {
					substitute(map, context);
					continue;
				}
				list.set(index, replacement);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		Map<String, Object> map = (Map<String, Object>) input;
		substitute(map, context);
		return map;
	}
}
