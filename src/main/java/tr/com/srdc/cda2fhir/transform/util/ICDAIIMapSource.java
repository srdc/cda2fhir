package tr.com.srdc.cda2fhir.transform.util;

import java.util.Map;

public interface ICDAIIMapSource<T> {

	void putRootValuesTo(Map<String, T> target);

	void putExtensionValuesTo(Map<String, Map<String, T>> target);

	boolean hasIIMapValues();

}
