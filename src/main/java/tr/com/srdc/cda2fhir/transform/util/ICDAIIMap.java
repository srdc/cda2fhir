package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.openhealthtools.mdht.uml.hl7.datatypes.II;

public interface ICDAIIMap<T> {

	void put(II id, T value);

	T get(II ii);

	T get(List<II> iis);
}
