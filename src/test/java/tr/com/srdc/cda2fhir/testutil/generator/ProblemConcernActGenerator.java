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
	private CDGenerator codeGenerator;
	private List<CDGenerator> valueGenerators = new ArrayList<>();

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

		if (codeGenerator != null) {
			CD code = codeGenerator.generate(factories);
			po.setCode(code);
		}

		valueGenerators.forEach(vg -> {
			CD value = vg.generate(factories);
			po.getValues().add(value);
		});

		return pca;
	}

	public static ProblemConcernActGenerator getDefaultInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.codeGenerator = CDGenerator.getNextInstance();
		pcag.valueGenerators.add(CDGenerator.getNextInstance());

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

		if (codeGenerator != null) {
			Assert.assertEquals("COndition category cpunt", 1, condition.getCategory().size());
			codeGenerator.verify(condition.getCategory().get(0));
		} else {
			Assert.assertTrue("No condition category", !condition.hasCategory());
		}

		if (valueGenerators.isEmpty()) {
			Assert.assertTrue("No condition code", !condition.hasCode());
		} else {
			CDGenerator valueGenerator = valueGenerators.get(valueGenerators.size() - 1);
			valueGenerator.verify(condition.getCode());
		}
	}
}
