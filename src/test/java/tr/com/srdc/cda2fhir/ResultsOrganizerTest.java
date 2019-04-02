package tr.com.srdc.cda2fhir;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class ResultsOrganizerTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		factories = CDAFactories.init();
	}

	@Test
	public void testDiagnosticReportOriginalText() throws Exception {

		org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer org = factories.consol.createResultOrganizer();

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

		org.setCode(cd);
		Bundle bundle = rt.tResultOrganizer2DiagnosticReport(org, bundleInfo).getBundle();

		DiagnosticReport report = BundleUtil.findOneResource(bundle, DiagnosticReport.class);
		CodeableConcept cc = report.getCode();
		Assert.assertEquals("Diagnostic Report Code text value assigned", expectedValue, cc.getText());

	}

}
