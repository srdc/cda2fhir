package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.CDAIIResourceMaps;
import tr.com.srdc.cda2fhir.transform.entry.IEntityInfo;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.entry.IMedicationsInformation;
import tr.com.srdc.cda2fhir.transform.util.impl.CDACDMap;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public interface IBundleInfo {
	IResourceTransformer getResourceTransformer();

	Map<String, String> getIdedAnnotations();

	Reference getReferenceByIdentifier(String fhirType, Identifier identifier);

	IEntityInfo findEntityResult(II ii);

	IEntityInfo findEntityResult(List<II> iis);

	IBaseResource findResourceResult(II ii, Class<? extends IBaseResource> clazz);

	IBaseResource findResourceResult(List<II> iis, Class<? extends IBaseResource> clazz);

	public IMedicationsInformation findResourceResult(CD cd);

	public void updateFrom(IResult source);

	public void updateFrom(IEntityResult entityResult);

	public CDAIIMap<IEntityInfo> getEntities();

	public CDAIIResourceMaps<IBaseResource> getResourceMaps();

	public CDACDMap<IMedicationsInformation> getCDMap();
}
