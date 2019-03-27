package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActGenerator {
	private List<ProblemObservationGenerator> problemObservationGenerators = new ArrayList<>();

	public ProblemConcernAct generate(CDAFactories factories) {
		ProblemConcernAct pca = factories.consol.createProblemConcernAct();

		if (!problemObservationGenerators.isEmpty()) {
			problemObservationGenerators.forEach(pog -> {
				EntryRelationship er = factories.base.createEntryRelationship();
				pca.getEntryRelationships().add(er);
				er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
				ProblemObservation po = pog.generate(factories);
				er.setObservation(po);
			});
		}
		return pca;
	}

	public static ProblemConcernActGenerator getDefaultInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

		ProblemObservationGenerator pog = ProblemObservationGenerator.getDefaultInstance();
		pcag.problemObservationGenerators.add(pog);

		return pcag;
	}

	public static ProblemConcernActGenerator getFullInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

		ProblemObservationGenerator pog0 = ProblemObservationGenerator.getDefaultInstance();
		ProblemObservationGenerator pog1 = ProblemObservationGenerator.getFullInstance();
		pcag.problemObservationGenerators.add(pog0);
		pcag.problemObservationGenerators.add(pog1);

		return pcag;
	}

	public void verify(Bundle bundle) {
		BundleUtil util = new BundleUtil(bundle);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		int count = problemObservationGenerators == null ? 0 : problemObservationGenerators.size();
		Assert.assertEquals("Num of condition resources", count, conditions.size());
		for (int index = 0; index < 1; ++index) { // assume in order until proven otherwise
			Condition condition = conditions.get(index);
			ProblemObservationGenerator pog = problemObservationGenerators.get(index);
			pog.verify(condition);

			if (!condition.hasAsserter()) {
				pog.verify((Organization) null);
				pog.verify((PractitionerRole) null);
				pog.verify((Organization) null);
				continue;
			}
			String practitionerId = condition.getAsserter().getReference();
			Practitioner practitioner = util.getResourceFromReference(practitionerId, Practitioner.class);
			pog.verify(practitioner);

			PractitionerRole role = util.getPractitionerRole(practitionerId);
			pog.verify(role);

			if (!role.hasOrganization()) {
				pog.verify((Organization) null);
			} else {
				String reference = role.getOrganization().getReference();
				Organization organization = util.getResourceFromReference(reference, Organization.class);
				pog.verify(organization);
			}
		}
	}
}
