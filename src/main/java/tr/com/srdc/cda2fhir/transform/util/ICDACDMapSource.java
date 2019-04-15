package tr.com.srdc.cda2fhir.transform.util;

import java.util.Map;

public interface ICDACDMapSource<T> {
	void putCDValuesTo(Map<String, Map<String, T>> target);

	boolean hasCDMapValues();

}
