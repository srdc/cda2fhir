package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Product;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
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

		return mso;
	}

	public static MedicationSupplyOrderGenerator getDefaultInstance() {
		MedicationSupplyOrderGenerator mso = new MedicationSupplyOrderGenerator();

		mso.idGenerators.add(IDGenerator.getNextInstance());
		mso.statusCode = "active";
		mso.medInfoGenerator = MedicationInformationGenerator.getDefaultInstance();

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
	}

	public void verify(Bundle bundle) throws Exception {
		MedicationRequest ms = BundleUtil.findOneResource(bundle, MedicationRequest.class);

		verify(ms);

		BundleUtil util = new BundleUtil(bundle);

		if (medInfoGenerator == null) {
			Assert.assertTrue("No med statetement medication", ms.hasMedicationReference());
		} else {
			String medId = ms.getMedicationReference().getReference();
			Medication medication = util.getResourceFromReference(medId, Medication.class);
			medInfoGenerator.verify(medication);
			medInfoGenerator.verify(bundle);
		}
	}
}
