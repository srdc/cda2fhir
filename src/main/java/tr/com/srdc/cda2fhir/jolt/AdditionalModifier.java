package tr.com.srdc.cda2fhir.jolt;

import java.util.Comparator;
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
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.StringUtil;

@SuppressWarnings("deprecation")
public class AdditionalModifier implements SpecDriven, ContextualTransform {
	private static Map<String, Object> temporaryContext; // Hack for now
		
	public static final class DatetimeAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			
			if (!(arg instanceof String || arg instanceof Integer)) {
				return Optional.empty();
			}
			String datetimeWithZone = arg.toString();
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

	public static final class IdSystemAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null || !(arg instanceof String)) {
				return Optional.empty();
			}
			String system = (String) arg;
			if (StringUtil.isOID(system)) {
				return Optional.of("urn:oid:" + system);
			} else if (StringUtil.isUUID(system)) {
				return Optional.of("urn:uuid:" + system);
			}						
			return Optional.of(system);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ReferenceAdapter extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
            if(argList == null || argList.size() != 2 ) {
                return Optional.empty();
            }
            String fhirType = (String) argList.get(0);
			Object arg = argList.get(1);
			if (!(arg instanceof Map)) {
				return Optional.empty();
			}
			Map<String, Object> identifier =(Map<String, Object>) arg;
			String system = (String) identifier.get("system");
			String value = (String) identifier.get("value");			
			IdentifierMap<String> map = (IdentifierMap<String>) temporaryContext.get("RefsByIdentifier");
			if (map == null) {
				return Optional.empty();
			}
			String reference = map.get(fhirType, system, value);
			if (reference == null) {
				return Optional.empty();
			}
			return Optional.of(reference);
		}
	}

    public static final class MaxDateTime extends Function.ListFunction {
    	@SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Optional applyList( final List<Object> argList ) {
        	if (argList == null || argList.size() < 1) {
        		return  Optional.empty();
        	}
        	java.util.Optional<String> result = argList.stream().map(dt -> (String) dt).max(Comparator.comparing(String::valueOf));
        	if (result.isPresent()) {
        		return Optional.of(result.get());
        	}
        	return Optional.empty();
        }
    }

	public static final class SelectOnNull extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
            if(argList == null || argList.size() != 3 ) {
                return Optional.empty();
            }
            String value = (String) argList.get(2);
            int index = value.isEmpty() ? 0 : 1;
            String result = (String) argList.get(index);
			return Optional.of(result);
		}
	}
    
	public static final class GetId extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return Optional.empty();
			}
			return Optional.of(arg);
		}
	}

	private static final Map<String, Function> AMIDA_FUNCTIONS = new HashMap<>();
	static {
		AMIDA_FUNCTIONS.put("defaultid", new DefaultId());
		AMIDA_FUNCTIONS.put("datetimeAdapter", new DatetimeAdapter());
		AMIDA_FUNCTIONS.put("referenceAdapter", new ReferenceAdapter());
		AMIDA_FUNCTIONS.put("valueSetAdapter", new ValueSetAdapter());
		AMIDA_FUNCTIONS.put("systemAdapter", new SystemAdapter());
		AMIDA_FUNCTIONS.put("idSystemAdapter", new IdSystemAdapter());
		AMIDA_FUNCTIONS.put("maxDateTime", new MaxDateTime());
		AMIDA_FUNCTIONS.put("selectOnNull", new SelectOnNull());
		AMIDA_FUNCTIONS.put("getId", new GetId());
	}

	private Modifier.Overwritr modifier;
	
    @Override
    public Object transform(final Object input, final Map<String, Object> context) {
    	temporaryContext = context; // TODO: Improve modifiers to have context available
		return modifier.transform(input, context);
	}

	public AdditionalModifier(final Object spec) {
		modifier = new Modifier.Overwritr(spec, AMIDA_FUNCTIONS);
	}
}
