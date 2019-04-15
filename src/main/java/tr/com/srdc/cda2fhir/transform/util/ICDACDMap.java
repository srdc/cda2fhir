package tr.com.srdc.cda2fhir.transform.util;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

public interface ICDACDMap<T> {

	void put(CD cd, T value);

	T get(CD cd);

}
