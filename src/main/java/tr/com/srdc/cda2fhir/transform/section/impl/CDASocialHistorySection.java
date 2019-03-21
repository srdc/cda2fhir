package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDASocialHistorySection implements ICDASection {
	private SocialHistorySection section;

	@SuppressWarnings("unused")
	private CDASocialHistorySection() {
	};

	public CDASocialHistorySection(SocialHistorySection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		/**
		 * The generic observation transformer should be able to transform all the
		 * possible entries: Caregiver Characteristics Characteristics of Home
		 * Environment Cultural and Religious Observation Pregnancy Observation Smoking
		 * Status - Meaningful Use (V2) Social History Observation (V3) Tobacco Use (V2)
		 */
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Observation> result = SectionResultSingular.getInstance(Observation.class);
		for (org.openhealthtools.mdht.uml.cda.Observation obs : section.getObservations()) {
			IEntryResult er = rt.tObservation2Observation(obs, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;
	}
}
