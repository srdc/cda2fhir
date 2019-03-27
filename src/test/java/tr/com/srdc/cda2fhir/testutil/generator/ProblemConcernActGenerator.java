package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActGenerator {
	private List<IDGenerator> idGenerators = new ArrayList<>();
	private CDGenerator codeGenerator;
	private List<CDGenerator> valueGenerators = new ArrayList<>();

	private EffectiveTimeGenerator effectiveTimeGenerator;

	private AuthorGenerator authorGenerator;

	private String authorTime;

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

		return pca;
	}

	public static ProblemConcernActGenerator getDefaultInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

		pcag.idGenerators.add(IDGenerator.getNextInstance());
		pcag.codeGenerator = CDGenerator.getNextInstance();
		pcag.valueGenerators.add(CDGenerator.getNextInstance());
		pcag.effectiveTimeGenerator = new EffectiveTimeGenerator("20171008");

		return pcag;
	}

	public static ProblemConcernActGenerator getFullInstance() {
		ProblemConcernActGenerator pcag = new ProblemConcernActGenerator();

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

	public void verifyPractitioners(List<Practitioner> practitioners) {
		if (authorGenerator == null) {
			Assert.assertTrue("No practitioner", practitioners.size() == 0);
		} else {
			Assert.assertTrue("One practitioner", practitioners.size() == 1);
			authorGenerator.verify(practitioners.get(0));
		}
	}

	public void verifyPractitionerRoles(List<PractitionerRole> practitionerRoles) {
		if (authorGenerator == null) {
			Assert.assertTrue("No practitioner role", practitionerRoles.size() == 0);
		} else {
			Assert.assertTrue("One practitioner role", practitionerRoles.size() == 1);
			authorGenerator.verify(practitionerRoles.get(0));
		}
	}

	public void verifyOrganizations(List<Organization> organizations) {
		if (authorGenerator == null) {
			Assert.assertTrue("No organization", organizations.size() == 0);
		} else {
			Assert.assertTrue("One organization", organizations.size() == 1);
			authorGenerator.verify(organizations.get(0));
		}
	}
}
