package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
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

		return ma;
	}

	public static MedicationActivityGenerator getDefaultInstance() {
		MedicationActivityGenerator ma = new MedicationActivityGenerator();

		ma.idGenerators.add(IDGenerator.getNextInstance());
		ma.statusCode = "active";
		ma.authorGenerator = AuthorGenerator.getDefaultInstance();

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
	}

	public void verify(Practitioner practitioner) {
		if (authorGenerator == null) {
			Assert.assertNull("Practitioner", practitioner);
		} else {
			authorGenerator.verify(practitioner);
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
	}
}
