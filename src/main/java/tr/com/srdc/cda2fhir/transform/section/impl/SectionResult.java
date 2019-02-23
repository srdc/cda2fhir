package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Bundle;

import tr.com.srdc.cda2fhir.transform.section.ISectionResult;

public abstract class SectionResult  implements ISectionResult {
	private Bundle bundle;

	SectionResult(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}
}
