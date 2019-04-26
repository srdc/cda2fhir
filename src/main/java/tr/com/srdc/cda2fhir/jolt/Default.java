package tr.com.srdc.cda2fhir.jolt;

import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Defaultr;
import com.bazaarvoice.jolt.SpecDriven;

public class Default implements ContextualTransform, SpecDriven {
	/**
	 * Default is a simple wrapper around Jolt stock transform Defaultr to not allow
	 * defaults for null input.
	 **/

	private Defaultr defaultr;

	@Inject
	public Default(Object spec) {
		defaultr = new Defaultr(spec);
	}

	@Override
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		return defaultr.transform(input);
	}
}
