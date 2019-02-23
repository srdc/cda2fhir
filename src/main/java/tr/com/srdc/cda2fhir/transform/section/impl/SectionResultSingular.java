package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;

import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class SectionResultSingular<T extends Resource> extends SectionResult {
	private Class<T> clazz;
	
	public SectionResultSingular(Bundle bundle, Class<T> clazz) {
		super(bundle);
		this.clazz = clazz;
	}
	
	@Override
	public List<T> getSectionResources() {
		Bundle bundle = getBundle();
		return FHIRUtil.findResources(bundle, clazz);
	}
}
