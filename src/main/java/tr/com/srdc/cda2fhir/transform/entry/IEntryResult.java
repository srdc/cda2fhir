package tr.com.srdc.cda2fhir.transform.entry;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import tr.com.srdc.cda2fhir.transform.util.ICDACDMapSource;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;
import tr.com.srdc.cda2fhir.transform.util.ICDAIIResourceMapsSource;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public interface IEntryResult
		extends ICDAIIMapSource<IEntityInfo>, ICDAIIResourceMapsSource<IBaseResource>, ICDACDMapSource<IBaseResource> {
	Bundle getBundle();

	void copyTo(Bundle bundle);

	boolean hasDeferredReferences();

	boolean hasResult();

	boolean hasEntities();

	boolean hasIIResourceMaps();

	boolean hasCDMap();

	List<IDeferredReference> getDeferredReferences();

	void updateFrom(IEntryResult entryResult);
}
