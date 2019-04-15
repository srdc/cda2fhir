package tr.com.srdc.cda2fhir.transform.entry;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.util.IResult;

public interface IEntryResult extends IResult {
	Bundle getBundle();

	void copyTo(Bundle bundle);

	boolean hasResult();

}
