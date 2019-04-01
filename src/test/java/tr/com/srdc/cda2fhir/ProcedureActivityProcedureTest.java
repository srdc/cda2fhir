package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.bazaarvoice.jolt.JsonUtils;
import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
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
	public void testDefault() throws Exception {
		ProcedureActivityProcedureGenerator generator = ProcedureActivityProcedureGenerator.getDefaultInstance();

		ProcedureActivityProcedure pap = generator.generate(factories);

		DiagnosticChain dxChain = new BasicDiagnostic();
		pap.validateProcedureActivityProcedureIndication(dxChain, null);

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
	public void testProcedureOriginalText() throws Exception {

		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();

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

		pap.setCode(cd);
		Bundle bundle = rt.tProcedure2Procedure(pap, bundleInfo).getBundle();

		Procedure proc = BundleUtil.findOneResource(bundle, Procedure.class);
		CodeableConcept cc = proc.getCode();
		Assert.assertEquals("Procedure Code text value assigned", expectedValue, cc.getText());

	}
}
