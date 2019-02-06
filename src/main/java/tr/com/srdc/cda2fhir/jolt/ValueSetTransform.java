package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.Transform;

public class ValueSetTransform implements Transform, SpecDriven {
    private Map<String, Object> map;
    private String property;
	
	@Inject
	@SuppressWarnings("unchecked")
    public ValueSetTransform( Object spec ) {
		Map<String, String> specCasted = (Map<String, String>) spec;
        String fileName = specCasted.get("name");
        this.map = JsonUtils.filepathToMap("src/test/resources/jolt/" + fileName);
        this.property = specCasted.get("path");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input) {
		LinkedHashMap<String, Object> casted = (LinkedHashMap<String, Object>) input;
		String key = (String) casted.get(this.property);
		String value = (String) this.map.get(key);
		casted.put(this.property, value);
		return casted;
	}
}
