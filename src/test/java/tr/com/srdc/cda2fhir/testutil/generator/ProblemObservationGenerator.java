package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemObservationGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();
	private CDGenerator codeGenerator;
	private List<CDGenerator> valueGenerators = new ArrayList<>();

	private EffectiveTimeGenerator effectiveTimeGenerator;

	private AuthorGenerator authorGenerator;

	private String authorTime;

	public ProblemObservation generate(CDAFactories factories) {
		ProblemObservation po = factories.consol.createProblemObservation();

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

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			po.setEffectiveTime(ivlTs);
		}

		if (authorGenerator != null) {
			Author author = authorGenerator.generate(factories);

			if (authorTime != null) {
				TS ts = factories.datatype.createTS(authorTime);
				author.setTime(ts);
			}

			po.getAuthors().add(author);
		}

		return po;
	}

	public static ProblemObservationGenerator getDefaultInstance() {
		ProblemObservationGenerator pcag = new ProblemObservationGenerator();

		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.codeGenerator = CDGenerator.getNextInstance();
		pcag.valueGenerators.add(CDGenerator.getNextInstance());
		pcag.effectiveTimeGenerator = new EffectiveTimeGenerator("20171008");

		return pcag;
	}

	public static ProblemObservationGenerator getFullInstance() {
		ProblemObservationGenerator pcag = new ProblemObservationGenerator();

		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.codeGenerator = CDGenerator.getNextInstance();
		pcag.valueGenerators.add(CDGenerator.getNextInstance());
		pcag.valueGenerators.add(CDGenerator.getNextInstance());
		pcag.effectiveTimeGenerator = new EffectiveTimeGenerator("20171008", "20181123");
		pcag.authorGenerator = AuthorGenerator.getDefaultInstance();
		pcag.authorTime = "20190101203500-0500";

		return pcag;
	}

	public IDGenerator getIDGenerator(int index) {
		return idGenerators.get(index);
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
			Assert.assertEquals("Condition category count", 1, condition.getCategory().size());
			Coding actual = condition.getCategory().get(0).getCoding().get(0);
			Assert.assertEquals("Condition category code", "problem-list-item", actual.getCode());
			Assert.assertEquals("Condition category system", "http://hl7.org/fhir/condition-category",
					actual.getSystem());
			Assert.assertEquals("Condition category display", "Problem List Item", actual.getDisplay());
		} else {
			Assert.assertTrue("No condition category", !condition.hasCategory());
		}

		if (valueGenerators.isEmpty()) {
			Assert.assertTrue("No condition code", !condition.hasCode());
		} else {
			CDGenerator valueGenerator = valueGenerators.get(valueGenerators.size() - 1);
			valueGenerator.verify(condition.getCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No condition onset", !condition.hasOnset());
		} else {
			String value = effectiveTimeGenerator.getLowOrValue();
			if (value == null) {
				Assert.assertTrue("No condition onset", !condition.hasOnset());
			} else {
				String actual = FHIRUtil.toCDADatetime(condition.getOnsetDateTimeType().asStringValue());
				Assert.assertEquals("Condition offset", value, actual);
			}
		}

		if (authorTime != null) {
			Assert.assertTrue("Condition asserter date", condition.hasAssertedDate());
			String datetime = condition.getAssertedDateElement().asStringValue();
			String actual = FHIRUtil.toCDADatetime(datetime);
			Assert.assertEquals("Condition asserter date", authorTime, actual);
		}
	}

	public void verify(Bundle bundle, Condition condition) throws Exception {
		verify(condition);

		if (authorGenerator == null) {
			Assert.assertTrue("No recorder", !condition.hasAsserter());
		} else {
			String practitionerId = condition.getAsserter().getReference();
			authorGenerator.verifyFromPractionerId(bundle, practitionerId);
		}
	}

	public void verify(Practitioner practitioner) {
		if (authorGenerator == null) {
			Assert.assertNull("Practitioner", practitioner);
		} else {
			authorGenerator.verify(practitioner);
		}
	}

	public void verify(PractitionerRole practitionerRole) {
		if (authorGenerator == null) {
			Assert.assertNull("Practitioner role", practitionerRole);
		} else {
			authorGenerator.verify(practitionerRole);
		}
	}

	public void verify(Organization organization) {
		if (authorGenerator == null) {
			Assert.assertNull("Organization", organization);
		} else {
			authorGenerator.verify(organization);
		}
	}
}
