package tr.com.srdc.cda2fhir.transform.section;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;

import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public interface ISectionResult {
	Bundle getBundle();
	
	List<? extends Resource> getSectionResources();
	
	boolean hasDefferredReferences();
	
	List<IDeferredReference> getDeferredReferences();
	
	void updateFrom(IEntryResult entryResult);
}
