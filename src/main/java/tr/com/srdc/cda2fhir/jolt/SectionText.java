package tr.com.srdc.cda2fhir.jolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.ContextualTransform;

public class SectionText implements ContextualTransform {
	@SuppressWarnings("unchecked")
	private void update(String id, Object content, Map<String, String> annotations) {
		if (content instanceof String) {
			annotations.put(id, (String) content);
			return;
		}
		if (content instanceof List) {
			List<Object> contentAsList = (List<Object>) content;
			for (Object element : contentAsList) {
				if (element instanceof String) {
					annotations.put(id, (String) element);
					return;
				}
			}
		}
	}

	private void update(Map<String, String> annotations, Map<String, Object> text) {
		String id = (String) text.get("ID");
		if (id != null) {
			Object content = text.get("content");
			if (content != null) {
				update(id, content, annotations);
			}
		}
		text.entrySet().forEach(entry -> {
			Object value = entry.getValue();
			if (value instanceof Map || value instanceof List) {
				update(annotations, value);
			}
		});
	}

	private void update(Map<String, String> annotations, List<Object> text) {
		text.forEach(t -> {
			if (t instanceof Map || t instanceof List) {
				update(annotations, t);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void update(Map<String, String> annotations, Object text) {
		if (text instanceof Map) {
			update(annotations, (Map<String, Object>) text);
			return;
		}
		if (text instanceof List) {
			update(annotations, (List<Object>) text);
			return;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object transform(Object input, Map<String, Object> context) {
		if (input == null) {
			return null;
		}
		Map<String, Object> inputAsMap = (Map<String, Object>) input;
		Map<String, Object> section = (Map<String, Object>) inputAsMap.get("section");
		if (section == null) {
			return input;
		}
		Object text = section.get("text");
		if (text == null || (text instanceof String)) {
			return input;
		}
		Map<String, String> annotations = (Map<String, String>) context.get("Annotations");
		if (annotations == null) {
			annotations = new HashMap<String, String>();
			context.put("Annotations", annotations);
		}
		update(annotations, text);
		return input;
	}
}
