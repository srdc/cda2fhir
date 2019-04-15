package tr.com.srdc.cda2fhir.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.openhealthtools.mdht.uml.cda.StrucDocText;

public class EMFUtil {

	private static String[] supportedTypes = { "content", "td", "paragraph" };

	static private String findAttribute(FeatureMap attributes, String name) {
		if (attributes != null) {
			for (Entry attribute : attributes) {
				String attrName = attribute.getEStructuralFeature().getName();
				if (name.equalsIgnoreCase(attrName)) {
					return attribute.getValue().toString();
				}
			}
		}
		return null;
	}

	static private void putReferences(FeatureMap featureMap, Map<String, String> result) {
		if (featureMap == null) {
			return;
		}
		for (Entry entry : featureMap) {
			EStructuralFeature feature = entry.getEStructuralFeature();
			if (feature instanceof EReference) {
				AnyType anyType = (AnyType) entry.getValue();

				if (Arrays.stream(supportedTypes).anyMatch(feature.getName().toLowerCase()::equals)) {
					String id = findAttribute(anyType.getAnyAttribute(), "id");
					if (id != null) {
						FeatureMap idValueMap = anyType.getMixed();
						if (idValueMap != null && !idValueMap.isEmpty()) {
							Object value = idValueMap.get(0).getValue();
							if (value != null) {
								result.put(id, value.toString().trim());
							}
						}
					}
				}
				putReferences(anyType.getMixed(), result);
			}
		}
	}

	/***
	 * Pulls text references out of source HTML for later use.
	 *
	 * @param text the structured text portion of the CCD document.
	 * @return map of ids and values from that section.
	 */
	static public Map<String, String> findReferences(StrucDocText text) {
		if(text != null) {
			Map<String, String> result = new HashMap<String, String>();
			FeatureMap featureMap = text.getMixed();
			putReferences(featureMap, result);
			return result;
		}
		return null;
	}
}
