package tr.com.srdc.cda2fhir;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.hl7.fhir.dstu3.model.Configuration;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;

public class ValueSetTransformationTest {
	private static CDAFactories factories;
	private static ValueSetsTransformerImpl vst; 
	
	private static enum ActStatus {
			NORMAL,
			ABORTED,
			ACTIVE,
			CANCELLED,
			COMPLETED,
			HELD,
			NEW,
			SUSPENDED,
			NULLIFIED,
			OBSOLETE;
		public String toCode() {
	          switch (this) {
	            case NORMAL: return "normal";
	            case ABORTED: return "aborted";
	            case ACTIVE: return "active";
	            case CANCELLED: return "cancelled";
	            case COMPLETED: return "completed";
	            case HELD: return "held";
	            case NEW: return "new";
	            case SUSPENDED: return "suspended";
	            case NULLIFIED: return "nullified";
	            case OBSOLETE: return "obsolete";
	            default: return "?";
	          }
	        }
	}
	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		vst = new ValueSetsTransformerImpl();
	}
	
	@Test
	public void testtActStatus2MedicationRequestStatus() {
		String normalResult = vst.tActStatus2MedicationRequestStatus(ActStatus.NORMAL.toCode()).toCode();
		String abortedResult = vst.tActStatus2MedicationRequestStatus(ActStatus.ABORTED.toCode()).toCode();
		String activeResult = vst.tActStatus2MedicationRequestStatus(ActStatus.ACTIVE.toCode()).toCode();
		String cancelledResult = vst.tActStatus2MedicationRequestStatus(ActStatus.CANCELLED.toCode()).toCode();
		String completedResult = vst.tActStatus2MedicationRequestStatus(ActStatus.COMPLETED.toCode()).toCode();
		String heldResult = vst.tActStatus2MedicationRequestStatus(ActStatus.HELD.toCode()).toCode();
		String newResult = vst.tActStatus2MedicationRequestStatus(ActStatus.NEW.toCode()).toCode();
		String suspendedResult = vst.tActStatus2MedicationRequestStatus(ActStatus.SUSPENDED.toCode()).toCode();
		String nullifiedResult = vst.tActStatus2MedicationRequestStatus(ActStatus.NULLIFIED.toCode()).toCode();
		String obsoleteResult = vst.tActStatus2MedicationRequestStatus(ActStatus.OBSOLETE.toCode()).toCode();

		Assert.assertEquals(normalResult,"unknown");
		Assert.assertEquals(abortedResult,"cancelled");
		Assert.assertEquals(activeResult,"active");
		Assert.assertEquals(cancelledResult,"cancelled");
		Assert.assertEquals(completedResult,"completed");
		Assert.assertEquals(heldResult,"on-hold");
		Assert.assertEquals(newResult,"unknown");
		Assert.assertEquals(suspendedResult,"on-hold");
		Assert.assertEquals(nullifiedResult,"cancelled");
		Assert.assertEquals(obsoleteResult,"unknown");

	}
}
