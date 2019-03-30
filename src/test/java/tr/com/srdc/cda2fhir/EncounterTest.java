package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class EncounterTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		factories = CDAFactories.init();
	}

	@Test
	public void testEncounterOriginalText() throws Exception {

		// Make an encounter activity.
		EncounterActivities encounterActivities = factories.consol.createEncounterActivities();

		BundleInfo bundleInfo = new BundleInfo(rt);
		String expectedValue = "freetext entry";
		String referenceValue = "fakeid1";
		CD cd = factories.datatype.createCD();
		ED ed = factories.datatype.createED();
		TEL tel = factories.datatype.createTEL();
		tel.setValue("#" + referenceValue);
		ed.setReference(tel);
		cd.setCode("code");
		cd.setCodeSystem("codeSystem");
		cd.setOriginalText(ed);
		Map<String, String> idedAnnotations = new StringMap();
		idedAnnotations.put(referenceValue, expectedValue);
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		encounterActivities.setCode(cd);
		Bundle bundle = rt.tEncounterActivity2Encounter(encounterActivities, bundleInfo).getBundle();
		Encounter fhirEncounter = BundleUtil.findOneResource(bundle, Encounter.class);
		CodeableConcept cc = fhirEncounter.getType().get(0);
		Assert.assertEquals("Encounter Activity Code text value assigned", expectedValue, cc.getText());

	}

}
