package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;

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
		Observation observation = factories.base.createObservation();
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
}
