package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;

import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class SectionResultDynamic extends SectionResult {
	private List<Resource> resources = new ArrayList<Resource>();

	public void add(Resource resource) {
		getBundle().addEntry().setResource(resource);
		resources.add(resource);
	}

	public <T extends Resource> void updateFrom(IEntryResult entryResult, Class<T> clazz) {
		updateFrom(entryResult);
		Bundle bundle = entryResult.getBundle();
		if (bundle != null) {
			List<? extends Resource> sectionResources = FHIRUtil.findResources(bundle, clazz);
			resources.addAll(sectionResources);
		}
	}

	@Override
	public List<Resource> getSectionResources() {
		return resources;
	}

}
