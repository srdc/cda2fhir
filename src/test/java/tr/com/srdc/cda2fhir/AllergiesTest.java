package tr.com.srdc.cda2fhir;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.impl.AllergyProblemActImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;

import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class AllergiesTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
	}

	static private AllergyIntolerance findOneResource(Bundle bundle) throws Exception {
    	List<AllergyIntolerance> allergyResources = bundle.getEntry().stream()
    			.map(r -> r.getResource())
    			.filter(r -> (r instanceof AllergyIntolerance))
    			.map(r -> (AllergyIntolerance) r)
				.collect(Collectors.toList());
    	Assert.assertEquals(1, allergyResources.size());
    	return allergyResources.get(0);	
	}
	
	static private void verifyAllergyIntoleranceVerificationStatus(AllergyProblemAct act, String expected) throws Exception {
		Bundle bundle = rt.tAllergyProblemAct2AllergyIntolerance(act);
		AllergyIntolerance allergyIntolerance = findOneResource(bundle);
		
    	AllergyIntoleranceVerificationStatus verificationStatus = allergyIntolerance.getVerificationStatus();
    	String actual = verificationStatus == null ? null : verificationStatus.toCode();
		Assert.assertEquals(expected, actual);		
	}
	
	@Test
	public void testAllergyIntoleranceStatusCode() throws Exception {
		ConsolFactoryImpl factory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		
		AllergyProblemActImpl act = (AllergyProblemActImpl) factory.createAllergyProblemAct();
		verifyAllergyIntoleranceVerificationStatus(act, null);
		
		DatatypesFactory dataTypesFactory = DatatypesFactoryImpl.init();
		
		act.setStatusCode(dataTypesFactory.createCS("invalid"));
		Assert.assertFalse(act.validateAllergyProblemActStatusCode(null, null));
				
		Map<String, Object> map = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/AllergyStatusCodeToVerificationStatus.json");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();
			
			CS cs = dataTypesFactory.createCS(cdaStatusCode);
			act.setStatusCode(cs);
			Assert.assertTrue(act.validateAllergyProblemActStatusCode(null, null));

			verifyAllergyIntoleranceVerificationStatus(act, fhirStatus);
		}
	}
}
