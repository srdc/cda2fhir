package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Product;
import org.openhealthtools.mdht.uml.cda.consol.Instructions;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class MedicationSupplyOrderGenerator {
	private static final Map<String, Object> MED_REQUEST_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/MedicationRequestStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private String statusCode;
	private String statusCodeNullFlavor;

	private MedicationInformationGenerator medInfoGenerator;

	private IVL_PQSimpleQuantityGenerator quantityGenerator;

	private Integer repeat;

	private List<IVL_TSPeriodGenerator> effectiveTimeGenerators = new ArrayList<>();

	private String annotation;

	private AuthorGenerator authorGenerator;

	public MedicationSupplyOrder generate(CDAFactories factories) {
		MedicationSupplyOrder mso = factories.consol.createMedicationSupplyOrder();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			mso.getIds().add(ii);
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
			mso.setStatusCode(cs);
		}

		if (medInfoGenerator != null) {
			ManufacturedProduct med = medInfoGenerator.generate(factories);
			Product product = factories.base.createProduct();
			product.setManufacturedProduct(med);
			mso.setProduct(product);
		}

		if (quantityGenerator != null) {
			PQ pq = quantityGenerator.generate(factories);
			mso.setQuantity(pq);
		}

		if (repeat != null) {
			IVL_INT ivlInt = factories.datatype.createIVL_INT();
			ivlInt.setValue(repeat);
			mso.setRepeatNumber(ivlInt);
		}

		if (!effectiveTimeGenerators.isEmpty()) {
			effectiveTimeGenerators.forEach(et -> {
				IVL_TS ivlTs = et.generate(factories);
				mso.getEffectiveTimes().add(ivlTs);
			});
		}

		if (annotation != null) {
			Instructions instructions = factories.consol.createInstructions();
			Act act = factories.base.createAct();
			ED ed = factories.datatype.createED();
			ed.addText(annotation);
			act.setText(ed);
			instructions.addAct(act);
			mso.addAct(instructions);
		}

		if (authorGenerator != null) {
			Author author = authorGenerator.generate(factories);
			mso.getAuthors().add(author);
		}

		return mso;
	}

	public static MedicationSupplyOrderGenerator getDefaultInstance() {
		MedicationSupplyOrderGenerator mso = new MedicationSupplyOrderGenerator();

		mso.idGenerators.add(IDGenerator.getNextInstance());
		mso.statusCode = "active";
		mso.medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();
		mso.quantityGenerator = IVL_PQSimpleQuantityGenerator.getDefaultInstance();
		mso.repeat = 3;
		mso.effectiveTimeGenerators.add(IVL_TSPeriodGenerator.getDefaultInstance());
		mso.authorGenerator = AuthorGenerator.getDefaultInstance();

		return mso;
	}

	public void verify(MedicationRequest medRequest) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(medRequest.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No med request identifier", !medRequest.hasIdentifier());
		}

		if (statusCode == null || statusCodeNullFlavor != null) {
			Assert.assertTrue("Missing med request status", !medRequest.hasStatus());
		} else {
			String expected = (String) MED_REQUEST_STATUS.get(statusCode);
			if (expected == null) {
				expected = "unknown";
			}
			Assert.assertEquals("Med request status", expected, medRequest.getStatus().toCode());
		}

		if (quantityGenerator == null) {
			boolean hasQuantity = medRequest.hasDispenseRequest() && medRequest.getDispenseRequest().hasQuantity();
			Assert.assertTrue("Missing med request dispense quantity", !hasQuantity);
		} else {
			quantityGenerator.verify(medRequest.getDispenseRequest().getQuantity());
		}

		if (repeat == null) {
			boolean hasQuantity = medRequest.hasDispenseRequest()
					&& medRequest.getDispenseRequest().hasNumberOfRepeatsAllowed();
			Assert.assertTrue("Missing med request number repeats", !hasQuantity);
		} else {
			int actual = medRequest.getDispenseRequest().getNumberOfRepeatsAllowed();
			Assert.assertEquals("Med request number repeats", repeat.intValue(), actual);
		}

		if (effectiveTimeGenerators.isEmpty()) {
			boolean hasQuantity = medRequest.hasDispenseRequest()
					&& medRequest.getDispenseRequest().hasValidityPeriod();
			Assert.assertTrue("Missing med request dispense validity period", !hasQuantity);
		} else {
			Period period = medRequest.getDispenseRequest().getValidityPeriod();
			IVL_TSPeriodGenerator g = effectiveTimeGenerators.get(effectiveTimeGenerators.size() - 1);
			g.verify(period);
		}

		if (annotation == null) {
			Assert.assertTrue("Missing med request note", !medRequest.hasNote());
		} else {
			List<Annotation> notes = medRequest.getNote();
			Assert.assertEquals("Note annotation count", 1, notes.size());
			Assert.assertEquals("Note annotation", annotation, notes.get(0).getText());
		}
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationRequest mr = BundleUtil.findOneResource(bundle, MedicationRequest.class);

		verify(mr);

		BundleUtil util = new BundleUtil(bundle);

		if (medInfoGenerator == null) {
			Assert.assertTrue("No med statetement medication", !mr.hasMedicationReference());
		} else {
			String medId = mr.getMedicationReference().getReference();
			Medication medication = util.getResourceFromReference(medId, Medication.class);
			medInfoGenerator.verify(medication);
			medInfoGenerator.verify(bundle);
		}

		if (authorGenerator == null) {
			Assert.assertTrue("No med request requester", !mr.hasRequester());
		} else if (!mr.hasRequester()) {
			authorGenerator.verify((Organization) null);
			authorGenerator.verify((PractitionerRole) null);
			authorGenerator.verify((Organization) null);
		} else {
			String practitionerId = mr.getRequester().getAgent().getReference();
			Practitioner practitioner = util.getResourceFromReference(practitionerId, Practitioner.class);
			authorGenerator.verify(practitioner);

			PractitionerRole role = util.getPractitionerRole(practitionerId);
			authorGenerator.verify(role);

			if (!role.hasOrganization()) {
				authorGenerator.verify((Organization) null);
			} else {
				String reference = role.getOrganization().getReference();
				Organization organization = util.getResourceFromReference(reference, Organization.class);
				authorGenerator.verify(organization);
			}
		}
	}
}
