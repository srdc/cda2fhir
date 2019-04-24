package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.entry.CDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.entry.IMedicationsInformation;

public interface IResult extends ICDAIIMapSource<IEntityInfo>, ICDAIIResourceMapsSource<IBaseResource>,
		ICDACDMapSource<IMedicationsInformation> {

	List<IDeferredReference> getDeferredReferences();

	void updateFrom(IEntryResult entryResult);

	boolean hasDeferredReferences();

	boolean hasEntities();

	boolean hasIIResourceMaps();

	boolean hasCDMap();

	public void putCDResource(CD cd, IMedicationsInformation resource);

	CDAIIResourceMaps<IBaseResource> getResourceMaps();

	void putIIResource(II ii, Class<? extends IBaseResource> clazz, IBaseResource resource);

	void putIIResource(List<II> iis, Class<? extends IBaseResource> clazz, IBaseResource resource);
}
