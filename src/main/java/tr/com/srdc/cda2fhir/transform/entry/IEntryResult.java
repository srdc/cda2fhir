package tr.com.srdc.cda2fhir.transform.entry;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public interface IEntryResult {
	Bundle getBundle();

	boolean hasDeferredReferences();

	List<IDeferredReference> getDeferredReferences();
}
