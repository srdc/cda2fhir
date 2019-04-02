package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Timing;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class MedicationActivityGenerator {
	private static final Map<String, Object> MED_STATEMENT_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/MedicationStatementStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private String statusCode;
	private String statusCodeNullFlavor;

	private AuthorGenerator authorGenerator;
	private MedicationInformationGenerator medInfoGenerator;

	private IVL_TSPeriodGenerator ivlTsPeriodGenerator;
	private PIVL_TSTimingGenerator pivlTsTimingGenerator;

	public MedicationActivity generate(CDAFactories factories) {
		MedicationActivity ma = factories.consol.createMedicationActivity();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			ma.getIds().add(ii);
		});

		if (statusCode != null || statusCodeNullFlavor != null) {
			CS cs = factories.datatype.createCS();
			if (statusCode != null) {
				cs.setCode(statusCode);
			}
			if (statusCodeNullFlavor != null) {
				NullFlavor nf = NullFlavor.get(statusCodeNullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				cs.setNullFlavor(nf);
			}
			ma.setStatusCode(cs);
		}

		if (authorGenerator != null) {
			Author author = authorGenerator.generate(factories);
			ma.getAuthors().add(author);
		}

		if (medInfoGenerator != null) {
			ManufacturedProduct med = medInfoGenerator.generate(factories);
			Consumable consumable = factories.base.createConsumable();
			consumable.setManufacturedProduct(med);
			ma.setConsumable(consumable);
		}

		if (ivlTsPeriodGenerator != null) {
			IVL_TS ivlTs = ivlTsPeriodGenerator.generate(factories);
			ma.getEffectiveTimes().add(ivlTs);
		}

		if (pivlTsTimingGenerator != null) {
			PIVL_TS pivlTs = pivlTsTimingGenerator.generate(factories);
			ma.getEffectiveTimes().add(pivlTs);
		}

		return ma;
	}

	public static MedicationActivityGenerator getDefaultInstance() {
		MedicationActivityGenerator ma = new MedicationActivityGenerator();

		ma.idGenerators.add(IDGenerator.getNextInstance());
		ma.statusCode = "active";
		ma.authorGenerator = AuthorGenerator.getDefaultInstance();
		ma.medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();
		ma.ivlTsPeriodGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		ma.pivlTsTimingGenerator = PIVL_TSTimingGenerator.getDefaultInstance();

		return ma;
	}

	public void verify(MedicationStatement medStatement) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(medStatement.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !medStatement.hasIdentifier());
		}

		if (statusCode == null || statusCodeNullFlavor != null) {
			Assert.assertTrue("Missing med statement status", !medStatement.hasStatus());
		} else {
			String expected = (String) MED_STATEMENT_STATUS.get(statusCode);
			if (expected == null) {
				expected = "unknown";
			}
			Assert.assertEquals("Med statement status", expected, medStatement.getStatus().toCode());
		}

		if (ivlTsPeriodGenerator == null) {
			Assert.assertTrue("Missing med statement effective", !medStatement.hasEffectivePeriod());
		} else {
			ivlTsPeriodGenerator.verify(medStatement.getEffectivePeriod());
		}

		if (pivlTsTimingGenerator == null) {
			boolean hasDosageTiming = medStatement.hasDosage() && medStatement.getDosage().get(0).hasTiming();
			Assert.assertTrue("Missing med statement dosage time", !hasDosageTiming);
		} else {
			Timing timing = medStatement.getDosage().get(0).getTiming();
			pivlTsTimingGenerator.verify(timing);
		}
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationStatement ms = BundleUtil.findOneResource(bundle, MedicationStatement.class);

		verify(ms);

		BundleUtil util = new BundleUtil(bundle);

		if (authorGenerator == null) {
			Assert.assertTrue("No med statetement information source", ms.hasInformationSource());
		} else {
			String practitionerId = ms.getInformationSource().getReference();
			Practitioner practitioner = util.getResourceFromReference(practitionerId, Practitioner.class);
			authorGenerator.verify(practitioner);

			PractitionerRole role = util.getPractitionerRole(practitionerId);
			authorGenerator.verify(role);

			if (role.hasOrganization()) {
				String reference = role.getOrganization().getReference();
				Organization organization = util.getResourceFromReference(reference, Organization.class);
				authorGenerator.verify(organization);
			}
		}

		if (medInfoGenerator == null) {
			Assert.assertTrue("No med statetement medication", ms.hasMedication());
		} else {
			String medId = ms.getMedicationReference().getReference();
			Medication medication = util.getResourceFromReference(medId, Medication.class);
			medInfoGenerator.verify(medication);
			medInfoGenerator.verify(bundle);
		}
	}
}
