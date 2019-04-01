package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

public class Identity implements ContextualTransform {
	@Override
	public Object transform(Object input, Map<String, Object> context) {
		return input;
	}
}
