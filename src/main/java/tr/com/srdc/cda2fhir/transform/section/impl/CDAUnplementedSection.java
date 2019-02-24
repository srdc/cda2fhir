package tr.com.srdc.cda2fhir.transform.section.impl;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAUnplementedSection implements ICDASection {
	@Override
	public ISectionResult transform(IBundleInfo bundleInfo) {
		return null;
	}
}
