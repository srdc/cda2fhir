package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ProcedureActivityProcedureGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();

	public ProcedureActivityProcedure generate(CDAFactories factories) {
		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			pap.getIds().add(ii);
		});

		return pap;
	}

	public static ProcedureActivityProcedureGenerator getDefaultInstance() {
		ProcedureActivityProcedureGenerator papg = new ProcedureActivityProcedureGenerator();

		papg.idGenerators.add(IDGenerator.getNextInstance());

		return papg;
	}

	public static ProcedureActivityProcedureGenerator getFullInstance() {
		ProcedureActivityProcedureGenerator papg = new ProcedureActivityProcedureGenerator();

		papg.idGenerators.add(IDGenerator.getNextInstance());

		return papg;
	}

	public IDGenerator getIDGenerator(int index) {
		return idGenerators.get(index);
	}

	public void verify(Procedure procedure) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(procedure.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !procedure.hasIdentifier());
		}
	}
}
