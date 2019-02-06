package tr.com.srdc.cda2fhir;




//import ca.uhn.fhir.model.dstu2.composite.CodingDt;
//import ca.uhn.fhir.model.dstu2.resource.*;

import org.hl7.fhir.dstu3.model.Coding;

import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.PatientRole;

//import ca.uhn.fhir.model.dstu2.resource.Patient.Contact;
import tr.com.srdc.cda2fhir.transform.IValueSetsTransformer;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import org.openhealthtools.mdht.uml.cda.Guardian;

import org.junit.BeforeClass;
import org.junit.Assert;

import org.junit.Test;
public class FHIR3Tests {

	
	@BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
    
	@Test
	public void testPatientContactRelationship() {
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		Assert.assertEquals("http://hl7.org/fhir/v2/0131", vst.tRoleCode2PatientContactRelationshipCode("econ").getSystem());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("econ").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("econ").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("ext").getDisplay());;
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("ext").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("guard").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("guard").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("frnd").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("frnd").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("sps").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("sps").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("husb").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("husb").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("wife").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("wife").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("prn").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("prn").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("fth").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("fth").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("mth").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("mth").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("nprn").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("nprn").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("nmth").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("nmth").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("prinlaw").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("prinlaw").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("fthinlaw").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("fthinlaw").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("mthinlaw").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("mthinlaw").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("stpprn").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("stpprn").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("stpfth").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("stpfth").getCode());
		Assert.assertEquals("Emergency Contact", vst.tRoleCode2PatientContactRelationshipCode("stpmth").getDisplay());
		Assert.assertEquals("C", vst.tRoleCode2PatientContactRelationshipCode("stpmth").getCode());
		Assert.assertEquals("Billing contact person", vst.tRoleCode2PatientContactRelationshipCode("gt").getDisplay());
		Assert.assertEquals("BP", vst.tRoleCode2PatientContactRelationshipCode("gt").getCode());
		Assert.assertEquals("Employer", vst.tRoleCode2PatientContactRelationshipCode("work").getDisplay());
		Assert.assertEquals("E", vst.tRoleCode2PatientContactRelationshipCode("work").getCode());
		Assert.assertEquals("Next-of-Kin", vst.tRoleCode2PatientContactRelationshipCode("fammemb").getDisplay());
		Assert.assertEquals("N", vst.tRoleCode2PatientContactRelationshipCode("fammemb").getCode());
	}	
	
}
