package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActGenerator {
	private static final Map<String, Object> CONDITION_VERIFICATION_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ConditionVerificationStatus.json");

	private List<ProblemObservationGenerator> problemObservationGenerators = new ArrayList<>();

	private String statusCode;

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

		if (statusCode != null) {
			CS cs = factories.datatype.createCS(statusCode);
			pca.setStatusCode(cs);
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
		pcag.statusCode = "active";

		return pcag;
	}

	private int findProblemObservationGeneratorIndex(Condition condition) {
		for (int index = 0; index < problemObservationGenerators.size(); ++index) {
			IDGenerator idg = problemObservationGenerators.get(index).getIDGenerator(0);
			String system = idg.getSystem();
			String value = idg.getValue();

			List<Identifier> identifiers = condition.getIdentifier();
			for (int index2 = 0; index2 < identifiers.size(); ++index2) {
				Identifier identifier = identifiers.get(index2);
				String idSystem = identifier.getSystem();
				String idValue = identifier.getValue();
				if (system.equals(idSystem) && value.equals(idValue)) {
					return index;
				}
			}
		}
		return -1;
	}

	public void verify(Bundle bundle) {
		BundleUtil util = new BundleUtil(bundle);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		int count = problemObservationGenerators == null ? 0 : problemObservationGenerators.size();
		Assert.assertEquals("Num of condition resources", count, conditions.size());
		for (int index = 0; index < 1; ++index) { // assume in order until proven otherwise
			Condition condition = conditions.get(index);
			int pogIndex = count > 0 ? findProblemObservationGeneratorIndex(condition) : 0;
			if (pogIndex < 0) {
				throw new TestSetupException("Cannot find problem observation from identifiers.");
			}
			ProblemObservationGenerator pog = problemObservationGenerators.get(pogIndex);
			pog.verify(condition);

			if (statusCode == null) {
				Assert.assertEquals("Condition verification status", "unknown",
						condition.getVerificationStatus().toCode());
			} else {
				String actual = (String) CONDITION_VERIFICATION_STATUS.get(statusCode);
				if (actual == null) {
					actual = "unknown";
				}
				Assert.assertEquals("Condition verification status", actual,
						condition.getVerificationStatus().toCode());
			}

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
