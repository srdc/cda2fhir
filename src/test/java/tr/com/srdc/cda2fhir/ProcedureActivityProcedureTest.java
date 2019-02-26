package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.IndicationGenerator;
import tr.com.srdc.cda2fhir.testutil.PerformerGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ProcedureActivityProcedureTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;
	
	private static Map<String, Object> statusMap = JsonUtils.filepathToMap("src/test/resources/jolt/value-maps/ProcedureStatus.json");
	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		factories = CDAFactories.init();
	}
	
	@Test
	public void testPerformer() throws Exception {
		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();

		String organizationName = "PAP Organization";
		
		PerformerGenerator performerGenerator = new PerformerGenerator();
		performerGenerator.setCode();
		performerGenerator.setOrganizationName(organizationName);
		Performer2 performer = performerGenerator.generate(factories);
		pap.getPerformers().add(performer);
		
		Bundle bundle = rt.tProcedure2Procedure(pap);

		Organization organization = BundleUtil.findOneResource(bundle, Organization.class);
		Assert.assertEquals("Unexpected organization name", organizationName, organization.getName());		

		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);

		Assert.assertTrue("Expect a performer", procedure.hasPerformer());
		ProcedurePerformerComponent fhirPerformer = procedure.getPerformer().get(0);
		Assert.assertTrue("Expect a performer on behalf organization", fhirPerformer.hasOnBehalfOf());
		Reference organizationReference = fhirPerformer.getOnBehalfOf();
		Assert.assertEquals("Expect performer on behalf to point bundle organization", organization.getId(), organizationReference.getReference());
		Assert.assertTrue("Expect a performer role", fhirPerformer.hasRole());
		Coding role = fhirPerformer.getRole().getCoding().get(0);
		Assert.assertEquals("Expect the default role code", PerformerGenerator.DEFAULT_CODE_CODE, role.getCode());
	}

	static private void verifyProcedureStatus(ProcedureActivityProcedure pap, String expected) throws Exception {
		Bundle bundle = rt.tProcedure2Procedure(pap);
		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		
    	ProcedureStatus status = procedure.getStatus();
    	String actual = status == null ? null : status.toCode();
		Assert.assertEquals("Expect correct procedure status", expected, actual);		
	}
	
	@Test
	public void testStatusCode() throws Exception {
		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();
		verifyProcedureStatus(pap, null);
		
		DiagnosticChain dxChain = new BasicDiagnostic();
		
		pap.setStatusCode(factories.datatype.createCS("invalid"));
		Assert.assertFalse("Expect Procedure Activity Procedure validation failure", pap.validateProcedureActivityProcedureStatusCode(dxChain,  null));		
		verifyProcedureStatus(pap, "unknown");
			
		for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();
			
			CS cs = factories.datatype.createCS(cdaStatusCode);
			pap.setStatusCode(cs);
			Assert.assertTrue("Expect Procedure Activity Procedure validation", pap.validateProcedureActivityProcedureStatusCode(dxChain, null));

			verifyProcedureStatus(pap, fhirStatus);
		}
	}
	
	@Test
	public void testIndication() throws Exception {
		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();
		
		IndicationGenerator indicationGenerator = new IndicationGenerator();
		Indication indication = indicationGenerator.generate(factories);
		
		EntryRelationship entryRelationship = factories.base.createEntryRelationship();
		entryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.RSON);
		entryRelationship.setObservation(indication);
		
		pap.getEntryRelationships().add(entryRelationship);
		
		DiagnosticChain dxChain = new BasicDiagnostic();
		pap.validateProcedureActivityProcedureIndication(dxChain, null);
		
		Bundle bundle = rt.tProcedure2Procedure(pap);
		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		Assert.assertTrue("Expect a reason code", procedure.hasReasonCode());
		Coding coding = procedure.getReasonCode().get(0).getCodingFirstRep();
		Assert.assertEquals("Expect the default code", IndicationGenerator.DEFAULT_CODE_CODE, coding.getCode());
		Assert.assertEquals("Expect the default display name", IndicationGenerator.DEFAULT_CODE_DISPLAYNAME, coding.getDisplay());		
	}
}
