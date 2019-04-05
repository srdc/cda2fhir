package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ResultOrganizerGenerator {
	private static final Map<String, Object> DIAGNOSTIC_REPORT_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/DiagnosticReportStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private CDGenerator codeGenerator;

	private StatusCodeGenerator statusCodeGenerator;

	private IVL_TSPeriodGenerator effectiveTimeGenerator;

	private List<AuthorGenerator> authorGenerators = new ArrayList<>();

	private List<ResultObservationGenerator> observationGenerators = new ArrayList<>();

	public ResultOrganizer generate(CDAFactories factories) {
		ResultOrganizer ro = factories.consol.createResultOrganizer();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ro.getIds().add(ii);
		});

		if (codeGenerator != null) {
			CD cd = codeGenerator.generate(factories);
			ro.setCode(cd);
		}

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			ro.setStatusCode(cs);
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ts = effectiveTimeGenerator.generate(factories);
			ro.setEffectiveTime(ts);
		}

		authorGenerators.forEach(ag -> {
			Author author = ag.generate(factories);
			ro.getAuthors().add(author);
		});

		observationGenerators.forEach(og -> {
			org.openhealthtools.mdht.uml.cda.Observation obs = og.generate(factories);
			ro.addObservation(obs);
		});

		return ro;
	}

	public static ResultOrganizerGenerator getDefaultInstance() {
		ResultOrganizerGenerator rog = new ResultOrganizerGenerator();

		rog.idGenerators.add(IDGenerator.getNextInstance());
		rog.codeGenerator = CDGenerator.getNextInstance();
		rog.statusCodeGenerator = new StatusCodeGenerator(DIAGNOSTIC_REPORT_STATUS);
		rog.statusCodeGenerator.set("active");
		rog.effectiveTimeGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		rog.authorGenerators.add(AuthorGenerator.getDefaultInstance());
		rog.observationGenerators.add(ResultObservationGenerator.getDefaultInstance());

		return rog;
	}

	public void verify(DiagnosticReport diagnosticReport) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No diagnostic report identifier", !diagnosticReport.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(diagnosticReport.getIdentifier().get(index));
			}
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No diagnostic report code", !diagnosticReport.hasCode());
		} else {
			codeGenerator.verify(diagnosticReport.getCode());
		}

		if (statusCodeGenerator == null) {
			Assert.assertTrue("No diagnostic report status", !diagnosticReport.hasStatus());
		} else {
			statusCodeGenerator.verify(diagnosticReport.getStatus().toCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No diagnostic report effective period", !diagnosticReport.hasEffectivePeriod());
		} else {
			effectiveTimeGenerator.verify(diagnosticReport.getEffectivePeriod());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		DiagnosticReport diagnosticReport = BundleUtil.findOneResource(bundle, DiagnosticReport.class);
		verify(diagnosticReport);

		if (authorGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner", !diagnosticReport.hasPerformer());
		} else {
			Assert.assertEquals("Performer count", authorGenerators.size(), diagnosticReport.getPerformer().size());
			for (int index = 0; index < authorGenerators.size(); ++index) {
				String practitionerId = diagnosticReport.getPerformer().get(index).getActor().getReference();
				AuthorGenerator ag = authorGenerators.get(index);
				ag.verifyFromPractionerId(bundle, practitionerId);
			}
		}

		if (observationGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner", !diagnosticReport.hasResult());
		} else {
			BundleUtil util = new BundleUtil(bundle);
			Assert.assertEquals("Result count", observationGenerators.size(), diagnosticReport.getResult().size());
			for (int index = 0; index < observationGenerators.size(); ++index) {
				String observationId = diagnosticReport.getResult().get(index).getReference();
				ObservationGenerator og = observationGenerators.get(index);
				org.hl7.fhir.dstu3.model.Observation observation = util.getResourceFromReference(observationId,
						org.hl7.fhir.dstu3.model.Observation.class);
				og.verify(observation);
			}
		}
	}
}
