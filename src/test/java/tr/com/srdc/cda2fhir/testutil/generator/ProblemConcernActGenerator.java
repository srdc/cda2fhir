package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

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

		ProblemObservationGenerator pog = ProblemObservationGenerator.getFullInstance();
		pcag.problemObservationGenerators.add(pog);

		return pcag;
	}

	public void verify(Condition condition) {
		if (problemObservationGenerators.isEmpty()) {
			Assert.assertNull("No conditions", condition);
		} else {
			problemObservationGenerators.get(0).verify(condition);
		}
	}

	public void verifyPractitioners(List<Practitioner> practitioners) {
		if (problemObservationGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner", practitioners.size() == 0);
		} else {
			problemObservationGenerators.get(0).verifyPractitioners(practitioners);
		}
	}

	public void verifyPractitionerRoles(List<PractitionerRole> practitionerRoles) {
		if (problemObservationGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner role", practitionerRoles.size() == 0);
		} else {
			problemObservationGenerators.get(0).verifyPractitionerRoles(practitionerRoles);
		}
	}

	public void verifyOrganizations(List<Organization> organizations) {
		if (problemObservationGenerators.isEmpty()) {
			Assert.assertTrue("No organization", organizations.size() == 0);
		} else {
			problemObservationGenerators.get(0).verifyOrganizations(organizations);
		}
	}
}
