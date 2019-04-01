package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class ObservationTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		factories = CDAFactories.init();
	}

	public static void verifyBooleanValue(boolean value) throws Exception {
		org.openhealthtools.mdht.uml.cda.Observation observation = factories.base.createObservation();
		BL bl = factories.datatype.createBL(value);
		observation.getValues().add(bl);

		BundleInfo bundleInfo = new BundleInfo(rt);
		Bundle bundle = rt.tObservation2Observation(observation, bundleInfo).getBundle();
		org.hl7.fhir.dstu3.model.Observation fhirObservation = BundleUtil.findOneResource(bundle,
				org.hl7.fhir.dstu3.model.Observation.class);
		BooleanType bt = fhirObservation.getValueBooleanType();
		Assert.assertEquals("Pull back the observation " + value + " boolean value", value,
				bt.getValue().booleanValue());
	}

	@Test
	public void valueBooleanTest() throws Exception {
		verifyBooleanValue(true);
		verifyBooleanValue(false);
	}

	@Test
	public void testObservationOriginalText() throws Exception {

		org.openhealthtools.mdht.uml.cda.Observation observation = factories.base.createObservation();

		BundleInfo bundleInfo = new BundleInfo(rt);
		String expectedValue = "freetext entry";
		String referenceValue = "fakeid1";
		CE ce = factories.datatype.createCE();
		ED ed = factories.datatype.createED();
		TEL tel = factories.datatype.createTEL();
		tel.setValue("#" + referenceValue);
		ed.setReference(tel);
		ce.setCode("code");
		ce.setCodeSystem("codeSystem");
		ce.setOriginalText(ed);
		Map<String, String> idedAnnotations = new StringMap();
		idedAnnotations.put(referenceValue, expectedValue);
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		observation.setCode(ce);
		Bundle bundle = rt.tObservation2Observation(observation, bundleInfo).getBundle();
		Observation fhirObservation = BundleUtil.findOneResource(bundle, Observation.class);
		CodeableConcept cc = fhirObservation.getCode();
		Assert.assertEquals("Observation Code text value assigned", expectedValue, cc.getText());

	}

}
