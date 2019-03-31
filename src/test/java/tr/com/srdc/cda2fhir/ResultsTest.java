package tr.com.srdc.cda2fhir;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class ResultsTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();

		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();
	}

	@Test
	public void testResultsIssuedNotPresent() throws Exception {
		ResultOrganizer org = cdaObjFactory.createResultOrganizer();

		String low = "2018-01-01";
		String high = "2019-01-01";

		IVL_TS interval = cdaTypeFactory.createIVL_TS(low, high);

		org.setEffectiveTime(interval);

		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult result = rt.tResultOrganizer2DiagnosticReport(org, bundleInfo);
		DiagnosticReport report = BundleUtil.findOneResource(result.getBundle(), DiagnosticReport.class);
		Date issuedDate = report.getIssued();
		Assert.assertEquals("Report issued date not populated", null, issuedDate);
	}

	@Test
	public void testNoNewPractitioner() throws Exception {
		ResultOrganizer org = cdaObjFactory.createResultOrganizer();

		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult result = rt.tResultOrganizer2DiagnosticReport(org, bundleInfo);
		List<Practitioner> prac = BundleUtil.findResources(result.getBundle(), Practitioner.class, 0);
		Assert.assertEquals("Practitioner is not created when not present", 0, prac.size());
	}

	@Test
	public void testResultsPopulation() throws Exception {
		ResultOrganizer org = cdaObjFactory.createResultOrganizer();

		ResultObservation result = cdaObjFactory.createResultObservation();
		CD code = cdaTypeFactory.createCD();

		code.setCode("6690-2");
		result.setCode(code);
		org.addObservation(result);

		BundleInfo bundleInfo = new BundleInfo(rt);
		EntryResult resultBundle = rt.tResultOrganizer2DiagnosticReport(org, bundleInfo);
		DiagnosticReport report = BundleUtil.findOneResource(resultBundle.getBundle(), DiagnosticReport.class);
		Assert.assertEquals("Result Observations is getting populated", 1, report.getResult().size());
	}

}
