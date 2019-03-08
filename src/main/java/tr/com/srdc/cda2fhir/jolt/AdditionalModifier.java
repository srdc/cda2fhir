package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Modifier;
import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;

import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;

@SuppressWarnings("deprecation")
public class AdditionalModifier implements SpecDriven, ContextualTransform {
	public static final class DatetimeAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (!(arg instanceof String)) {
				return Optional.empty();
			}
			String datetimeWithZone = (String) arg;
			if (datetimeWithZone.length() < 4) {
				return Optional.empty();
			}
			String[] pieces = datetimeWithZone.split("-");
			String datetime = pieces[0];
			int length = datetime.length();
			String result = datetime.substring(0, 4);
			if (length > 5) {
				result += "-" + datetime.substring(4, 6);
				if (length > 7) {
					result += "-" + datetime.substring(6, 8);					
					if (length > 11) {
						result += "T" + datetime.substring(8, 10) + ":" + datetime.substring(10, 12);
						if (length > 13) {
							result += ":" + datetime.substring(12, 14);
						} else {
							result += ":00";
						}
					}
				}			
			}
			String zone = pieces.length > 1 ? pieces[1] : null;
			if (zone != null && zone.length() > 0) {
				result += "-" + zone.substring(0, 2) + ":" + zone.substring(2, 4);
			}
			return Optional.of(result);
		}
	}

	public static final class ValueSetAdapter extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
            if(argList == null || argList.size() != 2 ) {
                return Optional.empty();
            }
            String filename = (String) argList.get(0);
			String value = argList.get(1).toString().toLowerCase();

			Map<String, Object> map = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/" + filename + ".json");
			String mappedValue = (String) map.get(value);
			if (mappedValue != null) {
				return Optional.of(mappedValue);
			}
			return Optional.empty();
		}
	}
	
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

	public static final class SystemAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (!(arg instanceof String)) {
				return Optional.empty();
			}
			String oid = (String) arg;
			ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
			String system = vst.tOid2Url(oid);
			return Optional.of(system);
		}
	}

	private static final Map<String, Function> AMIDA_FUNCTIONS = new HashMap<>();
	static {
		AMIDA_FUNCTIONS.put("defaultid", new DefaultId());
		AMIDA_FUNCTIONS.put("datetimeAdapter", new DatetimeAdapter());
		AMIDA_FUNCTIONS.put("valueSetAdapter", new ValueSetAdapter());
		AMIDA_FUNCTIONS.put("systemAdapter", new SystemAdapter());
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
