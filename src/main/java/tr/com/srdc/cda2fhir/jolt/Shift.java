package tr.com.srdc.cda2fhir.jolt;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.SpecDriven;

public class Shift implements ContextualTransform, SpecDriven {
	/**
	 * Shift is a simple wrapper around Jolt stock transform Shiftr to not return
	 * null when input is not null. For these cases empty is returned.
	 **/

	private Shiftr shiftr;

	@Inject
	public Shift(Object spec) {
		shiftr = new Shiftr(spec);
	}

	@Override
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Object output = shiftr.transform(input);
		if (output == null) {
			return new LinkedHashMap<String, Object>();
		}
		return output;
	}
}
