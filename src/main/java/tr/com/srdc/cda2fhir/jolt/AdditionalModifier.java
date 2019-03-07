package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.Modifier;
import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;

@SuppressWarnings("deprecation")
public class AdditionalModifier implements SpecDriven, ContextualTransform {
	public static final class DefaultId extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg instanceof String && ((String) arg).isEmpty()) {
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				result.put("root", "amida.id");
				result.put("extension", "135792468");
				return Optional.of(result);
			}
			return Optional.of(arg);
		}
	}

	private static final Map<String, Function> AMIDA_FUNCTIONS = new HashMap<>();
	static {
		AMIDA_FUNCTIONS.put("defaultid", new DefaultId());
	}

	private Modifier.Overwritr modifier;
	
    @Override
    public Object transform(final Object input, final Map<String, Object> context) {
		return modifier.transform(input, context);
	}

	public AdditionalModifier(final Object spec) {
		modifier = new Modifier.Overwritr(spec, AMIDA_FUNCTIONS);
	}
}
