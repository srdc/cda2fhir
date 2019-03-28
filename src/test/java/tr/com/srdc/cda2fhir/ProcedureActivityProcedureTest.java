package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.IndicationGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.ProcedureActivityProcedureGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class ProcedureActivityProcedureTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;

	private static Map<String, Object> statusMap = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ProcedureStatus.json");

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testPerformer() throws Exception {
		ProcedureActivityProcedureGenerator generator = ProcedureActivityProcedureGenerator.getDefaultInstance();

		ProcedureActivityProcedure pap = generator.generate(factories);

		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult entryResult = rt.tProcedure2Procedure(pap, bundleInfo);
		Bundle bundle = entryResult.getBundle();

		generator.verify(bundle);
	}

	static private void verifyProcedureStatus(ProcedureActivityProcedure pap, String expected) throws Exception {
		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult entryResult = rt.tProcedure2Procedure(pap, bundleInfo);
		Bundle bundle = entryResult.getBundle();
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
		Assert.assertFalse("Expect Procedure Activity Procedure validation failure",
				pap.validateProcedureActivityProcedureStatusCode(dxChain, null));
		verifyProcedureStatus(pap, "unknown");

		for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
			String cdaStatusCode = entry.getKey();
			String fhirStatus = (String) entry.getValue();

			CS cs = factories.datatype.createCS(cdaStatusCode);
			pap.setStatusCode(cs);
			Assert.assertTrue("Expect Procedure Activity Procedure validation",
					pap.validateProcedureActivityProcedureStatusCode(dxChain, null));

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

		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult entryResult = rt.tProcedure2Procedure(pap, bundleInfo);
		Bundle bundle = entryResult.getBundle();
		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		Assert.assertTrue("Expect a reason code", procedure.hasReasonCode());
		Coding coding = procedure.getReasonCode().get(0).getCodingFirstRep();
		Assert.assertEquals("Expect the default code", IndicationGenerator.DEFAULT_CODE_CODE, coding.getCode());
		Assert.assertEquals("Expect the default display name", IndicationGenerator.DEFAULT_CODE_DISPLAYNAME,
				coding.getDisplay());
	}
}
