package tr.com.srdc.cda2fhir.transform.section;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.ICDACDMapSource;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIResourceMapsSource;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public interface ISectionResult
		extends ICDAIIMapSource<IEntityInfo>, ICDAIIResourceMapsSource<IBaseResource>, ICDACDMapSource<IBaseResource> {
	Bundle getBundle();

	List<? extends Resource> getSectionResources();

	boolean hasDefferredReferences();

	List<IDeferredReference> getDeferredReferences();

	void updateFrom(IEntryResult entryResult);

	boolean hasResourceMaps();
}
