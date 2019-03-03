package tr.com.srdc.cda2fhir.transform.util;

import java.util.Map;

public interface ICDAIIMapSource<T> {
	public void putRootValuesTo(Map<String, T> target);

	public void putExtensionValuesTo(Map<String, Map<String, T>> target);		
}
