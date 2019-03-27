package tr.com.srdc.cda2fhir.transform.entry;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.util.ICDAIIMapSource;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public interface IEntryResult extends ICDAIIMapSource<IEntityInfo> {
	Bundle getBundle();

	void copyTo(Bundle bundle);

	boolean hasDeferredReferences();

	boolean hasResult();

	List<IDeferredReference> getDeferredReferences();

	void updateFrom(IEntryResult entryResult);
}
