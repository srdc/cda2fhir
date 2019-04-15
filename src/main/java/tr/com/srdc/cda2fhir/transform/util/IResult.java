package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;

public interface IResult
		extends ICDAIIMapSource<IEntityInfo>, ICDAIIResourceMapsSource<IBaseResource>, ICDACDMapSource<IBaseResource> {

	List<IDeferredReference> getDeferredReferences();

	void updateFrom(IEntryResult entryResult);

	boolean hasDeferredReferences();

	boolean hasEntities();

	boolean hasIIResourceMaps();

	boolean hasCDMap();

	public void putCDResource(CD cd, IBaseResource resource);

}
