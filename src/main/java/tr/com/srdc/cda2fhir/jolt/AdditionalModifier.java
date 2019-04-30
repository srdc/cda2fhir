package tr.com.srdc.cda2fhir.jolt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Modifier;
import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;

import tr.com.srdc.cda2fhir.jolt.report.ReportException;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.StringUtil;

@SuppressWarnings("deprecation")
public class AdditionalModifier implements SpecDriven, ContextualTransform {
	private static Map<String, Object> temporaryContext; // Hack for now

	@SuppressWarnings("unchecked")
	private static String getDisplayFromCC(Map<String, Object> codebleConcept) {
		if (codebleConcept.get("text") instanceof String) {
			return (String) codebleConcept.get("text");
		} else if (codebleConcept.get("coding") instanceof List) {
			List<Object> coding = (List<Object>) codebleConcept.get("coding");
			if (!coding.isEmpty() && coding.get(0) instanceof Map) {
				for (Object entry : coding) {
					Map<String, Object> currentEntry = (Map<String, Object>) entry;
					if (currentEntry.get("display") instanceof String) {
						return (String) currentEntry.get("display");
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static String getDisplayFromCode(Map<String, Object> map, String property) {
		Object topValue = map.get(property);

		if (topValue instanceof List) {
			List<Object> elements = (List<Object>) topValue;
			for (Object element : elements) {
				if (element instanceof Map) {
					Map<String, Object> elementMap = (Map<String, Object>) element;
					String display = getDisplayFromCC(elementMap);
					if (display != null) {
						return display;
					}
				}
			}
		}

		if (topValue instanceof Map) {
			Map<String, Object> cc = (Map<String, Object>) topValue;
			return getDisplayFromCC(cc);
		}
		return null;
	}

	private static String getDisplayFromCode(Map<String, Object> map) {
		return getDisplayFromCode(map, "code");
	}

	@SuppressWarnings("unchecked")
	public static String getDisplay(Map<String, Object> map) {
		String display = getDisplayFromCode(map);
		if (display != null) {
			return display;
		}

		Object medicationReference = map.get("medicationReference");
		if (medicationReference != null) {
			if (medicationReference instanceof Map) {
				Object medRef = ((Map<String, Object>) medicationReference).get("reference");
				if (medRef != null && medRef instanceof String) {
					String mefRefString = (String) medRef;
					Map<String, Object> resourceMap = (Map<String, Object>) temporaryContext.get("RESOURCE_MAP");

					if (resourceMap != null) {
						Map<String, Object> resource = (Map<String, Object>) resourceMap.get(mefRefString);
						if (resource != null) {
							display = getDisplayFromCode(resource);
						}
					}

				}
			}
		}
		if (display != null) {
			return display;
		}

		display = getDisplayFromCode(map, "vaccineCode");
		if (display != null) {
			return display;
		}

		if (!"Device".equals(map.get("resourceType"))) {
			display = getDisplayFromCode(map, "type");
			if (display != null) {
				return display;
			}
		}

		if (map.get("name") != null) {
			Object name = map.get("name");
			if (name instanceof String) {
				display = (String) name;
			} else if (name instanceof List) {
				List<Object> nameList = (List<Object>) name;
				if (!nameList.isEmpty()) {
					List<String> allNames = new ArrayList<String>();
					Iterator<Object> iter = nameList.listIterator();
					while (iter.hasNext()) {
						Object humanNameObj = iter.next();
						if (humanNameObj instanceof Map) {
							Map<String, Object> humanName = (Map<String, Object>) humanNameObj;
							ArrayList<String> currentNameList = new ArrayList<String>();
							// TODO make array list
							if (humanName.get("prefix") instanceof List)
								currentNameList.addAll((List<String>) humanName.get("prefix"));
							if (humanName.get("given") instanceof List)
								currentNameList.addAll((List<String>) humanName.get("given"));
							if (humanName.get("family") instanceof String)
								currentNameList.add((String) humanName.get("family"));
							if (humanName.get("suffix") instanceof List)
								currentNameList.addAll((List<String>) humanName.get("suffix"));
							String currentName = currentNameList.stream().collect(Collectors.joining(" "));
							if (!currentName.contentEquals(""))
								allNames.add(currentName);
						}
					}
					if (allNames.size() > 0) {
						display = allNames.stream().collect(Collectors.joining(", "));
					}
				}
			}
		}

		return display;
	}

	public static final class DatetimeAdapter extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {

			if (!(arg instanceof String || arg instanceof Integer || arg instanceof Long)) {
				return Optional.empty();
			}
			String datetimeWithZone = arg.toString();
			if (datetimeWithZone.length() < 4) {
				return Optional.empty();
			}
			String[] pieces = datetimeWithZone.split("[-+]");
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
							result += ":" + datetime.substring(12, datetime.length());
						} else {
							result += ":00";
						}
					}
				}
			}
			String zone = pieces.length > 1 ? pieces[1] : null;
			if (zone != null && zone.length() > 0) {
				String sign = datetimeWithZone.indexOf("-") >= 0 ? "-" : "+";
				result += sign + zone.substring(0, 2) + ":" + zone.substring(2, 4);
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
				if (mappedValue == null) {
					mappedValue = map.get("_");
				}
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
				return Optional.of(null);
			}
			String fhirType = (String) argList.get(0);
			Object arg = argList.get(1);
			if (!(arg instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> identifier = (Map<String, Object>) arg;
			String system = (String) identifier.get("system");
			String value = (String) identifier.get("value");
			IdentifierMap<String> map = (IdentifierMap<String>) temporaryContext.get("RefsByIdentifier");
			if (map == null) {
				return Optional.of(null);
			}
			String reference = map.get(fhirType, system, value);
			if (reference == null) {
				return Optional.of(null);
			}
			return Optional.of(reference);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ReferenceDisplayAdapter extends Function.ListFunction {
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
			IdentifierMap<String> map = (IdentifierMap<String>) temporaryContext.get("RefDisplaysByIdentifier");
			if (map == null) {
				return Optional.of(null);
			}
			String display = map.get(fhirType, system, value);
			if (display == null) {
				return Optional.of(null);
			}
			return Optional.of(display);
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

	@SuppressWarnings("unchecked")
	public static final class GetReferenceDisplay extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return Optional.of(null);
			}
			if (!(arg instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> map = (Map<String, Object>) arg;
			String display = getDisplay(map);
			return Optional.of(display);
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

	public static final class ConditionClinicalStatusAdapter extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			if (argList == null) {
				return null;
			}
			if (argList.indexOf("high") >= 0 && argList.indexOf("low") >= 0) {
				return Optional.of("inactive");
			}
			if (argList.indexOf("value") >= 0 || argList.indexOf("low") >= 0) {
				return Optional.of("active");
			}
			return Optional.of(null);
		}
	}

	public static final class ConstantValue extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null || !(arg instanceof String)) {
				throw new ReportException("Invalid argument for ConstantValue modifier.");
			}
			String filename = (String) arg;

			Object constantValue = JsonUtils
					.filepathToObject("src/test/resources/jolt/value-maps/" + filename + ".json");
			return Optional.of(constantValue);
		}
	}

	public static final class True extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			return Optional.of(true);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ConstantSystem extends Function.ListFunction {
		private Optional<Object> applyForCD(Map<String, Object> cd, String system) {
			List<Object> codings = (List<Object>) cd.get("coding");
			if (codings != null) {
				codings.forEach(coding -> {
					Map<String, Object> codingAsMap = (Map<String, Object>) coding;
					if (codingAsMap != null) {
						codingAsMap.put("system", system);
					}
				});
			}
			return Optional.of(cd);
		}

		private Optional<Object> applyForPQ(Map<String, Object> pq, String system) {
			pq.put("system", system);
			return Optional.of(pq);
		}

		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			String system = (String) argList.get(0);
			String type = (String) argList.get(1);
			Map<String, Object> object = (Map<String, Object>) argList.get(2);
			if ("cd".equals(type)) {
				return applyForCD(object, system);
			}
			if ("pq".equals(type)) {
				return applyForPQ(object, system);
			}
			throw new ReportException("Unknown constant system type " + system + ".");
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ContentOrSelf extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return null;
			}
			if (!(arg instanceof Map)) {
				return Optional.of(arg);
			}
			Map<String, Object> map = (Map<String, Object>) arg;
			return Optional.of(map.get("content"));
		}
	}

	@SuppressWarnings("unchecked")
	public static final class ResolveText extends Function.SingleFunction<Object> {
		private static Optional<Object> getTextValue(String rawValue) {
			if (rawValue.isEmpty()) {
				return Optional.of(null);
			}
			if (rawValue.charAt(0) != '#') {
				return Optional.of(rawValue);
			}
			Map<String, Object> map = (Map<String, Object>) temporaryContext.get("Annotations");
			if (map == null) {
				return Optional.of(null);
			}
			String value = (String) map.get(rawValue.substring(1));
			return Optional.of(value);
		}

		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return Optional.of(null);
			}
			if (arg instanceof String) {
				return getTextValue((String) arg);
			}
			if (!(arg instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> argAsMap = (Map<String, Object>) arg;
			Object reference = argAsMap.get("reference");
			if (reference == null || !(reference instanceof Map)) {
				return Optional.of(null);
			}
			Map<String, Object> referenceAsMap = (Map<String, Object>) reference;
			Object valueObject = referenceAsMap.get("value");
			if (valueObject == null || !(valueObject instanceof String)) {
				return Optional.of(null);
			}
			return getTextValue((String) valueObject);
		}
	}

	public static final class NullIfMap extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return null;
			}
			if (arg instanceof Map) {
				return Optional.of(null);
			}
			return Optional.empty();
		}
	}

	public static final class ToString extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			if (arg == null) {
				return Optional.of(null);
			}
			if (arg instanceof Double) {
				String result = Double.toString((Double) arg);
				return Optional.of(result);
			}
			if (arg instanceof Float) {
				String result = Float.toString((Float) arg);
				return Optional.of(result);
			}
			if (!(arg instanceof String)) {
				return Optional.of(arg.toString());
			}
			return Optional.of(arg);
		}
	}

	@SuppressWarnings("unchecked")
	public static final class InterpretationCodeAdapter extends Function.ListFunction {
		@Override
		protected Optional<Object> applyList(List<Object> argList) {
			int size = argList.size();
			if (argList == null || size != 2) {
				return Optional.empty();
			}
			String filename = (String) argList.get(0);
			Object object = argList.get(1);
			if (object == null) {
				return Optional.empty();
			}
			Map<String, Object> value = (Map<String, Object>) object;
			String code = (String) value.get("code");
			if (code == null) {
				return Optional.of(null);
			}
			Map<String, Object> map = JsonUtils
					.filepathToMap("src/test/resources/jolt/value-maps/" + filename + ".json");
			Object mappedValue = map.get(code);
			if (mappedValue == null) {
				return Optional.empty();
			}
			return Optional.of(mappedValue);
		}
	}

	public static final class NoOpSingle extends Function.SingleFunction<Object> {
		@Override
		protected Optional<Object> applySingle(final Object arg) {
			return Optional.of(arg);
		}
	}

	private static final Map<String, Function> AMIDA_FUNCTIONS = new HashMap<>();
	static {
		AMIDA_FUNCTIONS.put("defaultid", new DefaultId());
		AMIDA_FUNCTIONS.put("datetimeAdapter", new DatetimeAdapter());
		AMIDA_FUNCTIONS.put("referenceAdapter", new ReferenceAdapter());
		AMIDA_FUNCTIONS.put("referenceDisplayAdapter", new ReferenceDisplayAdapter());
		AMIDA_FUNCTIONS.put("valueSetAdapter", new ValueSetAdapter());
		AMIDA_FUNCTIONS.put("interpretationCodeAdapter", new InterpretationCodeAdapter());
		AMIDA_FUNCTIONS.put("systemAdapter", new SystemAdapter());
		AMIDA_FUNCTIONS.put("idSystemAdapter", new IdSystemAdapter());
		AMIDA_FUNCTIONS.put("maxDateTime", new MaxDateTime());
		AMIDA_FUNCTIONS.put("selectOnNull", new SelectOnNull());
		AMIDA_FUNCTIONS.put("getId", new GetId());
		AMIDA_FUNCTIONS.put("getReferenceDisplay", new GetReferenceDisplay());
		AMIDA_FUNCTIONS.put("piece", new Piece());
		AMIDA_FUNCTIONS.put("lastElement", new LastElement());
		AMIDA_FUNCTIONS.put("lastPiece", new LastPiece());
		AMIDA_FUNCTIONS.put("conditionClinicalStatusAdapter", new ConditionClinicalStatusAdapter());
		AMIDA_FUNCTIONS.put("constantValue", new ConstantValue());
		AMIDA_FUNCTIONS.put("true", new True());
		AMIDA_FUNCTIONS.put("constantSystem", new ConstantSystem());
		AMIDA_FUNCTIONS.put("contentOrSelf", new ContentOrSelf());
		AMIDA_FUNCTIONS.put("nullIfMap", new NullIfMap());
		AMIDA_FUNCTIONS.put("resolveText", new ResolveText());
		AMIDA_FUNCTIONS.put("toString", new ToString());
		AMIDA_FUNCTIONS.put("deferredUpdate", new NoOpSingle());
	}

	private Modifier.Overwritr modifier;

	@SuppressWarnings("unchecked")
	@Override
	public Object transform(final Object input, final Map<String, Object> context) {
		temporaryContext = context; // TODO: Improve modifiers to have context available
		Object result = modifier.transform(input, context);
		if (result == null || !(result instanceof Map)) {
			return result;
		}
		Map<String, Object> resultAsMap = (Map<String, Object>) result;
		Iterator<Entry<String, Object>> itr = resultAsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Object> entry = itr.next();
			Object value = entry.getValue();
			if (value == null) {
				itr.remove();
				continue;
			}
			if (value instanceof Map) {
				Map<String, Object> valueAsMap = (Map<String, Object>) value;
				if (valueAsMap.isEmpty()) {
					itr.remove();
					continue;
				}
			}
		}
		return result;
	}

	public AdditionalModifier(final Object spec) {
		modifier = new Modifier.Overwritr(spec, AMIDA_FUNCTIONS);
	}
}
