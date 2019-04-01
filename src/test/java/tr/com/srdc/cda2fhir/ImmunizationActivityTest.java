package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationRefusalReason;
import org.openhealthtools.mdht.uml.cda.consol.impl.ImmunizationActivityImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ImmunizationRefusalReasonImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;
import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.PerformerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class ImmunizationActivityTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;

	private static Map<String, Object> statusMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ImmunizationStatus.json");

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testPerformer() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) factories.consol.createImmunizationActivity();
		BundleInfo bundleInfo = new BundleInfo(rt);

		Bundle bundle = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected positive primary source", false, immunization.getPrimarySource());

		PerformerGenerator performerGenerator = PerformerGenerator.getDefaultInstance();
		Performer2 performer = performerGenerator.generate(factories);
		act.getPerformers().add(performer);

		Bundle bundle1 = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();
		Immunization immunization1 = BundleUtil.findOneResource(bundle1, Immunization.class);
		Assert.assertEquals("Unexpected negative primary source", true, immunization1.getPrimarySource());

		String reference = immunization1.getPractitioner().get(0).getActor().getReference();
		Practitioner practitioner = BundleUtil.findOneResource(bundle1, Practitioner.class);
		Assert.assertEquals("Unexpected Reference", reference, practitioner.getId());
		performerGenerator.verify(practitioner);
	}

	static private void verifyNotGiven(ImmunizationActivity act, ImmunizationRefusalReason refusal, Boolean value)
			throws Exception {
		if (value != null) {
			act.setNegationInd(value);
			act.addObservation(refusal);
			DiagnosticChain dxChain = new BasicDiagnostic();
			Boolean validation = act.validateImmunizationActivityNegationInd(dxChain, null);
			Assert.assertTrue("Invalid Immunization Activity in Test", validation);
		}

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);
		Assert.assertEquals("Unexpected not given", value == null ? false : value, immunization.getNotGiven());
		Assert.assertEquals("Unexpected Not Given Reason", value == null ? false : value,
				immunization.getExplanation().getReasonNotGiven().size() > 0);
	}

	@Test
	public void testNegationInd() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) factories.consol.createImmunizationActivity();
		ImmunizationRefusalReasonImpl refusal = (ImmunizationRefusalReasonImpl) factories.consol
				.createImmunizationRefusalReason();
		CD cd = factories.datatype.createCD();
		cd.setCode("PATOBJ");
		refusal.setCode(cd);
		verifyNotGiven(act, refusal, true);
		verifyNotGiven(act, refusal, false);
	}

	static private void verifyImmunizationStatus(ImmunizationActivityImpl act, String expected) throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);

		ImmunizationStatus status = immunization.getStatus();
		String actual = status == null ? null : status.toCode();
		Assert.assertEquals("Expect the correct immunization status", expected, actual);
	}

	static private void verifyImmunizationOriginalText(ImmunizationActivityImpl act, Map<String, String> annotations,
			String expected) throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		bundleInfo.mergeIdedAnnotations(annotations);
		Bundle bundle = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();
		Immunization immunization = BundleUtil.findOneResource(bundle, Immunization.class);

		CodeableConcept code = immunization.getVaccineCode();
		Assert.assertEquals("Expect the correct immunization original text", expected, code.getText());
	}

	@Test
	public void testStatusCode() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) factories.consol.createImmunizationActivity();
		DiagnosticChain dxChain = new BasicDiagnostic();
		verifyImmunizationStatus(act, null);

		act.setStatusCode(null);
		verifyImmunizationStatus(act, null);

		act.setStatusCode(factories.datatype.createCS("invalid"));
		// Boolean invalidation = act.validateImmunizationActivityStatusCode(dxChain,
		// null);
		// Assert.assertFalse("Expect status code validation failure", invalidation) ;
		// Maybe CDA implementation error??

		CS csNullFlavor = factories.datatype.createCS();
		csNullFlavor.setNullFlavor(NullFlavor.UNK);
		act.setStatusCode(csNullFlavor);
		Boolean validationNF = act.validateImmunizationActivityStatusCode(dxChain, null);
		Assert.assertTrue("Expect null flavor status code validation success", validationNF);
		verifyImmunizationStatus(act, null);

		for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();

			CS cs = factories.datatype.createCS(cdaStatusCode);
			act.setStatusCode(cs);
			Boolean validation = act.validateImmunizationActivityStatusCode(dxChain, null);
			Assert.assertTrue("Expect status code validation success for valid status code", validation);

			verifyImmunizationStatus(act, fhirStatus);
		}
	}

	@Test
	public void testCodeReference() throws Exception {
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) factories.consol.createImmunizationActivity();
		verifyImmunizationStatus(act, null);

		CE code = factories.datatype.createCE();
		ED ed = factories.datatype.createED();
		TEL tel = factories.datatype.createTEL();

		tel.setValue("#fakeid1");
		ed.setReference(tel);

		code.setCode("code");
		code.setCodeSystem("codeSystem");
		code.setOriginalText(ed);

		Consumable cons = factories.base.createConsumable();
		ManufacturedProduct manProd = factories.base.createManufacturedProduct();
		Material mat = factories.base.createMaterial();
		mat.setCode(code);
		manProd.setManufacturedMaterial(mat);
		cons.setManufacturedProduct(manProd);
		act.setConsumable(cons);

		Map<String, String> idedAnnotations = new StringMap();
		idedAnnotations.put("fakeid1", "fakevalue2");

		verifyImmunizationOriginalText(act, idedAnnotations, "fakevalue2");

	}

	@Test
	public void testImmunizationDosage() throws Exception {

		// Make a imm activity.
		ImmunizationActivityImpl act = (ImmunizationActivityImpl) factories.consol.createImmunizationActivity();

		IVL_PQ doseQuantity = factories.datatype.createIVL_PQ();

		doseQuantity.setUnit("mg");
		doseQuantity.setValue(100.000);
		act.setDoseQuantity(doseQuantity);

		// Transform from CDA to FHIR.
		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle fhirBundle = rt.tImmunizationActivity2Immunization(act, bundleInfo).getBundle();

		org.hl7.fhir.dstu3.model.Resource fhirResource = fhirBundle.getEntry().get(0).getResource();

		String systemString = fhirResource.getNamedProperty("doseQuantity").getValues().get(0)
				.getNamedProperty("system").getValues().get(0).toString();

		Assert.assertEquals("URI attached for ucum", "UriType[http://unitsofmeasure.org/ucum.html]", systemString);

	}
}
