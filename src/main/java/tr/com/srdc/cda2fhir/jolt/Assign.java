package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.SpecDriven;

public class Assign implements ContextualTransform, SpecDriven {
	private Shiftr shiftr;

	@Inject
	public Assign(Object spec) {
		shiftr = new Shiftr(spec);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Object output = shiftr.transform(input);
		if (output == null) {
			return input;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> outputAsMap = (Map<String, Object>) output;
		outputAsMap.putAll(inputAsMap);
		return outputAsMap;
	}
}
