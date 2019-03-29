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
			int size = argList.size();
			if (argList == null || size < 2 || size > 3) {
				return Optional.empty();
			}
			String filename = (String) argList.get(0);
			String defaultValue = size == 3 ? (String) argList.get(1) : null;
			Object object = argList.get(size - 1);
			if (object == null) {
				return Optional.empty();
			}
			String value = object.toString();

			Map<String, Object> map = JsonUtils
					.filepathToMap("src/test/resources/jolt/value-maps/" + filename + ".json");
			Object mappedValue = map.get(value);
			if (mappedValue == null) {
				mappedValue = defaultValue;
			}
			if (mappedValue != null) {
				return Optional.of(mappedValue);
			}
			return Optional.of(null);
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
			if (argList == null || argList.size() != 2) {
				return Optional.empty();
			}
			String fhirType = (String) argList.get(0);
			Object arg = argList.get(1);
			if (!(arg instanceof Map)) {
				return Optional.empty();
			}
			Map<String, Object> identifier = (Map<String, Object>) arg;
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
		protected Optional applyList(final List<Object> argList) {
			if (argList == null || argList.size() < 1) {
				return Optional.empty();
			}
			java.util.Optional<String> result = argList.stream().map(dt -> (String) dt)
					.max(Comparator.comparing(String::valueOf));
			if (result.isPresent()) {
				return Optional.of(result.get());
			}
			return Optional.empty();
		}
	}

	public static final class SelectOnNull extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			if (argList == null || argList.size() != 3) {
				return Optional.empty();
			}
			Object valueObject = argList.get(2);
			String value = valueObject == null ? "" : valueObject.toString();
			int index = value.isEmpty() ? 0 : 1;
			String result = (String) argList.get(index);
			return Optional.of(result);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class GetId extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return Optional.of(null);
			}
			if (!(arg instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> map = (Map<String, Object>) arg;
			String resourceType = (String) map.get("resourceType");
			Object id = map.get("id");
			String result = String.format("%s/%s", resourceType, id.toString());

			return Optional.of(result);
		}
	}

	public static final class Piece extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			if (argList == null || argList.size() != 3) {
				return Optional.empty();
			}
			String value = (String) argList.get(2);
			if (value == null || value.isEmpty()) {
				return Optional.empty();
			}
			int pieceIndex = (int) argList.get(1);
			String delimiter = (String) argList.get(0);
			String[] pieces = value.split(delimiter);
			if (pieceIndex < pieces.length) {
				return Optional.of(pieces[pieceIndex]);
			}
			return Optional.empty();
		}
	}

	public static final class LastElement extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			if (argList == null) {
				return Optional.empty();
			}
			for (int index = argList.size() - 1; index >= 0; --index) {
				Object element = argList.get(index);
				if (element != null) {
					return Optional.of(element);
				}
			}
			return Optional.of(null);
		}
	}

	public static final class LastPiece extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			if (argList == null || argList.size() != 2) {
				return Optional.empty();
			}
			String value = (String) argList.get(1);
			if (value == null || value.isEmpty()) {
				return Optional.empty();
			}
			String delimiter = (String) argList.get(0);
			String[] pieces = value.split(delimiter);
			return Optional.of(pieces[pieces.length - 1]);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ConditionClinicalStatusAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return null;
			}
			if (!(arg instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> map = (Map<String, Object>) arg;
			Object low = map.get("low");
			Object high = map.get("high");
			if (low != null && high != null) {
				return Optional.of("resolved");
			}
			if (low != null) {
				return Optional.of("active");
			}
			return Optional.of(null);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class PutConstantValue extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			int size = argList.size();
			if (argList == null || size != 3) {
				return Optional.empty();
			}
			String filename = (String) argList.get(0);
			String key = (String) argList.get(1);
			Object object = argList.get(2);
			if (object == null) {
				return Optional.empty();
			}
			if (!(object instanceof Map)) {
				return Optional.empty();
			}
			Map<String, Object> target = (Map<String, Object>) object;

			Object constantValue = JsonUtils
					.filepathToObject("src/test/resources/jolt/value-maps/" + filename + ".json");
			target.put(key, constantValue);
			return Optional.of(object);
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
		AMIDA_FUNCTIONS.put("piece", new Piece());
		AMIDA_FUNCTIONS.put("lastElement", new LastElement());
		AMIDA_FUNCTIONS.put("lastPiece", new LastPiece());
		AMIDA_FUNCTIONS.put("conditionClinicalStatusAdapter", new ConditionClinicalStatusAdapter());
		AMIDA_FUNCTIONS.put("putConstantValue", new PutConstantValue());
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
