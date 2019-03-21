package tr.com.srdc.cda2fhir.transform.section.impl;

import org.hl7.fhir.dstu3.model.Condition;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDAProblemsSection implements ICDASection {
	private ProblemSection section;

	@SuppressWarnings("unused")
	private CDAProblemsSection() {
	};

	public CDAProblemsSection(ProblemSection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Condition> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Condition> result = SectionResultSingular.getInstance(Condition.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (ProblemConcernAct act : section.getConsolProblemConcerns()) {
			IEntryResult er = rt.tProblemConcernAct2Condition(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;
	}
}
