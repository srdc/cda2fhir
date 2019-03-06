package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.SpecDriven;

public class Substitute implements ContextualTransform, SpecDriven {
    private static Chainr generateChainr(String filepath) {
        List<Object> spec = JsonUtils.filepathToList("src/test/resources/jolt/" + filepath);
        Chainr chainr = Chainr.fromSpec(spec);
        return chainr;
    }
    
    static Map<String, Chainr> templates = new HashMap<String, Chainr>();
    static {
    	templates.put("->ID", generateChainr("data-type/ID.json"));
    	templates.put("->AuthorParticipation", generateChainr("entry/AuthorParticipation.json"));
    }
    
	@Inject
    public Substitute( Object spec ) {}

	private Object findTemplateValue(Map<String, Object> map) {
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
        Object replacement = chainr.transform(input);
        return replacement;
	}
	
	@SuppressWarnings("unchecked")
	private void substitute(Map<String, Object> object) {
		for (Map.Entry<String, Object> entry: object.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof List) {
				List<Object> elements = (List<Object>) value;
				substitute(elements);
				continue;
			}
			if (value instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) value;
				Object replacement = findTemplateValue(map);
				if (replacement == null) {
					substitute(map);
					continue;
				}
				entry.setValue(replacement);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void substitute(List<Object> list) {
		int size = list.size();
		for (int index = 0; index < size; ++index) {
			Object element = list.get(index);
			if (element instanceof List) {
				substitute((List<Object>) element);
				continue;
			}
			if (element instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) element;
				Object replacement = findTemplateValue(map);
				if (replacement == null) {
					substitute(map);
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
		substitute(map);
		return map;
	}
}
