package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ProblemConcernActGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();
	private CDGenerator cdGenerator;

	public ProblemConcernAct generate(CDAFactories factories) {
		ProblemConcernAct pca = factories.consol.createProblemConcernAct();

		EntryRelationship er = factories.base.createEntryRelationship();
		pca.getEntryRelationships().add(er);
		er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
		ProblemObservation po = factories.consol.createProblemObservation();
		er.setObservation(po);

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			po.getIds().add(ii);
		});

		if (cdGenerator != null) {
			CD code = cdGenerator.generate(factories);
			po.setCode(code);
		}

		return pca;
	}

	public static ProblemConcernActGenerator getDefaultInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.cdGenerator = CDGenerator.getNextInstance();

		return pcag;
	}

	public void verify(Condition condition) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(condition.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !condition.hasIdentifier());
		}

		if (cdGenerator != null) {
			Assert.assertEquals("COndition category cpunt", 1, condition.getCategory().size());
			cdGenerator.verify(condition.getCategory().get(0));
		} else {
			Assert.assertTrue("No condition category", !condition.hasCategory());
		}
	}
}
