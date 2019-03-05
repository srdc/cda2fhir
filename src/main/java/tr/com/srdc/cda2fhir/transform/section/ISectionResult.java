package tr.com.srdc.cda2fhir.transform.section;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;

public interface ISectionResult {
	Bundle getBundle();
	
	List<? extends Resource> getSectionResources();
}
